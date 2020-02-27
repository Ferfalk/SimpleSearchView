package com.ferfalk.simplesearchview.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.TypedValue;

public class DimensUtils {
    private DimensUtils() {
    }

    public static int convertDpToPx(int dp, @NonNull Context context) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
    }

    public static float convertDpToPx(float dp, @NonNull Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
