package ru.irinavb.whatsupclone.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ru.irinavb.whatsupclone.R
import ru.irinavb.whatsupclone.activities.fragments.ChatFragment
import ru.irinavb.whatsupclone.activities.fragments.StatusFragment
import ru.irinavb.whatsupclone.activities.fragments.StatusUpdateFragment
import ru.irinavb.whatsupclone.activities.listeners.FailureCallback
import ru.irinavb.whatsupclone.activities.ui.SectionsPagerAdapter
import ru.irinavb.whatsupclone.databinding.ActivityMainBinding
import ru.irinavb.whatsupclone.databinding.FragmentMainBinding
import ru.irinavb.whatsupclone.util.DATA_USERS
import ru.irinavb.whatsupclone.util.DATA_USER_PHONE
import ru.irinavb.whatsupclone.util.PERMISSION_REQUEST_READ_CONTACTS
import ru.irinavb.whatsupclone.util.REQUEST_NEW_CHAT


class MainActivity : AppCompatActivity(), FailureCallback {

    private var sectionPagerAdapter: SectionPagerAdapter? = null

    private lateinit var binding: ActivityMainBinding

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDB = FirebaseFirestore.getInstance()

    private val chatsFragment = ChatFragment()
    private val statusUpdateFragment = StatusUpdateFragment()
    private val statusListFragment = StatusFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setSupportActionBar(binding.toolbar)
        setContentView(binding.root)


        chatsFragment.setFailureCallbackListener(this)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.container
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    1, 2 -> binding.fab.hide()
                    0 -> binding.fab.show()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })

    }

    private fun resizeTabs() {
        val layout = (binding.tabs.getChildAt(0) as LinearLayout)
            .getChildAt(0) as LinearLayout
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0.4f
        layout.layoutParams = layoutParams
    }

    fun onNewChat(v: View) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission
                        .READ_CONTACTS
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Contacts permission")
                    .setMessage(
                        "This app requires access to your contacts to initiate a " +
                                "conversation"
                    )
                    .setPositiveButton("Ask me") { _, _ -> requestContactPermission() }
                    .setNegativeButton("No") { _, _ -> }
                    .show()
            } else {
                requestContactPermission()
            }
        } else {
            startNewActivity()
        }
    }

    private fun requestContactPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.READ_CONTACTS),
            PERMISSION_REQUEST_READ_CONTACTS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_READ_CONTACTS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager
                        .PERMISSION_GRANTED
                ) {
                    startNewActivity()
                }
            }
        }
    }

//    private fun startNewActivity() {
//        startActivityForResult(ContactsActivity.newIntent(this), REQUEST_NEW_CHAT)
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK) {
//            when (requestCode) {
//                REQUEST_NEW_CHAT -> {
//                    val name = data?.getStringExtra(PARAM_NAME) ?: ""
//                    val phone = data?.getStringExtra(PARAM_PHONE) ?: ""
//                    checkNewChatUser(name, phone)
//                }
//            }
//        }
//    }

    private fun startNewActivity() {
        resultLauncher.launch(ContactsActivity.newIntent(this))
    }

    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val name = result.data?.getStringExtra(PARAM_NAME) ?: ""
            val phone = result.data?.getStringExtra(PARAM_PHONE) ?: ""
            checkNewChatUser(name, phone)
        }
    }

    private fun checkNewChatUser(name: String, phone: String) {
        if (name.isNotEmpty() && phone.isNotEmpty()) {
            firebaseDB.collection(DATA_USERS)
                .whereEqualTo(DATA_USER_PHONE, phone)
                .get()
                .addOnSuccessListener { result ->
                    if (result.documents.size > 0) {
                        chatsFragment.newChat(result.documents[0].id)
                    } else {
                        AlertDialog.Builder(this)
                            .setTitle("User not found")
                            .setMessage(
                                "$name does not have an account. Send them an sms to " +
                                        "install this app"
                            )
                            .setPositiveButton("OK") { _, _ ->
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse("sms:$phone")
                                intent.putExtra(
                                    "sms_body", "Hi. I'm using this new cool " +
                                            "WhatsUpClone. You should install it too so we can chat " +
                                            "there"
                                )
                                startActivity(intent)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.d("DATABASE ERROR", e.toString())
                    Toast.makeText(
                        this,
                        "An error occurred. Please try again later",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
        }
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

    override fun onResume() {
        super.onResume()

        if (firebaseAuth.currentUser == null) {
            startActivity(LoginActivity.newIntent(this))
            finish()
        }
    }

    inner class SectionPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int {
            return 4
        }

        override fun getItem(position: Int): Fragment {
            return MainFragment.newIntent(position + 1)
        }
    }

    class MainFragment : Fragment() {

        private var _binding: FragmentMainBinding? = null
        private val binding get() = _binding!!

        @SuppressLint("SetTextI18n")
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {

            _binding = FragmentMainBinding.inflate(inflater, container, false)
            return binding.root

        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

        companion object {
            private const val ARG_SECTION_NUMBER = "Section number"

            fun newIntent(sectionNumber: Int): MainFragment {
                val fragment = MainFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }

    override fun onUserError() {
        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

    companion object {
        const val PARAM_NAME = "Param name"
        const val PARAM_PHONE = "Param phone"
        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}