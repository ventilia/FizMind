package com.example.fizmind.animation;

import android.graphics.Color;
import android.view.MotionEvent;
import android.widget.TextView;

import com.example.fizmind.R;

public class KeyboardAnimation {

    public static void applyButtonAnimation(TextView button) {

        button.setOnTouchListener((view, event) -> {

            if (view.isSelected()) {
                return false;
            }

            Object tag = view.getTag();
            if (tag != null && tag.equals("MODE_BUTTON")) {
                return false;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    view.setBackgroundResource(R.drawable.ic_back_black);
                    button.setTextColor(Color.WHITE);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:

                    view.setBackgroundResource(R.drawable.ic_back);
                    button.setTextColor(Color.BLACK);
                    break;
                case MotionEvent.ACTION_HOVER_ENTER:

                    view.setBackgroundResource(R.drawable.ic_back_black);
                    button.setTextColor(Color.WHITE);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:

                    view.setBackgroundResource(R.drawable.ic_back);
                    button.setTextColor(Color.BLACK);
                    break;
            }
            return false;
        });
    }
}
