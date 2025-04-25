package com.example.fizmind.animation;

import android.view.MotionEvent;
import android.view.View;

public class ButtonAnimation {


    public static void addAnimation(View button) {
        button.setOnTouchListener((v, event) -> {
            boolean isPressed = event.getAction() == MotionEvent.ACTION_DOWN;
            float scale = isPressed ? 0.95f : 1f;
            float alpha = isPressed ? 0.8f : 1f;
            v.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .alpha(alpha)
                    .setDuration(150)
                    .start();
            return false;
        });
    }
}
