package com.ferfalk.simplesearchview.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Point;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fernando A. H. Falkiewicz
 */
public class SimpleAnimationUtils {
    public static final int ANIMATION_DURATION_DEFAULT = 250;

    private SimpleAnimationUtils() {
    }


    public static Animator revealOrFadeIn(@NonNull final View view) {
        return revealOrFadeIn(view, ANIMATION_DURATION_DEFAULT);
    }

    public static Animator revealOrFadeIn(@NonNull final View view, int duration) {
        return revealOrFadeIn(view, duration, null, null);
    }

    public static Animator revealOrFadeIn(@NonNull final View view, int duration, @Nullable final AnimationListener listener) {
        return revealOrFadeIn(view, duration, listener, null);
    }

    public static Animator revealOrFadeIn(@NonNull final View view, int duration, @Nullable Point center) {
        return revealOrFadeIn(view, duration, null, center);
    }

    public static Animator revealOrFadeIn(@NonNull final View view, @Nullable final AnimationListener listener) {
        return revealOrFadeIn(view, ANIMATION_DURATION_DEFAULT, listener, null);
    }

    public static Animator revealOrFadeIn(@NonNull final View view, @Nullable Point center) {
        return revealOrFadeIn(view, ANIMATION_DURATION_DEFAULT, null, center);
    }

    public static Animator revealOrFadeIn(@NonNull final View view, @Nullable final AnimationListener listener, @Nullable Point center) {
        return revealOrFadeIn(view, ANIMATION_DURATION_DEFAULT, listener, center);
    }

