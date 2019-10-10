package de.androvdr.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by lado on 17.11.17.
 */

public class FontAwesomeTextView extends TextView {
    public FontAwesomeTextView(Context context) {
        super(context);
        initFontAwesome();
    }

    public FontAwesomeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initFontAwesome();
    }

    public FontAwesomeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFontAwesome();
    }

    private void initFontAwesome() {
        if (isInEditMode() == false) {
            setTypeface(FontAwesome.getFontAwesome(getContext().getApplicationContext().getApplicationContext()));
        }
    }


}
