package com.example.fizmind.keyboard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.widget.TextView;

import com.example.fizmind.PhysicalQuantity;
import com.example.fizmind.PhysicalQuantityRegistry;
import com.example.fizmind.animation.CustomTypefaceSpan;
import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.measurement.Measurement;
import com.example.fizmind.measurement.UnknownQuantity;
import com.example.fizmind.modules.InputModule;
import com.example.fizmind.modules.ModuleType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputController {

    public enum InputState {
        ENTERING_DESIGNATION, // Ввод обозначения
        ENTERING_VALUE,       // Ввод числа
        ENTERING_UNIT         // Ввод единицы измерения
    }

    private InputState currentState;                    // Текущее состояние ввода
    private final StringBuilder designationBuffer;      // Буфер для обозначения
    private final StringBuilder valueBuffer;            // Буфер для числового значения
    private final StringBuilder unitBuffer;             // Буфер для единицы измерения
    private final TextView designationsView;            // Поле "Введите обозначение"
    private final TextView unknownView;                 // Поле "Введите неизвестное"
    private final List<Measurement> measurements;       // Список сохраненных измерений
    private final List<SpannableStringBuilder> history; // История введенных данных
    private final List<UnknownQuantity> unknowns;       // Список сохраненных неизвестных
    private Boolean designationUsesStix;                // Используется ли шрифт STIX для обозначения
    private Boolean unknownUsesStix;                    // Используется ли шрифт STIX для неизвестного
    private String logicalDesignation;                  // Логический идентификатор обозначения
    private Typeface stixTypeface;                      // Шрифт STIX
    private KeyboardModeSwitcher keyboardModeSwitcher;  // Переключатель режимов клавиатуры
    private boolean isCurrentConstant;                  // Является ли текущая величина константой
    private final StringBuilder operationBuffer;        // Буфер операций над обозначением
    private final StringBuilder valueOperationBuffer;   // Буфер операций над числом
    private final Map<String, String> lastUnitForDesignation; // Последние единицы для обозначений
    private String currentInputField;                   // Текущее поле ввода
    private String unknownDesignation;                  // Обозначение для неизвестного
    private String logicalUnknownDesignation;           // Логический идентификатор неизвестного
    private InputModule currentModule;                  // Текущий активный модуль (степень или индекс)

    // Конструктор
    public InputController(TextView designationsView, TextView unknownView) {
        this.designationsView = designationsView;
        this.unknownView = unknownView;
        this.currentState = InputState.ENTERING_DESIGNATION;
        this.designationBuffer = new StringBuilder();
        this.valueBuffer = new StringBuilder();
        this.unitBuffer = new StringBuilder();
        this.operationBuffer = new StringBuilder();
        this.valueOperationBuffer = new StringBuilder();
        this.measurements = new ArrayList<>();
        this.history = new ArrayList<>();
        this.unknowns = new ArrayList<>();
        this.designationUsesStix = null;
        this.unknownUsesStix = null;
        this.logicalDesignation = null;
        this.isCurrentConstant = false;
        this.currentInputField = "designations";
        this.lastUnitForDesignation = new HashMap<>();
        this.currentModule = null;
        updateDisplay();
    }

    // Установка шрифта STIX
    public void setStixTypeface(Typeface stixTypeface) {
        this.stixTypeface = stixTypeface;
    }

    // Установка переключателя режимов клавиатуры
    public void setKeyboardModeSwitcher(KeyboardModeSwitcher switcher) {
        this.keyboardModeSwitcher = switcher;
    }

    // Установка текущего поля ввода
    public void setCurrentInputField(String field) {
        if (!field.equals(currentInputField)) {
            currentModule = null; // Сбрасываем модуль при смене поля
        }
        this.currentInputField = field;
        if ("unknown".equals(field)) {
            if (keyboardModeSwitcher != null) {
                keyboardModeSwitcher.switchToDesignation();
            }
        } else if ("designations".equals(field)) {
            updateKeyboardMode();
        }
        updateDisplay();
        Log.d("InputController", "Текущее поле ввода: " + field);
    }

    // Обработка ввода с клавиатуры

    public void onKeyInput(String input, String sourceKeyboardMode, boolean keyUsesStix, String logicalId) {
        Log.d("InputController", "Текущее состояние: " + currentState + ", ввод: " + input + ", logicalId: " + logicalId);

        if ("designations".equals(currentInputField)) {
            // Существующая логика для поля "Введите обозначение"
            if (currentModule != null && currentModule.isActive()) {
                currentModule.apply(input);
                updateDisplay();
                return;
            }

            if (currentState == InputState.ENTERING_DESIGNATION) {
                if (designationBuffer.length() == 0) {
                    if (!"Designation".equals(sourceKeyboardMode)) {
                        Log.w("InputController", "Символ обозначения должен быть из режима 'Designation'");
                        return;
                    }
                    if (logicalId.equals("op_exponent") || logicalId.equals("op_subscript")) {
                        Log.w("InputController", "Нельзя начинать ввод с модуля");
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
                } else {
                    if (input.matches("[0-9]") || ".".equals(input) || "-".equals(input)) {
                        currentState = InputState.ENTERING_VALUE;
                        if (keyboardModeSwitcher != null) {
                            keyboardModeSwitcher.switchToNumbersAndOperations();
                        }
                        handleValueInput(input, logicalId);
                    } else if (logicalId.equals("op_subscript")) {
                        if (currentModule != null) {
                            Log.w("InputController", "Нельзя применить два модуля одновременно");
                            return;
                        }
                        currentModule = new InputModule(ModuleType.SUBSCRIPT);
                        updateDisplay();
                    } else if (logicalId.equals("op_exponent")) {
                        Log.w("InputController", "Степень применима только к числу");
                    } else {
                        Log.w("InputController", "Обозначение уже введено, ожидается число или модуль");
                    }
                }
            } else if (currentState == InputState.ENTERING_VALUE) {
                if (designationBuffer.length() == 0) {
                    Log.w("InputController", "Невозможно ввести число: отсутствует обозначение");
                    return;
                }
                if (logicalId.equals("op_exponent")) {
                    if (valueBuffer.length() == 0 && valueOperationBuffer.length() == 0) {
                        Log.w("InputController", "Нельзя применить степень без числа");
                        return;
                    }
                    if (currentModule != null) {
                        Log.w("InputController", "Нельзя применить два модуля одновременно");
                        return;
                    }
                    currentModule = new InputModule(ModuleType.EXPONENT);
                    updateDisplay();
                } else if (logicalId.equals("op_subscript")) {
                    if (currentModule != null) {
                        Log.w("InputController", "Нельзя применить два модуля одновременно");
                        return;
                    }
                    currentModule = new InputModule(ModuleType.SUBSCRIPT);
                    updateDisplay();
                } else {
                    handleValueInput(input, logicalId);
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
            // Запрещаем использование модулей (ни степень, ни нижний индекс) в поле "Введите неизвестное"
            if (logicalId.equals("op_subscript") || logicalId.equals("op_exponent")) {
                Log.w("InputController", "В поле 'Введите неизвестное' модули не поддерживаются");
                return;
            }
            if (unknownDesignation == null) {
                if ("Designation".equals(sourceKeyboardMode)) {
                    unknownDesignation = input;
                    logicalUnknownDesignation = logicalId;
                    unknownUsesStix = keyUsesStix;
                    Log.d("InputController", "Введено неизвестное обозначение: " + input);
                } else {
                    Log.w("InputController", "В 'Введите неизвестное' можно вводить только обозначения из режима 'Designation'");
                    return;
                }
            } else {
                Log.w("InputController", "В 'Введите неизвестное' можно ввести только одно обозначение");
                return;
            }
        }
        updateDisplay();
    }


    // Обработка ввода чисел и операций над числом
    private void handleValueInput(String input, String logicalId) {
        Log.d("InputController", "Обработка ввода значения: " + input);
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
        } else if (logicalId.equals("op_abs_open")) {
            valueOperationBuffer.append("|");
            updateDisplay();
        } else if (logicalId.equals("op_abs_close") && valueOperationBuffer.toString().contains("|")) {
            valueOperationBuffer.append(valueBuffer).append("|");
            valueBuffer.setLength(0);
            updateDisplay();
        } else {
            currentState = InputState.ENTERING_UNIT;
            onKeyInput(input, "Units_of_measurement", false, logicalId);
            return;
        }
        updateDisplay();
    }

    // Автоматическое сохранение неизвестного
    private void saveUnknown() {
        if (unknownDesignation != null) {
            String subscript = (currentModule != null && currentModule.getType() == ModuleType.SUBSCRIPT) ? currentModule.getDisplayText().toString() : "";
            UnknownQuantity unknown = new UnknownQuantity(logicalUnknownDesignation, subscript);
            if (!unknown.validate()) {
                Log.e("InputController", "Ошибка валидации неизвестного: " + unknown.toString());
                return;
            }
            unknowns.add(unknown);
            Log.d("InputController", "Автоматически сохранено неизвестное: " + unknown.toString());
            unknownDesignation = null;
            logicalUnknownDesignation = null;
            unknownUsesStix = null;
            currentModule = null;
            updateDisplay();
        }
    }

    // Удаление последнего символа
    public void onDeletePressed() {
        if ("designations".equals(currentInputField)) {
            if (currentModule != null && currentModule.isActive()) {
                if (currentModule.delete()) {
                    currentModule = null;
                    Log.d("InputController", "Модуль удалён полностью");
                }
                updateDisplay();
                return;
            }
            if (currentState == InputState.ENTERING_UNIT) {
                unitBuffer.setLength(0);
                currentState = InputState.ENTERING_VALUE;
                updateKeyboardMode();
                Log.d("InputController", "Удалены единицы измерения, переключено в режим ввода числа");
            } else if (currentState == InputState.ENTERING_VALUE) {
                if (valueBuffer.length() > 0) {
                    valueBuffer.deleteCharAt(valueBuffer.length() - 1);
                    Log.d("InputController", "Удалён последний символ из числа");
                } else if (valueOperationBuffer.length() > 0) {
                    valueOperationBuffer.deleteCharAt(valueOperationBuffer.length() - 1);
                    Log.d("InputController", "Удалён последний символ из операции над числом");
                } else if (designationBuffer.length() > 0) {
                    designationBuffer.setLength(0);
                    logicalDesignation = null;
                    designationUsesStix = null;
                    isCurrentConstant = false;
                    unitBuffer.setLength(0);
                    operationBuffer.setLength(0);
                    valueBuffer.setLength(0);
                    valueOperationBuffer.setLength(0);
                    currentState = InputState.ENTERING_DESIGNATION;
                    currentModule = null;
                    updateKeyboardMode();
                    Log.d("InputController", "Число и операции пусты, удалено обозначение");
                }
            } else if (currentState == InputState.ENTERING_DESIGNATION) {
                if (designationBuffer.length() > 0) {
                    designationBuffer.setLength(0);
                    logicalDesignation = null;
                    designationUsesStix = null;
                    isCurrentConstant = false;
                    unitBuffer.setLength(0);
                    operationBuffer.setLength(0);
                    valueBuffer.setLength(0);
                    valueOperationBuffer.setLength(0);
                    currentModule = null;
                    Log.d("InputController", "Удалено обозначение, все буферы очищены");
                }
            }
        } else if ("unknown".equals(currentInputField)) {
            if (currentModule != null && currentModule.isActive()) {
                if (currentModule.delete()) {
                    currentModule = null;
                    Log.d("InputController", "Модуль удалён полностью в 'Введите неизвестное'");
                }
                updateDisplay();
                return;
            }
            if (unknownDesignation != null) {
                unknownDesignation = null;
                logicalUnknownDesignation = null;
                unknownUsesStix = null;
                currentModule = null;
                if (!unknowns.isEmpty()) {
                    unknowns.remove(unknowns.size() - 1);
                    Log.d("InputController", "Удалено неизвестное обозначение из списка");
                }
            }
        }
        updateDisplay();
    }

    // Переключение влево
    public void onLeftArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if (currentModule != null && currentModule.isActive()) {
                currentModule.deactivate();
                Log.d("InputController", "Фокус снят с модуля");
                updateDisplay();
                return;
            }
            if (currentState == InputState.ENTERING_UNIT) {
                currentState = InputState.ENTERING_VALUE;
                Log.d("InputController", "Переключено в режим ввода числа");
            } else if (currentState == InputState.ENTERING_VALUE && designationBuffer.length() > 0) {
                currentState = InputState.ENTERING_DESIGNATION;
                Log.d("InputController", "Переключено в режим ввода обозначения");
            }
            updateKeyboardMode();
            updateDisplay();
        } else if ("unknown".equals(currentInputField)) {
            if (currentModule != null && currentModule.isActive()) {
                currentModule.deactivate();
                Log.d("InputController", "Фокус снят с модуля в 'Введите неизвестное'");
                updateDisplay();
            }
        }
    }

    // Переключение вправо
    public void onRightArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if (currentModule != null && currentModule.isActive()) {
                currentModule.deactivate();
                Log.d("InputController", "Фокус снят с модуля");
                updateDisplay();
                return;
            }
            if (currentState == InputState.ENTERING_DESIGNATION && designationBuffer.length() > 0) {
                currentState = InputState.ENTERING_VALUE;
                Log.d("InputController", "Переключено в режим ввода числа");
            } else if (currentState == InputState.ENTERING_VALUE && (valueBuffer.length() > 0 || valueOperationBuffer.length() > 0)) {
                currentState = InputState.ENTERING_UNIT;
                Log.d("InputController", "Переключено в режим ввода единицы измерения");
            }
            updateKeyboardMode();
            updateDisplay();
        } else if ("unknown".equals(currentInputField)) {
            if (currentModule != null && currentModule.isActive()) {
                currentModule.deactivate();
                Log.d("InputController", "Фокус снят с модуля в 'Введите неизвестное'");
                updateDisplay();
            }
        }
    }

    // Сохранение измерения
    public void onDownArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if (designationBuffer.length() == 0) {
                Log.w("InputController", "Невозможно сохранить: отсутствует обозначение");
                return;
            }
            if (valueBuffer.length() == 0 && valueOperationBuffer.length() == 0 && (currentModule == null || currentModule.getType() != ModuleType.EXPONENT)) {
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

            String exponent = (currentModule != null && currentModule.getType() == ModuleType.EXPONENT) ? currentModule.getDisplayText().toString() : "";
            String subscript = (currentModule != null && currentModule.getType() == ModuleType.SUBSCRIPT) ? currentModule.getDisplayText().toString() : "";

            ConcreteMeasurement measurement = new ConcreteMeasurement(
                    logicalDesignation, value, unit, operationBuffer.toString(), valueOperationBuffer.toString(), exponent, subscript);
            if (!measurement.validate()) {
                Log.e("InputController", "Ошибка валидации: " + measurement.toString());
                return;
            }

            measurements.add(measurement);
            SpannableStringBuilder historyEntry = new SpannableStringBuilder();
            int start = historyEntry.length();
            if (operationBuffer.length() > 0) {
                historyEntry.append(operationBuffer).append("(");
            }
            historyEntry.append(designationBuffer);
            int designationEnd = historyEntry.length();
            if (subscript != null && !subscript.isEmpty()) {
                int subscriptStart = historyEntry.length();
                historyEntry.append(subscript);
                int subscriptEnd = historyEntry.length();
                historyEntry.setSpan(new SubscriptSpan(), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                historyEntry.setSpan(new RelativeSizeSpan(0.75f), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (operationBuffer.length() > 0) {
                historyEntry.append(")");
            }
            if (designationUsesStix != null && designationUsesStix && stixTypeface != null) {
                historyEntry.setSpan(new CustomTypefaceSpan(stixTypeface), start, designationEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            historyEntry.append(" = ");
            int valueStart = historyEntry.length();
            if (valueOperationBuffer.length() > 0) {
                historyEntry.append(valueOperationBuffer);
            } else {
                historyEntry.append(valueBuffer);
            }
            int valueEnd = historyEntry.length();
            if (exponent != null && !exponent.isEmpty()) {
                int exponentStart = historyEntry.length();
                historyEntry.append(exponent);
                int exponentEnd = historyEntry.length();
                historyEntry.setSpan(new SuperscriptSpan(), exponentStart, exponentEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                historyEntry.setSpan(new RelativeSizeSpan(0.75f), exponentStart, exponentEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
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
            logAllSavedData();
        } else if ("unknown".equals(currentInputField)) {
            saveUnknown();
        }
    }

    // Обновление режима клавиатуры
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

    // Обновление отображения
    private void updateDisplay() {
        SpannableStringBuilder designationsText = new SpannableStringBuilder();

        // Отображаем историю измерений
        for (int i = 0; i < history.size(); i++) {
            designationsText.append(history.get(i));
            if (i < history.size() - 1) designationsText.append("\n\n");
        }
        if (history.size() > 0) designationsText.append("\n\n");

        // Текущий ввод
        if (designationBuffer.length() > 0 || valueBuffer.length() > 0 || unitBuffer.length() > 0 || (currentModule != null && currentModule.isActive())) {
            int designationStart = designationsText.length();
            if (operationBuffer.length() > 0) {
                designationsText.append(operationBuffer).append("(").append(designationBuffer).append(")");
            } else {
                designationsText.append(designationBuffer);
            }
            int designationEnd = designationsText.length();
            if (currentModule != null && currentModule.getType() == ModuleType.SUBSCRIPT) {
                designationsText.append(currentModule.getDisplayText());
            }
            if (designationUsesStix != null && designationUsesStix && stixTypeface != null) {
                designationsText.setSpan(
                        new CustomTypefaceSpan(stixTypeface),
                        designationStart,
                        designationEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            designationsText.append(" = ");
            int valueStart = designationsText.length();
            if (valueOperationBuffer.length() > 0) {
                designationsText.append(valueOperationBuffer);
            } else {
                designationsText.append(valueBuffer);
            }
            int valueEnd = designationsText.length();
            if (currentModule != null && currentModule.getType() == ModuleType.EXPONENT) {
                designationsText.append(currentModule.getDisplayText());
            }
            int unitStart = designationsText.length();
            if (unitBuffer.length() > 0) {
                designationsText.append(" ").append(unitBuffer);
            } else if (valueBuffer.length() > 0 || valueOperationBuffer.length() > 0) {
                designationsText.append(" ?");
            }

            // Применяем жирный шрифт к активной части
            if ("designations".equals(currentInputField)) {
                if (currentModule != null && currentModule.isActive()) {
                    int moduleStart = currentModule.getType() == ModuleType.EXPONENT ? valueEnd : designationEnd;
                    designationsText.setSpan(
                            new StyleSpan(Typeface.BOLD),
                            moduleStart,
                            designationsText.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                } else if (currentState == InputState.ENTERING_DESIGNATION && designationStart < valueStart - 3) {
                    designationsText.setSpan(
                            new StyleSpan(Typeface.BOLD),
                            designationStart,
                            valueStart - 3,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                } else if (currentState == InputState.ENTERING_VALUE && valueStart < valueEnd) {
                    designationsText.setSpan(
                            new StyleSpan(Typeface.BOLD),
                            valueStart,
                            valueEnd,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                } else if (currentState == InputState.ENTERING_UNIT && unitStart < designationsText.length()) {
                    designationsText.setSpan(
                            new StyleSpan(Typeface.BOLD),
                            unitStart + 1,
                            designationsText.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                }
            }
        } else {
            int start = designationsText.length();
            designationsText.append("Введите обозначение");
            int color = "designations".equals(currentInputField) ? Color.BLACK : Color.GRAY;
            designationsText.setSpan(
                    new ForegroundColorSpan(color),
                    start,
                    designationsText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        designationsView.setText(designationsText);

        // Обработка поля неизвестного
        SpannableStringBuilder unknownText = new SpannableStringBuilder();
        if (unknownDesignation != null || (currentModule != null && currentModule.isActive())) {
            int start = unknownText.length();
            if (unknownDesignation != null) {
                unknownText.append(unknownDesignation);
            }
            int end = unknownText.length();
            if (currentModule != null && currentModule.getType() == ModuleType.SUBSCRIPT) {
                unknownText.append(currentModule.getDisplayText());
            }
            if (unknownUsesStix != null && unknownUsesStix && stixTypeface != null) {
                unknownText.setSpan(
                        new CustomTypefaceSpan(stixTypeface),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            unknownText.append(" = ?");
            if ("unknown".equals(currentInputField) && currentModule != null && currentModule.isActive()) {
                unknownText.setSpan(
                        new StyleSpan(Typeface.BOLD),
                        end,
                        unknownText.length() - 4,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        } else if (!unknowns.isEmpty()) {
            unknownText.append(unknowns.get(unknowns.size() - 1).getDisplayText());
        } else {
            unknownText.append("Введите неизвестное");
            int color = "unknown".equals(currentInputField) ? Color.BLACK : Color.GRAY;
            unknownText.setSpan(
                    new ForegroundColorSpan(color),
                    0,
                    unknownText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
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

    // Полная очистка
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
            currentModule = null;
            history.clear();
            measurements.clear();
            Log.d("InputController", "Очищены все данные для 'Введите обозначение'");
        } else if ("unknown".equals(currentInputField)) {
            unknownDesignation = null;
            logicalUnknownDesignation = null;
            unknownUsesStix = null;
            currentModule = null;
            unknowns.clear();
            Log.d("InputController", "Очищены все данные для 'Введите неизвестное'");
        }
        updateDisplay();
        if (keyboardModeSwitcher != null) {
            keyboardModeSwitcher.switchToDesignation();
        }
    }

    // Сброс текущего ввода
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
        currentModule = null;
        updateDisplay();
    }

    // Получение списка измерений
    public List<Measurement> getMeasurements() {
        return new ArrayList<>(measurements);
    }

    // Получение списка неизвестных
    public List<UnknownQuantity> getUnknowns() {
        return new ArrayList<>(unknowns);
    }

    // Логирование всех сохраненных данных
    public void logAllSavedData() {
        StringBuilder logMessage = new StringBuilder("Все сохраненные данные:\n");

        logMessage.append("Измерения ('Введите обозначение'):\n");
        if (measurements.isEmpty()) {
            logMessage.append("  Нет сохраненных измерений\n");
        } else {
            for (Measurement m : measurements) {
                logMessage.append("  ").append(m.toString()).append("\n");
            }
        }

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
}