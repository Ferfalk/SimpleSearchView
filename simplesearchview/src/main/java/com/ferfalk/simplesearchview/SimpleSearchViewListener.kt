package com.ferfalk.simplesearchview

import com.ferfalk.simplesearchview.SimpleSearchView.SearchViewListener

/**
 * @author Fernando A. H. Falkiewicz
 */
abstract class SimpleSearchViewListener : SearchViewListener {
    override fun onSearchViewShown() {
        // No action
    }

    override fun onSearchViewClosed() {
        // No action
    }

    override fun onSearchViewShownAnimation() {
        // No action
    }

    override fun onSearchViewClosedAnimation() {
        // No action
    }
}