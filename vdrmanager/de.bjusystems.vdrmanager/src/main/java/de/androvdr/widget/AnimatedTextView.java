package de.androvdr.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

/**
 * Created by lado on 04.05.15.
 */
public class AnimatedTextView  extends TextView {
    private static final int IS_ANIMATING_TAG_ID = "isAnimating".hashCode();

    private Animation fadeInAnimation;
    private Animation fadeOutAnimation;

    public AnimatedTextView(Context context) {
        super(context);

        initAnimations(context);
    }

    public AnimatedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initAnimations(context);
    }

    public AnimatedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initAnimations(context);
    }

    public void initAnimations(Context context) {
        fadeInAnimation = AnimationUtils.loadAnimation(this.getContext(), android.R.anim.fade_in);

        fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        fadeOutAnimation.setDuration(100);
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                setAnimatingFlag(true);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setAnimatingFlag(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                setAnimatingFlag(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });

        setAnimatingFlag(false);
    }

    public void fadeOut() {
        if (getVisibility() == View.VISIBLE) {
            startAnimation(fadeOutAnimation);
            setVisibility(View.INVISIBLE);
        }
    }

    public void fadeIn() {
        //if (getVisibility() == View.INVISIBLE && !isAnimating()) {
//            startAnimation(fadeInAnimation);
            setVisibility(View.VISIBLE);
    //    }
    }

    private boolean isAnimating() {
        return (Boolean) getTag(IS_ANIMATING_TAG_ID) == true;
    }

    private void setAnimatingFlag(boolean isAnimating) {
        setTag(IS_ANIMATING_TAG_ID, new Boolean(isAnimating));
    }
}
