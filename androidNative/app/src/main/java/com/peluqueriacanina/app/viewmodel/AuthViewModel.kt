package com.peluqueriacanina.app.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    
    private val _currentUser = MutableLiveData<GoogleSignInAccount?>()
    val currentUser: LiveData<GoogleSignInAccount?> = _currentUser
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private var signInLauncher: ActivityResultLauncher<Intent>? = null
    
    companion object {
        // Replace with your Web Client ID
        const val WEB_CLIENT_ID = "657200314182-8e95la9638tnt2nmdqjj5s9i6mmqbf6d.apps.googleusercontent.com"
        
        private val CALENDAR_SCOPE = Scope("https://www.googleapis.com/auth/calendar")
        private val CALENDAR_EVENTS_SCOPE = Scope("https://www.googleapis.com/auth/calendar.events")
        // Use full drive scope to access files created by webapp
        private val DRIVE_SCOPE = Scope("https://www.googleapis.com/auth/drive")
    }
    
    fun getSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestIdToken(WEB_CLIENT_ID)
            .requestScopes(CALENDAR_SCOPE, CALENDAR_EVENTS_SCOPE, DRIVE_SCOPE)
            .build()
    }
    
    fun getSignInClient(activity: Activity): GoogleSignInClient {
        return GoogleSignIn.getClient(activity, getSignInOptions())
    }
    
    fun checkExistingSignIn(activity: Activity) {
        val account = GoogleSignIn.getLastSignedInAccount(activity)
        _currentUser.value = account
    }
    
    fun setSignedInAccount(account: GoogleSignInAccount) {
        _currentUser.value = account
        _error.value = null
    }
    
    fun signIn(activity: Activity) {
        _isLoading.value = true
        val signInClient = getSignInClient(activity)
        val signInIntent = signInClient.signInIntent
        
        // Note: In a real app, you'd use ActivityResultLauncher
        // For simplicity, we'll handle this in the Activity
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    
    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                _currentUser.value = account
                _error.value = null
            } catch (e: ApiException) {
                _error.value = "Error de autenticación: ${e.statusCode}"
                _currentUser.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun signOut(activity: Activity) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                getSignInClient(activity).signOut().addOnCompleteListener {
                    _currentUser.value = null
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error al cerrar sesión"
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

const val RC_SIGN_IN = 9001
