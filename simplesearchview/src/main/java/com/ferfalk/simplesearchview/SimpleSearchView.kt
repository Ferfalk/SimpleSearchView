package com.ferfalk.simplesearchview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.ferfalk.simplesearchview.databinding.SearchViewBinding
import com.ferfalk.simplesearchview.utils.ContextUtils.getAccentColor
import com.ferfalk.simplesearchview.utils.ContextUtils.getPrimaryColor
import com.ferfalk.simplesearchview.utils.ContextUtils.hideKeyboard
import com.ferfalk.simplesearchview.utils.ContextUtils.scanForActivity
import com.ferfalk.simplesearchview.utils.ContextUtils.showKeyboard
import com.ferfalk.simplesearchview.utils.DimensUtils.convertDpToPx
import com.ferfalk.simplesearchview.utils.EditTextReflectionUtils.setCursorColor
import com.ferfalk.simplesearchview.utils.EditTextReflectionUtils.setCursorDrawable
import com.ferfalk.simplesearchview.utils.SimpleAnimationListener
import com.ferfalk.simplesearchview.utils.SimpleAnimationUtils
import com.ferfalk.simplesearchview.utils.SimpleAnimationUtils.hideOrFadeOut
import com.ferfalk.simplesearchview.utils.SimpleAnimationUtils.revealOrFadeIn
import com.ferfalk.simplesearchview.utils.SimpleAnimationUtils.verticalSlideView
import com.google.android.material.tabs.TabLayout

/**
 * @author Fernando A. H. Falkiewicz
 */
