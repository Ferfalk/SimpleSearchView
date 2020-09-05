package com.ferfalk.simplesearchview

import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

/**
 * @author Fernando A. H. Falkiewicz
 */
abstract class SimpleOnTabSelectedListener : OnTabSelectedListener {
    override fun onTabSelected(tab: TabLayout.Tab) {
        // No action
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        // No action
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
        // No action
    }
}