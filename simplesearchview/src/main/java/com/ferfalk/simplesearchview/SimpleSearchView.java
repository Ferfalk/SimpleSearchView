package com.ferfalk.simplesearchview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.core.widget.ImageViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.ferfalk.simplesearchview.utils.ContextUtils;
import com.ferfalk.simplesearchview.utils.DimensUtils;
import com.ferfalk.simplesearchview.utils.EditTextReflectionUtils;
import com.ferfalk.simplesearchview.utils.SimpleAnimationListener;
import com.ferfalk.simplesearchview.utils.SimpleAnimationUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * @author Fernando A. H. Falkiewicz
 */
public class SimpleSearchView extends FrameLayout {
    public static final int REQUEST_VOICE_SEARCH = 735;
    public static final int CARD_CORNER_RADIUS = 4;
    public static final int ANIMATION_CENTER_PADDING = 26;
    private static final int CARD_PADDING = 6;
    private static final int CARD_ELEVATION = 2;
    private static final float BACK_ICON_ALPHA_DEFAULT = 0.87f;
    private static final float ICONS_ALPHA_DEFAULT = 0.54f;

    public static final int STYLE_BAR = 0;
    public static final int STYLE_CARD = 1;

    @IntDef({STYLE_BAR, STYLE_CARD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Style {
    }

    private Context context;
    private int animationDuration = SimpleAnimationUtils.ANIMATION_DURATION_DEFAULT;
    private Point revealAnimationCenter;
    private CharSequence query;
    private CharSequence oldQuery;
    private boolean allowVoiceSearch = false;
    private boolean isSearchOpen = false;
    private boolean isClearingFocus = false;
    private String voiceSearchPrompt = "";
    @Style
    private int style = STYLE_BAR;

    private ViewGroup searchContainer;
    private EditText searchEditText;
    private ImageButton backButton;
    private ImageButton clearButton;
    private ImageButton voiceButton;
    private View bottomLine;

    private TabLayout tabLayout;
    private int tabLayoutInitialHeight;

    private OnQueryTextListener onQueryChangeListener;
    private SearchViewListener searchViewListener;

    private boolean searchIsClosing = false;
    private boolean keepQuery = false;

    public SimpleSearchView(Context context) {
        this(context, null);
    }

    public SimpleSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        inflate();
        initStyle(attrs, defStyleAttr);
        initSearchEditText();
        initClickListeners();
        showVoice(true);

        if (!isInEditMode()) {
            setVisibility(View.INVISIBLE);
        }
    }

    private void inflate() {
        LayoutInflater.from(context).inflate(R.layout.search_view, this, true);

        searchContainer = findViewById(R.id.searchContainer);
        searchEditText = findViewById(R.id.searchEditText);
        backButton = findViewById(R.id.buttonBack);
        clearButton = findViewById(R.id.buttonClear);
        voiceButton = findViewById(R.id.buttonVoice);
        bottomLine = findViewById(R.id.bottomLine);
    }

    private void initStyle(AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SimpleSearchView, defStyleAttr, 0);
        if (typedArray == null) {
            setCardStyle(style);
            return;
        }

        if (typedArray.hasValue(R.styleable.SimpleSearchView_type)) {
            setCardStyle(typedArray.getInt(R.styleable.SimpleSearchView_type, style));
        }

        if (typedArray.hasValue(R.styleable.SimpleSearchView_backIconAlpha)) {
            setBackIconAlpha(typedArray.getFloat(R.styleable.SimpleSearchView_backIconAlpha, BACK_ICON_ALPHA_DEFAULT));
        }

        if (typedArray.hasValue(R.styleable.SimpleSearchView_iconsAlpha)) {
            setIconsAlpha(typedArray.getFloat(R.styleable.SimpleSearchView_iconsAlpha, ICONS_ALPHA_DEFAULT));
        }

        if (typedArray.hasValue(R.styleable.SimpleSearchView_backIconTint)) {
            setBackIconColor(typedArray.getColor(R.styleable.SimpleSearchView_backIconTint, ContextUtils.getPrimaryColor(context)));
        }

        if (typedArray.hasValue(R.styleable.SimpleSearchView_iconsTint)) {
            setIconsColor(typedArray.getColor(R.styleable.SimpleSearchView_iconsTint, Color.BLACK));
        }

        if (typedArray.hasValue(R.styleable.SimpleSearchView_cursorColor)) {
            setCursorColor(typedArray.getColor(R.styleable.SimpleSearchView_cursorColor, ContextUtils.getAccentColor(context)));
        }

        if (typedArray.hasValue(R.styleable.SimpleSearchView_hintColor)) {
            setHintTextColor(typedArray.getColor(R.styleable.SimpleSearchView_hintColor, getResources().getColor(R.color.default_textColorHint)));
        }

        if (typedArray.hasValue(R.styleable.SimpleSearchView_searchBackground)) {
            setSearchBackground(typedArray.getDrawable(R.styleable.SimpleSearchView_searchBackground));
        }

        if (typedArray.hasValue(R.styleable.SimpleSearchView_searchBackIcon)) {
            setBackIconDrawable(typedArray.getDrawable(R.styleable.SimpleSearchView_searchBackIcon));
        }

        if (typedArray.hasValue(R.styleable.SimpleSearchView_searchClearIcon)) {
            setClearIconDrawable(typedArray.getDrawable(R.styleable.SimpleSearchView_searchClearIcon));
        }

        if (typedArray.hasValue(R.styleable.SimpleSearchView_searchVoiceIcon)) {
            setVoiceIconDrawable(typedArray.getDrawable(R.styleable.SimpleSearchView_searchVoiceIcon));
        }

        if (typedArray.hasValue(R.styleable.SimpleSearchView_voiceSearch)) {
            enableVoiceSearch(typedArray.getBoolean(R.styleable.SimpleSearchView_voiceSearch, allowVoiceSearch));
        }

        if (typedArray.hasValue(R.styleable.SimpleSearchView_voiceSearchPrompt)) {
            setVoiceSearchPrompt(typedArray.getString(R.styleable.SimpleSearchView_voiceSearchPrompt));
        }

        if (typedArray.hasValue(R.styleable.SimpleSearchView_android_hint)) {
            setHint(typedArray.getString(R.styleable.SimpleSearchView_android_hint));
        }

        if (typedArray.hasValue(R.styleable.SimpleSearchView_android_inputType)) {
            setInputType(typedArray.getInt(R.styleable.SimpleSearchView_android_inputType, EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS));
        }

        if (typedArray.hasValue(R.styleable.SimpleSearchView_android_textColor)) {
            setTextColor(typedArray.getColor(R.styleable.SimpleSearchView_android_textColor, getResources().getColor(R.color.default_textColor)));
        }

        typedArray.recycle();
    }

