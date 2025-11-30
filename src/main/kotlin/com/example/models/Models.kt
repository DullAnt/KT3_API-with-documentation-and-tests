package com.example.models

import kotlinx.serialization.Serializable

// ===== USER MODELS =====
@Serializable
data class User(
    val id: Int,
    val username: String,
    val email: String,
    val passwordHash: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null
)

// ===== TASK MODELS =====
@Serializable
data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val completed: Boolean = false,
    val userId: Int
)

@Serializable
data class TaskRequest(
    val title: String,
    val description: String,
    val completed: Boolean = false
)

@Serializable
data class TaskResponse(
    val success: Boolean,
    val message: String,
    val data: Task? = null
)

@Serializable
data class TaskListResponse(
    val success: Boolean,
    val message: String,
    val data: List<Task>? = null
)

// ===== ERROR & INFO =====
@Serializable
data class ErrorResponse(
    val success: Boolean = false,
    val error: String,
    val code: Int
)

@Serializable
data class ApiInfo(
    val success: Boolean,
    val message: String,
    val version: String,
    val docs: String
)
