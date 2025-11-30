package com.example.plugins

import com.example.models.ErrorResponse
import com.example.security.JwtConfig
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

// JSON сериализация
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

// JWT аутентификация
fun Application.configureAuthentication() {
    install(Authentication) {
        jwt("jwt-auth") {
            realm = JwtConfig.realm
            verifier(
                com.auth0.jwt.JWT
                    .require(JwtConfig.algorithm)
                    .withIssuer(JwtConfig.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asInt() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        error = "Требуется авторизация. Header: Authorization: Bearer <token>",
                        code = 401
                    )
                )
            }
        }
    }
}

// Обработка ошибок (StatusPages)
fun Application.configureStatusPages() {
    install(StatusPages) {
        // Обработка исключений
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = cause.message ?: "Внутренняя ошибка сервера",
                    code = 500
                )
            )
        }
        
        // 404 Not Found
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                status,
                ErrorResponse(
                    error = "Ресурс не найден: ${call.request.path()}",
                    code = 404
                )
            )
        }
        
        // 405 Method Not Allowed
        status(HttpStatusCode.MethodNotAllowed) { call, status ->
            call.respond(
                status,
                ErrorResponse(
                    error = "Метод ${call.request.httpMethod.value} не поддерживается",
                    code = 405
                )
            )
        }
        
        // 415 Unsupported Media Type
        status(HttpStatusCode.UnsupportedMediaType) { call, status ->
            call.respond(
                status,
                ErrorResponse(
                    error = "Используйте Content-Type: application/json",
                    code = 415
                )
            )
        }
    }
}

// Middleware логирования
fun Application.configureLogging() {
    install(CallLogging) {
        level = Level.INFO
        
        // Формат лога
        format { call ->
            val status = call.response.status()
            val method = call.request.httpMethod.value
            val path = call.request.path()
            val duration = call.processingTimeMillis()
            val userAgent = call.request.headers["User-Agent"] ?: "Unknown"
            
            "HTTP $method $path -> $status (${duration}ms) | UA: $userAgent"
        }
        
        // Фильтруем health-check и статику
        filter { call ->
            !call.request.path().startsWith("/swagger") &&
            !call.request.path().startsWith("/openapi")
        }
        
        // Добавляем MDC для корреляции
        mdc("requestId") {
            java.util.UUID.randomUUID().toString().take(8)
        }
    }
}