    private void initSearchEditText() {
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            onSubmitQuery();
            return true;
        });

        searchEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!searchIsClosing) {
                    SimpleSearchView.this.onTextChanged(s);
                }
            }
        });

        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ContextUtils.showKeyboard(searchEditText);
            }
        });
    }

    private void initClickListeners() {
        backButton.setOnClickListener(v -> closeSearch());
        clearButton.setOnClickListener(v -> clearSearch());
        voiceButton.setOnClickListener(v -> voiceSearch());
    }

    @Override
    public void clearFocus() {
        isClearingFocus = true;
        ContextUtils.hideKeyboard(this);
        super.clearFocus();
        searchEditText.clearFocus();
        isClearingFocus = false;
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if (isClearingFocus) {
            return false;
        }
        if (!isFocusable()) {
            return false;
        }
        return searchEditText.requestFocus(direction, previouslyFocusedRect);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState savedState = new SavedState(superState);
        savedState.query = query != null ? query.toString() : null;
        savedState.isSearchOpen = isSearchOpen;
        savedState.animationDuration = animationDuration;
        savedState.keepQuery = keepQuery;

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;

        query = savedState.query;
        animationDuration = savedState.animationDuration;
        voiceSearchPrompt = savedState.voiceSearchPrompt;
        keepQuery = savedState.keepQuery;

        if (savedState.isSearchOpen) {
            showSearch(false);
            setQuery(savedState.query, false);
        }

        super.onRestoreInstanceState(savedState.getSuperState());
    }

    private void voiceSearch() {
        Activity activity = ContextUtils.scanForActivity(context);
        if (activity == null) {
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        if (voiceSearchPrompt != null && voiceSearchPrompt.isEmpty()) {
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, voiceSearchPrompt);
        }
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        activity.startActivityForResult(intent, REQUEST_VOICE_SEARCH);
    }

    private void clearSearch() {
        searchEditText.setText(null);
        if (onQueryChangeListener != null) {
            onQueryChangeListener.onQueryTextCleared();
        }
    }

    private void onTextChanged(CharSequence newText) {
        query = newText;
        boolean hasText = !TextUtils.isEmpty(newText);
        if (hasText) {
            clearButton.setVisibility(VISIBLE);
            showVoice(false);
        } else {
            clearButton.setVisibility(GONE);
            showVoice(true);
        }

        if (onQueryChangeListener != null && !TextUtils.equals(newText, oldQuery)) {
            onQueryChangeListener.onQueryTextChange(newText.toString());
        }
        oldQuery = newText.toString();
    }

    private void onSubmitQuery() {
        CharSequence submittedQuery = searchEditText.getText();
        if (submittedQuery != null && TextUtils.getTrimmedLength(submittedQuery) > 0) {
            if (onQueryChangeListener == null || !onQueryChangeListener.onQueryTextSubmit(submittedQuery.toString())) {
                closeSearch();
                searchIsClosing = true;
                searchEditText.setText(null);
                searchIsClosing = false;
            }
        }
    }

    private boolean isVoiceAvailable() {
        if (isInEditMode()) {
            return true;
        }
        PackageManager pm = getContext().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return !activities.isEmpty();
    }

    /**
     * Saves query value in EditText after close/open events
     *
     * @param keepQuery keeps query if true
     */
    public void setKeepQuery(boolean keepQuery) {
        this.keepQuery = keepQuery;
    }

    /**
     * Shows search with animation
     */
    public void showSearch() {
        showSearch(true);
    }

    /**
     * Shows search
     *
     * @param animate true to animate
     */
    public void showSearch(boolean animate) {
        if (isSearchOpen()) {
            return;
        }

        searchEditText.setText(keepQuery ? query : null);
        searchEditText.requestFocus();

        if (animate) {
            SimpleAnimationUtils.AnimationListener animationListener = new SimpleAnimationListener() {
                @Override
                public boolean onAnimationEnd(@NonNull View view) {
                    if (searchViewListener != null) {
                        searchViewListener.onSearchViewShownAnimation();
                    }
                    return false;
                }
            };
            SimpleAnimationUtils.revealOrFadeIn(this, animationDuration, animationListener, getRevealAnimationCenter()).start();
        } else {
            setVisibility(View.VISIBLE);
        }

        hideTabLayout(animate);

        isSearchOpen = true;
        if (searchViewListener != null) {
            searchViewListener.onSearchViewShown();
        }
    }

    /**
     * Closes search with animation
     */
    public void closeSearch() {
        closeSearch(true);
    }

    /**
     * Closes search
     *
     * @param animate true if should be animated
     */
    public void closeSearch(boolean animate) {
        if (!isSearchOpen()) {
            return;
        }

        searchIsClosing = true;
        searchEditText.setText(null);
        searchIsClosing = false;
        clearFocus();

        if (animate) {
            SimpleAnimationUtils.AnimationListener animationListener = new SimpleAnimationListener() {
                @Override
                public boolean onAnimationEnd(@NonNull View view) {
                    if (searchViewListener != null) {
                        searchViewListener.onSearchViewClosedAnimation();
                    }
                    return false;
                }
            };
            SimpleAnimationUtils.hideOrFadeOut(this, animationDuration, animationListener, getRevealAnimationCenter()).start();
        } else {
            setVisibility(View.INVISIBLE);
        }

        showTabLayout(animate);

        isSearchOpen = false;
        if (searchViewListener != null) {
            searchViewListener.onSearchViewClosed();
        }
    }

    /**
     * @return the TabLayout attached to the SimpleSearchView behavior
     */
    public TabLayout getTabLayout() {
        return tabLayout;
    }

    /**
     * Sets a TabLayout that is automatically hidden when the search opens, and shown when the search closes
     */
    public void setTabLayout(TabLayout tabLayout) {
        this.tabLayout = tabLayout;

        this.tabLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                tabLayoutInitialHeight = tabLayout.getHeight();
                tabLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });

        this.tabLayout.addOnTabSelectedListener(new SimpleOnTabSelectedListener() {
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                closeSearch();
            }
        });
    }

    /**
     * Shows the attached TabLayout with animation
     */
    public void showTabLayout() {
        showTabLayout(true);
    }

    /**
     * Shows the attached TabLayout
     *
     * @param animate true if should be animated
     */
    public void showTabLayout(boolean animate) {
        if (tabLayout == null) {
            return;
        }

        if (animate) {
            SimpleAnimationUtils.verticalSlideView(tabLayout, 0, tabLayoutInitialHeight, animationDuration).start();
        } else {
            tabLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hides the attached TabLayout with animation
     */
    public void hideTabLayout() {
        hideTabLayout(true);
    }

    /**
     * Hides the attached TabLayout
     *
     * @param animate true if should be animated
     */
    public void hideTabLayout(boolean animate) {
        if (tabLayout == null) {
            return;
        }

        if (animate) {
            SimpleAnimationUtils.verticalSlideView(tabLayout, tabLayout.getHeight(), 0, animationDuration).start();
        } else {
            tabLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Call this method on the onBackPressed method of the activity.
     * Returns true if the search was open and it closed with the call.
     * Returns false if the search was already closed and can continue with the default activity behavior.
     *
     * @return true if acted, false if not acted
     */
    public boolean onBackPressed() {
        if (isSearchOpen()) {
            closeSearch();
            return true;
        }
        return false;
    }

    /**
     * Call this method on the onActivityResult method of the activity.
     * <p>
     * Returns true if it was a voice search result and submits it.
     * Returns false if it was not a voice search result.
     *
     * @return true if acted, false if not acted
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return onActivityResult(requestCode, resultCode, data, true);
    }

    /**
     * Call this method on the onActivityResult method of the activity.
     * <p>
     * Returns true if it was a voice search result and sets it to the search query.
     * Returns false if it was not a voice search result.
     *
     * @param submit true if it should submit automatically.
     * @return true if acted, false if not acted
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data, boolean submit) {
        if (requestCode == REQUEST_VOICE_SEARCH && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    setQuery(searchWrd, submit);
                }
            }
            return true;
        }
        return false;
    }

    @Style
    public int getCardStyle() {
        return style;
    }

    /**
     * Will reset the search background as the default for the selected style
     *
     * @param style STYLE_CARD or STYLE_BAR
     */
    public void setCardStyle(@Style int style) {
        this.style = style;

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        float elevation = 0;

        switch (style) {
            case STYLE_CARD:
                searchContainer.setBackground(getCardStyleBackground());
                bottomLine.setVisibility(View.GONE);

                int cardPadding = DimensUtils.convertDpToPx(CARD_PADDING, context);
                layoutParams.setMargins(cardPadding, cardPadding, cardPadding, cardPadding);

                elevation = DimensUtils.convertDpToPx(CARD_ELEVATION, context);
                break;
            case STYLE_BAR:
            default:
                searchContainer.setBackgroundColor(Color.WHITE);
                bottomLine.setVisibility(View.VISIBLE);
                break;
        }

        searchContainer.setLayoutParams(layoutParams);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            searchContainer.setElevation(elevation);
        }
    }

    private GradientDrawable getCardStyleBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE);
        drawable.setCornerRadius(DimensUtils.convertDpToPx(CARD_CORNER_RADIUS, context));
        return drawable;
    }

    /**
     * Sets icons alpha, does not set the back/up icon
     */
    public void setIconsAlpha(float alpha) {
        clearButton.setAlpha(alpha);
        voiceButton.setAlpha(alpha);
    }

    /**
     * Sets icons colors, does not set back/up icon
     */
    public void setIconsColor(@ColorInt int color) {
        ImageViewCompat.setImageTintList(clearButton, ColorStateList.valueOf(color));
        ImageViewCompat.setImageTintList(voiceButton, ColorStateList.valueOf(color));
    }

    /**
     * Sets the back/up icon alpha; does not set other icons
     */
    public void setBackIconAlpha(float alpha) {
        backButton.setAlpha(alpha);
    }

    /**
     * Sets the back/up icon drawable; does not set other icons
     */
    public void setBackIconColor(@ColorInt int color) {
        ImageViewCompat.setImageTintList(backButton, ColorStateList.valueOf(color));
    }

    /**
     * Sets the back/up icon drawable
     */
    public void setBackIconDrawable(Drawable drawable) {
        backButton.setImageDrawable(drawable);
    }

    /**
     * Sets a custom Drawable for the voice search button
     */
    public void setVoiceIconDrawable(Drawable drawable) {
        voiceButton.setImageDrawable(drawable);
    }

    /**
     * Sets a custom Drawable for the clear text button
     */
    public void setClearIconDrawable(Drawable drawable) {
        clearButton.setImageDrawable(drawable);
    }

    public void setSearchBackground(Drawable background) {
        searchContainer.setBackground(background);
    }

    public void setTextColor(@ColorInt int color) {
        searchEditText.setTextColor(color);
    }

    public void setHintTextColor(@ColorInt int color) {
        searchEditText.setHintTextColor(color);
    }

    public void setHint(CharSequence hint) {
        searchEditText.setHint(hint);
    }

    public void setInputType(int inputType) {
        searchEditText.setInputType(inputType);
    }

    /**
     * Uses reflection to set the search EditText cursor drawable
     */
    public void setCursorDrawable(@DrawableRes int drawable) {
        EditTextReflectionUtils.setCursorDrawable(searchEditText, drawable);
    }

    /**
     * Uses reflection to set the search EditText cursor color
     */
    public void setCursorColor(@ColorInt int color) {
        EditTextReflectionUtils.setCursorColor(searchEditText, color);
    }

    public void enableVoiceSearch(boolean voiceSearch) {
        allowVoiceSearch = voiceSearch;
    }

    /**
     * @return EditText view that contains the search query, can be used with hooks like RxBinding
     */
    public EditText getSearchEditText() {
        return searchEditText;
    }

    /**
     * @param query  query text
     * @param submit true to submit the query
     */
    public void setQuery(CharSequence query, boolean submit) {
        searchEditText.setText(query);
        if (query != null) {
            searchEditText.setSelection(searchEditText.length());
            this.query = query;
        }
        if (submit && !TextUtils.isEmpty(query)) {
            onSubmitQuery();
        }
    }

    /**
     * If voice is not available on the device, this method call has not effect.
     *
     * @param show true to enable the voice search icon
     */
    public void showVoice(boolean show) {
        if (show && isVoiceAvailable() && allowVoiceSearch) {
            voiceButton.setVisibility(VISIBLE);
        } else {
            voiceButton.setVisibility(GONE);
        }
    }

    /**
     * Handle click events for the MenuItem.
     *
     * @param menuItem MenuItem that opens the search
     */
    public void setMenuItem(@NonNull MenuItem menuItem) {
        menuItem.setOnMenuItemClickListener(item -> {
            showSearch();
            return true;
        });
    }

    public boolean isSearchOpen() {
        return isSearchOpen;
    }

    /**
     * @return current reveal or fade animations duration
     */
    public int getAnimationDuration() {
        return animationDuration;
    }

    /**
     * @param duration duration, in ms, of the reveal or fade animations
     */
    public void setAnimationDuration(int duration) {
        animationDuration = duration;
    }

    /**
     * @return center of the reveal animation, by default it is placed where the rightmost MenuItem would be
     */
    public Point getRevealAnimationCenter() {
        if (revealAnimationCenter != null) {
            return revealAnimationCenter;
        }

        int centerX = getWidth() - DimensUtils.convertDpToPx(ANIMATION_CENTER_PADDING, context);
        int centerY = getHeight() / 2;

        revealAnimationCenter = new Point(centerX, centerY);
        return revealAnimationCenter;
    }

    /**
     * @param revealAnimationCenter center of the reveal animation, used to customize the origin of the animation
     */
    public void setRevealAnimationCenter(Point revealAnimationCenter) {
        this.revealAnimationCenter = revealAnimationCenter;
    }

    /**
     * @param listener listens to query changes
     */
    public void setOnQueryTextListener(OnQueryTextListener listener) {
        onQueryChangeListener = listener;
    }

    /**
     * Set this listener to listen to search open and close events
     *
     * @param listener listens to SimpleSearchView opening, closing, and the animations end
     */
    public void setOnSearchViewListener(SearchViewListener listener) {
        searchViewListener = listener;
    }

    public void setVoiceSearchPrompt(String voiceSearchPrompt) {
        this.voiceSearchPrompt = voiceSearchPrompt;
    }


    static class SavedState extends BaseSavedState {
        //required field that makes Parcelables from a Parcel
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
        String query;
        boolean isSearchOpen;
        int animationDuration;
        String voiceSearchPrompt;
        boolean keepQuery;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.query = in.readString();
            this.isSearchOpen = in.readInt() == 1;
            this.animationDuration = in.readInt();
            this.voiceSearchPrompt = in.readString();
            this.keepQuery = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(query);
            out.writeInt(isSearchOpen ? 1 : 0);
            out.writeInt(animationDuration);
            out.writeString(voiceSearchPrompt);
            out.writeInt(keepQuery ? 1 : 0);
        }
    }


    public interface OnQueryTextListener {

        /**
         * @param query the query text
         * @return true to override the default action
         */
        boolean onQueryTextSubmit(String query);

        /**
         * @param newText the query text
         * @return true to override the default action
         */
        boolean onQueryTextChange(String newText);

        /**
         * Called when the query text is cleared by the user.
         *
         * @return true to override the default action
         */
        boolean onQueryTextCleared();
    }


    public interface SearchViewListener {

        /**
         * Called instantly when the search opens
         */
        void onSearchViewShown();

        /**
         * Called instantly when the search closes
         */
        void onSearchViewClosed();

        /**
         * Called at the end of the show animation
         */
        void onSearchViewShownAnimation();

        /**
         * Called at the end of the close animation
         */
        void onSearchViewClosedAnimation();
    }
}
