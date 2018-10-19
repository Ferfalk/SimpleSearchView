package com.ferfalk.simplesearchview.utils;

import androidx.annotation.NonNull;
import android.view.View;

/**
 * @author Fernando A. H. Falkiewicz
 */
public abstract class SimpleAnimationListener implements SimpleAnimationUtils.AnimationListener {
    @Override
    public boolean onAnimationStart(@NonNull View view) {
        // No action
        return false;
    }

    @Override
    public boolean onAnimationEnd(@NonNull View view) {
        // No action
        return false;
    }

    @Override
    public boolean onAnimationCancel(@NonNull View view) {
        // No action
        return false;
    }
}
