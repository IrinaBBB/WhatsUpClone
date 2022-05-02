package ru.irinavb.whatsupclone.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ru.irinavb.whatsupclone.databinding.ActivitySignupBinding
import ru.irinavb.whatsupclone.util.DATA_USERS
import ru.irinavb.whatsupclone.util.User

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var imm: InputMethodManager

    private val firebaseDb = FirebaseFirestore.getInstance()

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        if (user != null) {
            startActivity(MainActivity.newIntent(this))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    }

    fun onSignUp(view: View) {
        var proceed = true
        if (binding.nameEditText.text.isNullOrEmpty()) {
            binding.nameEditText.error = "Name is required!"
            proceed = false
        }

        if (binding.phoneEditText.text.isNullOrEmpty()) {
            binding.phoneEditText.error = "Phone is required!"
            proceed = false
        }

        if (binding.emailEditText.text.isNullOrEmpty()) {
            binding.emailEditText.error = "Email is required!"
            proceed = false
        }

        if (binding.passwordEditText.text.isNullOrEmpty()) {
            binding.passwordEditText.error = "Password is required!"
            proceed = false
        }

        if (proceed) {
            binding.loading.visibility = View.VISIBLE
            firebaseAuth.createUserWithEmailAndPassword(
                binding.emailEditText.text.toString(),
                binding.passwordEditText.text.toString()
            )
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        binding.loading.visibility = View.GONE
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        Toast.makeText(
                            this@SignupActivity,
                            "Login error: ${task.exception?.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (firebaseAuth.uid != null) {
                        val email = binding.emailEditText.text.toString()
                        val phone = binding.phoneEditText.text.toString()
                        val name = binding.nameEditText.text.toString()
                        val user = User(
                            email = email,
                            phone = phone,
                            name = name,
                            imageUrl = "",
                            status = "Hello! I'm new",
                            statusUrl = "",
                            statusTime = ""
                        )
                        firebaseDb.collection(DATA_USERS).document(firebaseAuth.uid!!).set(user)
                    }
                    binding.loading.visibility = View.GONE
                }
                .addOnFailureListener { e ->
                    binding.loading.visibility = View.GONE
                    e.printStackTrace()
                }
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(firebaseAuthListener)
    }

    fun onClick(view: View) {
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, SignupActivity::class.java)
    }
}