package ru.irinavb.whatsupclone

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import ru.irinavb.whatsupclone.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun onLogin(view: View) {
        Toast.makeText(this, "Login clicked", Toast.LENGTH_LONG).show()
    }

    fun onClick(view: View) {
        startActivity(SignupActivity.newIntent(this))
        finish()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }
}