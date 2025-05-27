package com.example.fizmind.animation;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.content.res.ColorStateList;

public class AnimationManager {


    public static void applyButtonPressAnimation(View button, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // для Android 5.0 и выше: используем ripple-эффект
            applyRippleEffect(button, context);
        } else {
            // для Android ниже 5.0: используем анимацию масштабирования
            applyScaleAnimation(button);
        }
    }


    private static void applyRippleEffect(View button, Context context) {
        int[] attrs = new int[]{android.R.attr.selectableItemBackground};
        android.content.res.TypedArray typedArray = context.obtainStyledAttributes(attrs);
        Drawable rippleDrawable = typedArray.getDrawable(0);
        typedArray.recycle();

        if (rippleDrawable != null) {
            button.setBackground(rippleDrawable);
        } else {
            RippleDrawable customRipple = new RippleDrawable(
                    ColorStateList.valueOf(0x40000000), // серый цвет с прозрачностью
                    button.getBackground(),             // сохраняем существующий фон
                    null                                // маска не требуется
            );
            button.setBackground(customRipple);
        }
    }

    private static void applyScaleAnimation(View button) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // уменьшаем кнопку на 5% при нажатии
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // возвращаем кнопку к исходному размеру
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    break;
            }
            return false; // не перехватываем события клика
        });
    }
}