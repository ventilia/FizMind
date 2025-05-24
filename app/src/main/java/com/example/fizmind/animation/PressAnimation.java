package com.example.fizmind.animation;



import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;

/**
 * класс-утилита для эффекта нажатия на кнопку:
 * - при ACTION_DOWN кнопка слегка сжимается и становится полупрозрачной
 * - при ACTION_UP/CANCEL возвращается в исходное состояние
 */
public class PressAnimation {

    // длительность анимации в миллисекундах
    private static final long DURATION = 100;

    /**
     * навешивает на любую View обработчик касаний,
     * создающий эффект «сжатия» и полупрозрачности
     */
    public static void apply(final View view) {
        view.setClickable(true);
        view.setFocusable(true);
        view.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    animatePress(v, 0.7f, 0.6f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    animateRelease(v);
                    break;
            }
            // возвращаем false, чтобы штатный onClick тоже отрабатывал
            return false;
        });
    }

    // анимация при нажатии: scale и alpha
    private static void animatePress(View v, float scale, float alpha) {
        ViewPropertyAnimator anim = v.animate();
        anim.scaleX(scale)
                .scaleY(scale)
                .alpha(alpha)
                .setDuration(DURATION)
                .start();
    }

    // возврат к исходным параметрам
    private static void animateRelease(View v) {
        ViewPropertyAnimator anim = v.animate();
        anim.scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(DURATION)
                .start();
    }
}