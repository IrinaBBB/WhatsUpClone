package ru.irinavb.whatsupclone.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue.delete
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import ru.irinavb.whatsupclone.R
import ru.irinavb.whatsupclone.databinding.ActivityProfileBinding
import ru.irinavb.whatsupclone.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val firebaseStorage = FirebaseStorage.getInstance().reference

    private var imageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        if (userId.isNullOrEmpty()) {
            finish()
        }

        binding.photoImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PHOTO)
        }
        populateInfo()
    }

    private fun populateInfo() {
        binding.loading.visibility = View.VISIBLE
        firebaseDb.collection(DATA_USERS)
            .document(userId!!)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                imageUrl = user?.imageUrl
                if (imageUrl != null) {
                    populateImage(this, imageUrl, binding.photoImageView, R.drawable.default_user)
                }
                binding.nameEditText.setText(user?.name, TextView.BufferType.EDITABLE)
                binding.emailEditText.setText(user?.email, TextView.BufferType.EDITABLE)
                binding.phoneEditText.setText(user?.phone, TextView.BufferType.EDITABLE)
                binding.loading.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                finish()
            }
    }

    fun onApply(view: View) {
        binding.loading.visibility = View.VISIBLE
        val name = binding.nameEditText.text
        val email = binding.emailEditText.text
        val phone = binding.phoneEditText.text
        val map = HashMap<String, Any>()
        map[DATA_USER_NAME] = name
        map[DATA_USER_EMAIL] = email
        map[DATA_USER_PHONE] = phone
        firebaseDb.collection(DATA_USERS)
            .document(userId!!)
            .update(map)
            .addOnSuccessListener {
                Toast.makeText(this, "Update Successful", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
                binding.loading.visibility = View.GONE
            }
    }

    fun onDelete(view: View) {
        binding.loading.visibility = View.VISIBLE
        AlertDialog.Builder(this)
            .setTitle("Delete account")
            .setMessage("This will delete your profile information. Are you sure?")
            .setPositiveButton("Yes") { _, _ ->
                Toast.makeText(this, "Profile Deleted", Toast.LENGTH_SHORT).show()
                firebaseDb
                    .collection(DATA_USERS)
                    .document(userId!!)
                    .delete()
                firebaseStorage
                    .child(DATA_IMAGES)
                    .child(userId)
                    .delete()
                firebaseAuth
                    .currentUser?.delete()
                binding.loading.visibility = View.GONE
                startActivity(LoginActivity.newIntent(this))
            }
            .setNegativeButton("No") { _, _ ->
                binding.loading.visibility = View.GONE
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_profile -> onProfile()
            R.id.action_logout -> onLogout()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun onProfile() {
        startActivity(ProfileActivity.newIntent(this))
        finish()
    }

    private fun onLogout() {
        firebaseAuth.signOut()
        startActivity(LoginActivity.newIntent(this))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            storeImage(data?.data)
        }
    }

    private fun storeImage(imageUri: Uri?) {
        if (imageUri != null) {
            Toast.makeText(this, "Uploading ...", Toast.LENGTH_SHORT).show()
            binding.loading.visibility = View.VISIBLE
            val filePath = firebaseStorage.child(DATA_IMAGES).child(userId!!)

            filePath.putFile(imageUri)
                .addOnSuccessListener {
                    filePath.downloadUrl
                        .addOnSuccessListener { taskSnapshot ->
                            val url = taskSnapshot.toString()
                            firebaseDb.collection(DATA_USERS)
                                .document(userId)
                                .update(DATA_USER_IMAGE_URL, url)
                                .addOnSuccessListener {
                                    imageUrl = url
                                    populateImage(
                                        this,
                                        imageUrl,
                                        binding.photoImageView,
                                        R.drawable.default_user
                                    )
                                }
                            binding.loading.visibility = View.GONE
                        }
                        .addOnFailureListener { e ->
                            onUploadFailure(e)
                        }
                }
                .addOnFailureListener { e ->
                    onUploadFailure(e)
                }
        }
    }

    private fun onUploadFailure(e: Exception) {
        binding.loading.visibility = View.GONE
        Log.d("ErrorFirebase", e.toString())
        Toast.makeText(
            this, "Image upload failed. Please try again later.",
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, ProfileActivity::class.java)
    }
}