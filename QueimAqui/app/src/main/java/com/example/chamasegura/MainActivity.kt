package com.example.chamasegura

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.chamasegura.data.entities.User
import com.example.chamasegura.data.entities.UserType
import com.example.chamasegura.data.vm.UserViewModel
import com.example.chamasegura.utils.AuthManager
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navigationView: NavigationView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userViewModel: UserViewModel
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if this is the first launch
        val prefs: SharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE)
        val firstLaunch = prefs.getBoolean("firstLaunch", true)

        if (firstLaunch) {
            // Launch OnboardingActivity
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()

            // Update the flag
            val editor: SharedPreferences.Editor = prefs.edit()
            editor.putBoolean("firstLaunch", false)
            editor.apply()
            return
        }

        if (Build.VERSION.SDK_INT < 16) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            supportActionBar?.hide()
        }

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        authManager = AuthManager(this)
        drawerLayout = findViewById(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.fragment_home,
                R.id.fragment_manage_profile,
                R.id.fragment_my_burn_history,
                R.id.fragment_burn_history,
                R.id.fragment_contact_us,
                R.id.fragment_county_history,
                R.id.fragment_manage_users,
                R.id.fragment_manage_user,
                R.id.fragment_pending_burn_requests
            ), drawerLayout
        )

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragment_home,
                R.id.fragment_manage_profile,
                R.id.fragment_my_burn_history,
                R.id.fragment_burn_history,
                R.id.fragment_contact_us,
                R.id.fragment_county_history,
                R.id.fragment_manage_users,
                R.id.fragment_manage_user,
                R.id.fragment_pending_burn_requests -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
                else -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
            }
        }

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        if (savedInstanceState == null) {
            navController.navigate(R.id.fragment_first_screen)
        }

        // Observe the user type change
        userViewModel.user.observe(this) {
            it?.let { user ->
                sharedPreferences.edit().putString("user_type", user.type.name).apply()
                updateNavigationHeader(it)
            }
        }

        // Call updateMenuItems with the current user type if user data is already loaded
        userViewModel.user.value?.let {
            updateMenuItems(it.type)
        }
    }

    override fun onResume() {
        super.onResume()
        // Initialize the menu based on the last known user type
        val userTypeString = sharedPreferences.getString("user_type", "REGULAR")
        val userType = UserType.valueOf(userTypeString ?: "REGULAR")
        updateMenuItems(userType)

        // Update the navigation header with the current user data if available
        userViewModel.user.value?.let {
            updateNavigationHeader(it)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_manage_profile -> navController.navigate(R.id.fragment_manage_profile)
            R.id.nav_home -> navController.navigate(R.id.fragment_home)
            R.id.nav_my_burn_history -> navController.navigate(R.id.fragment_my_burn_history)
            R.id.nav_burn_history -> navController.navigate(R.id.fragment_burn_history)
            R.id.nav_county_history -> navController.navigate(R.id.fragment_county_history)
            R.id.nav_manage_users -> navController.navigate(R.id.fragment_manage_users)
            R.id.nav_pending_burn_requests -> navController.navigate(R.id.fragment_pending_burn_requests)
            R.id.nav_contact_us -> navController.navigate(R.id.fragment_contact_us)
            R.id.nav_logout -> logout()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logout() {
        authManager.clearAuthData()
        sharedPreferences.edit().remove("user_type").apply()
        Toast.makeText(this, "Logout realizado com sucesso!", Toast.LENGTH_SHORT).show()
        // Redirecionar para o fragment_first_screen
        navController.navigate(R.id.fragment_first_screen)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        updateMenuItems(UserType.REGULAR)
    }

    fun updateNavigationHeader(user: User) {
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val textViewName = headerView.findViewById<TextView>(R.id.textViewName)
        val textViewNIF = headerView.findViewById<TextView>(R.id.textViewNIF)
        val textViewEmail = headerView.findViewById<TextView>(R.id.textViewEmail)

        textViewName.text = user.name
        textViewNIF.text = user.nif.toString()
        textViewEmail.text = user.email

        // Carregar a imagem de perfil usando Glide
        user.photo?.let {
            val imageUrl = it
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.baseline_account_circle_24)
                .into(imageView)
        }

        updateMenuItems(user.type)
    }

    fun updateMenuItems(userType: UserType) {
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val menu = navigationView.menu

        menu.findItem(R.id.nav_manage_profile).isVisible = true
        menu.findItem(R.id.nav_home).isVisible = true
        menu.findItem(R.id.nav_my_burn_history).isVisible = true
        menu.findItem(R.id.nav_contact_us).isVisible = true
        menu.findItem(R.id.nav_logout).isVisible = true

        menu.findItem(R.id.nav_burn_history).isVisible = userType == UserType.ICNF
        menu.findItem(R.id.nav_county_history).isVisible = userType == UserType.CM
        menu.findItem(R.id.nav_manage_users).isVisible = userType == UserType.ICNF
        menu.findItem(R.id.nav_pending_burn_requests).isVisible = userType == UserType.CM
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}