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
import com.example.fizmind.modules.ModuleValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputController {

    public enum InputState {
        ENTERING_DESIGNATION,
        ENTERING_VALUE,
        ENTERING_UNIT
    }

    public enum FocusState {
        DESIGNATION,
        VALUE,
        UNIT,
        MODULE
    }

    private InputState currentState;
    private FocusState focusState;
    private final StringBuilder designationBuffer;
    private final StringBuilder valueBuffer;
    private final StringBuilder unitBuffer;
    private final TextView designationsView;
    private final TextView unknownView;
    private final List<Measurement> measurements;
    private final List<SpannableStringBuilder> history;
    private final List<UnknownQuantity> unknowns;
    private Boolean designationUsesStix;
    private Boolean unknownUsesStix;
    private String logicalDesignation;
    private Typeface stixTypeface;
    private KeyboardModeSwitcher keyboardModeSwitcher;
    private boolean isCurrentConstant;
    private final StringBuilder operationBuffer;
    private final StringBuilder valueOperationBuffer;
    private final Map<String, String> lastUnitForDesignation;
    private String currentInputField;
    private String unknownDesignation;
    private String logicalUnknownDesignation;

    private InputModule designationExponentModule;
    private InputModule designationSubscriptModule;
    private InputModule unknownSubscriptModule;

    public InputController(TextView designationsView, TextView unknownView) {
        this.designationsView = designationsView;
        this.unknownView = unknownView;
        this.currentState = InputState.ENTERING_DESIGNATION;
        this.focusState = FocusState.DESIGNATION;
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
        this.designationExponentModule = null;
        this.designationSubscriptModule = null;
        this.unknownSubscriptModule = null;
        updateDisplay();
    }

    public void setStixTypeface(Typeface stixTypeface) {
        this.stixTypeface = stixTypeface;
    }

    public void setKeyboardModeSwitcher(KeyboardModeSwitcher switcher) {
        this.keyboardModeSwitcher = switcher;
    }

    public void setCurrentInputField(String field) {
        if (!field.equals(currentInputField)) {
            designationExponentModule = null;
            designationSubscriptModule = null;
            unknownSubscriptModule = null;
            focusState = FocusState.DESIGNATION;
            currentState = InputState.ENTERING_DESIGNATION;
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

    public void onKeyInput(String input, String sourceKeyboardMode, boolean keyUsesStix, String logicalId) {
        Log.d("InputController", "Текущее состояние: " + currentState + ", ввод: " + input + ", logicalId: " + logicalId);

        if ("designations".equals(currentInputField)) {
            if (focusState == FocusState.MODULE) {
                if (designationExponentModule != null && designationExponentModule.isActive()) {
                    if ("Units_of_measurement".equals(sourceKeyboardMode)) {
                        designationExponentModule.deactivate();
                        focusState = FocusState.UNIT;
                        currentState = InputState.ENTERING_UNIT;
                        handleUnitInput(input, logicalId);
                        return;
                    } else {
                        designationExponentModule.apply(input);
                        updateDisplay();
                        return;
                    }
                } else if (designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                    designationSubscriptModule.apply(input);
                    updateDisplay();
                    return;
                }
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
                        focusState = FocusState.VALUE;
                        if (keyboardModeSwitcher != null) {
                            keyboardModeSwitcher.switchToNumbersAndOperations();
                        }
                    }
                } else {
                    if (input.matches("[0-9]") || ".".equals(input) || "-".equals(input)) {
                        currentState = InputState.ENTERING_VALUE;
                        focusState = FocusState.VALUE;
                        if (keyboardModeSwitcher != null) {
                            keyboardModeSwitcher.switchToNumbersAndOperations();
                        }
                        handleValueInput(input, logicalId);
                    } else if (logicalId.equals("op_subscript")) {
                        if (!ModuleValidator.canAddModule(ModuleType.SUBSCRIPT, designationExponentModule, designationSubscriptModule)) {
                            return;
                        }
                        designationSubscriptModule = new InputModule(ModuleType.SUBSCRIPT);
                        focusState = FocusState.MODULE;
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
                    if (!ModuleValidator.canApplyModuleToValue(valueBuffer, valueOperationBuffer)) {
                        return;
                    }
                    if (!ModuleValidator.canAddModule(ModuleType.EXPONENT, designationExponentModule, designationSubscriptModule)) {
                        return;
                    }
                    designationExponentModule = new InputModule(ModuleType.EXPONENT);
                    focusState = FocusState.MODULE;
                    updateDisplay();
                } else if (logicalId.equals("op_subscript")) {
                    if (!ModuleValidator.canAddModule(ModuleType.SUBSCRIPT, designationExponentModule, designationSubscriptModule)) {
                        return;
                    }
                    designationSubscriptModule = new InputModule(ModuleType.SUBSCRIPT);
                    focusState = FocusState.MODULE;
                    updateDisplay();
                } else {
                    handleValueInput(input, logicalId);
                }
            } else if (currentState == InputState.ENTERING_UNIT) {
                if (logicalId.equals("op_subscript")) {
                    Log.w("InputController", "Нельзя ввести модуль индекса в режиме ввода единиц измерения");
                    return;
                }
                handleUnitInput(input, logicalId);
            }
        } else if ("unknown".equals(currentInputField)) {
            if (focusState == FocusState.MODULE && unknownSubscriptModule != null && unknownSubscriptModule.isActive()) {
                unknownSubscriptModule.apply(input);
                updateDisplay();
                return;
            }
            if (logicalId.equals("op_exponent")) {
                Log.w("InputController", "Степень не поддерживается в 'Введите неизвестное'");
                return;
            }
            if (logicalId.equals("op_subscript")) {
                if (!ModuleValidator.canAddModule(ModuleType.SUBSCRIPT, null, unknownSubscriptModule)) {
                    return;
                }
                unknownSubscriptModule = new InputModule(ModuleType.SUBSCRIPT);
                focusState = FocusState.MODULE;
                updateDisplay();
                return;
            }
            if (unknownDesignation == null) {
                unknownDesignation = input;
                logicalUnknownDesignation = logicalId;
                unknownUsesStix = keyUsesStix;
                focusState = FocusState.DESIGNATION;
                Log.d("InputController", "Введено неизвестное обозначение: " + input);
                // saveUnknown() убрано, чтобы обозначение не сбрасывалось автоматически
            } else {
                Log.w("InputController", "В 'Введите неизвестное' можно ввести только одно обозначение");
                return;
            }
        }
        updateDisplay();
    }

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
            focusState = FocusState.UNIT;
            onKeyInput(input, "Units_of_measurement", false, logicalId);
            return;
        }
        updateDisplay();
    }

    private void handleUnitInput(String input, String logicalId) {
        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
        if (pq == null) {
            Log.w("InputController", "Неизвестная физическая величина: " + logicalDesignation);
            return;
        }
        int maxAllowedLength = pq.getAllowedUnits().stream().mapToInt(String::length).max().orElse(0);
        String potentialUnit = unitBuffer.toString() + input;
        boolean validPrefix = pq.getAllowedUnits().stream().anyMatch(allowed -> allowed.startsWith(potentialUnit));
        if (validPrefix && potentialUnit.length() <= maxAllowedLength) {
            unitBuffer.append(input);
            Log.d("InputController", "Добавлена единица измерения: " + input);
        } else {
            Log.w("InputController", "Недопустимая единица измерения: " + potentialUnit);
        }
        updateDisplay();
    }

    private void saveUnknown() {
        if (unknownDesignation != null) {
            if (unknownSubscriptModule != null && unknownSubscriptModule.isActive() && unknownSubscriptModule.isEmpty()) {
                Log.w("InputController", "Нельзя сохранить с пустым активным индексом");
                return;
            }
            String subscript = (unknownSubscriptModule != null && !unknownSubscriptModule.isEmpty()) ? unknownSubscriptModule.getDisplayText().toString() : "";
            UnknownQuantity unknown = new UnknownQuantity(unknownDesignation, subscript, unknownUsesStix != null && unknownUsesStix);
            if (!unknown.validate()) {
                Log.e("InputController", "Ошибка валидации неизвестного: " + unknown.toString());
                return;
            }
            unknowns.add(unknown);
            Log.d("InputController", "Сохранено неизвестное: " + unknown.toString());
            unknownDesignation = null;
            logicalUnknownDesignation = null;
            unknownUsesStix = null;
            unknownSubscriptModule = null;
            focusState = FocusState.DESIGNATION;
            updateDisplay();
        }
    }

    public void onDeletePressed() {
        if ("designations".equals(currentInputField)) {
            if (focusState == FocusState.MODULE) {
                if (designationExponentModule != null && designationExponentModule.isActive()) {
                    if (designationExponentModule.delete()) {
                        designationExponentModule = null;
                        focusState = FocusState.VALUE;
                        Log.d("InputController", "Степень удалена полностью");
                    }
                } else if (designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                    if (designationSubscriptModule.delete()) {
                        designationSubscriptModule = null;
                        focusState = FocusState.DESIGNATION;
                        Log.d("InputController", "Индекс удалён полностью");
                    }
                }
                updateDisplay();
                return;
            }
            if (currentState == InputState.ENTERING_UNIT) {
                unitBuffer.setLength(0);
                currentState = InputState.ENTERING_VALUE;
                focusState = FocusState.VALUE;
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
                    focusState = FocusState.DESIGNATION;
                    designationExponentModule = null;
                    designationSubscriptModule = null;
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
                    designationExponentModule = null;
                    designationSubscriptModule = null;
                    focusState = FocusState.DESIGNATION;
                    Log.d("InputController", "Удалено обозначение, все буферы очищены");
                }
            }
        } else if ("unknown".equals(currentInputField)) {
            if (focusState == FocusState.MODULE && unknownSubscriptModule != null && unknownSubscriptModule.isActive()) {
                if (unknownSubscriptModule.delete()) {
                    unknownSubscriptModule = null;
                    focusState = FocusState.DESIGNATION;
                    Log.d("InputController", "Индекс удалён полностью в 'Введите неизвестное'");
                }
            } else if (unknownDesignation != null) {
                unknownDesignation = null;
                logicalUnknownDesignation = null;
                unknownUsesStix = null;
                unknownSubscriptModule = null;
                focusState = FocusState.DESIGNATION;
                Log.d("InputController", "Удалено текущее неизвестное обозначение");
            } else if (!unknowns.isEmpty()) {
                unknowns.remove(unknowns.size() - 1);
                Log.d("InputController", "Удалено последнее сохраненное неизвестное");
            }
        }
        updateDisplay();
    }

    public void onLeftArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if (focusState == FocusState.MODULE) {
                if (designationExponentModule != null && designationExponentModule.isActive()) {
                    handleModuleNavigation(ModuleType.EXPONENT, true);
                } else if (designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                    handleModuleNavigation(ModuleType.SUBSCRIPT, true);
                }
            } else if (focusState == FocusState.UNIT) {
                focusState = FocusState.VALUE;
                currentState = InputState.ENTERING_VALUE;
                Log.d("InputController", "Фокус переключен на значение");
            } else if (focusState == FocusState.VALUE && designationBuffer.length() > 0) {
                if (designationSubscriptModule != null) {
                    designationSubscriptModule.activate();
                    focusState = FocusState.MODULE;
                    Log.d("InputController", "Фокус переключен на индекс");
                } else {
                    focusState = FocusState.DESIGNATION;
                    currentState = InputState.ENTERING_DESIGNATION;
                    Log.d("InputController", "Фокус переключен на обозначение");
                }
            } else if (focusState == FocusState.DESIGNATION && designationSubscriptModule != null) {
                designationSubscriptModule.activate();
                focusState = FocusState.MODULE;
                Log.d("InputController", "Фокус переключен на индекс");
            }
            updateKeyboardMode();
            updateDisplay();
        } else if ("unknown".equals(currentInputField)) {
            if (focusState == FocusState.MODULE && unknownSubscriptModule != null && unknownSubscriptModule.isActive()) {
                unknownSubscriptModule.deactivate();
                focusState = FocusState.DESIGNATION;
                Log.d("InputController", "Фокус снят с индекса в 'Введите неизвестное'");
            }
            updateDisplay();
        }
    }

    public void onRightArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if (focusState == FocusState.MODULE) {
                if (designationExponentModule != null && designationExponentModule.isActive()) {
                    handleModuleNavigation(ModuleType.EXPONENT, false);
                } else if (designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                    handleModuleNavigation(ModuleType.SUBSCRIPT, false);
                }
            } else if (focusState == FocusState.DESIGNATION) {
                if (designationSubscriptModule != null) {
                    designationSubscriptModule.activate();
                    focusState = FocusState.MODULE;
                    Log.d("InputController", "Фокус переключен на индекс");
                } else if (designationBuffer.length() > 0) {
                    focusState = FocusState.VALUE;
                    currentState = InputState.ENTERING_VALUE;
                    Log.d("InputController", "Фокус переключен на значение");
                }
            } else if (focusState == FocusState.VALUE) {
                if (designationExponentModule != null) {
                    designationExponentModule.activate();
                    focusState = FocusState.MODULE;
                    Log.d("InputController", "Фокус переключен на степень");
                } else if (valueBuffer.length() > 0 || valueOperationBuffer.length() > 0) {
                    focusState = FocusState.UNIT;
                    currentState = InputState.ENTERING_UNIT;
                    Log.d("InputController", "Фокус переключен на единицу измерения");
                }
            } else if (focusState == FocusState.UNIT && designationExponentModule != null) {
                designationExponentModule.activate();
                focusState = FocusState.MODULE;
                Log.d("InputController", "Фокус переключен на степень");
            }
            updateKeyboardMode();
            updateDisplay();
        } else if ("unknown".equals(currentInputField)) {
            if (focusState == FocusState.MODULE && unknownSubscriptModule != null && unknownSubscriptModule.isActive()) {
                unknownSubscriptModule.deactivate();
                focusState = FocusState.DESIGNATION;
                Log.d("InputController", "Фокус снят с индекса в 'Введите неизвестное'");
            } else if (focusState == FocusState.DESIGNATION && unknownSubscriptModule != null) {
                unknownSubscriptModule.activate();
                focusState = FocusState.MODULE;
                Log.d("InputController", "Фокус переключен на индекс в 'Введите неизвестное'");
            }
            updateDisplay();
        }
    }

    private void handleModuleNavigation(ModuleType moduleType, boolean isLeftArrow) {
        if (moduleType == ModuleType.EXPONENT) {
            if (isLeftArrow) {
                if (designationExponentModule != null && designationExponentModule.isActive()) {
                    designationExponentModule.deactivate();
                    focusState = FocusState.VALUE;
                    Log.d("InputController", "Фокус снят со степени на значение");
                }
            } else {
                if (designationExponentModule != null && designationExponentModule.isActive()) {
                    designationExponentModule.deactivate();
                    focusState = FocusState.UNIT;
                    currentState = InputState.ENTERING_UNIT;
                    Log.d("InputController", "Фокус снят со степени на единицы измерения");
                }
            }
        } else if (moduleType == ModuleType.SUBSCRIPT) {
            if (isLeftArrow) {
                if (designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                    designationSubscriptModule.deactivate();
                    focusState = FocusState.DESIGNATION;
                    Log.d("InputController", "Фокус снят с индекса на обозначение");
                }
            } else {
                if (designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                    designationSubscriptModule.deactivate();
                    focusState = FocusState.VALUE;
                    currentState = InputState.ENTERING_VALUE;
                    Log.d("InputController", "Фокус снят с индекса на значение");
                }
            }
        }
        updateKeyboardMode();
        updateDisplay();
    }

    public void onDownArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if ((designationExponentModule != null && designationExponentModule.isActive() && designationExponentModule.isEmpty()) ||
                    (designationSubscriptModule != null && designationSubscriptModule.isActive() && designationSubscriptModule.isEmpty())) {
                Log.w("InputController", "Нельзя сохранить с пустым активным модулем");
                return;
            }
            if (designationBuffer.length() == 0) {
                Log.w("InputController", "Невозможно сохранить: отсутствует обозначение");
                return;
            }
            if (valueBuffer.length() == 0 && valueOperationBuffer.length() == 0 && designationExponentModule == null) {
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

            String exponent = (designationExponentModule != null && !designationExponentModule.isEmpty()) ? designationExponentModule.getDisplayText().toString() : "";
            String subscript = (designationSubscriptModule != null && !designationSubscriptModule.isEmpty()) ? designationSubscriptModule.getDisplayText().toString() : "";

            if (!ModuleValidator.isSubscriptUnique(logicalDesignation, subscript, measurements)) {
                Log.e("InputController", "Ошибка: индекс уже используется для обозначения: " + logicalDesignation);
                return;
            }

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

    private void updateDisplay() {
        SpannableStringBuilder designationsText = new SpannableStringBuilder();

        for (int i = 0; i < history.size(); i++) {
            designationsText.append(history.get(i));
            if (i < history.size() - 1) designationsText.append("\n\n");
        }
        if (history.size() > 0) designationsText.append("\n\n");

        if (designationBuffer.length() > 0 || valueBuffer.length() > 0 || unitBuffer.length() > 0 ||
                designationExponentModule != null || designationSubscriptModule != null) {
            int designationStart = designationsText.length();
            if (operationBuffer.length() > 0) {
                designationsText.append(operationBuffer).append("(").append(designationBuffer).append(")");
            } else {
                designationsText.append(designationBuffer);
            }
            int designationEnd = designationsText.length();
            if (designationSubscriptModule != null) {
                designationsText.append(designationSubscriptModule.getDisplayText());
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
            if (designationExponentModule != null) {
                designationsText.append(designationExponentModule.getDisplayText());
            }
            int unitStart = designationsText.length();
            if (unitBuffer.length() > 0) {
                designationsText.append(" ").append(unitBuffer);
            } else if (valueBuffer.length() > 0 || valueOperationBuffer.length() > 0) {
                designationsText.append(" ?");
            }

            if ("designations".equals(currentInputField)) {
                if (focusState == FocusState.MODULE) {
                    int moduleStart = (designationExponentModule != null && designationExponentModule.isActive()) ? valueEnd : designationEnd;
                    designationsText.setSpan(
                            new StyleSpan(Typeface.BOLD),
                            moduleStart,
                            designationsText.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                } else if (focusState == FocusState.DESIGNATION && designationStart < valueStart - 3) {
                    designationsText.setSpan(
                            new StyleSpan(Typeface.BOLD),
                            designationStart,
                            valueStart - 3,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                } else if (focusState == FocusState.VALUE && valueStart < valueEnd) {
                    designationsText.setSpan(
                            new StyleSpan(Typeface.BOLD),
                            valueStart,
                            valueEnd,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                } else if (focusState == FocusState.UNIT && unitStart < designationsText.length()) {
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

        SpannableStringBuilder unknownText = new SpannableStringBuilder();
        if (unknownDesignation != null) {
            int start = unknownText.length();
            unknownText.append(unknownDesignation);
            int end = unknownText.length();
            if (unknownUsesStix != null && unknownUsesStix && stixTypeface != null) {
                unknownText.setSpan(new CustomTypefaceSpan(stixTypeface), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (unknownSubscriptModule != null) {
                SpannableStringBuilder subscriptText = unknownSubscriptModule.getDisplayText();
                unknownText.append(subscriptText);
            }
            unknownText.append(" = ?");
            if ("unknown".equals(currentInputField) && focusState == FocusState.MODULE && unknownSubscriptModule != null && unknownSubscriptModule.isActive()) {
                int moduleStart = end;
                int moduleEnd = unknownText.length() - 4; // " = ?"
                unknownText.setSpan(new StyleSpan(Typeface.BOLD), moduleStart, moduleEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } else if (!unknowns.isEmpty()) {
            unknownText.append(unknowns.get(unknowns.size() - 1).getDisplayText(stixTypeface));
        } else {
            unknownText.append("Введите неизвестное");
            int color = "unknown".equals(currentInputField) ? Color.BLACK : Color.GRAY;
            unknownText.setSpan(new ForegroundColorSpan(color), 0, unknownText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    public void clearAll() {
        if ("designations".equals(currentInputField)) {
            designationBuffer.setLength(0);
            valueBuffer.setLength(0);
            unitBuffer.setLength(0);
            operationBuffer.setLength(0);
            valueOperationBuffer.setLength(0);
            currentState = InputState.ENTERING_DESIGNATION;
            focusState = FocusState.DESIGNATION;
            designationUsesStix = null;
            logicalDesignation = null;
            isCurrentConstant = false;
            designationExponentModule = null;
            designationSubscriptModule = null;
            history.clear();
            measurements.clear();
            Log.d("InputController", "Очищены все данные для 'Введите обозначение'");
        } else if ("unknown".equals(currentInputField)) {
            unknownDesignation = null;
            logicalUnknownDesignation = null;
            unknownUsesStix = null;
            unknownSubscriptModule = null;
            focusState = FocusState.DESIGNATION;
            unknowns.clear();
            Log.d("InputController", "Очищены все данные для 'Введите неизвестное'");
        }
        updateDisplay();
        if (keyboardModeSwitcher != null) {
            keyboardModeSwitcher.switchToDesignation();
        }
    }

    private void resetInput() {
        designationBuffer.setLength(0);
        valueBuffer.setLength(0);
        unitBuffer.setLength(0);
        operationBuffer.setLength(0);
        valueOperationBuffer.setLength(0);
        currentState = InputState.ENTERING_DESIGNATION;
        focusState = FocusState.DESIGNATION;
        designationUsesStix = null;
        logicalDesignation = null;
        isCurrentConstant = false;
        designationExponentModule = null;
        designationSubscriptModule = null;
        updateDisplay();
    }

    public List<Measurement> getMeasurements() {
        return new ArrayList<>(measurements);
    }

    public List<UnknownQuantity> getUnknowns() {
        return new ArrayList<>(unknowns);
    }

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