package com.example.fizmind.keyboard;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.TextView;

import com.example.fizmind.PhysicalQuantity;
import com.example.fizmind.PhysicalQuantityRegistry;
import com.example.fizmind.animation.CustomTypefaceSpan;
import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.measurement.Measurement;

import java.util.ArrayList;
import java.util.List;

public class InputController {

    public enum InputState {
        ENTERING_DESIGNATION,
        ENTERING_VALUE,
        ENTERING_UNIT
    }

    private static final int MAX_DESIGNATION_LENGTH = 1;

    private InputState currentState;
    private final StringBuilder designationBuffer;
    private final StringBuilder valueBuffer;
    private final StringBuilder unitBuffer;
    private final TextView displayView;
    private final List<Measurement> measurements;
    private final List<SpannableStringBuilder> history;
    private Boolean designationUsesStix;
    private String logicalDesignation;
    private android.graphics.Typeface stixTypeface;
    private KeyboardModeSwitcher keyboardModeSwitcher;
    private boolean isCurrentConstant; // Флаг, указывающий, является ли текущее измерение константой

    public InputController(TextView displayView) {
        this.displayView = displayView;
        this.currentState = InputState.ENTERING_DESIGNATION;
        this.designationBuffer = new StringBuilder();
        this.valueBuffer = new StringBuilder();
        this.unitBuffer = new StringBuilder();
        this.measurements = new ArrayList<>();
        this.history = new ArrayList<>();
        this.designationUsesStix = null;
        this.logicalDesignation = null;
        this.isCurrentConstant = false;
        updateDisplay();
    }

    public void setStixTypeface(android.graphics.Typeface stixTypeface) {
        this.stixTypeface = stixTypeface;
    }

    public void setKeyboardModeSwitcher(KeyboardModeSwitcher switcher) {
        this.keyboardModeSwitcher = switcher;
    }

    public void onKeyInput(String input, String sourceKeyboardMode, boolean keyUsesStix, String logicalId) {
        if (currentState == InputState.ENTERING_DESIGNATION) {
            if (designationBuffer.length() == 0) {
                if (!"Designation".equals(sourceKeyboardMode)) {
                    Log.w("InputController", "Символ обозначения должен быть из режима 'Designation'");
                    return;
                }
                if (input.length() > MAX_DESIGNATION_LENGTH) {
                    input = input.substring(0, MAX_DESIGNATION_LENGTH);
                }
                designationUsesStix = keyUsesStix;
                designationBuffer.append(designationUsesStix ? convertToStix(input) : input);
                logicalDesignation = logicalId;
                PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
                if (pq != null && pq.isConstant()) {
                    // Это константа, подставляем значение и единицу
                    valueBuffer.append(String.valueOf(pq.getConstantValue()));
                    unitBuffer.append(pq.getSiUnit());
                    isCurrentConstant = true;
                    onDownArrowPressed(); // Сразу сохраняем измерение
                } else {
                    currentState = InputState.ENTERING_VALUE;
                    if (keyboardModeSwitcher != null) {
                        keyboardModeSwitcher.switchToNumbersAndOperations();
                    }
                }
            } else {
                Log.w("InputController", "Обозначение уже введено, нужен ввод числа.");
            }
        } else if (currentState == InputState.ENTERING_VALUE) {
            if (input.matches("[0-9]")) {
                valueBuffer.append(input);
            } else if (".".equals(input)) {
                if (valueBuffer.length() == 0 || valueBuffer.toString().equals("-")) {
                    Log.e("InputController", "Ошибка ввода: числовое значение не может начинаться с точки.");
                } else if (valueBuffer.indexOf(".") != -1) {
                    Log.e("InputController", "Ошибка ввода: числовое значение уже содержит точку.");
                } else {
                    valueBuffer.append(input);
                }
            } else if ("-".equals(input)) {
                if (valueBuffer.length() > 0) {
                    Log.e("InputController", "Ошибка: минус можно вводить только в начале.");
                } else {
                    valueBuffer.append(input);
                }
            } else {
                currentState = InputState.ENTERING_UNIT;
                onKeyInput(input, sourceKeyboardMode, keyUsesStix, logicalId);
                return;
            }
        } else if (currentState == InputState.ENTERING_UNIT) {
            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
            if (pq == null) {
                Log.e("InputController", "Нет данных о величине для: " + logicalDesignation);
                return;
            }
            int maxAllowedLength = 0;
            for (String allowed : pq.getAllowedUnits()) {
                if (allowed.length() > maxAllowedLength) {
                    maxAllowedLength = allowed.length();
                }
            }
            String potentialUnit = unitBuffer.toString() + input;
            boolean validPrefix = false;
            for (String allowed : pq.getAllowedUnits()) {
                if (allowed.startsWith(potentialUnit)) {
                    validPrefix = true;
                    break;
                }
            }
            if (validPrefix && potentialUnit.length() <= maxAllowedLength) {
                unitBuffer.append(input);
            } else {
                Log.w("InputController", "Введён недопустимый символ для единицы измерения: " + input);
            }
        }
        updateDisplay();
    }

    public void onKeyInput(String input) {
        onKeyInput(input, "Designation", false, "");
    }

