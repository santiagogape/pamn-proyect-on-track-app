package com.example.on_track_app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.data.abstractions.repositories.GroupRepository
import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.abstractions.repositories.ReminderRepository
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.MockGroup
import com.example.on_track_app.model.MockProject
import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.viewModels.main.ItemStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun LocalTime.format(): String =
    remember(this) {
        DateTimeFormatter.ofPattern("HH:mm").format(this)
    }

interface ConsultProject {
    fun project(id:String): MockProject?
}

interface ProjectsConsultFlow:ConsultProject {
    fun projects(group: String?): Flow<List<MockProject>>
}

interface ProjectsConsult:ConsultProject {
    fun projects(group: String?): StateFlow<ItemStatus<List<MockProject>>>
}




interface ModifyTask {
    fun update(task: MockTask)
    fun delete(task: MockTask)
}
interface ModifyEvent {
    fun update(event: MockEvent)
    fun delete(event: MockEvent)
}
interface ModifyProject {
    fun update(project: MockProject)
    fun delete(project: MockProject)
}
interface ModifyReminder {
    fun update(reminder: MockReminder)
    fun delete(reminder: MockReminder)
}
interface ModifyGroup {
    fun update(group: MockGroup)
    fun delete(group: MockGroup)
}


class DelegateConsultProject(val projectRepo: ProjectRepository): ProjectsConsultFlow {
    override fun project(id:String): MockProject? {
        return this.projectRepo.getById(id)
    }

    override fun projects(group: String?): Flow<List<MockProject>> =
        group?.let { this.projectRepo.of(it)
        } ?:
        this.projectRepo.getAll()
}


class DelegateModifyEvent(val update: EventRepository, val scope: CoroutineScope): ModifyEvent {
    override fun update(event: MockEvent){
        scope.launch {
            update.update(event)
        }
    }

    override fun delete(event: MockEvent) {
        scope.launch {
            update.markAsDeleted(event.id)
            update.delete(event.id)
        }
    }
}

class DelegateModifyProject(val update: ProjectRepository, val scope: CoroutineScope): ModifyProject {
    override fun update(project: MockProject){
        scope.launch {
            update.update(project)
        }
    }

    override fun delete(project: MockProject) {
        scope.launch {
            update.markAsDeleted(project.id)
            update.delete(project.id)
        }
    }
}

class DelegateModifyGroup(val update: GroupRepository, val scope: CoroutineScope): ModifyGroup {
    override fun update(group: MockGroup){
        scope.launch {
            update.update(group)
        }
    }

    override fun delete(group: MockGroup) {
        scope.launch {
            update.markAsDeleted(group.id)
            update.delete(group.id)
        }
    }
}

class DelegateModifyReminder(val update: ReminderRepository, val scope: CoroutineScope): ModifyReminder {
    override fun update(reminder: MockReminder){
        scope.launch {
            update.update(reminder)
        }
    }

    override fun delete(reminder: MockReminder) {
        scope.launch {
            update.markAsDeleted(reminder.id)
            update.delete(reminder.id)
        }
    }
}

class DelegateModifyTask(val update: TaskRepository, val scope: CoroutineScope): ModifyTask {
    override fun update(task: MockTask){
        scope.launch {
            update.update(task)
        }
    }

    override fun delete(task: MockTask) {
        scope.launch {
            update.markAsDeleted(task.id)
            update.delete(task.id)
        }
    }
}


