plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-server-status-pages:2.3.7")
    
    // JWT Authentication
    implementation("io.ktor:ktor-server-auth:2.3.7")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.7")
    
    // JSON Serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    
    // Swagger / OpenAPI
    implementation("io.ktor:ktor-server-openapi:2.3.7")
    implementation("io.ktor:ktor-server-swagger:2.3.7")
    
    // Logging middleware
    implementation("io.ktor:ktor-server-call-logging:2.3.7")
    
    // Password hashing
    implementation("org.mindrot:jbcrypt:0.4")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
    
    // Testing
    testImplementation("io.ktor:ktor-server-tests:2.3.7")
    testImplementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.22")
}

application {
    mainClass.set("com.example.ApplicationKt")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnit()
}