class SimpleSearchView @JvmOverloads constructor(creationContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(creationContext, attrs, defStyleAttr) {
    @IntDef(STYLE_BAR, STYLE_CARD)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class Style

    /**
     * @param animationDuration duration, in ms, of the reveal or fade animations
     * @return current reveal or fade animations duration
     */
    var animationDuration = SimpleAnimationUtils.ANIMATION_DURATION_DEFAULT

    /**
     * @param revealAnimationCenter center of the reveal animation, used to customize the origin of the animation
     * @return center of the reveal animation, by default it is placed where the rightmost MenuItem would be
     */
    var revealAnimationCenter: Point? = null
        get() {
            if (field != null) {
                return field
            }

            val centerX = width - convertDpToPx(ANIMATION_CENTER_PADDING, context)
            val centerY = height / 2

            field = Point(centerX, centerY)
            return field
        }
    private var query: CharSequence? = null
    private var oldQuery: CharSequence? = null
    private var allowVoiceSearch = false
    var isSearchOpen = false
        private set
    private var isClearingFocus = false
    private var voiceSearchPrompt: String? = ""

    @Style
    private var style = STYLE_BAR

    /**
     * @return the TabLayout attached to the SimpleSearchView behavior
     */
    var tabLayout: TabLayout? = null
        private set
    private var tabLayoutInitialHeight = 0
    private var onQueryChangeListener: OnQueryTextListener? = null
    private var searchViewListener: SearchViewListener? = null
    private var searchIsClosing = false
    private var keepQuery = false

    private val binding = SearchViewBinding.inflate(LayoutInflater.from(context), this, true)

    private fun initStyle(attrs: AttributeSet?, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SimpleSearchView, defStyleAttr, 0)
        if (typedArray.hasValue(R.styleable.SimpleSearchView_type)) {
            cardStyle = typedArray.getInt(R.styleable.SimpleSearchView_type, style)
        }
        if (typedArray.hasValue(R.styleable.SimpleSearchView_backIconAlpha)) {
            setBackIconAlpha(typedArray.getFloat(R.styleable.SimpleSearchView_backIconAlpha, BACK_ICON_ALPHA_DEFAULT))
        }
        if (typedArray.hasValue(R.styleable.SimpleSearchView_iconsAlpha)) {
            setIconsAlpha(typedArray.getFloat(R.styleable.SimpleSearchView_iconsAlpha, ICONS_ALPHA_DEFAULT))
        }
        if (typedArray.hasValue(R.styleable.SimpleSearchView_backIconTint)) {
            setBackIconColor(typedArray.getColor(R.styleable.SimpleSearchView_backIconTint, getPrimaryColor(context)))
        }
        if (typedArray.hasValue(R.styleable.SimpleSearchView_iconsTint)) {
            setIconsColor(typedArray.getColor(R.styleable.SimpleSearchView_iconsTint, Color.BLACK))
        }
        if (typedArray.hasValue(R.styleable.SimpleSearchView_cursorColor)) {
            setCursorColor(typedArray.getColor(R.styleable.SimpleSearchView_cursorColor, getAccentColor(context)))
        }
        if (typedArray.hasValue(R.styleable.SimpleSearchView_hintColor)) {
            setHintTextColor(typedArray.getColor(R.styleable.SimpleSearchView_hintColor, ContextCompat.getColor(context, R.color.default_textColorHint)))
        }
        if (typedArray.hasValue(R.styleable.SimpleSearchView_searchBackground)) {
            setSearchBackground(typedArray.getDrawable(R.styleable.SimpleSearchView_searchBackground))
        }
        if (typedArray.hasValue(R.styleable.SimpleSearchView_searchBackIcon)) {
            setBackIconDrawable(typedArray.getDrawable(R.styleable.SimpleSearchView_searchBackIcon))
        }
        if (typedArray.hasValue(R.styleable.SimpleSearchView_searchClearIcon)) {
            setClearIconDrawable(typedArray.getDrawable(R.styleable.SimpleSearchView_searchClearIcon))
        }
        if (typedArray.hasValue(R.styleable.SimpleSearchView_searchVoiceIcon)) {
            setVoiceIconDrawable(typedArray.getDrawable(R.styleable.SimpleSearchView_searchVoiceIcon))
        }
        if (typedArray.hasValue(R.styleable.SimpleSearchView_voiceSearch)) {
            enableVoiceSearch(typedArray.getBoolean(R.styleable.SimpleSearchView_voiceSearch, allowVoiceSearch))
        }
        if (typedArray.hasValue(R.styleable.SimpleSearchView_voiceSearchPrompt)) {
            setVoiceSearchPrompt(typedArray.getString(R.styleable.SimpleSearchView_voiceSearchPrompt))
        }
        if (typedArray.hasValue(R.styleable.SimpleSearchView_android_hint)) {
            setHint(typedArray.getString(R.styleable.SimpleSearchView_android_hint))
        }
        if (typedArray.hasValue(R.styleable.SimpleSearchView_android_inputType)) {
            setInputType(typedArray.getInt(R.styleable.SimpleSearchView_android_inputType, EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS))
        }
        if (typedArray.hasValue(R.styleable.SimpleSearchView_android_textColor)) {
            setTextColor(typedArray.getColor(R.styleable.SimpleSearchView_android_textColor, ContextCompat.getColor(context, R.color.default_textColor)))
        }
        typedArray.recycle()
    }

    private fun initSearchEditText() = with(binding) {
        searchEditText.setOnEditorActionListener { _: TextView?, _: Int, _: KeyEvent? ->
            onSubmitQuery()
            true
        }
        searchEditText.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!searchIsClosing) {
                    this@SimpleSearchView.onTextChanged(s)
                }
            }
        })
        searchEditText.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (hasFocus) {
                showKeyboard(searchEditText)
            }
        }
    }

    private fun initClickListeners() = with(binding) {
        backButton.setOnClickListener { closeSearch() }
        clearButton.setOnClickListener { clearSearch() }
        voiceButton.setOnClickListener { voiceSearch() }
    }

    override fun clearFocus() = with(binding) {
        isClearingFocus = true
        hideKeyboard(this@SimpleSearchView)
        super.clearFocus()
        searchEditText.clearFocus()
        isClearingFocus = false
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean = with(binding) {
        if (isClearingFocus) {
            return false
        }
        return if (!isFocusable) {
            false
        } else searchEditText.requestFocus(direction, previouslyFocusedRect)
    }

    public override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.query = if (query != null) query.toString() else null
        savedState.isSearchOpen = isSearchOpen
        savedState.animationDuration = animationDuration
        savedState.keepQuery = keepQuery
        return savedState
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        query = state.query
        animationDuration = state.animationDuration
        voiceSearchPrompt = state.voiceSearchPrompt
        keepQuery = state.keepQuery
        if (state.isSearchOpen) {
            showSearch(false)
            setQuery(state.query, false)
        }
        super.onRestoreInstanceState(state.superState)
    }

    private fun voiceSearch() {
        val activity = scanForActivity(context) ?: return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        if (!voiceSearchPrompt.isNullOrEmpty()) {
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, voiceSearchPrompt)
        }
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        activity.startActivityForResult(intent, REQUEST_VOICE_SEARCH)
    }

    private fun clearSearch() = with(binding) {
        searchEditText.text = null
        onQueryChangeListener?.onQueryTextCleared()
    }

    private fun onTextChanged(newText: CharSequence) = with(binding) {
        query = newText
        val hasText = !TextUtils.isEmpty(newText)
        if (hasText) {
            clearButton.visibility = VISIBLE
            showVoice(false)
        } else {
            clearButton.visibility = GONE
            showVoice(true)
        }
        if (!TextUtils.equals(newText, oldQuery)) {
            onQueryChangeListener?.onQueryTextChange(newText.toString())
        }
        oldQuery = newText.toString()
    }

    private fun onSubmitQuery() = with(binding) {
        val submittedQuery: CharSequence? = searchEditText.text
        if (submittedQuery != null && TextUtils.getTrimmedLength(submittedQuery) > 0) {
            if(onQueryChangeListener == null || !onQueryChangeListener!!.onQueryTextSubmit(submittedQuery.toString())) {
                closeSearch()
                searchIsClosing = true
                searchEditText.text = null
                searchIsClosing = false
            }
        }
    }

    private val isVoiceAvailable: Boolean
        get() {
            if (isInEditMode) {
                return true
            }
            val pm = context.packageManager
            val activities = pm.queryIntentActivities(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0)
            return activities.isNotEmpty()
        }

    /**
     * Saves query value in EditText after close/open events
     *
     * @param keepQuery keeps query if true
     */
    fun setKeepQuery(keepQuery: Boolean) {
        this.keepQuery = keepQuery
    }

    /**
     * Shows search with animation
     * @param animate true to animate
     */
    @JvmOverloads
    fun showSearch(animate: Boolean = true) = with(binding) {
        if (isSearchOpen) {
            return null
        }
        searchEditText.setText(if (keepQuery) query else null)
        searchEditText.requestFocus()
        if (animate) {
            val animationListener: SimpleAnimationUtils.AnimationListener = object : SimpleAnimationListener() {
                override fun onAnimationEnd(view: View): Boolean {
                    searchViewListener?.onSearchViewShownAnimation()
                    return false
                }
            }
            revealOrFadeIn(this@SimpleSearchView, animationDuration, animationListener, revealAnimationCenter).start()
        } else {
            visibility = VISIBLE
        }
        hideTabLayout(animate)
        isSearchOpen = true
        searchViewListener?.onSearchViewShown()
    }

    /**
     * Closes search with animation
     *
     * @param animate true if should be animated
     */
    @JvmOverloads
    fun closeSearch(animate: Boolean = true) = with(binding) {
        if (!isSearchOpen) {
            return null
        }
        searchIsClosing = true
        searchEditText.text = null
        searchIsClosing = false
        clearFocus()
        if (animate) {
            val animationListener: SimpleAnimationUtils.AnimationListener = object : SimpleAnimationListener() {
                override fun onAnimationEnd(view: View): Boolean {
                    searchViewListener?.onSearchViewClosedAnimation()
                    return false
                }
            }
            hideOrFadeOut(this@SimpleSearchView, animationDuration, animationListener, revealAnimationCenter).start()
        } else {
            visibility = INVISIBLE
        }
        showTabLayout(animate)
        isSearchOpen = false
        searchViewListener?.onSearchViewClosed()
    }

    /**
     * Sets a TabLayout that is automatically hidden when the search opens, and shown when the search closes
     */
    fun setTabLayout(tabLayout: TabLayout) {
        this.tabLayout = tabLayout
        this.tabLayout!!.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                tabLayoutInitialHeight = tabLayout.height
                tabLayout.viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })
        this.tabLayout!!.addOnTabSelectedListener(object : SimpleOnTabSelectedListener() {
            override fun onTabUnselected(tab: TabLayout.Tab) {
                closeSearch()
            }
        })
    }
    /**
     * Shows the attached TabLayout with animation
     *
     * @param animate true if should be animated
     */
    @JvmOverloads
    fun showTabLayout(animate: Boolean = true) {
        if (tabLayout == null) {
            return
        }

        if (animate) {
            verticalSlideView(tabLayout!!, 0, tabLayoutInitialHeight, animationDuration).start()
        } else {
            tabLayout?.visibility = VISIBLE
        }
    }
    /**
     * Hides the attached TabLayout with animation
     *
     * @param animate true if should be animated
     */
    @JvmOverloads
    fun hideTabLayout(animate: Boolean = true) {
        if (tabLayout == null) {
            return
        }

        if (animate) {
            verticalSlideView(tabLayout!!, tabLayout!!.height, 0, animationDuration).start()
        } else {
            tabLayout!!.visibility = GONE
        }
    }

    /**
     * Call this method on the onBackPressed method of the activity.
     * Returns true if the search was open and it closed with the call.
     * Returns false if the search was already closed and can continue with the default activity behavior.
     *
     * @return true if acted, false if not acted
     */
    fun onBackPressed(): Boolean {
        if (isSearchOpen) {
            closeSearch()
            return true
        }
        return false
    }

    /**
     * Call this method on the onActivityResult method of the activity.
     *
     *
     * Returns true if it was a voice search result and submits it.
     * Returns false if it was not a voice search result.
     *
     * @param submit true if it should submit automatically.
     * @return true if acted, false if not acted
     */
    @JvmOverloads
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent, submit: Boolean = true): Boolean {
        if (requestCode == REQUEST_VOICE_SEARCH && resultCode == Activity.RESULT_OK) {
            val matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val searchWrd = matches[0]
                if (!TextUtils.isEmpty(searchWrd)) {
                    setQuery(searchWrd, submit)
                }
            }
            return true
        }
        return false
    }

    /**
     * Will reset the search background as the default for the selected style
     *
     * @param style STYLE_CARD or STYLE_BAR
     */
    @get:Style
    var cardStyle: Int
        get() = style
        set(value) = with(binding) {
            style = value
            val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            var elevation = 0f
            when (value) {
                STYLE_CARD -> {
                    searchContainer.background = cardStyleBackground
                    bottomLine.visibility = GONE
                    val cardPadding = convertDpToPx(CARD_PADDING, context)
                    layoutParams.setMargins(cardPadding, cardPadding, cardPadding, cardPadding)
                    elevation = convertDpToPx(CARD_ELEVATION, context).toFloat()
                }
                STYLE_BAR -> {
                    searchContainer.setBackgroundColor(Color.WHITE)
                    bottomLine.visibility = VISIBLE
                }
                else -> {
                    searchContainer.setBackgroundColor(Color.WHITE)
                    bottomLine.visibility = VISIBLE
                }
            }
            searchContainer.layoutParams = layoutParams
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                searchContainer.elevation = elevation
            }
        }
    private val cardStyleBackground: GradientDrawable
        get() {
            val drawable = GradientDrawable()
            drawable.setColor(Color.WHITE)
            drawable.cornerRadius = convertDpToPx(CARD_CORNER_RADIUS, context).toFloat()
            return drawable
        }

    /**
     * Sets icons alpha, does not set the back/up icon
     */
    fun setIconsAlpha(alpha: Float) = with(binding) {
        clearButton.alpha = alpha
        voiceButton.alpha = alpha
    }

    /**
     * Sets icons colors, does not set back/up icon
     */
    fun setIconsColor(@ColorInt color: Int) = with(binding) {
        ImageViewCompat.setImageTintList(clearButton, ColorStateList.valueOf(color))
        ImageViewCompat.setImageTintList(voiceButton, ColorStateList.valueOf(color))
    }

    /**
     * Sets the back/up icon alpha; does not set other icons
     */
    fun setBackIconAlpha(alpha: Float) = with(binding) {
        backButton.alpha = alpha
    }

    /**
     * Sets the back/up icon drawable; does not set other icons
     */
    fun setBackIconColor(@ColorInt color: Int) = with(binding) {
        ImageViewCompat.setImageTintList(backButton, ColorStateList.valueOf(color))
    }

    /**
     * Sets the back/up icon drawable
     */
    fun setBackIconDrawable(drawable: Drawable?) = with(binding) {
        backButton.setImageDrawable(drawable)
    }

    /**
     * Sets a custom Drawable for the voice search button
     */
    fun setVoiceIconDrawable(drawable: Drawable?) = with(binding) {
        voiceButton.setImageDrawable(drawable)
    }

    /**
     * Sets a custom Drawable for the clear text button
     */
    fun setClearIconDrawable(drawable: Drawable?) = with(binding) {
        clearButton.setImageDrawable(drawable)
    }

    fun setSearchBackground(background: Drawable?) = with(binding) {
        searchContainer.background = background
    }

    fun setTextColor(@ColorInt color: Int) = with(binding) {
        searchEditText.setTextColor(color)
    }

    fun setHintTextColor(@ColorInt color: Int) = with(binding) {
        searchEditText.setHintTextColor(color)
    }

    fun setHint(hint: CharSequence?) = with(binding) {
        searchEditText.hint = hint
    }

    fun setInputType(inputType: Int) = with(binding) {
        searchEditText.inputType = inputType
    }

    /**
     * Uses reflection to set the search EditText cursor drawable
     */
    fun setCursorDrawable(@DrawableRes drawable: Int) = with(binding) {
        setCursorDrawable(searchEditText, drawable)
    }

    /**
     * Uses reflection to set the search EditText cursor color
     */
    fun setCursorColor(@ColorInt color: Int) = with(binding) {
        setCursorColor(searchEditText, color)
    }

    fun enableVoiceSearch(voiceSearch: Boolean) {
        allowVoiceSearch = voiceSearch
    }

    /**
     * @param sequence  query text
     * @param submit true to submit the query
     */
    fun setQuery(sequence: CharSequence?, submit: Boolean) = with(binding) {
        searchEditText.setText(sequence)
        if (sequence != null) {
            searchEditText.setSelection(searchEditText.length())
            query = sequence
        }
        if (submit && !TextUtils.isEmpty(sequence)) {
            onSubmitQuery()
        }
    }

    /**
     * If voice is not available on the device, this method call has not effect.
     *
     * @param show true to enable the voice search icon
     */
    fun showVoice(show: Boolean) = with(binding) {
        if (show && isVoiceAvailable && allowVoiceSearch) {
            voiceButton.visibility = VISIBLE
        } else {
            voiceButton.visibility = GONE
        }
    }

    /**
     * Handle click events for the MenuItem.
     *
     * @param menuItem MenuItem that opens the search
     */
    fun setMenuItem(menuItem: MenuItem) {
        menuItem.setOnMenuItemClickListener {
            showSearch()
            true
        }
    }

    /**
     * @param listener listens to query changes
     */
    fun setOnQueryTextListener(listener: OnQueryTextListener?) {
        onQueryChangeListener = listener
    }

    /**
     * Set this listener to listen to search open and close events
     *
     * @param listener listens to SimpleSearchView opening, closing, and the animations end
     */
    fun setOnSearchViewListener(listener: SearchViewListener?) {
        searchViewListener = listener
    }

    fun setVoiceSearchPrompt(voiceSearchPrompt: String?) {
        this.voiceSearchPrompt = voiceSearchPrompt
    }

    internal class SavedState : BaseSavedState {
        var query: String? = null
        var isSearchOpen = false
        var animationDuration = 0
        var voiceSearchPrompt: String? = null
        var keepQuery = false

        constructor(superState: Parcelable?) : super(superState)
        private constructor(`in`: Parcel) : super(`in`) {
            query = `in`.readString()
            isSearchOpen = `in`.readInt() == 1
            animationDuration = `in`.readInt()
            voiceSearchPrompt = `in`.readString()
            keepQuery = `in`.readInt() == 1
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeString(query)
            out.writeInt(if (isSearchOpen) 1 else 0)
            out.writeInt(animationDuration)
            out.writeString(voiceSearchPrompt)
            out.writeInt(if (keepQuery) 1 else 0)
        }

        companion object {
            //required field that makes Parcelables from a Parcel
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState?> = object : Parcelable.Creator<SavedState?> {
                override fun createFromParcel(`in`: Parcel): SavedState? {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    interface OnQueryTextListener {
        /**
         * @param query the query text
         * @return true to override the default action
         */
        fun onQueryTextSubmit(query: String): Boolean

        /**
         * @param newText the query text
         * @return true to override the default action
         */
        fun onQueryTextChange(newText: String): Boolean

        /**
         * Called when the query text is cleared by the user.
         *
         * @return true to override the default action
         */
        fun onQueryTextCleared(): Boolean
    }

    interface SearchViewListener {
        /**
         * Called instantly when the search opens
         */
        fun onSearchViewShown()

        /**
         * Called instantly when the search closes
         */
        fun onSearchViewClosed()

        /**
         * Called at the end of the show animation
         */
        fun onSearchViewShownAnimation()

        /**
         * Called at the end of the close animation
         */
        fun onSearchViewClosedAnimation()
    }

    companion object {
        const val REQUEST_VOICE_SEARCH = 735
        const val CARD_CORNER_RADIUS = 4
        const val ANIMATION_CENTER_PADDING = 26
        private const val CARD_PADDING = 6
        private const val CARD_ELEVATION = 2
        private const val BACK_ICON_ALPHA_DEFAULT = 0.87f
        private const val ICONS_ALPHA_DEFAULT = 0.54f
        const val STYLE_BAR = 0
        const val STYLE_CARD = 1
    }

    init {
        initStyle(attrs, defStyleAttr)
        initSearchEditText()
        initClickListeners()
        showVoice(true)
        if (!isInEditMode) {
            visibility = INVISIBLE
        }
    }
}