package com.example.job3lelinchakma

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.job3lelinchakma.ViewModels.AuthenticationViewModel
import com.example.job3lelinchakma.ViewModels.FirestoreViewModel
import com.example.job3lelinchakma.databinding.ActivityRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class RegisterActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }
    private lateinit var authViewModel: AuthenticationViewModel
    private lateinit var firestoreViewModel: FirestoreViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(this).get(AuthenticationViewModel::class.java)
        firestoreViewModel = ViewModelProvider(this).get(FirestoreViewModel::class.java)

        binding.registerBtn.setOnClickListener {
            val displayName = binding.displayNameEt.text.toString()
            val email = binding.emailEt.text.toString()
            val password = binding.passwordEt.text.toString()
            val conPassword = binding.conPasswordEt.text.toString()
            if (displayName.isNotEmpty() && email.isNotEmpty()){
                if (password.isNotEmpty() && conPassword.isNotEmpty()){
                    if (password == conPassword){
                        authViewModel.register(email, password, {
                            val location = "Location not available"
                            firestoreViewModel.saveUser(authViewModel.getCurrentUserId(), displayName, email, location)
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        },{ errorMessage ->
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                        })
                    }
                }
            }
        }
        binding.loginTxt.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
    override fun onStart() {
        super.onStart()
        if(Firebase.auth.currentUser!=null){
            startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
            finish()
        }
    }
}