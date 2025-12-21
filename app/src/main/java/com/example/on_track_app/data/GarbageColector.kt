package com.example.on_track_app.data

import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Group
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Reminder
import com.example.on_track_app.model.Task
import com.example.on_track_app.model.User
import com.example.on_track_app.model.UserMembership
import com.example.on_track_app.utils.DebugLogcatLogger
import kotlin.reflect.KClass

enum class GarbageCollectorPhase {
    NONE,          // not processed by GC
    SOFT_DONE,     // soft-cascade already propagated
    FINALIZED      // ready for hard delete
}

interface GCSynchronizableEntity {
    var phase: String

    fun setPhase(phase: GarbageCollectorPhase) { //same, just utility
        this.phase = phase.name
    }
}
/**
 * A single port for GC to call for one entity type.
 * Many repos will implement all 3; some might not need soft propagation (leaf types).
 */
interface GarbageCollectablePort {
    val domClass: KClass<*>


    /** Returns local ids for items that are DELETED and not yet SOFT_DONE. */
    suspend fun findSoftDeleteRoots(): List<String>

    /** Mark this root as SOFT_DONE (so it won't be re-processed as a root). */
    suspend fun markSoftDone(id: String)

    /**
     * Finalize as many entities as possible and return how many advanced state.
     * Implementations usually:
     * - query entities where status=DELETED and phase=SOFT_DONE
     * - check that no live dependents exist
     * - set phase=FINALIZED
     */
    suspend fun flushAsFinalized(): Int

    /** Hard delete FINALIZED items. Return how many deleted. */
    suspend fun purgeFinalized(): Int

}

sealed interface MarkAsDeleteByReference
interface SoftDeleteByOwnerPort: MarkAsDeleteByReference {
    suspend fun markAsDeletedByOwner(ownerId: String): Int
}

interface SoftDeleteByProjectPort: MarkAsDeleteByReference {
    suspend fun markAsDeletedByProject(projectId: String): Int
}
interface SoftDeleteByLinkPort: MarkAsDeleteByReference {
    suspend fun markAsDeletedByLink(linkId: String): Int
}
interface SoftDeleteByMembershipPort: MarkAsDeleteByReference {
    suspend fun markAsDeletedByMembership(membershipId: String): Int
    suspend fun markAsDeletedByMember(userId: String): Int
}

sealed interface GCRelation {
    data object ByOwner : GCRelation
    data object ByProject : GCRelation
    data object ByLink : GCRelation
    data object ByMembership : GCRelation
    data object ByMember : GCRelation
}

val GCRules: Map<KClass<*>, List<GCRelation>> = mapOf(
    User::class to listOf(
        GCRelation.ByOwner,
        GCRelation.ByMember
    ),

    Group::class to listOf(
        GCRelation.ByOwner,
        GCRelation.ByMembership
    ),

    Project::class to listOf(
        GCRelation.ByProject,
        GCRelation.ByMembership
    ),

    Task::class to listOf(
        GCRelation.ByLink
    ),

    Event::class to listOf(
        GCRelation.ByLink
    ),

    Reminder::class to emptyList(),
    UserMembership::class to emptyList()
)

interface ProgrammableGarbageCollector {
    suspend fun deleteOnCascade()
}

interface TriggeredGarbageCollector {
    suspend fun propagateOnMarkAsDeleted(clazz: KClass<*>, id:String)
}



class GarbageCollector(
    private val order: List<KClass<*>>,
    private val rules: Map<KClass<*>, List<GCRelation>> = GCRules,
): ProgrammableGarbageCollector, TriggeredGarbageCollector{

    private val owners =mutableMapOf<Class<*>,SoftDeleteByOwnerPort>()
    private val projects =mutableMapOf<Class<*>,SoftDeleteByProjectPort>()
    private val links =mutableMapOf<Class<*>,SoftDeleteByLinkPort>()

    private var memberships:SoftDeleteByMembershipPort? = null

    private val collectors = mutableMapOf<KClass<*>,GarbageCollectablePort>()


    fun subscribe(clazz: KClass<*>,port:SoftDeleteByOwnerPort){
        owners[clazz.java] = port
    }

    fun subscribe(clazz: KClass<*>,port:SoftDeleteByProjectPort){
        projects[clazz.java] = port
    }

    fun subscribe(clazz: KClass<*>,port:SoftDeleteByLinkPort) {
        links[clazz.java] = port
    }

    fun subscribe(port:SoftDeleteByMembershipPort){
        memberships = port
    }

    fun subscribe(clazz: KClass<*>,port:GarbageCollectablePort) {
        collectors[clazz] = port
    }

    fun init() {
        ownerPorts = order.mapNotNull { owners[it.java] }
        projectPorts = order.mapNotNull { projects[it.java] }
        linkPorts = order.mapNotNull { links[it.java] }
        membershipPorts = memberships?.let { listOf(it) } ?: emptyList()
        collectables = order.mapNotNull { collectors[it] }
    }


    // Puertos
    private lateinit var ownerPorts:      List<SoftDeleteByOwnerPort>
    private lateinit var projectPorts:    List<SoftDeleteByProjectPort>
    private lateinit var linkPorts:       List<SoftDeleteByLinkPort>
    private lateinit var membershipPorts: List<SoftDeleteByMembershipPort>

    // repos GC-aware (para fases)
    private lateinit var collectables: List<GarbageCollectablePort> // same order as domain sync


    private suspend fun propagate(relation: GCRelation, rootId:String) {
        when (relation) {

            GCRelation.ByOwner ->
                ownerPorts.forEach { it.markAsDeletedByOwner(rootId) }

            GCRelation.ByProject ->
                projectPorts.forEach { it.markAsDeletedByProject(rootId) }

            GCRelation.ByLink ->
                linkPorts.forEach { it.markAsDeletedByLink(rootId) }

            GCRelation.ByMembership ->
                membershipPorts.forEach { it.markAsDeletedByMembership(rootId) }

            GCRelation.ByMember ->
                membershipPorts.forEach { it.markAsDeletedByMember(rootId) }
        }
    }

    override suspend fun deleteOnCascade(){
        for (port in collectables){
            for (root in port.findSoftDeleteRoots()){
                propagate(port, root)
            }
        }
        flush()
        purge()
    }

    override suspend fun propagateOnMarkAsDeleted(clazz: KClass<*>, id:String){
        val relations = rules[clazz] ?: return
        for (relation in relations) {
            propagate(relation, id)
        }
    }

    private suspend fun propagate(
        port: GarbageCollectablePort,
        root: String
    ) {
        port.markSoftDone(root)
        DebugLogcatLogger.log("$root as soft done")
        rules[port.domClass].orEmpty().forEach { propagate(it, root) }
    }

    suspend fun flush(){
        for (port in collectables) {
            val count = port.flushAsFinalized()
            DebugLogcatLogger.log("eliminated $count")
        }
    }

    suspend fun purge() {
        for (port in collectables) {
            val count = port.purgeFinalized()
            DebugLogcatLogger.log("eliminated $count")
        }
    }
}


