package com.example.fizmind.keyboard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.view.View;
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
import com.example.fizmind.utils.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер ввода данных для управления обозначениями и неизвестными
 */
public class InputController {

    /** Перечисления для состояния ввода */
    public enum InputState {
        ENTERING_DESIGNATION,  // Ввод обозначения
        ENTERING_VALUE,        // Ввод значения
        ENTERING_UNIT          // Ввод единицы измерения
    }

    /** Перечисления для состояния фокуса */
    public enum FocusState {
        DESIGNATION,  // Фокус на обозначении
        VALUE,        // Фокус на значении
        UNIT,         // Фокус на единице измерения
        MODULE        // Фокус на модуле (индексе)
    }

    // Поля класса
    private InputState currentState;                    // Текущее состояние ввода
    private FocusState focusState;                      // Текущее состояние фокуса
    private final StringBuilder designationBuffer;      // Буфер для обозначения
    private final StringBuilder valueBuffer;            // Буфер для значения
    private final StringBuilder unitBuffer;             // Буфер для единицы измерения
    private final StringBuilder operationBuffer;        // Буфер для операций над обозначением
    private final StringBuilder valueOperationBuffer;   // Буфер для операций над значением
    private final TextView designationsView;            // Поле отображения обозначений
    private final TextView unknownView;                 // Поле отображения неизвестных
    private final View rootView;                        // Корневой вид для Snackbar
    private final List<ConcreteMeasurement> measurements; // Список сохраненных измерений
    private final List<SpannableStringBuilder> history;   // История отображения измерений
    private final List<UnknownQuantity> unknowns;         // Список неизвестных величин
    private Boolean designationUsesStix;                // Используется ли шрифт STIX для обозначения
    private Boolean unknownUsesStix;                    // Используется ли шрифт STIX для неизвестного
    private String logicalDesignation;                  // Логический идентификатор обозначения
    private Typeface stixTypeface;                      // Шрифт STIX
    private KeyboardModeSwitcher keyboardModeSwitcher;  // Переключатель режимов клавиатуры
    private boolean isCurrentConstant;                  // Является ли текущее значение константой
    private final Map<String, String> lastUnitForDesignation; // Последние единицы измерения для обозначений
    private String currentInputField;                   // Текущее поле ввода ("designations" или "unknown")
    private String unknownDesignation;                  // Обозначение неизвестного
    private String logicalUnknownDesignation;           // Логический идентификатор неизвестного
    private boolean isUnknownInputAllowed = true;       // Разрешён ли ввод неизвестного
    private InputModule designationSubscriptModule;     // Модуль нижнего индекса для обозначения
    private InputModule unknownSubscriptModule;         // Модуль нижнего индекса для неизвестного
    private long lastDeleteTime = 0;                    // Время последнего удаления
    private int deleteClickCount = 0;                   // Счётчик кликов удаления
    private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Интервал для двойного клика (мс)
    private final ConversionService conversionService;  // Сервис конвертации в СИ
    private boolean isConversionMode = false;           // Режим конвертации в СИ

    /**
     * Конструктор класса
     * @param designationsView Поле для отображения обозначений
     * @param unknownView Поле для отображения неизвестных
     * @param conversionService Сервис конвертации в СИ
     * @param rootView Корневой вид для Snackbar
     */
    public InputController(TextView designationsView, TextView unknownView, ConversionService conversionService, View rootView) {
        this.designationsView = designationsView;
        this.unknownView = unknownView;
        this.rootView = rootView;
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
        this.designationSubscriptModule = null;
        this.unknownSubscriptModule = null;
        updateDisplay();
        LogUtils.logControllerInitialized("InputController");
    }

    /** Установка разрешения ввода неизвестного */
    public void setUnknownInputAllowed(boolean allowed) {
        this.isUnknownInputAllowed = allowed;
        LogUtils.logPropertySet("InputController", "разрешение ввода неизвестного", allowed);
    }

