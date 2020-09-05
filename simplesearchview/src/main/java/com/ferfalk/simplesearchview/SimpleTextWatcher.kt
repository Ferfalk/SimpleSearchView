package com.ferfalk.simplesearchview;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * @author Fernando A. H. Falkiewicz
 */
public abstract class SimpleTextWatcher implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // No action
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // No action
    }

    @Override
    public void afterTextChanged(Editable s) {
        // No action
    }
}
