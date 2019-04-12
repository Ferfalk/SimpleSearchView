# SimpleSearchView
[![API](https://img.shields.io/badge/API-16%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=16)
[![Release](https://img.shields.io/github/release/Ferfalk/SimpleSearchView/all.svg?style=flat)](https://jitpack.io/#Ferfalk/SimpleSearchView)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-SimpleSearchView-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/7287)

A simple SearchView for Android based on Material Design

* API 16+ *(Reveal animation for API 21 and above, fade animation otherwise)*
* Two styles
* Option to hide TabLayout automatically when it opens
* Text and animations listeners
* Customization options

<img alt="Card sample" width="360" height="600" src="https://user-images.githubusercontent.com/13675455/46987845-4091c180-d0cc-11e8-8904-87f3c24d4c0a.gif" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img alt="Bar sample" width="360" height="600" src="https://user-images.githubusercontent.com/13675455/46987873-60c18080-d0cc-11e8-8d10-2b8cf8a4124b.gif" />


## Download

Add the JitPack repository to the build.gradle file:
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Add the Gradle dependency:
```groovy
implementation 'com.github.Ferfalk:SimpleSearchView:0.1.4'
```


## Usage
Add SimpleSearchView to your AppBarLayout:

```xml
<android.support.design.widget.AppBarLayout
    android:id="@+id/appbar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@color/colorPrimary"
    android:theme="@style/AppTheme.AppBarOverlay">

    <FrameLayout
        android:id="@+id/toolbar_container"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <android.support.v7.widget.Toolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:popupTheme="@style/AppTheme.PopupOverlay"
            app:subtitle="@string/app_subtitle"
            app:title="Example" />

        <com.ferfalk.simplesearchview.SimpleSearchView
            android:id="@+id/searchView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:background="@color/colorPrimary" />
    </FrameLayout>

</android.support.design.widget.AppBarLayout>
```

[Setup with an MenuItem](#menuitem) or [Open manually](#open-and-close-manually)

Setup the listener:  
*Return true to override default behaviour*
```java
simpleSearchView.setOnQueryTextListener(new SimpleSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("SimpleSearchView", "Submit:" + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("SimpleSearchView", "Text changed:" + newText);
                return false;
            }

            @Override
            public boolean onQueryTextCleared() {
                Log.d("SimpleSearchView", "Text cleared");
                return false;
            }
        });
```

## Options

### MenuItem
*Open when the MenuItem is clicked*  
Add the search item to the menu xml:
```xml
<item
    android:id="@+id/action_search"
    android:icon="@drawable/ic_search_black_24dp"
    android:title="@string/search_hint"
    app:iconTint="@android:color/white"
    app:showAsAction="ifRoom" />
```
Setup the MenuItem :
```java
@Override
public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);

    MenuItem item = menu.findItem(R.id.action_search);
    searchView.setMenuItem(item);

    return true;
}
```

### TabLayout
*Hides the TabLayout when the SimpleSearchView opens*  
Add it to the layout with a TabLayout:
```xml
<android.support.design.widget.AppBarLayout
    android:id="@+id/appbar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@color/colorPrimary"
    android:theme="@style/AppTheme.AppBarOverlay">

    <FrameLayout
        android:id="@+id/toolbar_container"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <android.support.v7.widget.Toolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:popupTheme="@style/AppTheme.PopupOverlay"
            app:subtitle="@string/app_subtitle"
            app:title="Example" />

        <com.ferfalk.simplesearchview.SimpleSearchView
            android:id="@+id/searchView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:background="@color/colorPrimary" />
    </FrameLayout>
  
    <android.support.design.widget.TabLayout
        android:id="@+id/tabLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:tabGravity="fill"
        app:tabMode="fixed">

        <android.support.design.widget.TabItem
            android:id="@+id/tabItem1"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/tab_text_1" />

        <android.support.design.widget.TabItem
            android:id="@+id/tabItem2"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/tab_text_2" />

        <android.support.design.widget.TabItem
            android:id="@+id/tabItem3"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/tab_text_3" />

    </android.support.design.widget.TabLayout>

</android.support.design.widget.AppBarLayout>
```
Setup the TabLayout:
```java
simpleSearchView.setTabLayout(findViewById(R.id.tabLayout));
```


### Open and close manually
```java
simpleSearchView.showSearch();
simpleSearchView.closeSearch();
```

### OnBackPressed
*Closes the SimpleSearchView automatically*
```java
@Override
public void onBackPressed() {
    if (searchView.onBackPressed()) {
        return;
    }

    super.onBackPressed();
}
```

### Voice search
```
app:voiceSearch="true"
```
or
```java
simpleSearchView.enableVoiceSearch(true);
```

Handle the result:  
*Will set the query automatically*
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (searchView.onActivityResult(requestCode, resultCode, data)) {
        return;
    }

    super.onActivityResult(requestCode, resultCode, data);
}
```

### Style
Bar style *(default)*:
```
app:type="bar"
```

Card style:
```
app:type="card"
```

### Open and close listener
```java
simpleSearchView.setOnSearchViewListener(new SimpleSearchView.SearchViewListener() {
    @Override
    public void onSearchViewShown() {
        Log.d("SimpleSearchView", "onSearchViewShown");
    }

    @Override
    public void onSearchViewClosed() {
        Log.d("SimpleSearchView", "onSearchViewClosed");
    }

    @Override
    public void onSearchViewShownAnimation() {
        Log.d("SimpleSearchView", "onSearchViewShownAnimation");
    }

    @Override
    public void onSearchViewClosedAnimation() {
        Log.d("SimpleSearchView", "onSearchViewClosedAnimation");
    }
});
```

### Changing the reveal animation starting point
```java
// Adding padding to the animation because of the hidden menu item
Point revealCenter = simpleSearchView.getRevealAnimationCenter();
revealCenter.x -= DimensUtils.convertDpToPx(EXTRA_REVEAL_CENTER_PADDING, this);
```

### Attributes
```xml
<style name="SimpleSearchViewStyle">
    <!-- Change search style -->
    <item name="type">card</item>

    <!-- Change search hint -->
    <item name="android:hint">Sample</item>

    <!-- Change search inputType -->
    <item name="android:inputType">text</item>

    <!-- Change search textColor -->
    <item name="android:textColor">@color/sample</item>

    <!-- Search bar/card background -->
    <item name="searchBackground">@drawable/sample</item>

    <!-- Change icons -->
    <item name="searchBackIcon">@drawable/sample</item>
    <item name="searchClearIcon">@drawable/sample</item>
    <item name="searchVoiceIcon">@drawable/sample</item>

    <!-- Change icons tint -->
    <item name="backIconTint">1</item>
    <item name="iconsTint">1</item>

    <!-- Change icons alpha -->
    <item name="backIconAlpha">0.8</item>
    <item name="iconsAlpha">0.8</item>

    <!-- Change search input colors -->
    <item name="cursorColor">@color/sample</item>
    <item name="hintColor">@color/sample</item>

    <!-- Enable voice search -->
    <item name="voiceSearch">true</item>

    <!-- Set voice search prompt -->
    <item name="voiceSearchPrompt">Sample</item>
</style>
```

## License
    Copyright (C) 2018 Fernando Augusto Heeren Falkiewicz

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
