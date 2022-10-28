package com.ferfalk.simplesearchview.utils

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat

/**
 * @author Fernando A. H. Falkiewicz
 */
object EditTextReflectionUtils {
    private val TAG = EditTextReflectionUtils::class.java.simpleName
    private const val EDIT_TEXT_FIELD_CURSOR_DRAWABLE_RES = "mCursorDrawableRes"
    private const val EDIT_TEXT_FIELD_EDITOR = "mEditor"
    private const val EDIT_TEXT_FIELD_CURSOR_DRAWABLE = "mCursorDrawable"

    /**
     * Uses reflection to set an EditText cursor drawable
     */
    @SuppressLint("DiscouragedPrivateApi")
    @JvmStatic
    fun setCursorDrawable(editText: EditText, drawable: Int) {
        if (isAndroidQ()) {
            editText.setTextCursorDrawable(drawable)
        } else {
            try {
                // https://github.com/android/platform_frameworks_base/blob/kitkat-release/core/java/android/widget/TextView.java#L562-564
                val f = TextView::class.java.getDeclaredField(EDIT_TEXT_FIELD_CURSOR_DRAWABLE_RES)
                f.isAccessible = true
                f[editText] = drawable
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
            }

        }
    }

    /**
     * Uses reflection to set an EditText cursor color
     */
    @SuppressLint("DiscouragedPrivateApi")
    @JvmStatic
    fun setCursorColor(editText: EditText, @ColorInt color: Int) {
        if (isAndroidQ()) {
            editText.textCursorDrawable = ColorDrawable(color)
        } else {
            try {
                // Get the cursor resource id
                var field = TextView::class.java.getDeclaredField(EDIT_TEXT_FIELD_CURSOR_DRAWABLE_RES)
                field.isAccessible = true
                val drawableResId = field.getInt(editText)

                // Get the editor
                field = TextView::class.java.getDeclaredField(EDIT_TEXT_FIELD_EDITOR)
                field.isAccessible = true
                val editor = field[editText]

                // Get the drawable and set a color filter
                val drawable = ContextCompat.getDrawable(editText.context, drawableResId)
                drawable?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_IN)
                val drawables = arrayOf(drawable, drawable)

                // Set the drawables
                field = editor.javaClass.getDeclaredField(EDIT_TEXT_FIELD_CURSOR_DRAWABLE)
                field.isAccessible = true
                field[editor] = drawables
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
            }
        }
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
    fun isAndroidQ() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}