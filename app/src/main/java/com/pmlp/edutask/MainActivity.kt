package com.pmlp.edutask

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Adjust for Edge-to-Edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Bind Views
        val cardSignIn = findViewById<MaterialCardView>(R.id.card_signin)
        val cardRegister = findViewById<MaterialCardView>(R.id.card_register)
        val btnToggleToRegister = findViewById<MaterialButton>(R.id.btn_toggle_to_register)
        val btnToggleToSignIn = findViewById<MaterialButton>(R.id.btn_toggle_to_signin)

        val btnSignInSubmit = findViewById<MaterialButton>(R.id.btn_signin_submit)
        val btnRegisterSubmit = findViewById<MaterialButton>(R.id.btn_register_submit)

        // Toggle to Register Card
        btnToggleToRegister.setOnClickListener {
            cardSignIn.visibility = View.GONE
            cardRegister.visibility = View.VISIBLE

            btnToggleToRegister.visibility = View.GONE
            btnToggleToSignIn.visibility = View.VISIBLE
        }

        // Toggle to Sign In Card
        btnToggleToSignIn.setOnClickListener {
            cardRegister.visibility = View.GONE
            cardSignIn.visibility = View.VISIBLE

            btnToggleToSignIn.visibility = View.GONE
            btnToggleToRegister.visibility = View.VISIBLE
        }

        // Simple action feedbacks
        btnSignInSubmit.setOnClickListener {
            Toast.makeText(this, "Sign In Clicked", Toast.LENGTH_SHORT).show()
        }

        btnRegisterSubmit.setOnClickListener {
            Toast.makeText(this, "Register Clicked", Toast.LENGTH_SHORT).show()
        }
    }
}