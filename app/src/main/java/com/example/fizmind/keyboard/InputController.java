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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер ввода для управления состоянием клавиатуры и отображением физических величин.
 */
public class InputController {

    /** Перечисление возможных состояний ввода */
    public enum InputState {
        ENTERING_DESIGNATION, // Ввод обозначения
        ENTERING_VALUE,       // Ввод числового значения
        ENTERING_UNIT         // Ввод единицы измерения
    }

    private static final int MAX_DESIGNATION_LENGTH = 1; // Максимальная длина обозначения

    private InputState currentState;                    // Текущее состояние ввода
    private final StringBuilder designationBuffer;      // Буфер для обозначения
    private final StringBuilder valueBuffer;            // Буфер для числового значения
    private final StringBuilder unitBuffer;             // Буфер для единицы измерения
    private final TextView displayView;                 // Поле для отображения текста
    private final List<Measurement> measurements;       // Список сохраненных измерений
    private final List<SpannableStringBuilder> history; // История введенных данных
    private Boolean designationUsesStix;                // Используется ли шрифт STIX для обозначения
    private String logicalDesignation;                  // Логический идентификатор обозначения
    private android.graphics.Typeface stixTypeface;     // Шрифт STIX
    private KeyboardModeSwitcher keyboardModeSwitcher;  // Переключатель режимов клавиатуры
    private boolean isCurrentConstant;                  // Является ли текущая величина константой
    private final StringBuilder operationBuffer = new StringBuilder();         // Буфер операций над обозначением
    private final StringBuilder valueOperationBuffer = new StringBuilder();    // Буфер операций над числом
    private final Map<String, String> lastUnitForDesignation = new HashMap<>(); // Карта для хранения последних единиц измерения

    /** Конструктор класса */
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

    /** Установка шрифта STIX */
    public void setStixTypeface(android.graphics.Typeface stixTypeface) {
        this.stixTypeface = stixTypeface;
    }

    /** Установка переключателя режимов клавиатуры */
    public void setKeyboardModeSwitcher(KeyboardModeSwitcher switcher) {
        this.keyboardModeSwitcher = switcher;
    }