    /** Установка шрифта STIX */
    public void setStixTypeface(Typeface stixTypeface) {
        this.stixTypeface = stixTypeface;
        LogUtils.logPropertySet("InputController", "шрифт STIX", "установлен");
    }

    /** Установка переключателя режимов клавиатуры */
    public void setKeyboardModeSwitcher(KeyboardModeSwitcher switcher) {
        this.keyboardModeSwitcher = switcher;
        LogUtils.logPropertySet("InputController", "переключатель режимов клавиатуры", "установлен");
    }

    /** Установка режима конвертации в СИ */
    public void setConversionMode(boolean isConversionMode) {
        this.isConversionMode = isConversionMode;
        LogUtils.logPropertySet("InputController", "режим", isConversionMode ? "перевод в СИ" : "калькулятор");
    }

    /** Установка текущего поля ввода */
    public void setCurrentInputField(String field) {
        if ("unknown".equals(field) && !isUnknownInputAllowed) {
            LogUtils.wWithSnackbar("InputController", "переключение на 'Введите неизвестное' заблокировано", rootView);
            return;
        }
        if (!field.equals(currentInputField)) {
            designationSubscriptModule = null;
            unknownSubscriptModule = null;
            focusState = FocusState.DESIGNATION;
            currentState = InputState.ENTERING_DESIGNATION;
            LogUtils.d("InputController", "сброшены модули и состояния при смене поля ввода");
        }
        this.currentInputField = field;
        if ("unknown".equals(field) && keyboardModeSwitcher != null) {
            keyboardModeSwitcher.switchToDesignation();
            LogUtils.d("InputController", "переключен режим клавиатуры на 'Designation' для 'Введите неизвестное'");
        } else if ("designations".equals(field)) {
            updateKeyboardMode();
            LogUtils.d("InputController", "обновлен режим клавиатуры для 'Введите обозначение'");
        }
        updateDisplay();
        LogUtils.d("InputController", "текущее поле ввода установлено: " + field);
    }

    /** Получение текущего обозначения */
    public String getCurrentDesignation() {
        return logicalDesignation;
    }

