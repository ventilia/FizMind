package com.example.fizmind.animation;

import android.animation.ValueAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Класс для анимации текста внутри TextView при нажатии и отпускании.
 */
public class TextPressAnimation {

    private static final long DURATION = 100;          // длительность анимации в миллисекундах
    private static final float ALPHA_PRESSED = 0.5f;  // прозрачность текста при нажатии
    private static final float ALPHA_NORMAL = 1.0f;   // нормальная прозрачность текста

    /**
     * Применяет анимацию к тексту внутри указанного TextView.
     *
     * @param textView TextView, к тексту которого применяется анимация
     */
    public static void apply(final TextView textView) {
        // делаем TextView кликабельным и фокусируемым
        textView.setClickable(true);
        textView.setFocusable(true);

        // устанавливаем слушатель касаний для запуска анимации
        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        animatePress(textView); // анимация нажатия
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        animateRelease(textView); // анимация отпускания
                        break;
                }
                // возвращаем false, чтобы сохранить обработку onClick
                return false;
            }
        });
    }

    /**
     * Запускает анимацию нажатия, уменьшая прозрачность текста.
     *
     * @param textView TextView, текст которого анимируется
     */
    private static void animatePress(TextView textView) {
        ValueAnimator animator = ValueAnimator.ofFloat(ALPHA_NORMAL, ALPHA_PRESSED);
        animator.setDuration(DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                textView.setAlpha(alpha); // изменяем прозрачность текста
            }
        });
        animator.start();
    }

    /**
     * Запускает анимацию отпускания, возвращая текст к нормальной прозрачности.
     *
     * @param textView TextView, текст которого анимируется
     */
    private static void animateRelease(TextView textView) {
        ValueAnimator animator = ValueAnimator.ofFloat(ALPHA_PRESSED, ALPHA_NORMAL);
        animator.setDuration(DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                textView.setAlpha(alpha); // изменяем прозрачность текста
            }
        });
        animator.start();
    }
}