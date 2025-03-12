package com.example.fizmind.keyboard;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.TextView;

import com.example.fizmind.PhysicalQuantity;
import com.example.fizmind.PhysicalQuantityRegistry;
import com.example.fizmind.animation.CustomTypefaceSpan;
import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.measurement.Measurement;
import com.example.fizmind.measurement.UnknownQuantity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер ввода для управления полями "Введите обозначение" и "Введите неизвестное".
 */
public class InputController {

    public enum InputState {
        ENTERING_DESIGNATION, // Ввод обозначения
        ENTERING_VALUE,       // Ввод числового значения
        ENTERING_UNIT         // Ввод единицы измерения
    }

    private InputState currentState;                    // Текущее состояние ввода для "Введите обозначение"
    private final StringBuilder designationBuffer;      // Буфер для обозначения
    private final StringBuilder valueBuffer;            // Буфер для числового значения
    private final StringBuilder unitBuffer;             // Буфер для единицы измерения
    private final TextView designationsView;            // Поле для "Введите обозначение"
    private final TextView unknownView;                 // Поле для "Введите неизвестное"
    private final List<Measurement> measurements;       // Список сохраненных измерений
    private final List<SpannableStringBuilder> history; // История введенных данных
    private final List<UnknownQuantity> unknowns;       // Список сохраненных неизвестных
    private Boolean designationUsesStix;                // Используется ли шрифт STIX для обозначения
    private String logicalDesignation;                  // Логический идентификатор обозначения
    private android.graphics.Typeface stixTypeface;     // Шрифт STIX
    private KeyboardModeSwitcher keyboardModeSwitcher;  // Переключатель режимов клавиатуры
    private boolean isCurrentConstant;                  // Является ли текущая величина константой
    private final StringBuilder operationBuffer = new StringBuilder();         // Буфер операций над обозначением
    private final StringBuilder valueOperationBuffer = new StringBuilder();    // Буфер операций над числом
    private final Map<String, String> lastUnitForDesignation = new HashMap<>(); // Карта для хранения последних единиц
    private String currentInputField = "designations";  // Текущее активное поле ("designations" или "unknown")
    private String unknownDesignation;                  // Обозначение для "Введите неизвестное"
    private String logicalUnknownDesignation;           // Логический идентификатор для неизвестного

    /**
     * Конструктор класса.
     * @param designationsView Поле для "Введите обозначение"
     * @param unknownView Поле для "Введите неизвестное"
     */
    public InputController(TextView designationsView, TextView unknownView) {
        this.designationsView = designationsView;
        this.unknownView = unknownView;
        this.currentState = InputState.ENTERING_DESIGNATION;
        this.designationBuffer = new StringBuilder();
        this.valueBuffer = new StringBuilder();
        this.unitBuffer = new StringBuilder();
        this.measurements = new ArrayList<>();
        this.history = new ArrayList<>();
        this.unknowns = new ArrayList<>();
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
     * Установка текущего активного поля ввода.
     * @param field "designations" или "unknown"
     */
    public void setCurrentInputField(String field) {
        this.currentInputField = field;
        updateDisplay();
        Log.d("InputController", "Текущее поле ввода: " + field);
    }

    /**
     * Обработка ввода с клавиатуры.
     * @param input Введенный символ
     * @param sourceKeyboardMode Режим клавиатуры
     * @param keyUsesStix Используется ли STIX
     * @param logicalId Логический идентификатор
     */

    /** Логирование всех сохраненных данных */
    public void logAllSavedData() {
        StringBuilder logMessage = new StringBuilder("Все сохраненные данные:\n");

        // Сохраненные измерения
        logMessage.append("Измерения ('Введите обозначение'):\n");
        if (measurements.isEmpty()) {
            logMessage.append("  Нет сохраненных измерений\n");
        } else {
            for (Measurement m : measurements) {
                logMessage.append("  ").append(m.toString()).append("\n");
            }
        }

        // Сохраненные неизвестные
        logMessage.append("Неизвестные ('Введите неизвестное'):\n");
        if (unknowns.isEmpty()) {
            logMessage.append("  Нет сохраненных неизвестных\n");
        } else {
            for (UnknownQuantity u : unknowns) {
                logMessage.append("  ").append(u.toString()).append("\n");
            }
        }

        Log.d("InputController", logMessage.toString());
    }
    public void onKeyInput(String input, String sourceKeyboardMode, boolean keyUsesStix, String logicalId) {
        if ("designations".equals(currentInputField)) {
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
                PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
                if (pq == null) return;
                int maxAllowedLength = pq.getAllowedUnits().stream().mapToInt(String::length).max().orElse(0);
                String potentialUnit = unitBuffer.toString() + input;
                boolean validPrefix = pq.getAllowedUnits().stream().anyMatch(allowed -> allowed.startsWith(potentialUnit));
                if (validPrefix && potentialUnit.length() <= maxAllowedLength) {
                    unitBuffer.append(input);
                }
            }
        } else if ("unknown".equals(currentInputField)) {
            if (unknownDesignation == null) {
                if ("Designation".equals(sourceKeyboardMode)) {
                    unknownDesignation = input;
                    logicalUnknownDesignation = logicalId;
                    Log.d("InputController", "Введено неизвестное обозначение: " + input);
                    saveUnknown();
                } else {
                    Log.w("InputController", "В 'Введите неизвестное' можно вводить только обозначения из режима 'Designation'");
                }
            } else {
                Log.w("InputController", "В 'Введите неизвестное' можно ввести только одно обозначение.");
            }
        }
        updateDisplay();
    }