    /**
     * Обработка ввода с клавиатуры
     * @param input Введённый символ
     * @param sourceKeyboardMode Режим клавиатуры
     * @param keyUsesStix Используется ли шрифт STIX
     * @param logicalId Логический идентификатор
     */
    public void onKeyInput(String input, String sourceKeyboardMode, boolean keyUsesStix, String logicalId) {
        LogUtils.logInputProcessing("InputController", currentState.toString(), focusState.toString(), input, logicalId, isConversionMode ? "СИ" : "калькулятор");

        if ("designations".equals(currentInputField)) {
            if (focusState == FocusState.MODULE && designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                if (!designationSubscriptModule.apply(input)) {
                    LogUtils.wWithSnackbar("InputController", "недопустимый символ для индекса: " + input, rootView);
                }
                updateDisplay();
                return;
            }

            if (currentState == InputState.ENTERING_DESIGNATION) {
                if (designationBuffer.length() == 0) {
                    if (!"Designation".equals(sourceKeyboardMode)) {
                        LogUtils.wWithSnackbar("InputController", "символ обозначения должен быть из режима 'Designation'", rootView);
                        return;
                    }
                    if (logicalId.equals("op_subscript")) {
                        LogUtils.wWithSnackbar("InputController", "нельзя начинать ввод с индекса", rootView);
                        return;
                    }
                    designationBuffer.append(input);
                    logicalDesignation = logicalId;
                    designationUsesStix = keyUsesStix;
                    if (lastUnitForDesignation.containsKey(logicalDesignation) && valueBuffer.length() > 0) {
                        unitBuffer.setLength(0);
                        unitBuffer.append(lastUnitForDesignation.get(logicalDesignation));
                        LogUtils.d("InputController", "восстановлена последняя единица измерения: " + unitBuffer);
                    } else {
                        unitBuffer.setLength(0);
                    }
                    PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
                    if (pq != null && pq.isConstant()) {
                        valueBuffer.append(String.valueOf(pq.getConstantValue()));
                        unitBuffer.append(pq.getSiUnit());
                        isCurrentConstant = true;
                        onDownArrowPressed();
                        LogUtils.d("InputController", "автоматически заполнена константа: " + pq.getConstantValue() + " " + pq.getSiUnit());
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
                        if (!ModuleValidator.canAddModule(ModuleType.SUBSCRIPT, designationSubscriptModule)) {
                            LogUtils.wWithSnackbar("InputController", "нельзя добавить индекс", rootView);
                            return;
                        }
                        designationSubscriptModule = new InputModule(ModuleType.SUBSCRIPT);
                        focusState = FocusState.MODULE;
                        updateKeyboardMode();
                        updateDisplay();
                    } else {
                        LogUtils.wWithSnackbar("InputController", "обозначение уже введено, ожидается число или индекс", rootView);
                    }
                }
            } else if (currentState == InputState.ENTERING_VALUE) {
                if (designationBuffer.length() == 0) {
                    LogUtils.wWithSnackbar("InputController", "нельзя ввести число без обозначения", rootView);
                    return;
                }
                if (logicalId.equals("op_subscript")) {
                    if (!ModuleValidator.canAddModule(ModuleType.SUBSCRIPT, designationSubscriptModule)) {
                        LogUtils.wWithSnackbar("InputController", "нельзя добавить индекс", rootView);
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
                    LogUtils.wWithSnackbar("InputController", "нельзя ввести индекс в режиме единиц", rootView);
                    return;
                }
                handleUnitInput(input, logicalId);
            }
        } else if ("unknown".equals(currentInputField)) {
            if (focusState == FocusState.MODULE && unknownSubscriptModule != null && unknownSubscriptModule.isActive()) {
                if (!unknownSubscriptModule.apply(input)) {
                    LogUtils.wWithSnackbar("InputController", "недопустимый символ для индекса: " + input, rootView);
                }
                updateDisplay();
                return;
            }
            if (logicalId.equals("op_subscript")) {
                if (!ModuleValidator.canAddModule(ModuleType.SUBSCRIPT, unknownSubscriptModule)) {
                    LogUtils.wWithSnackbar("InputController", "нельзя добавить индекс в 'Введите неизвестное'", rootView);
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
                LogUtils.d("InputController", "введено неизвестное обозначение: " + input);
            } else {
                LogUtils.wWithSnackbar("InputController", "в 'Введите неизвестное' можно ввести только одно обозначение", rootView);
                return;
            }
        }
        updateDisplay();
    }

    /** Обработка ввода значения */
    private void handleValueInput(String input, String logicalId) {
        LogUtils.d("InputController", "обработка значения: " + input);
        if (input.matches("[0-9]")) {
            valueBuffer.append(input);
        } else if (".".equals(input)) {
            if (valueBuffer.length() == 0 || valueBuffer.toString().equals("-")) {
                LogUtils.wWithSnackbar("InputController", "число не может начинаться с точки", rootView);
            } else if (valueBuffer.indexOf(".") != -1) {
                LogUtils.wWithSnackbar("InputController", "число уже содержит точку", rootView);
            } else {
                valueBuffer.append(input);
            }
        } else if ("-".equals(input)) {
            if (valueBuffer.length() > 0) {
                LogUtils.wWithSnackbar("InputController", "минус можно вводить только в начале", rootView);
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

    /** Обработка ввода единицы измерения */
    private void handleUnitInput(String input, String logicalId) {
        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
        if (pq == null) {
            LogUtils.wWithSnackbar("InputController", "неизвестная физическая величина: " + logicalDesignation, rootView);
            return;
        }
        int maxAllowedLength = pq.getAllowedUnits().stream().mapToInt(String::length).max().orElse(0);
        String potentialUnit = unitBuffer.toString() + input;
        boolean validPrefix = pq.getAllowedUnits().stream().anyMatch(allowed -> allowed.startsWith(potentialUnit));
        if (validPrefix && potentialUnit.length() <= maxAllowedLength) {
            unitBuffer.append(input);
            LogUtils.d("InputController", "добавлена единица измерения: " + input);
        } else {
            LogUtils.wWithSnackbar("InputController", "недопустимая единица измерения: " + potentialUnit, rootView);
        }
        updateDisplay();
    }

    /** Сохранение неизвестной величины */
    private void saveUnknown() {
        if (unknownDesignation != null) {
            if (unknownSubscriptModule != null && unknownSubscriptModule.isActive() && unknownSubscriptModule.isEmpty()) {
                LogUtils.wWithSnackbar("InputController", "нельзя сохранить с пустым активным индексом", rootView);
                return;
            }
            String subscript = (unknownSubscriptModule != null && !unknownSubscriptModule.isEmpty()) ? unknownSubscriptModule.getDisplayText().toString() : "";
            UnknownQuantity unknown = new UnknownQuantity(unknownDesignation, subscript, unknownUsesStix != null && unknownUsesStix);
            if (!unknown.validate()) {
                LogUtils.logValidationError("InputController", unknown.toString());
                return;
            }
            unknowns.add(unknown);
            LogUtils.logSaveUnknown("InputController", unknown.toString());
            unknownDesignation = null;
            logicalUnknownDesignation = null;
            unknownUsesStix = null;
            unknownSubscriptModule = null;
            focusState = FocusState.DESIGNATION;
            updateDisplay();
        }
    }

    /** Проверка, пустой ли ввод */
    private boolean isInputEmpty() {
        if ("designations".equals(currentInputField)) {
            return designationBuffer.length() == 0 && valueBuffer.length() == 0 && unitBuffer.length() == 0 &&
                    operationBuffer.length() == 0 && valueOperationBuffer.length() == 0 &&
                    designationSubscriptModule == null;
        } else if ("unknown".equals(currentInputField)) {
            return unknownDesignation == null && unknownSubscriptModule == null;
        }
        return true;
    }

    /** Обработка нажатия клавиши удаления */
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
            LogUtils.logDeletion("InputController", "выполнено двойное удаление последнего сохраненного поля");
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

    /** Выполнение одиночного удаления */
    private void performSingleDelete() {
        if ("designations".equals(currentInputField)) {
            if (focusState == FocusState.MODULE && designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                if (designationSubscriptModule.delete()) {
                    designationSubscriptModule = null;
                    focusState = FocusState.DESIGNATION;
                    LogUtils.d("InputController", "индекс удален");
                }
            } else if (currentState == InputState.ENTERING_UNIT) {
                unitBuffer.setLength(0);
                currentState = InputState.ENTERING_VALUE;
                focusState = FocusState.VALUE;
                LogUtils.d("InputController", "единицы измерения удалены");
            } else if (currentState == InputState.ENTERING_VALUE) {
                if (valueBuffer.length() > 0) {
                    valueBuffer.deleteCharAt(valueBuffer.length() - 1);
                    LogUtils.d("InputController", "удален символ из значения");
                } else if (valueOperationBuffer.length() > 0) {
                    valueOperationBuffer.deleteCharAt(valueOperationBuffer.length() - 1);
                    LogUtils.d("InputController", "удален символ из операции");
                } else if (designationBuffer.length() > 0) {
                    resetInput();
                    LogUtils.d("InputController", "обозначение удалено");
                }
            } else if (currentState == InputState.ENTERING_DESIGNATION && designationBuffer.length() > 0) {
                resetInput();
                LogUtils.d("InputController", "обозначение удалено");
            }
        } else if ("unknown".equals(currentInputField)) {
            if (focusState == FocusState.MODULE && unknownSubscriptModule != null && unknownSubscriptModule.isActive()) {
                if (unknownSubscriptModule.delete()) {
                    unknownSubscriptModule = null;
                    focusState = FocusState.DESIGNATION;
                    LogUtils.d("InputController", "индекс удален в 'Введите неизвестное'");
                }
            } else if (unknownDesignation != null) {
                unknownDesignation = null;
                logicalUnknownDesignation = null;
                unknownUsesStix = null;
                unknownSubscriptModule = null;
                focusState = FocusState.DESIGNATION;
                LogUtils.d("InputController", "неизвестное обозначение удалено");
            }
        }
    }

    /** Удаление последнего сохраненного поля */
    private void deleteLastSavedField() {
        if ("designations".equals(currentInputField)) {
            if (!measurements.isEmpty()) {
                measurements.remove(measurements.size() - 1);
                history.remove(history.size() - 1);
                LogUtils.d("InputController", "удалено последнее измерение");
            }
        } else if ("unknown".equals(currentInputField)) {
            if (!unknowns.isEmpty()) {
                unknowns.remove(unknowns.size() - 1);
                LogUtils.d("InputController", "удалено последнее неизвестное");
            }
        }
    }

    /** Обработка нажатия стрелки влево */
    public void onLeftArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if (focusState == FocusState.MODULE && designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                designationSubscriptModule.deactivate();
                focusState = FocusState.DESIGNATION;
                LogUtils.d("InputController", "фокус снят с индекса на обозначение");
            } else if (focusState == FocusState.UNIT) {
                focusState = FocusState.VALUE;
                currentState = InputState.ENTERING_VALUE;
                LogUtils.d("InputController", "фокус переключен с единицы измерения на значение");
            } else if (focusState == FocusState.VALUE && designationBuffer.length() > 0) {
                focusState = FocusState.DESIGNATION;
                currentState = InputState.ENTERING_DESIGNATION;
                LogUtils.d("InputController", "фокус переключен на обозначение");
            }
            updateKeyboardMode();
            updateDisplay();
        } else if ("unknown".equals(currentInputField)) {
            if (focusState == FocusState.MODULE && unknownSubscriptModule != null && unknownSubscriptModule.isActive()) {
                unknownSubscriptModule.deactivate();
                focusState = FocusState.DESIGNATION;
                LogUtils.d("InputController", "фокус снят с индекса в 'Введите неизвестное'");
            }
            updateDisplay();
        }
    }

    /** Обработка нажатия стрелки вправо */
    public void onRightArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if (focusState == FocusState.DESIGNATION) {
                if (designationSubscriptModule != null) {
                    designationSubscriptModule.activate();
                    focusState = FocusState.MODULE;
                    LogUtils.d("InputController", "фокус переключен на индекс");
                } else if (designationBuffer.length() > 0) {
                    focusState = FocusState.VALUE;
                    currentState = InputState.ENTERING_VALUE;
                    LogUtils.d("InputController", "фокус переключен на значение");
                }
            } else if (focusState == FocusState.VALUE) {
                if (valueBuffer.length() > 0 || valueOperationBuffer.length() > 0) {
                    focusState = FocusState.UNIT;
                    currentState = InputState.ENTERING_UNIT;
                    LogUtils.d("InputController", "фокус переключен на единицу измерения");
                }
            } else if (focusState == FocusState.MODULE && designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                designationSubscriptModule.deactivate();
                focusState = FocusState.VALUE;
                currentState = InputState.ENTERING_VALUE;
                LogUtils.d("InputController", "фокус снят с индекса на значение");
            }
            updateKeyboardMode();
            updateDisplay();
        } else if ("unknown".equals(currentInputField)) {
            if (focusState == FocusState.DESIGNATION && unknownSubscriptModule != null) {
                unknownSubscriptModule.activate();
                focusState = FocusState.MODULE;
                LogUtils.d("InputController", "фокус переключен на индекс в 'Введите неизвестное'");
            }
            updateDisplay();
        }
    }

    /** Обработка нажатия стрелки вниз (сохранение) */
    public void onDownArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if (designationSubscriptModule != null && designationSubscriptModule.isActive() && designationSubscriptModule.isEmpty()) {
                LogUtils.wWithSnackbar("InputController", "нельзя сохранить с пустым активным индексом", rootView);
                return;
            }
            if (designationBuffer.length() == 0) {
                LogUtils.wWithSnackbar("InputController", "нельзя сохранить: отсутствует обозначение", rootView);
                return;
            }
            if (valueBuffer.length() == 0 && valueOperationBuffer.length() == 0) {
                LogUtils.wWithSnackbar("InputController", "нельзя сохранить: отсутствует числовое значение", rootView);
                return;
            }

            String unit = unitBuffer.toString();
            if (unit.isEmpty()) {
                PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
                if (pq == null) {
                    LogUtils.eWithSnackbar("InputController", "неизвестная физическая величина: " + logicalDesignation, rootView);
                    return;
                }
                unit = pq.getSiUnit();
                if (unit.isEmpty()) {
                    LogUtils.wWithSnackbar("InputController", "нельзя сохранить: отсутствует единица измерения", rootView);
                    return;
                }
            }

            double value;
            try {
                value = Double.parseDouble(valueBuffer.length() > 0 ? valueBuffer.toString() : "0");
            } catch (NumberFormatException e) {
                LogUtils.eWithSnackbar("InputController", "ошибка формата числа: " + valueBuffer.toString(), rootView);
                return;
            }

            String subscript = (designationSubscriptModule != null && !designationSubscriptModule.isEmpty()) ? designationSubscriptModule.getDisplayText().toString() : "";

            if (!ModuleValidator.isSubscriptUnique(logicalDesignation, subscript, measurements)) {
                LogUtils.eWithSnackbar("InputController", "ошибка: индекс уже используется для обозначения: " + logicalDesignation, rootView);
                return;
            }

            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
            if (pq == null) {
                LogUtils.eWithSnackbar("InputController", "неизвестная физическая величина: " + logicalDesignation, rootView);
                return;
            }

            double siValue = value;
            String siUnit = unit;
            String steps = "";
            boolean isSIUnit = conversionService.isSiUnit(pq, unit);
            SpannableStringBuilder historyEntry = new SpannableStringBuilder();

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
            historyEntry.append(" = ").append(SIConverter.formatValue(value)).append(" ").append(unit);

            if (isConversionMode) {
                Object[] siData = conversionService.convert(pq, value, unit);
                if (siData == null) {
                    LogUtils.logConversionError("InputController", logicalDesignation, unit);
                    return;
                }
                siValue = (double) siData[0];
                siUnit = (String) siData[1];
                steps = conversionService.getSteps(pq, value, unit);

                if (!isSIUnit && !steps.isEmpty()) {
                    int stepsStart = historyEntry.length();
                    historyEntry.append(" = ").append(steps);
                    int lastEqualIndex = historyEntry.toString().lastIndexOf("= ");
                    if (lastEqualIndex != -1) {
                        int resultStart = lastEqualIndex + 2;
                        int resultEnd = historyEntry.length();
                        historyEntry.setSpan(new StyleSpan(Typeface.BOLD), resultStart, resultEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        LogUtils.w("InputController", "не найдено '= ' в шагах конвертации");
                    }
                }
            }

            if (isCurrentConstant) {
                historyEntry.append(" (константа)");
            }

            ConcreteMeasurement measurement = new ConcreteMeasurement(
                    logicalDesignation, siValue, siUnit,
                    operationBuffer.toString(), valueOperationBuffer.toString(),
                    subscript, isCurrentConstant, historyEntry,
                    value, unit, steps, isSIUnit, isConversionMode);
            if (!measurement.validate()) {
                LogUtils.logValidationError("InputController", measurement.toString());
                return;
            }

            measurements.add(measurement);
            history.add(historyEntry);
            LogUtils.logSaveMeasurement("InputController", measurement.toString());

            if (!unit.isEmpty()) {
                lastUnitForDesignation.put(logicalDesignation, unit);
                LogUtils.d("InputController", "сохранена последняя единица измерения для " + logicalDesignation + ": " + unit);
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

    /** Обновление режима клавиатуры */
    private void updateKeyboardMode() {
        if (keyboardModeSwitcher != null && "designations".equals(currentInputField)) {
            if (focusState == FocusState.MODULE) {
                keyboardModeSwitcher.switchToNumbersAndOperations();
                LogUtils.d("InputController", "переключен режим клавиатуры на 'Numbers_and_operations' для модуля");
            } else if (currentState == InputState.ENTERING_DESIGNATION) {
                keyboardModeSwitcher.switchToDesignation();
                LogUtils.d("InputController", "переключен режим клавиатуры на 'Designation'");
            } else if (currentState == InputState.ENTERING_VALUE) {
                keyboardModeSwitcher.switchToNumbersAndOperations();
                LogUtils.d("InputController", "переключен режим клавиатуры на 'Numbers_and_operations' для значения");
            } else if (currentState == InputState.ENTERING_UNIT) {
                keyboardModeSwitcher.switchToUnits();
                LogUtils.d("InputController", "переключен режим клавиатуры на 'Units_of_measurement'");
            }
        }
    }

    /** Обновление отображения интерфейса */
    private void updateDisplay() {
        SpannableStringBuilder designationsText = new SpannableStringBuilder();

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

        if (designationBuffer.length() > 0 || valueBuffer.length() > 0 || unitBuffer.length() > 0 ||
                designationSubscriptModule != null) {
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
            int unitStart = designationsText.length();
            if (unitBuffer.length() > 0) {
                designationsText.append(" ").append(unitBuffer);
            } else if (valueBuffer.length() > 0 || valueOperationBuffer.length() > 0) {
                designationsText.append(" ?");
            }

            if ("designations".equals(currentInputField)) {
                if (focusState == FocusState.MODULE && designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                    int moduleStart = designationEnd;
                    int moduleEnd = moduleStart + designationSubscriptModule.getDisplayText().length();
                    designationsText.setSpan(
                            new StyleSpan(Typeface.BOLD),
                            moduleStart,
                            moduleEnd,
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
                unknownText.append(unknownSubscriptModule.getDisplayText());
            }
            unknownText.append(" = ?");
            if ("unknown".equals(currentInputField) && focusState == FocusState.MODULE && unknownSubscriptModule != null && unknownSubscriptModule.isActive()) {
                int moduleStart = end;
                int moduleEnd = unknownText.length() - 4;
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
        LogUtils.d("InputController", "обновлен интерфейс отображения");
    }

    /** Полная очистка всех данных */
    public void clearAll() {
        if ("designations".equals(currentInputField)) {
            resetInput();
            history.clear();
            measurements.clear();
            LogUtils.d("InputController", "очищены все данные для 'Введите обозначение'");
        } else if ("unknown".equals(currentInputField)) {
            unknownDesignation = null;
            logicalUnknownDesignation = null;
            unknownUsesStix = null;
            unknownSubscriptModule = null;
            focusState = FocusState.DESIGNATION;
            unknowns.clear();
            LogUtils.d("InputController", "очищены все данные для 'Введите неизвестное'");
        }
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
        focusState = FocusState.DESIGNATION;
        designationUsesStix = null;
        logicalDesignation = null;
        isCurrentConstant = false;
        designationSubscriptModule = null;
        updateKeyboardMode();
        updateDisplay();
        LogUtils.d("InputController", "сброшены все буферы ввода");
    }

    /** Получение списка измерений */
    public List<ConcreteMeasurement> getMeasurements() {
        return new ArrayList<>(measurements);
    }

    /** Получение списка неизвестных */
    public List<UnknownQuantity> getUnknowns() {
        return new ArrayList<>(unknowns);
    }

    /** Логирование всех сохранённых данных */
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

        LogUtils.d("InputController", logMessage.toString());
    }
}