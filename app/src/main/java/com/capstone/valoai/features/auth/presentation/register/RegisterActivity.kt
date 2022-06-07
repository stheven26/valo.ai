package com.capstone.valoai.features.auth.presentation.register

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.capstone.valoai.databinding.ActivityRegisterBinding
import com.capstone.valoai.features.auth.presentation.login.LoginActivity
import com.capstone.valoai.features.dashboard.presentations.DashboardActivity
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {


    private var _binding: ActivityRegisterBinding? = null
    private val binding: ActivityRegisterBinding
        get() = _binding!!

    private var alreadyVaksin: Boolean = false;
    private var notAlreadyVaksin: Boolean = false

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        with(binding) {
            btnToLogin.setOnClickListener {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            }
            checkboxRegister.setOnClickListener {
                checkboxRegister.error = null
            }
            btnAlreadyVaksin.setOnClickListener {
                alreadyVaksin = true
                notAlreadyVaksin = false
                btnAlreadyVaksin.error = null
                btnNptAlreadyVaksin.error = null
                btnAlreadyVaksin.setBackgroundColor(0xff40D7FF.toInt())
                btnNptAlreadyVaksin.setBackgroundColor(0xff28B7DD.toInt())
            }
            btnNptAlreadyVaksin.setOnClickListener {
                alreadyVaksin = false
                notAlreadyVaksin = true
                btnAlreadyVaksin.error = null
                btnNptAlreadyVaksin.error = null
                btnAlreadyVaksin.setBackgroundColor(0xff28B7DD.toInt())
                btnNptAlreadyVaksin.setBackgroundColor(0xff40D7FF.toInt())
            }
            btnRegister.setOnClickListener {
                val email = fieldName.editText?.text.toString()
                val password = fieldPassword.editText?.text.toString()
                val alreadyVaksin = checkboxRegister.isChecked
                register(email, password, alreadyVaksin)
            }
        }

        // Initialize Firebase Auth
        firebaseAuth = Firebase.auth
        db = FirebaseFirestore.getInstance()
    }

    private fun register(email: String, password: String, alreadyVaksin: Boolean) {
        if (!validateForm()) return

        showProgressBar()

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                db.collection("users").document(firebaseAuth.currentUser?.uid ?: "").set(
                    mapOf(
                        "alreadyVaksin" to alreadyVaksin,
                        "email" to email
                    )
                ).addOnCompleteListener { tk ->
                    if (tk.isSuccessful) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(email)
                            .setPhotoUri(Uri.parse("https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png"))
                            .build()

                        firebaseAuth.currentUser?.updateProfile(profileUpdates)


                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        startActivity(Intent(this@RegisterActivity, FormPersonalActivity::class.java))
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserEmail:failure", task.exception)
                        Toast.makeText(
                            this@RegisterActivity,
                            "Authentication failed : ${task.exception?.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }


            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "createUserEmail:failure", task.exception)
                Toast.makeText(
                    this@RegisterActivity,
                    "Authentication failed : ${task.exception?.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            hideProgressBar()
        }
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = binding.fieldName.editText?.text.toString()
        if (TextUtils.isEmpty(email)) {
            binding.fieldName.error = "Required."
            valid = false
        } else {
            binding.fieldName.error = null
        }

        val password = binding.fieldPassword.editText?.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.fieldPassword.error = "Required."
            valid = false
        } else {
            binding.fieldPassword.error = null
        }

        val confirPassword = binding.fieldPasswordConfirmation.editText?.text.toString()
        when {
            TextUtils.isEmpty(confirPassword) -> {
                binding.fieldPasswordConfirmation.error = "Required."
                valid = false
            }
            password != confirPassword -> {
                binding.fieldPasswordConfirmation.error = "Password Tidak Sama"
                valid = false
            }
            else -> {
                binding.fieldPasswordConfirmation.error = null
            }
        }

        val checkBoxTermsAndPolicy = binding.checkboxRegister.isChecked
        if (!checkBoxTermsAndPolicy) {
            binding.checkboxRegister.error = "Required."
            valid = false
        } else {
            binding.checkboxRegister.error = null
        }

        if (!alreadyVaksin && !notAlreadyVaksin) {
            binding.btnAlreadyVaksin.error = "Required."
            binding.btnNptAlreadyVaksin.error = "Required."
            valid = false
        } else {
            binding.btnAlreadyVaksin.error = null
            binding.btnNptAlreadyVaksin.error = null
        }

        return valid
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            startActivity(
                Intent(
                    this@RegisterActivity,
                    DashboardActivity::class.java
                )
            )
            finish()
        }
    }

    companion object {
        internal val TAG = RegisterActivity::class.java.simpleName
    }
}