    /** Автоматическое сохранение неизвестного */
    private void saveUnknown() {
        if (unknownDesignation != null) {
            UnknownQuantity unknown = new UnknownQuantity(logicalUnknownDesignation);
            if (!unknown.validate()) {
                Log.e("InputController", "Ошибка валидации неизвестного: " + unknown.toString());
                return;
            }
            unknowns.add(unknown);
            Log.d("InputController", "Автоматически сохранено неизвестное: " + unknown.toString());
        }
    }

    /** Обработка нажатия клавиши Delete */
    public void onDeletePressed() {
        if ("designations".equals(currentInputField)) {
            if (currentState == InputState.ENTERING_VALUE) {
                if (valueBuffer.length() > 0) {
                    char removedChar = valueBuffer.charAt(valueBuffer.length() - 1);
                    valueBuffer.deleteCharAt(valueBuffer.length() - 1);
                    Log.d("InputController", "Удалён символ из числа: " + removedChar);
                } else if (valueOperationBuffer.length() > 0) {
                    char removedChar = valueOperationBuffer.charAt(valueOperationBuffer.length() - 1);
                    valueOperationBuffer.deleteCharAt(valueOperationBuffer.length() - 1);
                    Log.d("InputController", "Удалён символ из операции над числом: " + removedChar);
                }
                if (valueBuffer.length() == 0 && valueOperationBuffer.length() == 0) {
                    currentState = InputState.ENTERING_DESIGNATION;
                    updateKeyboardMode();
                    Log.d("InputController", "Число полностью удалено, переключено в режим ввода обозначения");
                }
            } else if (currentState == InputState.ENTERING_DESIGNATION) {
                if (designationBuffer.length() > 0) {
                    String removedDesignation = designationBuffer.toString();
                    designationBuffer.setLength(0);
                    logicalDesignation = null;
                    designationUsesStix = null;
                    isCurrentConstant = false;
                    unitBuffer.setLength(0); // Очищаем единицу измерения
                    operationBuffer.setLength(0); // Очищаем операции над обозначением
                    currentState = InputState.ENTERING_VALUE; // Переключаем в режим ввода числа
                    Log.d("InputController", "Удалено обозначение '" + removedDesignation + "', единица измерения очищена, переключено в режим ввода числа");
                }
            } else if (currentState == InputState.ENTERING_UNIT) {
                if (unitBuffer.length() > 0) {
                    String removedUnit = unitBuffer.toString();
                    unitBuffer.setLength(0);
                    Log.d("InputController", "Удалена единица измерения: " + removedUnit);
                    currentState = InputState.ENTERING_VALUE;
                    updateKeyboardMode();
                    Log.d("InputController", "Переключено в режим ввода числа");
                } else {
                    currentState = InputState.ENTERING_VALUE;
                    updateKeyboardMode();
                    Log.d("InputController", "Буфер единицы пуст, переключено в режим ввода числа");
                    if (valueBuffer.length() > 0) {
                        char removedChar = valueBuffer.charAt(valueBuffer.length() - 1);
                        valueBuffer.deleteCharAt(valueBuffer.length() - 1);
                        Log.d("InputController", "Удалён символ из числа: " + removedChar);
                    } else if (valueOperationBuffer.length() > 0) {
                        char removedChar = valueOperationBuffer.charAt(valueOperationBuffer.length() - 1);
                        valueOperationBuffer.deleteCharAt(valueOperationBuffer.length() - 1);
                        Log.d("InputController", "Удалён символ из операции над числом: " + removedChar);
                    }
                    if (valueBuffer.length() == 0 && valueOperationBuffer.length() == 0) {
                        currentState = InputState.ENTERING_DESIGNATION;
                        updateKeyboardMode();
                        Log.d("InputController", "Число полностью удалено, переключено в режим ввода обозначения");
                    }
                }
            }
        } else if ("unknown".equals(currentInputField)) {
            if (unknownDesignation != null) {
                String removedUnknown = unknownDesignation;
                unknownDesignation = null;
                logicalUnknownDesignation = null;
                if (!unknowns.isEmpty()) {
                    unknowns.remove(unknowns.size() - 1);
                    Log.d("InputController", "Удалено неизвестное обозначение: " + removedUnknown + " из списка");
                }
                updateDisplay();
            }
        }
        updateDisplay();
    }

