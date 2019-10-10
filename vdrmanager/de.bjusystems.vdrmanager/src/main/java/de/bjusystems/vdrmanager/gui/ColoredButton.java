package de.bjusystems.vdrmanager.gui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.util.AttributeSet;

import de.bjusystems.vdrmanager.R;

/**
 * Created by lado on 03.05.15.
 */
public class ColoredButton extends android.support.v7.widget.AppCompatButton {


private final float defaultRadius = 0.0f;

private int defaultPrimaryColor;

        public ColoredButton(Context context) {
            this(context, null);
        }

        public ColoredButton(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }


        //@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public ColoredButton(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            //defaultPrimaryColor = getResources().getColor(R.color.colorPrimary);

            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ColoredButton);
            int primaryColor = typedArray.getColor(R.styleable.ColoredButton_normalStateColor, defaultPrimaryColor);
            float radius = typedArray.getDimension(R.styleable.ColoredButton_cornerRadius, defaultRadius);

            int pressedStateColor = primaryColor & 0x00ffffff | 0x96000000;
            ShapeDrawable shapeSelected = new ShapeDrawable(new RectShape());
            shapeSelected.getPaint().setColor(pressedStateColor);
            shapeSelected.getPaint().setPathEffect(new CornerPathEffect(radius));
            shapeSelected.getPaint().setAntiAlias(true);
            shapeSelected.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
            shapeSelected.getPaint().setStrokeWidth(1);


            ShapeDrawable darkenSelected = new ShapeDrawable(new RectShape());
            darkenSelected.getPaint().setColor(Color.BLACK);
            darkenSelected.getPaint().setPathEffect(new CornerPathEffect(radius));
            darkenSelected.getPaint().setAntiAlias(true);
          if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                darkenSelected.getPaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.OVERLAY));
          }
            darkenSelected.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
            darkenSelected.getPaint().setStrokeWidth(1);


            LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{darkenSelected, shapeSelected});

            ShapeDrawable shapeNormal = new ShapeDrawable(new RectShape());
            shapeNormal.getPaint().setAntiAlias(true);
            shapeNormal.getPaint().setColor(primaryColor);
            shapeNormal.getPaint().setPathEffect(new CornerPathEffect(radius));
            shapeNormal.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
            shapeNormal.getPaint().setStrokeWidth(1);

            StateListDrawable states = new StateListDrawable();
            //Resources res = getResources();
            states.addState(new int[]{android.R.attr.state_pressed}, layerDrawable);
            states.addState(new int[]{android.R.attr.state_focused}, layerDrawable);
            states.addState(new int[]{}, shapeNormal);

            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                setBackground(states);
            } else {
                setBackgroundDrawable(states);
            }
            //typedArray.recycle();
        }
}
