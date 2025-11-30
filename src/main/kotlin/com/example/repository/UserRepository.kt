package com.example.repository

import com.example.models.User
import org.mindrot.jbcrypt.BCrypt
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

object UserRepository {
    private val users = ConcurrentHashMap<Int, User>()
    private val usernameIndex = ConcurrentHashMap<String, Int>()
    private val idCounter = AtomicInteger(0)

    fun create(username: String, email: String, password: String): User? {
        if (usernameIndex.containsKey(username)) {
            return null
        }
        
        val id = idCounter.incrementAndGet()
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
        val user = User(id, username, email, passwordHash)
        
        users[id] = user
        usernameIndex[username] = id
        
        return user
    }

    fun findByUsername(username: String): User? {
        val id = usernameIndex[username] ?: return null
        return users[id]
    }

    fun findById(id: Int): User? = users[id]

    fun validatePassword(user: User, password: String): Boolean {
        return BCrypt.checkpw(password, user.passwordHash)
    }

    // Для тестов
    fun clear() {
        users.clear()
        usernameIndex.clear()
        idCounter.set(0)
    }
}
