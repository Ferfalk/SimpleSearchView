package com.ferfalk.simplesearchview

/**
 * @author Fernando A. H. Falkiewicz
 */
abstract class SimpleOnQueryTextListener : SimpleSearchView.OnQueryTextListener {
    override fun onQueryTextSubmit(query: String): Boolean {
        // No action
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        // No action
        return false
    }

    override fun onQueryTextCleared(): Boolean {
        // No action
        return false
    }
}