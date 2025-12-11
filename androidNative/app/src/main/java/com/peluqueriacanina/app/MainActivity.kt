package com.peluqueriacanina.app

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.peluqueriacanina.app.databinding.ActivityMainBinding
import com.peluqueriacanina.app.viewmodel.AuthViewModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        
        // Setup Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Observe auth state
        authViewModel.currentUser.observe(this) { user ->
            invalidateOptionsMenu()
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val isLoggedIn = authViewModel.currentUser.value != null
        menu.findItem(R.id.action_login)?.isVisible = !isLoggedIn
        menu.findItem(R.id.action_logout)?.isVisible = isLoggedIn
        return super.onPrepareOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_login -> {
                authViewModel.signIn(this)
                true
            }
            R.id.action_logout -> {
                authViewModel.signOut(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