    public static Animator revealOrFadeIn(@NonNull final View view, int duration, @Nullable final AnimationListener listener, @Nullable Point center) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return reveal(view, duration, listener, center);
        } else {
            return fadeIn(view, duration, listener);
        }
    }


    public static Animator hideOrFadeOut(@NonNull final View view, int duration) {
        return hideOrFadeOut(view, duration, null, null);
    }

    public static Animator hideOrFadeOut(@NonNull final View view, int duration, @Nullable final AnimationListener listener) {
        return hideOrFadeOut(view, duration, listener, null);
    }

    public static Animator hideOrFadeOut(@NonNull final View view, int duration, @Nullable Point center) {
        return hideOrFadeOut(view, duration, null, center);
    }

    public static Animator hideOrFadeOut(@NonNull final View view) {
        return hideOrFadeOut(view, ANIMATION_DURATION_DEFAULT);
    }

    public static Animator hideOrFadeOut(@NonNull final View view, @Nullable final AnimationListener listener) {
        return hideOrFadeOut(view, ANIMATION_DURATION_DEFAULT, listener, null);
    }

    public static Animator hideOrFadeOut(@NonNull final View view, @Nullable Point center) {
        return hideOrFadeOut(view, ANIMATION_DURATION_DEFAULT, null, center);
    }

    public static Animator hideOrFadeOut(@NonNull final View view, @Nullable final AnimationListener listener, @Nullable Point center) {
        return hideOrFadeOut(view, ANIMATION_DURATION_DEFAULT, listener, center);
    }

    public static Animator hideOrFadeOut(@NonNull final View view, int duration, @Nullable final AnimationListener listener, @Nullable Point center) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return hide(view, duration, listener, center);
        } else {
            return fadeOut(view, duration, listener);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Animator reveal(@NonNull final View view, int duration) {
        return reveal(view, duration, null, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Animator reveal(@NonNull final View view, int duration, @Nullable Point center) {
        return reveal(view, duration, null, center);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Animator reveal(@NonNull final View view, int duration, @Nullable final AnimationListener listener) {
        return reveal(view, duration, listener, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Animator reveal(@NonNull final View view) {
        return reveal(view, ANIMATION_DURATION_DEFAULT);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Animator reveal(@NonNull final View view, @Nullable final AnimationListener listener) {
        return reveal(view, ANIMATION_DURATION_DEFAULT, listener, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Animator reveal(@NonNull final View view, @Nullable Point center) {
        return reveal(view, ANIMATION_DURATION_DEFAULT, null, center);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Animator reveal(@NonNull final View view, @Nullable final AnimationListener listener, @Nullable Point center) {
        return reveal(view, ANIMATION_DURATION_DEFAULT, listener, center);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Animator reveal(@NonNull final View view, int duration, @Nullable final AnimationListener listener, @Nullable Point center) {
        if (center == null) {
            center = getDefaultCenter(view);
        }

        Animator anim = ViewAnimationUtils.createCircularReveal(view, center.x, center.y, 0, getRevealRadius(center, view));
        anim.addListener(new DefaultActionAnimationListener(view, listener) {
            @Override
            void defaultOnAnimationStart(@NonNull View view) {
                view.setVisibility(View.VISIBLE);
            }
        });

        anim.setDuration(duration);
        anim.setInterpolator(getDefaultInterpolator());
        return anim;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Animator hide(@NonNull final View view, int duration) {
        return hide(view, duration, null, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Animator hide(@NonNull final View view, int duration, @Nullable Point center) {
        return hide(view, duration, null, center);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Animator hide(@NonNull final View view, int duration, @Nullable final AnimationListener listener) {
        return hide(view, duration, listener, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Animator hide(@NonNull final View view) {
        return hide(view, ANIMATION_DURATION_DEFAULT);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Animator hide(@NonNull final View view, @Nullable final AnimationListener listener) {
        return hide(view, ANIMATION_DURATION_DEFAULT, listener, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Animator hide(@NonNull final View view, @Nullable Point center) {
        return hide(view, ANIMATION_DURATION_DEFAULT, null, center);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Animator hide(@NonNull final View view, @Nullable final AnimationListener listener, @Nullable Point center) {
        return hide(view, ANIMATION_DURATION_DEFAULT, listener, center);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Animator hide(@NonNull final View view, int duration, @Nullable final AnimationListener listener, @Nullable Point center) {
        if (center == null) {
            center = getDefaultCenter(view);
        }

        Animator anim = ViewAnimationUtils.createCircularReveal(view, center.x, center.y, getRevealRadius(center, view), 0);
        anim.addListener(new DefaultActionAnimationListener(view, listener) {
            @Override
            void defaultOnAnimationEnd(@NonNull View view) {
                view.setVisibility(View.GONE);
            }
        });

        anim.setDuration(duration);
        anim.setInterpolator(getDefaultInterpolator());
        return anim;
    }


    protected static Point getDefaultCenter(@NonNull View view) {
        return new Point(view.getWidth() / 2, view.getHeight() / 2);
    }

    protected static int getRevealRadius(@NonNull Point center, @NonNull View view) {
        float radius = 0;
        List<Point> points = new ArrayList<>();
        points.add(new Point(view.getLeft(), view.getTop()));
        points.add(new Point(view.getRight(), view.getTop()));
        points.add(new Point(view.getLeft(), view.getBottom()));
        points.add(new Point(view.getRight(), view.getBottom()));

        for (Point point : points) {
            float distance = distance(center, point);
            if (distance > radius) {
                radius = distance;
            }
        }

        return (int) Math.ceil(radius);
    }


    public static float distance(Point first, Point second) {
        return (float) Math.sqrt(Math.pow(first.x - second.x, 2) + Math.pow(first.y - second.y, 2));
    }


    public static Animator fadeIn(@NonNull View view) {
        return fadeIn(view, ANIMATION_DURATION_DEFAULT);
    }

    public static Animator fadeIn(@NonNull View view, int duration) {
        return fadeIn(view, duration, null);
    }

    public static Animator fadeIn(@NonNull View view, @Nullable final AnimationListener listener) {
        return fadeIn(view, ANIMATION_DURATION_DEFAULT, listener);
    }

    public static Animator fadeIn(@NonNull View view, int duration, @Nullable final AnimationListener listener) {
        if (view.getAlpha() == 1f) {
            view.setAlpha(0);
        }

        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 1f);
        anim.addListener(new DefaultActionAnimationListener(view, listener) {
            @Override
            void defaultOnAnimationStart(@NonNull View view) {
                view.setVisibility(View.VISIBLE);
            }
        });

        anim.setDuration(duration);
        anim.setInterpolator(getDefaultInterpolator());
        return anim;
    }


    public static Animator fadeOut(@NonNull View view) {
        return fadeOut(view, ANIMATION_DURATION_DEFAULT);
    }

    public static Animator fadeOut(@NonNull View view, int duration) {
        return fadeOut(view, duration, null);
    }

    public static Animator fadeOut(@NonNull View view, @Nullable final AnimationListener listener) {
        return fadeOut(view, ANIMATION_DURATION_DEFAULT, listener);
    }

    public static Animator fadeOut(@NonNull View view, int duration, @Nullable final AnimationListener listener) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 0f);
        anim.addListener(new DefaultActionAnimationListener(view, listener) {
            @Override
            void defaultOnAnimationEnd(@NonNull View view) {
                view.setVisibility(View.GONE);
            }
        });

        anim.setDuration(duration);
        anim.setInterpolator(getDefaultInterpolator());
        return anim;
    }


    public static Animator verticalSlideView(@NonNull View view, int fromHeight, int toHeight) {
        return verticalSlideView(view, fromHeight, toHeight, null);
    }

    public static Animator verticalSlideView(@NonNull View view, int fromHeight, int toHeight, int duration) {
        return verticalSlideView(view, fromHeight, toHeight, duration, null);
    }

    public static Animator verticalSlideView(@NonNull View view, int fromHeight, int toHeight, @Nullable final AnimationListener listener) {
        return verticalSlideView(view, fromHeight, toHeight, ANIMATION_DURATION_DEFAULT, listener);
    }

    public static Animator verticalSlideView(@NonNull View view, int fromHeight, int toHeight, int duration, @Nullable final AnimationListener listener) {
        ValueAnimator anim = ValueAnimator
                .ofInt(fromHeight, toHeight);

        anim.addUpdateListener(animation -> {
            view.getLayoutParams().height = (int) (Integer) animation.getAnimatedValue();
            view.requestLayout();
        });

        anim.addListener(new DefaultActionAnimationListener(view, listener));

        anim.setDuration(duration);
        anim.setInterpolator(getDefaultInterpolator());
        return anim;
    }


    protected static Interpolator getDefaultInterpolator() {
        return new FastOutSlowInInterpolator();
    }


    public interface AnimationListener {
        /**
         * @return return true to override the default behaviour
         */
        boolean onAnimationStart(@NonNull View view);

        /**
         * @return return true to override the default behaviour
         */
        boolean onAnimationEnd(@NonNull View view);

        /**
         * @return return true to override the default behaviour
         */
        boolean onAnimationCancel(@NonNull View view);
    }

    private static class DefaultActionAnimationListener extends AnimatorListenerAdapter {
        private View view;
        private AnimationListener listener;

        DefaultActionAnimationListener(@NonNull View view, @Nullable AnimationListener listener) {
            this.view = view;
            this.listener = listener;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            if (listener == null || !listener.onAnimationStart(view)) {
                defaultOnAnimationStart(view);
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (listener == null || !listener.onAnimationEnd(view)) {
                defaultOnAnimationEnd(view);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            if (listener == null || !listener.onAnimationCancel(view)) {
                defaultOnAnimationCancel(view);
            }
        }

        void defaultOnAnimationStart(@NonNull View view) {
            // No default action
        }

        void defaultOnAnimationEnd(@NonNull View view) {
            // No default action
        }

        void defaultOnAnimationCancel(@NonNull View view) {
            // No default action
        }
    }
}
