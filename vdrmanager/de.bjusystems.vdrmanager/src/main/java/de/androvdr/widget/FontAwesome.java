package de.androvdr.widget;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by lado on 04.05.15.
 */
public class FontAwesome {

    private static Typeface mFont;


    public static Typeface getFontAwesome(Context context){

        if(mFont != null){
            return  mFont;
        }

        mFont = Typeface.createFromAsset(context.getAssets(), "fonts/fontawesome-webfont.ttf");
        return mFont;
    }


}
