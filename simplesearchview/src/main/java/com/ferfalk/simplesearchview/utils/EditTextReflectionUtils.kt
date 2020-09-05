package com.ferfalk.simplesearchview.utils;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * @author Fernando A. H. Falkiewicz
 */
public class EditTextReflectionUtils {
    private static final String TAG = EditTextReflectionUtils.class.getSimpleName();
    private static final String EDIT_TEXT_FIELD_CURSOR_DRAWABLE_RES = "mCursorDrawableRes";
    private static final String EDIT_TEXT_FIELD_EDITOR = "mEditor";
    private static final String EDIT_TEXT_FIELD_CURSOR_DRAWABLE = "mCursorDrawable";

    private EditTextReflectionUtils() {
    }

    /**
     * Uses reflection to set an EditText cursor drawable
     */
    public static void setCursorDrawable(@NonNull EditText editText, int drawable) {
        try {
            // https://github.com/android/platform_frameworks_base/blob/kitkat-release/core/java/android/widget/TextView.java#L562-564
            Field f = TextView.class.getDeclaredField(EDIT_TEXT_FIELD_CURSOR_DRAWABLE_RES);
            f.setAccessible(true);
            f.set(editText, drawable);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Uses reflection to set an EditText cursor color
     */
    public static void setCursorColor(@NonNull EditText editText, @ColorInt int color) {
        try {
            // Get the cursor resource id
            Field field = TextView.class.getDeclaredField(EDIT_TEXT_FIELD_CURSOR_DRAWABLE_RES);
            field.setAccessible(true);
            int drawableResId = field.getInt(editText);

            // Get the editor
            field = TextView.class.getDeclaredField(EDIT_TEXT_FIELD_EDITOR);
            field.setAccessible(true);
            Object editor = field.get(editText);

            // Get the drawable and set a color filter
            Drawable drawable = ContextCompat.getDrawable(editText.getContext(), drawableResId);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            Drawable[] drawables = {drawable, drawable};

            // Set the drawables
            field = editor.getClass().getDeclaredField(EDIT_TEXT_FIELD_CURSOR_DRAWABLE);
            field.setAccessible(true);
            field.set(editor, drawables);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
