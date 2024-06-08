package com.example.job3lelinchakma

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.job3lelinchakma.Adapter.UserAdapter
import com.example.job3lelinchakma.ViewModels.AuthenticationViewModel
import com.example.job3lelinchakma.ViewModels.FirestoreViewModel
import com.example.job3lelinchakma.ViewModels.LocationViewModel
import com.example.job3lelinchakma.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var authViewModel: AuthenticationViewModel
    private lateinit var firestoreViewModel: FirestoreViewModel
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var userAdapter: UserAdapter
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLocation()
            } else {
                // Handle the case where permission is not granted
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.nav_open, R.string.nav_close)
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.navview.setNavigationItemSelectedListener(this)

        binding.locationBtn.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }

        authViewModel = ViewModelProvider(this).get(AuthenticationViewModel::class.java)
        firestoreViewModel = ViewModelProvider(this).get(FirestoreViewModel::class.java)
        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationViewModel.initializeFusedLocationClient(fusedLocationClient)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getLocation()
        }

        userAdapter = UserAdapter(emptyList())
        binding.userRV.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        fetchUsers()
    }

    private fun fetchUsers() {
        firestoreViewModel.getAllUsers { userList ->
            userAdapter.updateData(userList)
        }
    }

    private fun getLocation() {
        locationViewModel.getLastLocation { location ->
            authViewModel.getCurrentUserId()?.let { userId ->
                firestoreViewModel.updateUserLocation(userId, location)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open_drawer -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
                true
            } else super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                binding.drawerLayout.closeDrawers()
            }
            R.id.logout -> {
                Firebase.auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                binding.drawerLayout.closeDrawers()
            }
        }
        return true
    }
}