    /**
     * Обработка ввода с клавиатуры
     * @param input Введенный символ
     * @param sourceKeyboardMode Режим клавиатуры
     * @param keyUsesStix Используется ли STIX
     * @param logicalId Логический идентификатор
     */
    public void onKeyInput(String input, String sourceKeyboardMode, boolean keyUsesStix, String logicalId) {
        if (currentState == InputState.ENTERING_DESIGNATION) {
            if (designationBuffer.length() == 0) {
                if (!"Designation".equals(sourceKeyboardMode)) {
                    Log.w("InputController", "Символ обозначения должен быть из режима 'Designation'");
                    return;
                }
                designationBuffer.append(input);
                logicalDesignation = logicalId;
                designationUsesStix = keyUsesStix;
                if (lastUnitForDesignation.containsKey(logicalDesignation) && valueBuffer.length() > 0) {
                    unitBuffer.setLength(0);
                    unitBuffer.append(lastUnitForDesignation.get(logicalDesignation));
                } else {
                    unitBuffer.setLength(0);
                }
                PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
                if (pq != null && pq.isConstant()) {
                    valueBuffer.append(String.valueOf(pq.getConstantValue()));
                    unitBuffer.append(pq.getSiUnit());
                    isCurrentConstant = true;
                    onDownArrowPressed();
                } else {
                    currentState = InputState.ENTERING_VALUE;
                    if (keyboardModeSwitcher != null) {
                        keyboardModeSwitcher.switchToNumbersAndOperations();
                    }
                }
            } else if (logicalId.equals("op_vec") || logicalId.equals("op_subscript") || logicalId.equals("op_superscript")) {
                operationBuffer.append(input);
                updateDisplay();
            } else {
                Log.w("InputController", "Обозначение уже введено, ожидается число или операция.");
            }
        } else if (currentState == InputState.ENTERING_VALUE) {
            if (input.matches("[0-9]") || ".".equals(input) || "-".equals(input)) {
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
                }
            } else if (logicalId.equals("op_abs_open")) {
                valueOperationBuffer.append("|");
                updateDisplay();
            } else if (logicalId.equals("op_abs_close") && valueOperationBuffer.toString().contains("|")) {
                valueOperationBuffer.append(valueBuffer).append("|");
                valueBuffer.setLength(0);
                updateDisplay();
            } else if (logicalId.equals("op_vec") || logicalId.equals("op_subscript") || logicalId.equals("op_superscript")) {
                Log.w("InputController", "Операция " + input + " применима только к обозначению.");
            } else {
                currentState = InputState.ENTERING_UNIT;
                onKeyInput(input, sourceKeyboardMode, keyUsesStix, logicalId);
                return;
            }
        } else if (currentState == InputState.ENTERING_UNIT) {
            // Если вводится число и единица измерения пуста, переключаемся в режим ввода числа
            if (input.matches("[0-9]") && unitBuffer.length() == 0) {
                currentState = InputState.ENTERING_VALUE;
                if (keyboardModeSwitcher != null) {
                    keyboardModeSwitcher.switchToNumbersAndOperations();
                }
                valueBuffer.append(input);
                updateDisplay();
                return;
            }
            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
            if (pq == null) return;
            int maxAllowedLength = pq.getAllowedUnits().stream().mapToInt(String::length).max().orElse(0);
            String potentialUnit = unitBuffer.toString() + input;
            boolean validPrefix = pq.getAllowedUnits().stream().anyMatch(allowed -> allowed.startsWith(potentialUnit));
            if (validPrefix && potentialUnit.length() <= maxAllowedLength) {
                unitBuffer.append(input);
            }
            updateDisplay();
        }
        updateDisplay();
    }

    /**
     * Обработка нажатия клавиши Delete
     */
    public void onDeletePressed() {
        if (currentState == InputState.ENTERING_VALUE) {
            // Удаляем символ из числа
            if (valueBuffer.length() > 0) {
                char removedChar = valueBuffer.charAt(valueBuffer.length() - 1);
                valueBuffer.deleteCharAt(valueBuffer.length() - 1);
                Log.d("InputController", "Удалён символ из числа: " + removedChar);
            } else if (valueOperationBuffer.length() > 0) {
                char removedChar = valueOperationBuffer.charAt(valueOperationBuffer.length() - 1);
                valueOperationBuffer.deleteCharAt(valueOperationBuffer.length() - 1);
                Log.d("InputController", "Удалён символ из операции над числом: " + removedChar);
            }
            // Если число полностью удалено, переключаемся в режим ввода обозначения
            if (valueBuffer.length() == 0 && valueOperationBuffer.length() == 0) {
                currentState = InputState.ENTERING_DESIGNATION;
                updateKeyboardMode();
                Log.d("InputController", "Число полностью удалено, переключено в режим ввода обозначения");
            }
        } else if (currentState == InputState.ENTERING_DESIGNATION) {
            // Удаляем обозначение, если оно есть
            if (designationBuffer.length() > 0) {
                String removedDesignation = designationBuffer.toString();
                designationBuffer.setLength(0);
                logicalDesignation = null;
                designationUsesStix = null;
                isCurrentConstant = false;
                valueBuffer.setLength(0);
                unitBuffer.setLength(0);
                valueOperationBuffer.setLength(0);
                operationBuffer.setLength(0);
                Log.d("InputController", "Удалено обозначение: " + removedDesignation + ", сброшены все буферы");
            }
        } else if (currentState == InputState.ENTERING_UNIT) {
            // Удаляем единицу измерения
            if (unitBuffer.length() > 0) {
                String removedUnit = unitBuffer.toString();
                unitBuffer.setLength(0);
                Log.d("InputController", "Удалена единица измерения: " + removedUnit);
            } else {
                currentState = InputState.ENTERING_VALUE;
                updateKeyboardMode();
                Log.d("InputController", "Буфер единицы пуст, переключено в режим ввода числа");
            }
        }
        updateDisplay();
    }

    /** Переключение влево по режимам */
    public void onLeftArrowPressed() {
        if (currentState == InputState.ENTERING_UNIT) {
            currentState = InputState.ENTERING_VALUE;
            Log.d("InputController", "Переключено в режим ввода числа");
        } else if (currentState == InputState.ENTERING_VALUE) {
            currentState = InputState.ENTERING_DESIGNATION;
            Log.d("InputController", "Переключено в режим ввода обозначения");
        }
        updateKeyboardMode();
        updateDisplay();
    }

    /** Переключение вправо по режимам */
    public void onRightArrowPressed() {
        if (currentState == InputState.ENTERING_DESIGNATION && designationBuffer.length() > 0) {
            currentState = InputState.ENTERING_VALUE;
            Log.d("InputController", "Переключено в режим ввода числа");
        } else if (currentState == InputState.ENTERING_VALUE && (valueBuffer.length() > 0 || valueOperationBuffer.length() > 0)) {
            currentState = InputState.ENTERING_UNIT;
            Log.d("InputController", "Переключено в режим ввода единицы измерения");
        }
        updateKeyboardMode();
        updateDisplay();
    }

    /** Обновление режима клавиатуры */
    private void updateKeyboardMode() {
        if (keyboardModeSwitcher != null) {
            if (currentState == InputState.ENTERING_DESIGNATION) {
                keyboardModeSwitcher.switchToDesignation();
            } else if (currentState == InputState.ENTERING_VALUE) {
                keyboardModeSwitcher.switchToNumbersAndOperations();
            } else if (currentState == InputState.ENTERING_UNIT) {
                keyboardModeSwitcher.switchToUnits();
            }
        }
    }

    /** Подтверждение ввода (нажатие вниз) */
    public void onDownArrowPressed() {
        if (designationBuffer.length() == 0) {
            Log.w("InputController", "Невозможно сохранить: отсутствует обозначение");
            return;
        }
        if (valueBuffer.length() == 0 && valueOperationBuffer.length() == 0) {
            Log.w("InputController", "Невозможно сохранить: отсутствует числовое значение");
            return;
        }
        String unit = unitBuffer.toString();
        if (unit.isEmpty()) {
            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
            if (pq == null) {
                Log.e("InputController", "Неизвестная физическая величина: " + logicalDesignation);
                return;
            }
            unit = pq.getSiUnit();
            if (unit.isEmpty()) {
                Log.w("InputController", "Невозможно сохранить: отсутствует единица измерения");
                return;
            }
        }

        String valueStr = valueOperationBuffer.length() > 0 ? valueOperationBuffer.toString() : valueBuffer.toString();
        double value;
        try {
            value = Double.parseDouble(valueBuffer.length() > 0 ? valueBuffer.toString() : "0");
        } catch (NumberFormatException e) {
            Log.e("InputController", "Ошибка формата числа: " + valueStr, e);
            return;
        }

        ConcreteMeasurement measurement = new ConcreteMeasurement(
                logicalDesignation, value, unit, operationBuffer.toString(), valueOperationBuffer.toString());
        if (!measurement.validate()) {
            Log.e("InputController", "Ошибка валидации: " + measurement.toString());
            return;
        }

        measurements.add(measurement);
        SpannableStringBuilder historyEntry = new SpannableStringBuilder();
        int start = historyEntry.length();
        if (operationBuffer.length() > 0) {
            historyEntry.append(operationBuffer).append("(").append(designationBuffer).append(")");
        } else {
            historyEntry.append(designationBuffer);
        }
        if (designationUsesStix != null && designationUsesStix && stixTypeface != null) {
            historyEntry.setSpan(new CustomTypefaceSpan(stixTypeface), start, historyEntry.length(), 0);
        }
        historyEntry.append(" = ").append(valueStr);
        if (!unit.isEmpty()) {
            historyEntry.append(" ").append(unit);
        }
        history.add(historyEntry);

        if (!unit.isEmpty()) {
            lastUnitForDesignation.put(logicalDesignation, unit);
        }

        resetInput();
        if (keyboardModeSwitcher != null) {
            keyboardModeSwitcher.switchToDesignation();
        }
    }

    /** Обновление отображаемого текста */
    private void updateDisplay() {
        SpannableStringBuilder displayText = new SpannableStringBuilder();
        for (int i = 0; i < history.size(); i++) {
            displayText.append(history.get(i));
            if (i < history.size() - 1) displayText.append("\n\n");
        }
        if (history.size() > 0) displayText.append("\n\n");

        if (designationBuffer.length() == 0 && valueBuffer.length() == 0 && unitBuffer.length() == 0 &&
                operationBuffer.length() == 0 && valueOperationBuffer.length() == 0) {
            int start = displayText.length();
            displayText.append("Введите обозначение");
            displayText.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.GRAY),
                    start, displayText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            if (designationBuffer.length() > 0) {
                int start = displayText.length();
                if (operationBuffer.length() > 0) {
                    displayText.append(operationBuffer).append("(").append(designationBuffer).append(")");
                } else {
                    displayText.append(designationBuffer);
                }
                if (designationUsesStix != null && designationUsesStix && stixTypeface != null) {
                    displayText.setSpan(new CustomTypefaceSpan(stixTypeface), start, displayText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                displayText.append(" = ");
            } else if (valueBuffer.length() > 0 || valueOperationBuffer.length() > 0) {
                displayText.append("= ");
            }
            if (valueOperationBuffer.length() > 0) {
                displayText.append(valueOperationBuffer);
            } else {
                displayText.append(valueBuffer);
            }
            if (unitBuffer.length() > 0) {
                displayText.append(" ").append(unitBuffer);
            } else if ((valueBuffer.length() > 0 || valueOperationBuffer.length() > 0) && designationBuffer.length() > 0) {
                displayText.append(" ?");
            }
        }

        displayView.setText(displayText);
        displayView.post(() -> {
            int scrollAmount = displayView.getLayout().getHeight() - displayView.getHeight();
            displayView.scrollTo(0, Math.max(scrollAmount, 0));
        });
    }

    /** Очистка всего ввода */
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

    /** Сброс текущего ввода */
    private void resetInput() {
        designationBuffer.setLength(0);
        valueBuffer.setLength(0);
        unitBuffer.setLength(0);
        operationBuffer.setLength(0);
        valueOperationBuffer.setLength(0);
        currentState = InputState.ENTERING_DESIGNATION;
        designationUsesStix = null;
        logicalDesignation = null;
        isCurrentConstant = false;
        updateDisplay();
    }
}