package com.ferfalk.simplesearchview

import android.text.Editable
import android.text.TextWatcher

/**
 * @author Fernando A. H. Falkiewicz
 */
abstract class SimpleTextWatcher : TextWatcher {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // No action
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        // No action
    }

    override fun afterTextChanged(s: Editable) {
        // No action
    }
}