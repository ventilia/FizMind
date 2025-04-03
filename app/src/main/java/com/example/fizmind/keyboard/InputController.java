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
import com.example.fizmind.ConversionService;
import com.example.fizmind.PhysicalQuantity;
import com.example.fizmind.PhysicalQuantityRegistry;
import com.example.fizmind.SIConverter;
import com.example.fizmind.animation.CustomTypefaceSpan;
import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.measurement.UnknownQuantity;
import com.example.fizmind.modules.InputModule;
import com.example.fizmind.modules.ModuleType;
import com.example.fizmind.modules.ModuleValidator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// контроллер ввода для полей "Введите обозначение" и "Введите неизвестное"
public class InputController {

    // состояния ввода и фокуса
    public enum InputState {
        ENTERING_DESIGNATION, // ввод обозначения
        ENTERING_VALUE,       // ввод значения
        ENTERING_UNIT         // ввод единицы измерения
    }

    public enum FocusState {
        DESIGNATION, // фокус на обозначении
        VALUE,       // фокус на значении
        UNIT,        // фокус на единице измерения
        MODULE       // фокус на модуле (степень или индекс)
    }

    // поля
    private InputState currentState;
    private FocusState focusState;
    private final StringBuilder designationBuffer;
    private final StringBuilder valueBuffer;
    private final StringBuilder unitBuffer;
    private final StringBuilder operationBuffer;
    private final StringBuilder valueOperationBuffer;
    private final TextView designationsView;
    private final TextView unknownView;
    private final List<ConcreteMeasurement> measurements;
    private final List<SpannableStringBuilder> history;
    private final List<UnknownQuantity> unknowns;
    private Boolean designationUsesStix;
    private Boolean unknownUsesStix;
    private String logicalDesignation;
    private Typeface stixTypeface;
    private KeyboardModeSwitcher keyboardModeSwitcher;
    private boolean isCurrentConstant;
    private final Map<String, String> lastUnitForDesignation;
    private String currentInputField;
    private String unknownDesignation;
    private String logicalUnknownDesignation;
    private boolean isUnknownInputAllowed = true;
    private InputModule designationExponentModule;
    private InputModule designationSubscriptModule;
    private InputModule unknownSubscriptModule;
    private long lastDeleteTime = 0;
    private int deleteClickCount = 0;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300;
    private final ConversionService conversionService;
    private boolean isConversionMode = false;

    // конструктор
    public InputController(TextView designationsView, TextView unknownView, ConversionService conversionService) {
        this.designationsView = designationsView;
        this.unknownView = unknownView;
        this.conversionService = conversionService;
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
        Log.d("InputController", "контроллер ввода инициализирован");
    }

    // сеттеры
    public void setUnknownInputAllowed(boolean allowed) {
        this.isUnknownInputAllowed = allowed;
        Log.d("InputController", "установлено разрешение ввода неизвестного: " + allowed);
    }

    public void setStixTypeface(Typeface stixTypeface) {
        this.stixTypeface = stixTypeface;
        Log.d("InputController", "установлен шрифт STIX");
    }

    public void setKeyboardModeSwitcher(KeyboardModeSwitcher switcher) {
        this.keyboardModeSwitcher = switcher;
        Log.d("InputController", "установлен переключатель режимов клавиатуры");
    }

    public void setConversionMode(boolean isConversionMode) {
        this.isConversionMode = isConversionMode;
        Log.d("InputController", "установлен режим: " + (isConversionMode ? "перевод в СИ" : "калькулятор"));
    }

    public void setCurrentInputField(String field) {
        if ("unknown".equals(field) && !isUnknownInputAllowed) {
            Log.w("InputController", "переключение на 'Введите неизвестное' заблокировано");
            return;
        }
        if (!field.equals(currentInputField)) {
            designationExponentModule = null;
            designationSubscriptModule = null;
            unknownSubscriptModule = null;
            focusState = FocusState.DESIGNATION;
            currentState = InputState.ENTERING_DESIGNATION;
            Log.d("InputController", "сброшены модули и состояния при смене поля ввода");
        }
        this.currentInputField = field;
        if ("unknown".equals(field) && keyboardModeSwitcher != null) {
            keyboardModeSwitcher.switchToDesignation();
            Log.d("InputController", "переключен режим клавиатуры на 'Designation' для 'Введите неизвестное'");
        } else if ("designations".equals(field)) {
            updateKeyboardMode();
            Log.d("InputController", "обновлен режим клавиатуры для 'Введите обозначение'");
        }
        updateDisplay();
        Log.d("InputController", "текущее поле ввода установлено: " + field);
    }

