package ru.irinavb.whatsupclone.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import ru.irinavb.whatsupclone.R
import ru.irinavb.whatsupclone.databinding.ActivityMainBinding
import ru.irinavb.whatsupclone.databinding.FragmentMainBinding


class MainActivity : AppCompatActivity() {

    private var sectionPagerAdapter: SectionPagerAdapter? = null

    private lateinit var binding: ActivityMainBinding
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        sectionPagerAdapter = SectionPagerAdapter(supportFragmentManager)
        binding.container.adapter = sectionPagerAdapter
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with action", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show()
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

    inner class SectionPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {
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

    companion object {
        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}