    private void updateDisplay() {
        SpannableStringBuilder displayText = new SpannableStringBuilder();

        for (int i = 0; i < history.size(); i++) {
            displayText.append(history.get(i));
            if (i < history.size() - 1) {
                displayText.append("\n\n");
            }
        }

        if (history.size() > 0) {
            displayText.append("\n\n");
        }

        if (designationBuffer.length() == 0 && valueBuffer.length() == 0 && unitBuffer.length() == 0) {
            int start = displayText.length();
            displayText.append("Введите обозначение");
            displayText.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.GRAY),
                    start, displayText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            int start = displayText.length();
            displayText.append(designationBuffer.toString());
            if (designationUsesStix != null && designationUsesStix && designationBuffer.length() > 0 && stixTypeface != null) {
                displayText.setSpan(new CustomTypefaceSpan(stixTypeface), start, displayText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (designationBuffer.length() > 0) {
                displayText.append(" = ");
            }
            displayText.append(valueBuffer.toString());
            if (unitBuffer.length() > 0) {
                displayText.append(" ").append(unitBuffer.toString());
            }
        }

        displayView.setText(displayText);

        displayView.post(() -> {
            if (displayView.getLayout() != null) {
                int scrollAmount = displayView.getLayout().getHeight() - displayView.getHeight();
                if (scrollAmount > 0) {
                    displayView.scrollTo(0, scrollAmount);
                } else {
                    displayView.scrollTo(0, 0);
                }
            }
        });
    }

    public void onDownArrowPressed() {
        if (designationBuffer.length() > 0 && valueBuffer.length() > 0) {
            String unit = unitBuffer.toString();
            if (unit.isEmpty()) {
                PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
                if (pq == null) {
                    Log.e("InputController", "Не найдена информация для физической величины: " + logicalDesignation);
                    return;
                }
                unit = pq.getSiUnit();
                Log.d("InputController", "Единица измерения не указана, подставлена SI-единица: " + unit);
            }

            try {
                double value = Double.parseDouble(valueBuffer.toString());
                ConcreteMeasurement measurement = new ConcreteMeasurement(logicalDesignation, value, unit);
                if (!measurement.validate()) {
                    Log.e("InputController", "Ошибка: измерение не прошло проверку: " + measurement.toString());
                } else {
                    measurements.add(measurement);
                    Log.d("InputController", "Измерение сохранено: " + measurement.toString());
                    // Выводим все сохранённые измерения
                    Log.d("InputController", "Все сохранённые измерения:");
                    for (Measurement m : measurements) {
                        Log.d("InputController", m.toString());
                    }

                    SpannableStringBuilder historyEntry = new SpannableStringBuilder();
                    int start = historyEntry.length();
                    historyEntry.append(designationBuffer.toString());
                    if (designationUsesStix != null && designationUsesStix && stixTypeface != null) {
                        historyEntry.setSpan(new CustomTypefaceSpan(stixTypeface), start, historyEntry.length(), 0);
                    }
                    historyEntry.append(" = ").append(valueBuffer.toString());
                    if (!unit.isEmpty()) {
                        historyEntry.append(" ").append(unit);
                    }
                    history.add(historyEntry);

                    resetInput();
                    if (keyboardModeSwitcher != null) {
                        keyboardModeSwitcher.switchToDesignation();
                    }
                }
            } catch (NumberFormatException e) {
                Log.e("InputController", "Ошибка формата числа: " + valueBuffer.toString(), e);
            }
        } else {
            Log.d("InputController", "Недостаточно данных для сохранения измерения.");
        }
    }

    public void onLeftArrowPressed() {
        Log.d("InputController", "Действие не определено.");
    }

    public void onRightArrowPressed() {
        Log.d("InputController", "Нажата кнопка RIGHT – действие не определено.");
    }

    public void onDeletePressed() {
        if (designationBuffer.length() == 0 && valueBuffer.length() == 0 && unitBuffer.length() == 0) {
            // Буферы пусты, удаляем последнее измерение из истории
            if (!history.isEmpty()) {
                history.remove(history.size() - 1);
                measurements.remove(measurements.size() - 1);
                updateDisplay();
            }
        } else {
            // Удаляем текущее измерение
            if (currentState == InputState.ENTERING_UNIT) {
                if (unitBuffer.length() > 0) {
                    unitBuffer.deleteCharAt(unitBuffer.length() - 1);
                } else {
                    currentState = InputState.ENTERING_VALUE;
                }
            } else if (currentState == InputState.ENTERING_VALUE) {
                if (valueBuffer.length() > 0) {
                    valueBuffer.deleteCharAt(valueBuffer.length() - 1);
                } else {
                    currentState = InputState.ENTERING_DESIGNATION;
                    designationBuffer.deleteCharAt(designationBuffer.length() - 1);
                    designationUsesStix = null;
                    logicalDesignation = null;
                    isCurrentConstant = false;
                }
            } else if (currentState == InputState.ENTERING_DESIGNATION) {
                if (designationBuffer.length() > 0) {
                    designationBuffer.deleteCharAt(designationBuffer.length() - 1);
                    designationUsesStix = null;
                    logicalDesignation = null;
                    isCurrentConstant = false;
                }
            }
            updateDisplay();
        }

        if (currentState == InputState.ENTERING_DESIGNATION && keyboardModeSwitcher != null) {
            keyboardModeSwitcher.switchToDesignation();
        }
    }

    public void clearAll() {
        designationBuffer.setLength(0);
        valueBuffer.setLength(0);
        unitBuffer.setLength(0);
        currentState = InputState.ENTERING_DESIGNATION;
        designationUsesStix = null;
        logicalDesignation = null;
        isCurrentConstant = false;
        history.clear();
        measurements.clear();
        updateDisplay();
        if (keyboardModeSwitcher != null) {
            keyboardModeSwitcher.switchToDesignation();
        }
    }

    private void resetInput() {
        designationBuffer.setLength(0);
        valueBuffer.setLength(0);
        unitBuffer.setLength(0);
        currentState = InputState.ENTERING_DESIGNATION;
        designationUsesStix = null;
        logicalDesignation = null;
        isCurrentConstant = false;
        updateDisplay();
    }

    private String convertToStix(String input) {
        return input;
    }
}