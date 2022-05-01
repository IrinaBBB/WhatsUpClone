package ru.irinavb.whatsupclone

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.snackbar.Snackbar
import ru.irinavb.whatsupclone.databinding.ActivityMainBinding
import ru.irinavb.whatsupclone.databinding.FragmentMainBinding


class MainActivity : AppCompatActivity() {

    private var sectionPagerAdapter: SectionPagerAdapter? = null

    private lateinit var binding: ActivityMainBinding

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
            binding.sectionLabel.text = "Hello world from section ${arguments?.getInt(ARG_SECTION_NUMBER)}"
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
}