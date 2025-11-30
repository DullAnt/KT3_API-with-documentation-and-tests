package com.example.repository

import com.example.models.Task
import com.example.models.TaskRequest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

object TaskRepository {
    private val tasks = ConcurrentHashMap<Int, Task>()
    private val idCounter = AtomicInteger(0)

    fun create(request: TaskRequest, userId: Int): Task {
        val id = idCounter.incrementAndGet()
        val task = Task(
            id = id,
            title = request.title,
            description = request.description,
            completed = request.completed,
            userId = userId
        )
        tasks[id] = task
        return task
    }

    fun getAllByUser(userId: Int): List<Task> {
        return tasks.values.filter { it.userId == userId }
    }

    fun getById(id: Int, userId: Int): Task? {
        val task = tasks[id] ?: return null
        return if (task.userId == userId) task else null
    }

    fun filter(userId: Int, completed: Boolean): List<Task> {
        return tasks.values.filter { it.userId == userId && it.completed == completed }
    }

    fun search(userId: Int, query: String): List<Task> {
        val lowerQuery = query.lowercase()
        return tasks.values.filter {
            it.userId == userId && (
                it.title.lowercase().contains(lowerQuery) ||
                it.description.lowercase().contains(lowerQuery)
            )
        }
    }

    fun update(id: Int, request: TaskRequest, userId: Int): Task? {
        val existing = tasks[id] ?: return null
        if (existing.userId != userId) return null
        
        val updated = existing.copy(
            title = request.title,
            description = request.description,
            completed = request.completed
        )
        tasks[id] = updated
        return updated
    }

    fun delete(id: Int, userId: Int): Boolean {
        val task = tasks[id] ?: return false
        if (task.userId != userId) return false
        return tasks.remove(id) != null
    }

    // Для тестов
    fun clear() {
        tasks.clear()
        idCounter.set(0)
    }
}
