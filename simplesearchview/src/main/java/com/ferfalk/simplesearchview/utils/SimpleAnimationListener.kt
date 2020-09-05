package com.ferfalk.simplesearchview.utils

import android.view.View

/**
 * @author Fernando A. H. Falkiewicz
 */
abstract class SimpleAnimationListener : SimpleAnimationUtils.AnimationListener {
    override fun onAnimationStart(view: View): Boolean {
        // No action
        return false
    }

    override fun onAnimationEnd(view: View): Boolean {
        // No action
        return false
    }

    override fun onAnimationCancel(view: View): Boolean {
        // No action
        return false
    }
}