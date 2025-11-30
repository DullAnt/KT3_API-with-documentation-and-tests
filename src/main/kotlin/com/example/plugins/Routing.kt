package com.example.plugins

import com.example.models.ApiInfo
import com.example.routes.authRoutes
import com.example.routes.taskRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        // Swagger UI - интерактивная документация
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml") {
            version = "5.10.3"
        }
        
        // OpenAPI JSON/YAML
        openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
        
        // Главная страница
        get("/") {
            call.respond(HttpStatusCode.OK, ApiInfo(
                success = true,
                message = "Ktor API with Swagger & Tests",
                version = "1.0.0",
                docs = "http://localhost:8080/swagger"
            ))
        }
        
        // Маршруты API
        authRoutes()
        taskRoutes()
    }
}
