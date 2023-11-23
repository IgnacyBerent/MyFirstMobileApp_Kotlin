package com.example.mad_l3

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import android.graphics.Color


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val emailinput = findViewById<EditText>(R.id.EmailInput)
        val passwordinput = findViewById<EditText>(R.id.PasswordInput)
        val loginButton = findViewById<Button>(R.id.SubmitButton)

        val registerLink = findViewById<TextView>(R.id.registerLink)
        val spannableString = SpannableString("Don't have an account? Register")
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Start RegisterActivity
                val intent = Intent(this@MainActivity, RegisterActivity::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        spannableString.setSpan(clickableSpan, 23, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(StyleSpan(Typeface.BOLD), 23, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        registerLink.text = spannableString
        registerLink.movementMethod = LinkMovementMethod.getInstance()

        loginButton.setOnClickListener() {
            val email = emailinput.text.toString()
            val password = passwordinput.text.toString()
            loginUser(email, password)
        }

    }

    private fun showErrorSnackBar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.RED)
            .setTextColor(Color.WHITE)
            .show()
    }

    private fun validateLoginDetails(email: String, password: String): Boolean {
        return when {
            // email must not be empty
            email.isEmpty() -> {
                showErrorSnackBar("Please enter your email address.")
                false
            }
            // password must not be empty
            password.isEmpty() -> {
                showErrorSnackBar("Please enter your password.")
                false
            }

            else -> {
                true
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        if (validateLoginDetails(email, password)) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showErrorSnackBar("You have successfully logged in.")
                        val user = FirebaseAuth.getInstance().currentUser;
                        val uid = user?.email.toString()
                        val intent = Intent(this, SecondActivity::class.java)
                        intent.putExtra("uID", uid)
                        startActivity(intent)
                        finish()
                    } else {
                        showErrorSnackBar("Login failed. Please try again.")
                    }
                }
        }
    }
}