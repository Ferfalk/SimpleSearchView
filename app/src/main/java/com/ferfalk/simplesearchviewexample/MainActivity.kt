package com.ferfalk.simplesearchviewexample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.ferfalk.simplesearchview.SimpleSearchView
import com.ferfalk.simplesearchview.utils.DimensUtils.convertDpToPx
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private val container by lazy { findViewById<ViewPager2>(R.id.container) }
    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val searchView by lazy { findViewById<SimpleSearchView>(R.id.searchView) }
    private val tabLayout by lazy { findViewById<TabLayout>(R.id.tabLayout) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        setSupportActionBar(toolbar)
        container.adapter = SectionsPagerAdapter(this)
//        container.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))
//        tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        setupSearchView(menu)
        return true
    }

    private fun setupSearchView(menu: Menu) {
        val item = menu.findItem(R.id.action_search)
        searchView.apply {
            setMenuItem(item)
            setTabLayout(this@MainActivity.tabLayout)
            setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener{
                override fun onQueryTextChange(newText: String): Boolean {
                    Log.e(TAG, getString(R.string.changed, newText))
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    Log.e(TAG, getString(R.string.submitted, query))
                    return false
                }

                override fun onQueryTextCleared(): Boolean {
                    Log.e(TAG, getString(R.string.cleared))
                    return false
                }
            })
        }

        // Adding padding to the animation because of the hidden menu item
        val revealCenter = searchView.revealAnimationCenter
        revealCenter!!.x -= convertDpToPx(EXTRA_REVEAL_CENTER_PADDING, this@MainActivity)
    }

    override fun onBackPressed() {
        if (searchView.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (searchView.onActivityResult(requestCode, resultCode, data!!)) {
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    class PlaceholderFragment : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_main, container, false)
            val textView = rootView.findViewById<TextView>(R.id.section_label)
            textView.text = getString(R.string.section_format, requireArguments().getInt(ARG_SECTION_NUMBER))
            return rootView
        }

        companion object {
            private const val ARG_SECTION_NUMBER = "section_number"
            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }

    class SectionsPagerAdapter internal constructor(fm: FragmentActivity) : FragmentStateAdapter(fm) {

        override fun getItemCount() = 3

        override fun createFragment(position: Int) = PlaceholderFragment.newInstance(position + 1)
    }

    companion object {
        const val EXTRA_REVEAL_CENTER_PADDING = 40
    }
}