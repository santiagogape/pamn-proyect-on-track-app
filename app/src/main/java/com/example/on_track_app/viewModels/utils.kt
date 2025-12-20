package com.example.on_track_app.viewModels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.data.abstractions.repositories.GroupRepository
import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.abstractions.repositories.ReminderRepository
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Group
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Reminder
import com.example.on_track_app.model.Task
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
    fun project(id:String): Project?
}

interface ProjectsConsultFlow:ConsultProject {
    fun projects(group: String?): Flow<List<Project>>
}

interface ProjectsConsult:ConsultProject {
    fun projects(group: String?): StateFlow<ItemStatus<List<Project>>>
}




interface ModifyTask {
    fun update(task: Task)
    fun delete(task: Task)
}
interface ModifyEvent {
    fun update(event: Event)
    fun delete(event: Event)
}
interface ModifyProject {
    fun update(project: Project)
    fun delete(project: Project)
}
interface ModifyReminder {
    fun update(reminder: Reminder)
    fun delete(reminder: Reminder)
}
interface ModifyGroup {
    fun update(group: Group)
    fun delete(group: Group)
}


class DelegateConsultProject(val projectRepo: ProjectRepository): ProjectsConsultFlow {
    override fun project(id:String): Project? {
        return this.projectRepo.getById(id)
    }

    override fun projects(group: String?): Flow<List<Project>> =
        group?.let { this.projectRepo.of(it)
        } ?:
        this.projectRepo.getAll()
}


class DelegateModifyEvent(val update: EventRepository, val scope: CoroutineScope): ModifyEvent {
    override fun update(event: Event){
        scope.launch {
            update.update(event)
        }
    }

    override fun delete(event: Event) {
        scope.launch {
            update.markAsDeleted(event.id)
            update.delete(event.id)
        }
    }
}

class DelegateModifyProject(val update: ProjectRepository, val scope: CoroutineScope): ModifyProject {
    override fun update(project: Project){
        scope.launch {
            update.update(project)
        }
    }

    override fun delete(project: Project) {
        scope.launch {
            update.markAsDeleted(project.id)
            update.delete(project.id)
        }
    }
}

class DelegateModifyGroup(val update: GroupRepository, val scope: CoroutineScope): ModifyGroup {
    override fun update(group: Group){
        scope.launch {
            update.update(group)
        }
    }

    override fun delete(group: Group) {
        scope.launch {
            update.markAsDeleted(group.id)
            update.delete(group.id)
        }
    }
}

class DelegateModifyReminder(val update: ReminderRepository, val scope: CoroutineScope): ModifyReminder {
    override fun update(reminder: Reminder){
        scope.launch {
            update.update(reminder)
        }
    }

    override fun delete(reminder: Reminder) {
        scope.launch {
            update.markAsDeleted(reminder.id)
            update.delete(reminder.id)
        }
    }
}

class DelegateModifyTask(val update: TaskRepository, val scope: CoroutineScope): ModifyTask {
    override fun update(task: Task){
        scope.launch {
            update.update(task)
        }
    }

    override fun delete(task: Task) {
        scope.launch {
            update.markAsDeleted(task.id)
            update.delete(task.id)
        }
    }
}


