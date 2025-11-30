package com.example

import com.example.models.AuthResponse
import com.example.models.LoginRequest
import com.example.models.RegisterRequest
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

class AuthTest {

    @BeforeTest
    fun setup() {
        UserRepository.clear()
        TaskRepository.clear()
    }

    @Test
    fun `register - success`() = testApplication {
        application {
            configureSerialization()
            configureAuthentication()
            configureStatusPages()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        
        val response = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("testuser", "test@mail.com", "1234"))
        }
        
        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<AuthResponse>()
        assertTrue(body.success)
        assertNotNull(body.token)
    }

    @Test
    fun `register - duplicate username returns 409`() = testApplication {
        application {
            configureSerialization()
            configureAuthentication()
            configureStatusPages()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        
        // Первая регистрация
        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("testuser", "test@mail.com", "1234"))
        }
        
        // Повторная регистрация
        val response = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("testuser", "test2@mail.com", "5678"))
        }
        
        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `register - short password returns 400`() = testApplication {
        application {
            configureSerialization()
            configureAuthentication()
            configureStatusPages()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        
        val response = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("testuser", "test@mail.com", "123"))
        }
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `login - success`() = testApplication {
        application {
            configureSerialization()
            configureAuthentication()
            configureStatusPages()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        
        // Регистрация
        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("testuser", "test@mail.com", "1234"))
        }
        
        // Вход
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("testuser", "1234"))
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<AuthResponse>()
        assertTrue(body.success)
        assertNotNull(body.token)
    }

    @Test
    fun `login - wrong password returns 401`() = testApplication {
        application {
            configureSerialization()
            configureAuthentication()
            configureStatusPages()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        
        // Регистрация
        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("testuser", "test@mail.com", "1234"))
        }
        
        // Вход с неверным паролем
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("testuser", "wrong"))
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `login - unknown user returns 401`() = testApplication {
        application {
            configureSerialization()
            configureAuthentication()
            configureStatusPages()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("unknown", "1234"))
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
