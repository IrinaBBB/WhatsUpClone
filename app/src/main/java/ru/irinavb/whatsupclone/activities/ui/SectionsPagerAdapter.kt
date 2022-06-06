package ru.irinavb.whatsupclone.activities.ui

import ru.irinavb.whatsupclone.R
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ru.irinavb.whatsupclone.activities.fragments.ChatFragment
import ru.irinavb.whatsupclone.activities.fragments.StatusFragment
import ru.irinavb.whatsupclone.activities.fragments.StatusUpdateFragment


private val TAB_TITLES = arrayOf(
    R.string.tab_text_1,
    R.string.tab_text_2,
    R.string.tab_text_3,
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> ChatFragment()
            1 -> StatusFragment()
            2 -> StatusUpdateFragment()
            else -> ChatFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return 3
    }
}