    /** Переключение влево по режимам */
    public void onLeftArrowPressed() {
        if ("designations".equals(currentInputField)) {
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
    }

    /** Переключение вправо по режимам */
    public void onRightArrowPressed() {
        if ("designations".equals(currentInputField)) {
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
    }

    /** Подтверждение ввода (нажатие вниз) */
    /** Подтверждение ввода (нажатие вниз) */
    public void onDownArrowPressed() {
        if ("designations".equals(currentInputField)) {
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
            logAllSavedData(); // Логируем все сохраненные данные
        }
    }

    /** Обновление режима клавиатуры */
    private void updateKeyboardMode() {
        if (keyboardModeSwitcher != null && "designations".equals(currentInputField)) {
            if (currentState == InputState.ENTERING_DESIGNATION) {
                keyboardModeSwitcher.switchToDesignation();
            } else if (currentState == InputState.ENTERING_VALUE) {
                keyboardModeSwitcher.switchToNumbersAndOperations();
            } else if (currentState == InputState.ENTERING_UNIT) {
                keyboardModeSwitcher.switchToUnits();
            }
        }
    }

    /** Обновление отображаемого текста */
    private void updateDisplay() {
        SpannableStringBuilder designationsText = new SpannableStringBuilder();
        for (int i = 0; i < history.size(); i++) {
            designationsText.append(history.get(i));
            if (i < history.size() - 1) designationsText.append("\n\n");
        }
        if (history.size() > 0) designationsText.append("\n\n");

        if (designationBuffer.length() == 0 && valueBuffer.length() == 0 && unitBuffer.length() == 0 &&
                operationBuffer.length() == 0 && valueOperationBuffer.length() == 0) {
            int start = designationsText.length();
            designationsText.append("Введите обозначение");
            int color = "designations".equals(currentInputField) ? Color.BLACK : Color.GRAY;
            designationsText.setSpan(new android.text.style.ForegroundColorSpan(color),
                    start, designationsText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            if (designationBuffer.length() > 0) {
                int start = designationsText.length();
                if (operationBuffer.length() > 0) {
                    designationsText.append(operationBuffer).append("(").append(designationBuffer).append(")");
                } else {
                    designationsText.append(designationBuffer);
                }
                if (designationUsesStix != null && designationUsesStix && stixTypeface != null) {
                    designationsText.setSpan(new CustomTypefaceSpan(stixTypeface), start, designationsText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                designationsText.append(" = ");
            } else {
                designationsText.append("= ");
            }
            if (valueOperationBuffer.length() > 0) {
                designationsText.append(valueOperationBuffer);
            } else {
                designationsText.append(valueBuffer);
            }
            if (unitBuffer.length() > 0) {
                designationsText.append(" ").append(unitBuffer);
            }
        }
        designationsView.setText(designationsText);

        SpannableStringBuilder unknownText = new SpannableStringBuilder();
        if (unknownDesignation != null) {
            unknownText.append(unknownDesignation).append(" = ?");
            Log.d("InputController", "Отображено неизвестное: " + unknownText.toString());
        } else {
            unknownText.append("Введите неизвестное");
            int color = "unknown".equals(currentInputField) ? Color.BLACK : Color.GRAY;
            unknownText.setSpan(new android.text.style.ForegroundColorSpan(color),
                    0, unknownText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        unknownView.setText(unknownText);

        if ("designations".equals(currentInputField)) {
            designationsView.setTextColor(Color.BLACK);
            unknownView.setTextColor(Color.parseColor("#A0A0A0"));
        } else if ("unknown".equals(currentInputField)) {
            designationsView.setTextColor(Color.parseColor("#A0A0A0"));
            unknownView.setTextColor(Color.BLACK);
        }
    }

    /** Очистка всего ввода */
    public void clearAll() {
        if ("designations".equals(currentInputField)) {
            designationBuffer.setLength(0);
            valueBuffer.setLength(0);
            unitBuffer.setLength(0);
            operationBuffer.setLength(0);
            valueOperationBuffer.setLength(0);
            currentState = InputState.ENTERING_DESIGNATION;
            designationUsesStix = null;
            logicalDesignation = null;
            isCurrentConstant = false;
            history.clear();
            measurements.clear();
            Log.d("InputController", "Очищены все данные для 'Введите обозначение'");
        } else if ("unknown".equals(currentInputField)) {
            unknownDesignation = null;
            logicalUnknownDesignation = null;
            unknowns.clear();
            Log.d("InputController", "Очищены все данные для 'Введите неизвестное'");
        }
        updateDisplay();
        if (keyboardModeSwitcher != null) {
            keyboardModeSwitcher.switchToDesignation();
        }
    }

    /** Сброс текущего ввода для "Введите обозначение" */
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

    /** Получение списка сохраненных измерений для логирования */
    public List<Measurement> getMeasurements() {
        return new ArrayList<>(measurements);
    }

    /** Получение списка сохраненных неизвестных для логирования */
    public List<UnknownQuantity> getUnknowns() {
        return new ArrayList<>(unknowns);
    }
}