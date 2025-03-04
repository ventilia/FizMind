package com.example.fizmind;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class InputController {

    public enum InputState {
        ENTERING_DESIGNATION,
        ENTERING_VALUE,
        ENTERING_UNIT
    }

    private static final int MAX_DESIGNATION_LENGTH = 1;
    private static final int MAX_UNIT_LENGTH = 3;

    private InputState currentState;
    private final StringBuilder designationBuffer;
    private final StringBuilder valueBuffer;
    private final StringBuilder unitBuffer;
    private final TextView displayView;
    private final List<Measurement> measurements;
    private boolean useStixFont;
    private android.graphics.Typeface stixTypeface;

    public InputController(TextView displayView) {
        this.displayView = displayView;
        this.currentState = InputState.ENTERING_DESIGNATION;
        this.designationBuffer = new StringBuilder();
        this.valueBuffer = new StringBuilder();
        this.unitBuffer = new StringBuilder();
        this.measurements = new ArrayList<>();
        this.useStixFont = false;
        updateDisplay();
    }

    public void setStixTypeface(android.graphics.Typeface stixTypeface) {
        this.stixTypeface = stixTypeface;
    }

    public void onKeyInput(String input, String sourceKeyboardMode, boolean useStixFont) {
        this.useStixFont = useStixFont;

        if (currentState == InputState.ENTERING_DESIGNATION) {
            if (designationBuffer.length() == 0) {
                if (!"Designation".equals(sourceKeyboardMode)) {
                    Log.w("InputController", " символ обозначения должен быть из режима 'Designation'");
                    return;
                }
                if (input.length() > MAX_DESIGNATION_LENGTH) {
                    input = input.substring(0, MAX_DESIGNATION_LENGTH);
                }
                designationBuffer.append(input);
            } else {
                if (input.matches("[0-9\\.-]")) {
                    currentState = InputState.ENTERING_VALUE;
                    valueBuffer.append(input);
                } else if ("Designation".equals(sourceKeyboardMode) && designationBuffer.length() < MAX_DESIGNATION_LENGTH) {
                    designationBuffer.append(input);
                } else {
                    Log.w("InputController", "Неверный символ для обозначения: " + input);
                }
            }
        } else if (currentState == InputState.ENTERING_VALUE) {
            if (input.matches("[0-9\\.-]")) {
                valueBuffer.append(input);
            } else {
                currentState = InputState.ENTERING_UNIT;
                if (unitBuffer.length() < MAX_UNIT_LENGTH) {
                    unitBuffer.append(input);
                }
            }
        } else if (currentState == InputState.ENTERING_UNIT) {
            if (unitBuffer.length() < MAX_UNIT_LENGTH) {
                unitBuffer.append(input);
            }
        }
        updateDisplay();
    }

    public void onKeyInput(String input) {
        onKeyInput(input, "Designation", false);
    }

    private void updateDisplay() {
        SpannableStringBuilder displayText = new SpannableStringBuilder();
        int start = displayText.length();
        displayText.append(designationBuffer.toString());

        if (useStixFont && designationBuffer.length() > 0 && stixTypeface != null) {
            displayText.setSpan(new CustomTypefaceSpan(stixTypeface), start, displayText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (designationBuffer.length() > 0) {
            displayText.append(" = ");
        }
        displayText.append(valueBuffer.toString());
        if (unitBuffer.length() > 0) {
            displayText.append(" ").append(unitBuffer.toString());
        }
        displayView.setText(displayText);
    }

    public void onDownArrowPressed() {
        if (designationBuffer.length() > 0 && valueBuffer.length() > 0) {
            try {
                double value = Double.parseDouble(valueBuffer.toString());
                Measurement measurement = new ConcreteMeasurement(designationBuffer.toString(), value, unitBuffer.toString());
                measurements.add(measurement);
                Log.d("InputController", "Измерение сохранено: " + measurement.toString());
            } catch (NumberFormatException e) {
                Log.e("InputController", "Ошибка формата числа: " + valueBuffer.toString(), e);
            }
            resetInput();
        } else {
            Log.d("InputController", "Недостаточно данных для сохранения измерения.");
        }
    }

    public void onLeftArrowPressed() {

        Log.d("InputController", "действие не определено.");
    }


    public void onRightArrowPressed() {
        Log.d("InputController", "Нажата кнопка RIGHT – действие не определено.");
    }

    public void onDeletePressed() {
        if (currentState == InputState.ENTERING_UNIT) {
            // дел. всю ед. измерения
            if (unitBuffer.length() > 0) {
                unitBuffer.setLength(0); // очистка буфера
            } else if (valueBuffer.length() > 0) {
                // если единица измерения пуста удаляем значение
                valueBuffer.setLength(0);
                currentState = InputState.ENTERING_VALUE;
            } else if (designationBuffer.length() > 0) {
                // если значение пусто удаляем обозначение
                designationBuffer.setLength(0);
                currentState = InputState.ENTERING_DESIGNATION;
            }
        } else if (currentState == InputState.ENTERING_VALUE) {
            // дел все значение
            if (valueBuffer.length() > 0) {
                valueBuffer.setLength(0);
            } else if (designationBuffer.length() > 0) {
                // если значение пустоудаляем обозначение
                designationBuffer.setLength(0);
                currentState = InputState.ENTERING_DESIGNATION;
            }
        } else if (currentState == InputState.ENTERING_DESIGNATION) {
            // удаляем всё обозначение
            if (designationBuffer.length() > 0) {
                designationBuffer.setLength(0);
            }
        }
        updateDisplay();
    }



    private void resetInput() {
        designationBuffer.setLength(0);
        valueBuffer.setLength(0);
        unitBuffer.setLength(0);
        currentState = InputState.ENTERING_DESIGNATION;
        updateDisplay();
    }
}
