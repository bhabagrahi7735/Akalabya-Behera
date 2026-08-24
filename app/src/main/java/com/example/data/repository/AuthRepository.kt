package com.example.data.repository

import android.content.Context
import com.example.data.model.User
import com.example.data.preferences.UserPreferencesRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthRepository(
    private val context: Context,
    private val preferencesRepository: UserPreferencesRepository
) {
    private var firebaseAuth: FirebaseAuth? = null
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authStateLoaded = MutableStateFlow(false)
    val authStateLoaded: StateFlow<Boolean> = _authStateLoaded.asStateFlow()

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firebaseAuth = FirebaseAuth.getInstance()
            }
        } catch (e: Exception) {
            firebaseAuth = null
        }

        initializeAuthState()
    }

    private fun initializeAuthState() {
        val auth = firebaseAuth
        if (auth != null) {
            val fbUser = auth.currentUser
            if (fbUser != null) {
                _currentUser.value = User(
                    uid = fbUser.uid,
                    email = fbUser.email ?: "",
                    displayName = fbUser.displayName ?: fbUser.email?.substringBefore("@") ?: "Journaler",
                    photoUrl = fbUser.photoUrl?.toString(),
                    isAnonymous = fbUser.isAnonymous
                )
            } else {
                _currentUser.value = null
            }
            auth.addAuthStateListener { fa ->
                val u = fa.currentUser
                if (u != null) {
                    _currentUser.value = User(
                        uid = u.uid,
                        email = u.email ?: "",
                        displayName = u.displayName ?: u.email?.substringBefore("@") ?: "Journaler",
                        photoUrl = u.photoUrl?.toString(),
                        isAnonymous = u.isAnonymous
                    )
                } else {
                    _currentUser.value = null
                }
            }
        } else {
            // Local fallback / offline profile check
            _currentUser.value = null
        }
        _authStateLoaded.value = true
    }

    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val auth = firebaseAuth
            if (auth != null) {
                val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
                val fbUser = authResult.user ?: throw Exception("Authentication returned empty user")
                val user = User(
                    uid = fbUser.uid,
                    email = fbUser.email ?: email,
                    displayName = fbUser.displayName ?: email.substringBefore("@"),
                    isAnonymous = false
                )
                _currentUser.value = user
                Result.success(user)
            } else {
                // Local demo/offline fallback mode when Firebase isn't initialized
                val user = User(
                    uid = "local_" + email.trim().replace("@", "_").replace(".", "_"),
                    email = email.trim(),
                    displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    isAnonymous = false
                )
                _currentUser.value = user
                preferencesRepository.setOfflineUserId(user.uid)
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(name: String, email: String, password: String): Result<User> {
        return try {
            val auth = firebaseAuth
            if (auth != null) {
                val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
                val fbUser = authResult.user ?: throw Exception("Sign up returned empty user")
                if (name.isNotBlank()) {
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(name.trim())
                        .build()
                    fbUser.updateProfile(profileUpdate).await()
                }
                val user = User(
                    uid = fbUser.uid,
                    email = fbUser.email ?: email,
                    displayName = if (name.isNotBlank()) name.trim() else email.substringBefore("@"),
                    isAnonymous = false
                )
                _currentUser.value = user
                Result.success(user)
            } else {
                // Local demo/offline fallback mode
                val user = User(
                    uid = "local_" + email.trim().replace("@", "_").replace(".", "_"),
                    email = email.trim(),
                    displayName = if (name.isNotBlank()) name.trim() else email.substringBefore("@"),
                    isAnonymous = false
                )
                _currentUser.value = user
                preferencesRepository.setOfflineUserId(user.uid)
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun continueAsGuest() {
        val guestUser = User(
            uid = "guest_" + UUID.randomUUID().toString().take(8),
            email = "guest@akalabya.local",
            displayName = "Guest Writer",
            isAnonymous = true
        )
        _currentUser.value = guestUser
        CoroutineScope(Dispatchers.IO).launch {
            preferencesRepository.setOfflineUserId(guestUser.uid)
        }
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) {}
        _currentUser.value = null
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val auth = firebaseAuth
            val user = auth?.currentUser
            if (user != null) {
                user.delete().await()
            }
            _currentUser.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            _currentUser.value = null
            Result.success(Unit)
        }
    }
}
