package com.example

import com.example.models.*
import com.example.plugins.*
import com.example.repository.TaskRepository
import com.example.repository.UserRepository
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlin.test.*

class TaskTest {

    @BeforeTest
    fun setup() {
        UserRepository.clear()
        TaskRepository.clear()
    }

    private fun ApplicationTestBuilder.configureApp() {
        application {
            configureSerialization()
            configureAuthentication()
            configureStatusPages()
            configureRouting()
        }
    }

    private suspend fun getToken(client: io.ktor.client.HttpClient): String {
        val response = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("testuser", "test@mail.com", "1234"))
        }
        return response.body<AuthResponse>().token!!
    }

    @Test
    fun `tasks - unauthorized without token`() = testApplication {
        configureApp()
        
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        
        val response = client.get("/api/tasks")
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `create task - success`() = testApplication {
        configureApp()
        
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        
        val token = getToken(client)
        
        val response = client.post("/api/tasks") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(TaskRequest("Test Task", "Description"))
        }
        
        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<TaskResponse>()
        assertTrue(body.success)
        assertEquals("Test Task", body.data?.title)
    }

    @Test
    fun `create task - empty title returns 400`() = testApplication {
        configureApp()
        
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        
        val token = getToken(client)
        
        val response = client.post("/api/tasks") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(TaskRequest("", "Description"))
        }
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `get all tasks - returns user tasks only`() = testApplication {
        configureApp()
        
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        
        val token = getToken(client)
        
        // Создаём задачи
        client.post("/api/tasks") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(TaskRequest("Task 1", "Desc 1"))
        }
        client.post("/api/tasks") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(TaskRequest("Task 2", "Desc 2"))
        }
        
        // Получаем список
        val response = client.get("/api/tasks") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<TaskListResponse>()
        assertEquals(2, body.data?.size)
    }

    @Test
    fun `get task by id - success`() = testApplication {
        configureApp()
        
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        
        val token = getToken(client)
        
        // Создаём задачу
        val createResponse = client.post("/api/tasks") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(TaskRequest("Test Task", "Description"))
        }
        val taskId = createResponse.body<TaskResponse>().data?.id
        
        // Получаем по ID
        val response = client.get("/api/tasks/$taskId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<TaskResponse>()
        assertEquals("Test Task", body.data?.title)
    }

    @Test
    fun `get task by id - not found returns 404`() = testApplication {
        configureApp()
        
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        
        val token = getToken(client)
        
        val response = client.get("/api/tasks/999") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `update task - success`() = testApplication {
        configureApp()
        
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        
        val token = getToken(client)
        
        // Создаём
        val createResponse = client.post("/api/tasks") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(TaskRequest("Original", "Desc"))
        }
        val taskId = createResponse.body<TaskResponse>().data?.id
        
        // Обновляем
        val response = client.put("/api/tasks/$taskId") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(TaskRequest("Updated", "New Desc", completed = true))
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<TaskResponse>()
        assertEquals("Updated", body.data?.title)
        assertTrue(body.data?.completed == true)
    }

    @Test
    fun `delete task - success`() = testApplication {
        configureApp()
        
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        
        val token = getToken(client)
        
        // Создаём
        val createResponse = client.post("/api/tasks") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(TaskRequest("To Delete", "Desc"))
        }
        val taskId = createResponse.body<TaskResponse>().data?.id
        
        // Удаляем
        val deleteResponse = client.delete("/api/tasks/$taskId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.OK, deleteResponse.status)
        
        // Проверяем что удалено
        val getResponse = client.get("/api/tasks/$taskId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun `filter by completed - query parameter`() = testApplication {
        configureApp()
        
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        
        val token = getToken(client)
        
        // Создаём задачи
        client.post("/api/tasks") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(TaskRequest("Task 1", "Desc", completed = false))
        }
        client.post("/api/tasks") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(TaskRequest("Task 2", "Desc", completed = true))
        }
        
        // Фильтруем completed=true
        val response = client.get("/api/tasks?completed=true") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<TaskListResponse>()
        assertEquals(1, body.data?.size)
        assertTrue(body.data?.first()?.completed == true)
    }

    @Test
    fun `search - query parameter`() = testApplication {
        configureApp()
        
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        
        val token = getToken(client)
        
        // Создаём задачи
        client.post("/api/tasks") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(TaskRequest("Learn Ktor", "Framework"))
        }
        client.post("/api/tasks") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(TaskRequest("Other Task", "Something"))
        }
        
        // Поиск
        val response = client.get("/api/tasks?search=Ktor") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<TaskListResponse>()
        assertEquals(1, body.data?.size)
        assertTrue(body.data?.first()?.title?.contains("Ktor") == true)
    }

    @Test
    fun `user isolation - cannot access other user tasks`() = testApplication {
        configureApp()
        
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        
        // User 1
        val token1 = getToken(client)
        val createResponse = client.post("/api/tasks") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token1")
            setBody(TaskRequest("User1 Task", "Desc"))
        }
        val taskId = createResponse.body<TaskResponse>().data?.id
        
        // User 2
        val response2 = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("user2", "user2@mail.com", "1234"))
        }
        val token2 = response2.body<AuthResponse>().token!!
        
        // User 2 пытается получить задачу User 1
        val getResponse = client.get("/api/tasks/$taskId") {
            header(HttpHeaders.Authorization, "Bearer $token2")
        }
        
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }
}
