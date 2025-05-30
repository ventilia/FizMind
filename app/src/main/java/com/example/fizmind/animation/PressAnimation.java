package com.example.fizmind.animation;



import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;


public class PressAnimation {

    // длительность анимации в миллисекундах
    private static final long DURATION = 100;


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

            return false;
        });
    }


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