    // возвращает текущее обозначение
    public String getCurrentDesignation() {
        return logicalDesignation;
    }

    // обработка ввода
    public void onKeyInput(String input, String sourceKeyboardMode, boolean keyUsesStix, String logicalId) {
        Log.d("InputController", "обработка ввода: состояние=" + currentState + ", ввод='" + input + "', logicalId=" + logicalId);

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
                        Log.w("InputController", "символ обозначения должен быть из режима 'Designation'");
                        return;
                    }
                    if (logicalId.equals("op_exponent") || logicalId.equals("op_subscript")) {
                        Log.w("InputController", "нельзя начинать ввод с модуля");
                        return;
                    }
                    designationBuffer.append(input);
                    logicalDesignation = logicalId;
                    designationUsesStix = keyUsesStix;
                    if (lastUnitForDesignation.containsKey(logicalDesignation) && valueBuffer.length() > 0) {
                        unitBuffer.setLength(0);
                        unitBuffer.append(lastUnitForDesignation.get(logicalDesignation));
                        Log.d("InputController", "восстановлена последняя единица измерения: " + unitBuffer);
                    } else {
                        unitBuffer.setLength(0);
                    }
                    PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
                    if (pq != null && pq.isConstant()) {
                        valueBuffer.append(String.valueOf(pq.getConstantValue()));
                        unitBuffer.append(pq.getSiUnit());
                        isCurrentConstant = true;
                        onDownArrowPressed();
                        Log.d("InputController", "автоматически заполнена константа: " + pq.getConstantValue() + " " + pq.getSiUnit());
                    } else {
                        currentState = InputState.ENTERING_VALUE;
                        focusState = FocusState.VALUE;
                        updateKeyboardMode();
                    }
                } else {
                    if (input.matches("[0-9]") || ".".equals(input) || "-".equals(input)) {
                        currentState = InputState.ENTERING_VALUE;
                        focusState = FocusState.VALUE;
                        handleValueInput(input, logicalId);
                    } else if (logicalId.equals("op_subscript")) {
                        if (!ModuleValidator.canAddModule(ModuleType.SUBSCRIPT, designationExponentModule, designationSubscriptModule)) {
                            Log.w("InputController", "нельзя добавить индекс");
                            return;
                        }
                        designationSubscriptModule = new InputModule(ModuleType.SUBSCRIPT);
                        focusState = FocusState.MODULE;
                        updateKeyboardMode();
                        updateDisplay();
                    } else if (logicalId.equals("op_exponent")) {
                        Log.w("InputController", "степень применима только к числу");
                    } else {
                        Log.w("InputController", "обозначение уже введено, ожидается число или модуль");
                    }
                }
            } else if (currentState == InputState.ENTERING_VALUE) {
                if (designationBuffer.length() == 0) {
                    Log.w("InputController", "нельзя ввести число без обозначения");
                    return;
                }
                if (logicalId.equals("op_exponent")) {
                    if (!ModuleValidator.canApplyModuleToValue(valueBuffer, valueOperationBuffer) ||
                            !ModuleValidator.canAddModule(ModuleType.EXPONENT, designationExponentModule, designationSubscriptModule)) {
                        Log.w("InputController", "нельзя добавить степень");
                        return;
                    }
                    designationExponentModule = new InputModule(ModuleType.EXPONENT);
                    focusState = FocusState.MODULE;
                    updateKeyboardMode();
                    updateDisplay();
                } else if (logicalId.equals("op_subscript")) {
                    if (!ModuleValidator.canAddModule(ModuleType.SUBSCRIPT, designationExponentModule, designationSubscriptModule)) {
                        Log.w("InputController", "нельзя добавить индекс");
                        return;
                    }
                    designationSubscriptModule = new InputModule(ModuleType.SUBSCRIPT);
                    focusState = FocusState.MODULE;
                    updateKeyboardMode();
                    updateDisplay();
                } else {
                    handleValueInput(input, logicalId);
                }
            } else if (currentState == InputState.ENTERING_UNIT) {
                if (logicalId.equals("op_subscript")) {
                    Log.w("InputController", "нельзя ввести индекс в режиме единиц");
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
                Log.w("InputController", "степень не поддерживается в 'Введите неизвестное'");
                return;
            }
            if (logicalId.equals("op_subscript")) {
                if (!ModuleValidator.canAddModule(ModuleType.SUBSCRIPT, null, unknownSubscriptModule)) {
                    Log.w("InputController", "нельзя добавить индекс в 'Введите неизвестное'");
                    return;
                }
                unknownSubscriptModule = new InputModule(ModuleType.SUBSCRIPT);
                focusState = FocusState.MODULE;
                updateKeyboardMode();
                updateDisplay();
                return;
            }
            if (unknownDesignation == null) {
                unknownDesignation = input;
                logicalUnknownDesignation = logicalId;
                unknownUsesStix = keyUsesStix;
                focusState = FocusState.DESIGNATION;
                Log.d("InputController", "введено неизвестное обозначение: " + input);
            } else {
                Log.w("InputController", "в 'Введите неизвестное' можно ввести только одно обозначение");
                return;
            }
        }
        updateDisplay();
    }

    private void handleValueInput(String input, String logicalId) {
        Log.d("InputController", "обработка значения: " + input);
        if (input.matches("[0-9]")) {
            valueBuffer.append(input);
        } else if (".".equals(input)) {
            if (valueBuffer.length() == 0 || valueBuffer.toString().equals("-")) {
                Log.w("InputController", "число не может начинаться с точки");
            } else if (valueBuffer.indexOf(".") != -1) {
                Log.w("InputController", "число уже содержит точку");
            } else {
                valueBuffer.append(input);
            }
        } else if ("-".equals(input)) {
            if (valueBuffer.length() > 0) {
                Log.w("InputController", "минус можно вводить только в начале");
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
            Log.w("InputController", "неизвестная физическая величина: " + logicalDesignation);
            return;
        }
        int maxAllowedLength = pq.getAllowedUnits().stream().mapToInt(String::length).max().orElse(0);
        String potentialUnit = unitBuffer.toString() + input;
        boolean validPrefix = pq.getAllowedUnits().stream().anyMatch(allowed -> allowed.startsWith(potentialUnit));
        if (validPrefix && potentialUnit.length() <= maxAllowedLength) {
            unitBuffer.append(input);
            Log.d("InputController", "добавлена единица измерения: " + input);
        } else {
            Log.w("InputController", "недопустимая единица измерения: " + potentialUnit);
        }
        updateDisplay();
    }

    private void saveUnknown() {
        if (unknownDesignation != null) {
            if (unknownSubscriptModule != null && unknownSubscriptModule.isActive() && unknownSubscriptModule.isEmpty()) {
                Log.w("InputController", "нельзя сохранить с пустым активным индексом");
                return;
            }
            String subscript = (unknownSubscriptModule != null && !unknownSubscriptModule.isEmpty()) ? unknownSubscriptModule.getDisplayText().toString() : "";
            UnknownQuantity unknown = new UnknownQuantity(unknownDesignation, subscript, unknownUsesStix != null && unknownUsesStix);
            if (!unknown.validate()) {
                Log.e("InputController", "ошибка валидации неизвестного: " + unknown.toString());
                return;
            }
            unknowns.add(unknown);
            Log.d("InputController", "сохранено неизвестное: " + unknown.toString());
            unknownDesignation = null;
            logicalUnknownDesignation = null;
            unknownUsesStix = null;
            unknownSubscriptModule = null;
            focusState = FocusState.DESIGNATION;
            updateDisplay();
        }
    }

    private boolean isInputEmpty() {
        if ("designations".equals(currentInputField)) {
            return designationBuffer.length() == 0 && valueBuffer.length() == 0 && unitBuffer.length() == 0 &&
                    operationBuffer.length() == 0 && valueOperationBuffer.length() == 0 &&
                    designationExponentModule == null && designationSubscriptModule == null;
        } else if ("unknown".equals(currentInputField)) {
            return unknownDesignation == null && unknownSubscriptModule == null;
        }
        return true;
    }

    // обработка удаления
    public void onDeletePressed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDeleteTime < DOUBLE_CLICK_TIME_DELTA) {
            deleteClickCount++;
        } else {
            deleteClickCount = 1;
        }
        lastDeleteTime = currentTime;

        if (deleteClickCount == 2) {
            deleteLastSavedField();
            deleteClickCount = 0;
            Log.d("InputController", "выполнено двойное удаление последнего сохраненного поля");
        } else {
            if (isInputEmpty() && !measurements.isEmpty()) {
                ConcreteMeasurement lastMeasurement = measurements.get(measurements.size() - 1);
                if (lastMeasurement.isConstant()) {
                    deleteLastSavedField();
                } else {
                    performSingleDelete();
                }
            } else {
                performSingleDelete();
            }
        }
        updateKeyboardMode();
        updateDisplay();
    }

    private void performSingleDelete() {
        if ("designations".equals(currentInputField)) {
            if (focusState == FocusState.MODULE) {
                if (designationExponentModule != null && designationExponentModule.isActive()) {
                    if (designationExponentModule.delete()) {
                        designationExponentModule = null;
                        focusState = FocusState.VALUE;
                        Log.d("InputController", "степень удалена");
                    }
                } else if (designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                    if (designationSubscriptModule.delete()) {
                        designationSubscriptModule = null;
                        focusState = FocusState.DESIGNATION;
                        Log.d("InputController", "индекс удален");
                    }
                }
                return;
            }
            if (currentState == InputState.ENTERING_UNIT) {
                unitBuffer.setLength(0);
                currentState = InputState.ENTERING_VALUE;
                focusState = FocusState.VALUE;
                Log.d("InputController", "единицы измерения удалены");
            } else if (currentState == InputState.ENTERING_VALUE) {
                if (valueBuffer.length() > 0) {
                    valueBuffer.deleteCharAt(valueBuffer.length() - 1);
                    Log.d("InputController", "удален символ из значения");
                } else if (valueOperationBuffer.length() > 0) {
                    valueOperationBuffer.deleteCharAt(valueOperationBuffer.length() - 1);
                    Log.d("InputController", "удален символ из операции");
                } else if (designationBuffer.length() > 0) {
                    resetInput();
                    Log.d("InputController", "обозначение удалено");
                }
            } else if (currentState == InputState.ENTERING_DESIGNATION && designationBuffer.length() > 0) {
                resetInput();
                Log.d("InputController", "обозначение удалено");
            }
        } else if ("unknown".equals(currentInputField)) {
            if (focusState == FocusState.MODULE && unknownSubscriptModule != null && unknownSubscriptModule.isActive()) {
                if (unknownSubscriptModule.delete()) {
                    unknownSubscriptModule = null;
                    focusState = FocusState.DESIGNATION;
                    Log.d("InputController", "индекс удален в 'Введите неизвестное'");
                }
            } else if (unknownDesignation != null) {
                unknownDesignation = null;
                logicalUnknownDesignation = null;
                unknownUsesStix = null;
                unknownSubscriptModule = null;
                focusState = FocusState.DESIGNATION;
                Log.d("InputController", "неизвестное обозначение удалено");
            }
        }
    }

    private void deleteLastSavedField() {
        if ("designations".equals(currentInputField)) {
            if (!measurements.isEmpty()) {
                measurements.remove(measurements.size() - 1);
                history.remove(history.size() - 1);
                Log.d("InputController", "удалено последнее измерение");
            }
        } else if ("unknown".equals(currentInputField)) {
            if (!unknowns.isEmpty()) {
                unknowns.remove(unknowns.size() - 1);
                Log.d("InputController", "удалено последнее неизвестное");
            }
        }
    }

    // навигация стрелками
    public void onLeftArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if (focusState == FocusState.MODULE) {
                if (designationExponentModule != null && designationExponentModule.isActive()) {
                    designationExponentModule.deactivate();
                    focusState = FocusState.VALUE;
                    Log.d("InputController", "фокус снят со степени на значение");
                } else if (designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                    designationSubscriptModule.deactivate();
                    focusState = FocusState.DESIGNATION;
                    Log.d("InputController", "фокус снят с индекса на обозначение");
                }
            } else if (focusState == FocusState.UNIT) {
                if (designationExponentModule != null) {
                    designationExponentModule.activate();
                    focusState = FocusState.MODULE;
                    Log.d("InputController", "фокус переключен с единицы измерения на степень");
                } else {
                    focusState = FocusState.VALUE;
                    currentState = InputState.ENTERING_VALUE;
                    Log.d("InputController", "фокус переключен с единицы измерения на значение");
                }
            } else if (focusState == FocusState.VALUE && designationBuffer.length() > 0) {
                if (designationSubscriptModule != null) {
                    designationSubscriptModule.activate();
                    focusState = FocusState.MODULE;
                    Log.d("InputController", "фокус переключен на индекс");
                } else {
                    focusState = FocusState.DESIGNATION;
                    currentState = InputState.ENTERING_DESIGNATION;
                    Log.d("InputController", "фокус переключен на обозначение");
                }
            }
            updateKeyboardMode();
            updateDisplay();
        } else if ("unknown".equals(currentInputField)) {
            if (focusState == FocusState.MODULE && unknownSubscriptModule != null && unknownSubscriptModule.isActive()) {
                unknownSubscriptModule.deactivate();
                focusState = FocusState.DESIGNATION;
                Log.d("InputController", "фокус снят с индекса в 'Введите неизвестное'");
            }
            updateDisplay();
        }
    }

    public void onRightArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if (focusState == FocusState.MODULE) {
                if (designationExponentModule != null && designationExponentModule.isActive()) {
                    designationExponentModule.deactivate();
                    focusState = FocusState.UNIT;
                    currentState = InputState.ENTERING_UNIT;
                    Log.d("InputController", "фокус снят со степени на единицы измерения");
                } else if (designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                    designationSubscriptModule.deactivate();
                    focusState = FocusState.VALUE;
                    currentState = InputState.ENTERING_VALUE;
                    Log.d("InputController", "фокус снят с индекса на значение");
                }
            } else if (focusState == FocusState.DESIGNATION) {
                if (designationSubscriptModule != null) {
                    designationSubscriptModule.activate();
                    focusState = FocusState.MODULE;
                    Log.d("InputController", "фокус переключен на индекс");
                } else if (designationBuffer.length() > 0) {
                    focusState = FocusState.VALUE;
                    currentState = InputState.ENTERING_VALUE;
                    Log.d("InputController", "фокус переключен на значение");
                }
            } else if (focusState == FocusState.VALUE) {
                if (designationExponentModule != null) {
                    designationExponentModule.activate();
                    focusState = FocusState.MODULE;
                    Log.d("InputController", "фокус переключен на степень из значения");
                } else if (valueBuffer.length() > 0 || valueOperationBuffer.length() > 0) {
                    focusState = FocusState.UNIT;
                    currentState = InputState.ENTERING_UNIT;
                    Log.d("InputController", "фокус переключен на единицу измерения");
                }
            }
            updateKeyboardMode();
            updateDisplay();
        } else if ("unknown".equals(currentInputField)) {
            if (focusState == FocusState.DESIGNATION && unknownSubscriptModule != null) {
                unknownSubscriptModule.activate();
                focusState = FocusState.MODULE;
                Log.d("InputController", "фокус переключен на индекс в 'Введите неизвестное'");
            }
            updateDisplay();
        }
    }

    // сохранение ввода
    public void onDownArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if ((designationExponentModule != null && designationExponentModule.isActive() && designationExponentModule.isEmpty()) ||
                    (designationSubscriptModule != null && designationSubscriptModule.isActive() && designationSubscriptModule.isEmpty())) {
                Log.w("InputController", "нельзя сохранить с пустым активным модулем");
                return;
            }
            if (designationBuffer.length() == 0) {
                Log.w("InputController", "нельзя сохранить: отсутствует обозначение");
                return;
            }
            if (valueBuffer.length() == 0 && valueOperationBuffer.length() == 0 && designationExponentModule == null) {
                Log.w("InputController", "нельзя сохранить: отсутствует числовое значение");
                return;
            }

            String unit = unitBuffer.toString();
            if (unit.isEmpty()) {
                PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
                if (pq == null) {
                    Log.e("InputController", "неизвестная физическая величина: " + logicalDesignation);
                    return;
                }
                unit = pq.getSiUnit();
                if (unit.isEmpty()) {
                    Log.w("InputController", "нельзя сохранить: отсутствует единица измерения");
                    return;
                }
            }

            double value;
            try {
                value = Double.parseDouble(valueBuffer.length() > 0 ? valueBuffer.toString() : "0");
            } catch (NumberFormatException e) {
                Log.e("InputController", "ошибка формата числа: " + valueBuffer.toString(), e);
                return;
            }

            String exponent = (designationExponentModule != null && !designationExponentModule.isEmpty()) ? designationExponentModule.getDisplayText().toString() : "";
            String subscript = (designationSubscriptModule != null && !designationSubscriptModule.isEmpty()) ? designationSubscriptModule.getDisplayText().toString() : "";

            if (!ModuleValidator.isSubscriptUnique(logicalDesignation, subscript, measurements)) {
                Log.e("InputController", "ошибка: индекс уже используется для обозначения: " + logicalDesignation);
                return;
            }

            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
            if (pq == null) {
                Log.e("InputController", "неизвестная физическая величина: " + logicalDesignation);
                return;
            }

            double siValue = value;
            String siUnit = unit;
            SpannableStringBuilder historyEntry;

            if (isConversionMode) {
                Object[] siData = conversionService.convert(pq, value, unit);
                if (siData == null) {
                    Log.e("InputController", "ошибка конвертации для " + logicalDesignation + " с единицей " + unit);
                    return;
                }
                siValue = (double) siData[0];
                siUnit = (String) siData[1];
                String steps = conversionService.getSteps(designationBuffer.toString(), pq, value, unit);
                if (!steps.isEmpty()) {
                    // шаги перевода есть, используем их
                    historyEntry = new SpannableStringBuilder(steps);
                    // применяем шрифт STIX к обозначению
                    if (designationUsesStix != null && designationUsesStix && stixTypeface != null) {
                        int designationIndex = steps.indexOf(designationBuffer.toString());
                        if (designationIndex != -1) {
                            int designationEnd = designationIndex + designationBuffer.length();
                            historyEntry.setSpan(
                                    new CustomTypefaceSpan(stixTypeface),
                                    designationIndex,
                                    designationEnd,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            );
                        }
                    }
                    // выделяем итоговое значение жирным шрифтом
                    int lastEqualIndex = steps.lastIndexOf("=");
                    if (lastEqualIndex != -1) {
                        int start = lastEqualIndex + 1;
                        while (start < steps.length() && Character.isWhitespace(steps.charAt(start))) {
                            start++;
                        }
                        if (start < steps.length()) {
                            historyEntry.setSpan(
                                    new StyleSpan(Typeface.BOLD),
                                    start,
                                    steps.length(),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            );
                        }
                    }
                } else {
                    // единица уже в СИ, отображаем только значение без шагов
                    historyEntry = new SpannableStringBuilder();
                    int start = historyEntry.length();
                    if (operationBuffer.length() > 0) {
                        historyEntry.append(operationBuffer).append("(").append(designationBuffer).append(")");
                    } else {
                        historyEntry.append(designationBuffer);
                    }
                    int designationEnd = historyEntry.length();
                    if (subscript != null && !subscript.isEmpty()) {
                        int subscriptStart = historyEntry.length();
                        historyEntry.append(subscript);
                        int subscriptEnd = historyEntry.length();
                        historyEntry.setSpan(new SubscriptSpan(), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        historyEntry.setSpan(new RelativeSizeSpan(0.75f), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    if (designationUsesStix != null && designationUsesStix && stixTypeface != null) {
                        historyEntry.setSpan(new CustomTypefaceSpan(stixTypeface), start, designationEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    historyEntry.append(" = ");
                    String valueStr = SIConverter.formatValue(value);
                    if (valueOperationBuffer.length() > 0) {
                        historyEntry.append(valueOperationBuffer);
                    } else {
                        historyEntry.append(valueStr);
                    }
                    if (!unit.isEmpty()) {
                        historyEntry.append(" ").append(unit);
                    }
                    // выделяем итоговое значение жирным шрифтом
                    int equalIndex = historyEntry.toString().indexOf("=");
                    if (equalIndex != -1) {
                        int startBold = equalIndex + 1;
                        while (startBold < historyEntry.length() && Character.isWhitespace(historyEntry.charAt(startBold))) {
                            startBold++;
                        }
                        historyEntry.setSpan(
                                new StyleSpan(Typeface.BOLD),
                                startBold,
                                historyEntry.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                    }
                }
            } else {
                // режим калькулятора, без перевода в СИ
                historyEntry = new SpannableStringBuilder();
                int start = historyEntry.length();
                if (operationBuffer.length() > 0) {
                    historyEntry.append(operationBuffer).append("(").append(designationBuffer).append(")");
                } else {
                    historyEntry.append(designationBuffer);
                }
                int designationEnd = historyEntry.length();
                if (subscript != null && !subscript.isEmpty()) {
                    int subscriptStart = historyEntry.length();
                    historyEntry.append(subscript);
                    int subscriptEnd = historyEntry.length();
                    historyEntry.setSpan(new SubscriptSpan(), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    historyEntry.setSpan(new RelativeSizeSpan(0.75f), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (designationUsesStix != null && designationUsesStix && stixTypeface != null) {
                    historyEntry.setSpan(new CustomTypefaceSpan(stixTypeface), start, designationEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                historyEntry.append(" = ");
                String valueStr = SIConverter.formatValue(value);
                if (valueOperationBuffer.length() > 0) {
                    historyEntry.append(valueOperationBuffer);
                } else {
                    historyEntry.append(valueStr);
                }
                if (!unit.isEmpty()) {
                    historyEntry.append(" ").append(unit);
                }
            }

            ConcreteMeasurement measurement = new ConcreteMeasurement(
                    logicalDesignation, siValue, siUnit,
                    operationBuffer.toString(), valueOperationBuffer.toString(),
                    exponent, subscript, isCurrentConstant, historyEntry);
            if (!measurement.validate()) {
                Log.e("InputController", "ошибка валидации измерения: " + measurement.toString());
                return;
            }

            measurements.add(measurement);
            history.add(historyEntry);
            Log.d("InputController", "сохранено измерение: " + measurement.toString());

            if (!unit.isEmpty()) {
                lastUnitForDesignation.put(logicalDesignation, unit);
                Log.d("InputController", "сохранена последняя единица измерения для " + logicalDesignation + ": " + unit);
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

    // обновление интерфейса и состояния
    private void updateKeyboardMode() {
        if (keyboardModeSwitcher != null && "designations".equals(currentInputField)) {
            if (focusState == FocusState.MODULE) {
                keyboardModeSwitcher.switchToNumbersAndOperations();
                Log.d("InputController", "переключен режим клавиатуры на 'Numbers_and_operations' для модуля");
            } else if (currentState == InputState.ENTERING_DESIGNATION) {
                keyboardModeSwitcher.switchToDesignation();
                Log.d("InputController", "переключен режим клавиатуры на 'Designation'");
            } else if (currentState == InputState.ENTERING_VALUE) {
                keyboardModeSwitcher.switchToNumbersAndOperations();
                Log.d("InputController", "переключен режим клавиатуры на 'Numbers_and_operations' для значения");
            } else if (currentState == InputState.ENTERING_UNIT) {
                keyboardModeSwitcher.switchToUnits();
                Log.d("InputController", "переключен режим клавиатуры на 'Units_of_measurement'");
            }
        }
    }

    private void updateDisplay() {
        SpannableStringBuilder designationsText = new SpannableStringBuilder();

        // отображение сохранённых измерений с использованием оригинального представления
        for (int i = 0; i < measurements.size(); i++) {
            ConcreteMeasurement measurement = measurements.get(i);
            if (i < history.size()) {
                designationsText.append(history.get(i));
            } else {
                designationsText.append(measurement.getOriginalDisplay());
            }
            if (i < measurements.size() - 1) designationsText.append("\n\n");
        }
        if (measurements.size() > 0) designationsText.append("\n\n");

        // отображение текущего ввода
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
            int exponentStart = designationsText.length();
            if (designationExponentModule != null) {
                designationsText.append(designationExponentModule.getDisplayText());
            }
            int exponentEnd = designationsText.length();
            int unitStart = designationsText.length();
            if (unitBuffer.length() > 0) {
                designationsText.append(" ").append(unitBuffer);
            } else if (valueBuffer.length() > 0 || valueOperationBuffer.length() > 0) {
                designationsText.append(" ?");
            }

            if ("designations".equals(currentInputField)) {
                if (focusState == FocusState.MODULE) {
                    int moduleStart;
                    int moduleEnd;
                    if (designationExponentModule != null && designationExponentModule.isActive()) {
                        moduleStart = exponentStart;
                        moduleEnd = exponentEnd;
                    } else if (designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                        moduleStart = designationEnd;
                        moduleEnd = moduleStart + designationSubscriptModule.getDisplayText().length();
                    } else {
                        moduleStart = moduleEnd = 0;
                    }
                    if (moduleStart < moduleEnd) {
                        designationsText.setSpan(
                                new StyleSpan(Typeface.BOLD),
                                moduleStart,
                                moduleEnd,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                    }
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
                unknownText.append(unknownSubscriptModule.getDisplayText());
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
        Log.d("InputController", "обновлен интерфейс отображения");
    }

    public void clearAll() {
        if ("designations".equals(currentInputField)) {
            resetInput();
            history.clear();
            measurements.clear();
            Log.d("InputController", "очищены все данные для 'Введите обозначение'");
        } else if ("unknown".equals(currentInputField)) {
            unknownDesignation = null;
            logicalUnknownDesignation = null;
            unknownUsesStix = null;
            unknownSubscriptModule = null;
            focusState = FocusState.DESIGNATION;
            unknowns.clear();
            Log.d("InputController", "очищены все данные для 'Введите неизвестное'");
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
        updateKeyboardMode();
        updateDisplay();
        Log.d("InputController", "сброшены все буферы ввода");
    }

    // геттеры
    public List<ConcreteMeasurement> getMeasurements() {
        return new ArrayList<>(measurements);
    }

    public List<UnknownQuantity> getUnknowns() {
        return new ArrayList<>(unknowns);
    }

    // логирование сохраненных данных
    public void logAllSavedData() {
        StringBuilder logMessage = new StringBuilder("все сохраненные данные:\n");

        logMessage.append("измерения ('Введите обозначение'):\n");
        if (measurements.isEmpty()) {
            logMessage.append("  нет сохраненных измерений\n");
        } else {
            for (ConcreteMeasurement m : measurements) {
                logMessage.append("  ").append(m.toString()).append("\n");
            }
        }

        logMessage.append("неизвестные ('Введите неизвестное'):\n");
        if (unknowns.isEmpty()) {
            logMessage.append("  нет сохраненных неизвестных\n");
        } else {
            for (UnknownQuantity u : unknowns) {
                logMessage.append("  ").append(u.toString()).append("\n");
            }
        }

        Log.d("InputController", logMessage.toString());
    }
}