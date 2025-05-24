package com.example.fizmind.animation;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.content.res.ColorStateList;

/**
 * класс для управления анимациями кнопок
 */
public class AnimationManager {

    /**
     * применяет анимацию нажатия с круглым затемнением к кнопке
     *
     * @param button кнопка, к которой применяется анимация
     * @param context контекст приложения для доступа к ресурсам
     */
    public static void applyButtonPressAnimation(View button, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // для Android 5.0 и выше: используем ripple-эффект
            applyRippleEffect(button, context);
        } else {
            // для Android ниже 5.0: используем анимацию масштабирования
            applyScaleAnimation(button);
        }
    }

    /**
     * применяет ripple-эффект к кнопке
     *
     * @param button кнопка
     * @param context контекст
     */
    private static void applyRippleEffect(View button, Context context) {
        // получаем стандартный selectableItemBackground из темы
        int[] attrs = new int[]{android.R.attr.selectableItemBackground};
        android.content.res.TypedArray typedArray = context.obtainStyledAttributes(attrs);
        Drawable rippleDrawable = typedArray.getDrawable(0);
        typedArray.recycle();

        if (rippleDrawable != null) {
            // используем стандартный ripple-эффект из темы
            button.setBackground(rippleDrawable);
        } else {
            // если стандартный ripple не доступен, создаём кастомный
            RippleDrawable customRipple = new RippleDrawable(
                    ColorStateList.valueOf(0x40000000), // серый цвет с прозрачностью
                    button.getBackground(),             // сохраняем существующий фон
                    null                                // маска не требуется
            );
            button.setBackground(customRipple);
        }
    }

    /**
     * применяет анимацию масштабирования к кнопке для Android ниже 5.0
     *
     * @param button кнопка
     */
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