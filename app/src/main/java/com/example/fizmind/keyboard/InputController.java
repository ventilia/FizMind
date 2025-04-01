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

/**
 * Контроллер ввода, управляющий логикой ввода данных в поля "Введите обозначение" и "Введите неизвестное".
 * <p>
 * В данной версии интегрирован функционал перевода в СИ с использованием ConversionService.
 * Если режим конвертации включён, значение измерения переводится в СИ и история сохраняется с подробностями перевода.
 * </p>
 */
public class InputController {

    // Перечисления состояний ввода и фокуса
    public enum InputState {
        ENTERING_DESIGNATION, // Ввод обозначения
        ENTERING_VALUE,       // Ввод значения
        ENTERING_UNIT         // Ввод единицы измерения
    }

    public enum FocusState {
        DESIGNATION, // Фокус на обозначении
        VALUE,       // Фокус на значении
        UNIT,        // Фокус на единице измерения
        MODULE       // Фокус на модуле (например, степень или индекс)
    }

    // Поля для хранения состояния ввода
    private InputState currentState;
    private FocusState focusState;
    private final StringBuilder designationBuffer;       // Буфер для обозначения
    private final StringBuilder valueBuffer;             // Буфер для значения
    private final StringBuilder unitBuffer;              // Буфер для единицы измерения
    private final StringBuilder operationBuffer;         // Буфер для операций с обозначением
    private final StringBuilder valueOperationBuffer;    // Буфер для операций со значением
    private final Map<String, String> lastUnitForDesignation; // Последние единицы измерения для обозначений

    // Поля для отображения данных
    private final TextView designationsView;             // Поле отображения "Введите обозначение"
    private final TextView unknownView;                  // Поле отображения "Введите неизвестное"

    // Списки сохранённых измерений и неизвестных
    private final List<ConcreteMeasurement> measurements; // Список сохранённых измерений
    private final List<SpannableStringBuilder> history;  // История отображения измерений
    private final List<UnknownQuantity> unknowns;        // Список сохранённых неизвестных

    // Флаги и настройки шрифта
    private Boolean designationUsesStix;                 // Используется ли шрифт STIX для обозначения
    private Boolean unknownUsesStix;                     // Используется ли шрифт STIX для неизвестного
    private String logicalDesignation;                   // Логический идентификатор обозначения
    private Typeface stixTypeface;                       // Шрифт STIX

    // Переключатель режимов клавиатуры
    private KeyboardModeSwitcher keyboardModeSwitcher;

    // Флаг, указывающий, является ли текущее значение константой
    private boolean isCurrentConstant;

    // Поля для работы с режимом "Введите неизвестное"
    private String currentInputField;                    // Текущее поле ввода ("designations" или "unknown")
    private String unknownDesignation;                   // Текущее неизвестное обозначение
    private String logicalUnknownDesignation;            // Логический идентификатор неизвестного
    private boolean isUnknownInputAllowed = true;        // Флаг разрешения переключения на "Введите неизвестное"

    // Модули для ввода степени и индекса
    private InputModule designationExponentModule;       // Модуль степени для обозначения
    private InputModule designationSubscriptModule;      // Модуль индекса для обозначения
    private InputModule unknownSubscriptModule;          // Модуль индекса для неизвестного

    // Переменные для отслеживания двойного нажатия на кнопку DELETE
    private long lastDeleteTime = 0;                     // Время последнего нажатия
    private int deleteClickCount = 0;                    // Счетчик нажатий
    private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Интервал для двойного нажатия (мс)

    // Новые поля для перевода в СИ
    private final ConversionService conversionService;   // Сервис для перевода в СИ
    private boolean isConversionMode = false;            // Флаг режима конвертации (true – режим перевода в СИ)

    /**
     * Конструктор контроллера ввода.
     *
     * @param designationsView   Поле отображения "Введите обозначение"
     * @param unknownView        Поле отображения "Введите неизвестное"
     * @param conversionService  Сервис для перевода в СИ
     */
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
        this.lastUnitForDesignation = new HashMap<>();
        this.measurements = new ArrayList<>();
        this.history = new ArrayList<>();
        this.unknowns = new ArrayList<>();
        this.designationUsesStix = null;
        this.unknownUsesStix = null;
        this.logicalDesignation = null;
        this.isCurrentConstant = false;
        this.currentInputField = "designations";
        this.designationExponentModule = null;
        this.designationSubscriptModule = null;
        this.unknownSubscriptModule = null;
        updateDisplay();
        Log.d("InputController", "Контроллер ввода инициализирован");
    }

    /**
     * Устанавливает флаг разрешения переключения на поле "Введите неизвестное".
     *
     * @param allowed true, если разрешено; false — если запрещено.
     */
    public void setUnknownInputAllowed(boolean allowed) {
        this.isUnknownInputAllowed = allowed;
        Log.d("InputController", "Разрешение ввода неизвестного: " + allowed);
    }

    /**
     * Устанавливает шрифт STIX для отображения.
     *
     * @param stixTypeface Шрифт STIX.
     */
    public void setStixTypeface(Typeface stixTypeface) {
        this.stixTypeface = stixTypeface;
    }

    /**
     * Устанавливает переключатель режимов клавиатуры.
     *
     * @param switcher Объект переключателя.
     */
    public void setKeyboardModeSwitcher(KeyboardModeSwitcher switcher) {
        this.keyboardModeSwitcher = switcher;
    }

    /**
     * Устанавливает текущий режим работы: конвертация (перевод в СИ) или калькулятор.
     *
     * @param isConversionMode true, если режим конвертации; false — для обычного калькулятора.
     */
    public void setConversionMode(boolean isConversionMode) {
        this.isConversionMode = isConversionMode;
        Log.d("InputController", "Режим установлен: " + (isConversionMode ? "конвертация" : "калькулятор"));
    }

    /**
     * Устанавливает текущее поле ввода.
     *
     * @param field "designations" или "unknown".
     */
    public void setCurrentInputField(String field) {
        if ("unknown".equals(field) && !isUnknownInputAllowed) {
            Log.w("InputController", "Переключение на 'Введите неизвестное' заблокировано");
            return;
        }
        if (!field.equals(currentInputField)) {
            designationExponentModule = null;
            designationSubscriptModule = null;
            unknownSubscriptModule = null;
            focusState = FocusState.DESIGNATION;
            currentState = InputState.ENTERING_DESIGNATION;
        }
        this.currentInputField = field;
        updateKeyboardMode();
        updateDisplay();
        Log.d("InputController", "Текущее поле ввода: " + field);
    }

    /**
     * Обрабатывает ввод символа с клавиатуры.
     *
     * @param input              Введённый символ.
     * @param sourceKeyboardMode Режим клавиатуры.
     * @param keyUsesStix        Флаг использования шрифта STIX.
     * @param logicalId          Логический идентификатор символа.
     */
    public void onKeyInput(String input, String sourceKeyboardMode, boolean keyUsesStix, String logicalId) {
        Log.d("InputController", "Ввод: " + input + ", logicalId: " + logicalId + ", состояние: " + currentState);

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
                    if (!"Designation".equals(sourceKeyboardMode)) return;
                    if (logicalId.equals("op_exponent") || logicalId.equals("op_subscript")) return;
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
                        if (keyboardModeSwitcher != null) keyboardModeSwitcher.switchToNumbersAndOperations();
                    }
                } else {
                    if (input.matches("[0-9]") || ".".equals(input) || "-".equals(input)) {
                        currentState = InputState.ENTERING_VALUE;
                        focusState = FocusState.VALUE;
                        if (keyboardModeSwitcher != null) keyboardModeSwitcher.switchToNumbersAndOperations();
                        handleValueInput(input, logicalId);
                    } else if (logicalId.equals("op_subscript")) {
                        if (!ModuleValidator.canAddModule(ModuleType.SUBSCRIPT, designationExponentModule, designationSubscriptModule))
                            return;
                        designationSubscriptModule = new InputModule(ModuleType.SUBSCRIPT);
                        focusState = FocusState.MODULE;
                        updateDisplay();
                    }
                }
            } else if (currentState == InputState.ENTERING_VALUE) {
                if (designationBuffer.length() == 0) return;
                if (logicalId.equals("op_exponent")) {
                    if (!ModuleValidator.canAddModule(ModuleType.EXPONENT, designationExponentModule, designationSubscriptModule))
                        return;
                    designationExponentModule = new InputModule(ModuleType.EXPONENT);
                    focusState = FocusState.MODULE;
                    updateDisplay();
                } else if (logicalId.equals("op_subscript")) {
                    if (!ModuleValidator.canAddModule(ModuleType.SUBSCRIPT, designationExponentModule, designationSubscriptModule))
                        return;
                    designationSubscriptModule = new InputModule(ModuleType.SUBSCRIPT);
                    focusState = FocusState.MODULE;
                    updateDisplay();
                } else {
                    handleValueInput(input, logicalId);
                }
            } else if (currentState == InputState.ENTERING_UNIT) {
                // В режиме ввода единицы нельзя вводить модуль индекса
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
                if (!ModuleValidator.canAddModule(ModuleType.SUBSCRIPT, null, unknownSubscriptModule))
                    return;
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
            } else {
                Log.w("InputController", "В 'Введите неизвестное' можно ввести только одно обозначение");
                return;
            }
        }
        updateDisplay();
    }

    /**
     * Обрабатывает ввод значения.
     *
     * @param input     Введённый символ.
     * @param logicalId Логический идентификатор.
     */
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

    /**
     * Обрабатывает ввод единицы измерения.
     *
     * @param input     Введённый символ.
     * @param logicalId Логический идентификатор.
     */
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

    /**
     * Сохраняет неизвестное значение.
     */
    private void saveUnknown() {
        if (unknownDesignation != null) {
            if (unknownSubscriptModule != null && unknownSubscriptModule.isActive() && unknownSubscriptModule.isEmpty()) {
                Log.w("InputController", "Нельзя сохранить с пустым активным индексом");
                return;
            }
            String subscript = (unknownSubscriptModule != null && !unknownSubscriptModule.isEmpty())
                    ? unknownSubscriptModule.getDisplayText().toString() : "";
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

    /**
     * Проверяет, пустое ли текущее поле ввода.
     *
     * @return true, если поле ввода пустое; false — если содержит данные.
     */
    private boolean isInputEmpty() {
        if ("designations".equals(currentInputField)) {
            return designationBuffer.length() == 0 && valueBuffer.length() == 0 && unitBuffer.length() == 0 &&
                    operationBuffer.length() == 0 && valueOperationBuffer.length() == 0 &&
                    designationExponentModule == null && designationSubscriptModule == null;
        } else {
            return unknownDesignation == null && unknownSubscriptModule == null;
        }
    }

    /**
     * Очищает все данные текущего поля ввода.
     */
    public void clearAll() {
        if ("designations".equals(currentInputField)) {
            resetInput();
            history.clear();
            measurements.clear();
            Log.d("InputController", "Очищены все данные для 'Введите обозначение'");
        } else if ("unknown".equals(currentInputField)) {
            unknownDesignation = null;
            logicalUnknownDesignation = null;
            unknownUsesStix = null;
            unknownSubscriptModule = null;
            unknowns.clear();
            focusState = FocusState.DESIGNATION;
            Log.d("InputController", "Очищены все данные для 'Введите неизвестное'");
        }
        updateDisplay();
        if (keyboardModeSwitcher != null) {
            keyboardModeSwitcher.switchToDesignation();
        }
    }

    /**
     * Сбрасывает текущий ввод.
     */
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

    /**
     * Возвращает копию списка сохранённых измерений.
     *
     * @return список ConcreteMeasurement.
     */
    public List<ConcreteMeasurement> getMeasurements() {
        return new ArrayList<>(measurements);
    }

    /**
     * Возвращает копию списка сохранённых неизвестных.
     *
     * @return список UnknownQuantity.
     */
    public List<UnknownQuantity> getUnknowns() {
        return new ArrayList<>(unknowns);
    }

    /**
     * Логирует все сохранённые данные (измерения и неизвестные).
     */
    public void logAllSavedData() {
        StringBuilder log = new StringBuilder("Сохраненные данные:\n");
        log.append("Измерения:\n");
        if (measurements.isEmpty()) log.append("  Нет данных\n");
        else {
            for (ConcreteMeasurement m : measurements)
                log.append("  ").append(m.toString()).append("\n");
        }
        log.append("Неизвестные:\n");
        if (unknowns.isEmpty()) log.append("  Нет данных\n");
        else {
            for (UnknownQuantity u : unknowns)
                log.append("  ").append(u.toString()).append("\n");
        }
        Log.d("InputController", log.toString());
    }

    /**
     * Обрабатывает нажатие кнопки "DELETE".
     * При двойном нажатии удаляет последнее сохранённое поле, при одиночном – удаляет последний символ или очищает ввод.
     */
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
        } else {
            if (isInputEmpty() && !measurements.isEmpty() && measurements.get(measurements.size() - 1).isConstant()) {
                deleteLastSavedField();
            } else {
                performSingleDelete();
            }
        }
        updateDisplay();
    }

    /**
     * Выполняет одиночное удаление (последнего символа или очистку текущего ввода).
     */
    private void performSingleDelete() {
        if ("designations".equals(currentInputField)) {
            if (focusState == FocusState.MODULE) {
                if (designationExponentModule != null && designationExponentModule.isActive()) {
                    if (designationExponentModule.delete()) {
                        designationExponentModule = null;
                        focusState = FocusState.VALUE;
                        Log.d("InputController", "Степень удалена");
                    }
                } else if (designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                    if (designationSubscriptModule.delete()) {
                        designationSubscriptModule = null;
                        focusState = FocusState.DESIGNATION;
                        Log.d("InputController", "Индекс удалён");
                    }
                }
                return;
            }
            if (currentState == InputState.ENTERING_UNIT) {
                unitBuffer.setLength(0);
                currentState = InputState.ENTERING_VALUE;
                focusState = FocusState.VALUE;
                if (keyboardModeSwitcher != null) keyboardModeSwitcher.switchToNumbersAndOperations();
                Log.d("InputController", "Единица измерения удалена, переключено в режим ввода числа");
            } else if (currentState == InputState.ENTERING_VALUE) {
                if (valueBuffer.length() > 0) {
                    valueBuffer.deleteCharAt(valueBuffer.length() - 1);
                    Log.d("InputController", "Удалён последний символ числа");
                } else if (valueOperationBuffer.length() > 0) {
                    valueOperationBuffer.deleteCharAt(valueOperationBuffer.length() - 1);
                    Log.d("InputController", "Удалён последний символ операции");
                } else if (designationBuffer.length() > 0) {
                    resetInput();
                    Log.d("InputController", "Ввод очищен, обозначение удалено");
                }
            } else if (currentState == InputState.ENTERING_DESIGNATION && designationBuffer.length() > 0) {
                resetInput();
                Log.d("InputController", "Обозначение удалено, буферы очищены");
            }
        } else if ("unknown".equals(currentInputField)) {
            if (focusState == FocusState.MODULE && unknownSubscriptModule != null && unknownSubscriptModule.isActive()) {
                if (unknownSubscriptModule.delete()) {
                    unknownSubscriptModule = null;
                    focusState = FocusState.DESIGNATION;
                    Log.d("InputController", "Индекс в 'Введите неизвестное' удалён");
                }
            } else if (unknownDesignation != null) {
                unknownDesignation = null;
                logicalUnknownDesignation = null;
                unknownUsesStix = null;
                unknownSubscriptModule = null;
                focusState = FocusState.DESIGNATION;
                Log.d("InputController", "Неизвестное обозначение удалено");
            }
        }
    }

    /**
     * Удаляет последнее сохранённое поле (измерение или неизвестное).
     */
    private void deleteLastSavedField() {
        if ("designations".equals(currentInputField)) {
            if (!measurements.isEmpty()) {
                measurements.remove(measurements.size() - 1);
                history.remove(history.size() - 1);
                Log.d("InputController", "Удалено последнее сохранённое измерение");
            } else {
                Log.w("InputController", "Нет измерений для удаления");
            }
        } else if ("unknown".equals(currentInputField)) {
            if (!unknowns.isEmpty()) {
                unknowns.remove(unknowns.size() - 1);
                Log.d("InputController", "Удалено последнее сохранённое неизвестное");
            } else {
                Log.w("InputController", "Нет неизвестных для удаления");
            }
        }
    }

    /**
     * Обрабатывает нажатие стрелки влево для навигации между частями ввода.
     */
    public void onLeftArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if (focusState == FocusState.MODULE) {
                if (designationExponentModule != null && designationExponentModule.isActive()) {
                    designationExponentModule.deactivate();
                    focusState = FocusState.VALUE;
                } else if (designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                    designationSubscriptModule.deactivate();
                    focusState = FocusState.DESIGNATION;
                }
            } else if (focusState == FocusState.UNIT) {
                focusState = FocusState.VALUE;
                currentState = InputState.ENTERING_VALUE;
            } else if (focusState == FocusState.VALUE && designationBuffer.length() > 0) {
                if (designationSubscriptModule != null) {
                    designationSubscriptModule.activate();
                    focusState = FocusState.MODULE;
                } else {
                    focusState = FocusState.DESIGNATION;
                    currentState = InputState.ENTERING_DESIGNATION;
                }
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

    /**
     * Обрабатывает нажатие стрелки вправо для навигации между частями ввода.
     */
    public void onRightArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if (focusState == FocusState.MODULE) {
                if (designationExponentModule != null && designationExponentModule.isActive()) {
                    designationExponentModule.deactivate();
                    focusState = FocusState.UNIT;
                    currentState = InputState.ENTERING_UNIT;
                } else if (designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                    designationSubscriptModule.deactivate();
                    focusState = FocusState.VALUE;
                    currentState = InputState.ENTERING_VALUE;
                }
            } else if (focusState == FocusState.DESIGNATION && designationBuffer.length() > 0) {
                focusState = FocusState.VALUE;
                currentState = InputState.ENTERING_VALUE;
            } else if (focusState == FocusState.VALUE && (valueBuffer.length() > 0 || valueOperationBuffer.length() > 0)) {
                focusState = FocusState.UNIT;
                currentState = InputState.ENTERING_UNIT;
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

    /**
     * Обрабатывает нажатие кнопки "Вниз" для сохранения ввода.
     * Здесь реализована логика перевода в СИ, если включён режим конвертации.
     */
    public void onDownArrowPressed() {
        if ("designations".equals(currentInputField)) {
            // Проверка на пустой ввод
            if (designationBuffer.length() == 0 || (valueBuffer.length() == 0 && valueOperationBuffer.length() == 0))
                return;

            String unit = unitBuffer.toString();
            if (unit.isEmpty()) {
                PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
                if (pq != null) unit = pq.getSiUnit();
                if (unit.isEmpty()) return;
            }

            double value;
            try {
                value = Double.parseDouble(valueBuffer.length() > 0 ? valueBuffer.toString() : "0");
            } catch (NumberFormatException e) {
                Log.e("InputController", "Ошибка формата числа", e);
                return;
            }

            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
            if (pq == null) return;

            double siValue = value;
            String siUnit = unit;
            // Если включён режим конвертации, переводим значение в СИ
            if (isConversionMode) {
                Object[] siData = conversionService.convert(pq, value, unit);
                if (siData == null) return;
                siValue = (double) siData[0];
                siUnit = (String) siData[1];
            }

            String exponent = (designationExponentModule != null && !designationExponentModule.isEmpty())
                    ? designationExponentModule.getDisplayText().toString() : "";
            String subscript = (designationSubscriptModule != null && !designationSubscriptModule.isEmpty())
                    ? designationSubscriptModule.getDisplayText().toString() : "";

            ConcreteMeasurement measurement = new ConcreteMeasurement(
                    logicalDesignation, siValue, siUnit,
                    operationBuffer.toString(), valueOperationBuffer.toString(),
                    exponent, subscript, isCurrentConstant);

            if (!measurement.validate()) return;

            measurements.add(measurement);

            // Формирование записи истории в зависимости от режима
            if (isConversionMode) {
                String steps = conversionService.getSteps(pq, value, unit);
                SpannableStringBuilder historyEntry = new SpannableStringBuilder(steps);
                int equalIndex = steps.lastIndexOf("=");
                if (equalIndex != -1) {
                    historyEntry.setSpan(new StyleSpan(Typeface.BOLD), equalIndex + 1, steps.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                history.add(historyEntry);
            } else {
                SpannableStringBuilder historyEntry = new SpannableStringBuilder();
                int start = historyEntry.length();
                if (operationBuffer.length() > 0)
                    historyEntry.append(operationBuffer).append("(");
                historyEntry.append(designationBuffer);
                int designationEnd = historyEntry.length();
                if (subscript != null && !subscript.isEmpty()) {
                    int subscriptStart = historyEntry.length();
                    historyEntry.append(subscript);
                    historyEntry.setSpan(new SubscriptSpan(), subscriptStart, historyEntry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    historyEntry.setSpan(new RelativeSizeSpan(0.75f), subscriptStart, historyEntry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (operationBuffer.length() > 0) historyEntry.append(")");
                if (designationUsesStix != null && designationUsesStix && stixTypeface != null) {
                    historyEntry.setSpan(new CustomTypefaceSpan(stixTypeface), start, designationEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                historyEntry.append(" = ");
                if (valueOperationBuffer.length() > 0)
                    historyEntry.append(valueOperationBuffer);
                else
                    historyEntry.append(String.valueOf(siValue));
                if (exponent != null && !exponent.isEmpty()) {
                    int exponentStart = historyEntry.length();
                    historyEntry.append(exponent);
                    historyEntry.setSpan(new SuperscriptSpan(), exponentStart, historyEntry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    historyEntry.setSpan(new RelativeSizeSpan(0.75f), exponentStart, historyEntry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (!siUnit.isEmpty())
                    historyEntry.append(" ").append(siUnit);
                history.add(historyEntry);
            }

            if (!unit.isEmpty()) lastUnitForDesignation.put(logicalDesignation, unit);
            resetInput();
            if (keyboardModeSwitcher != null) keyboardModeSwitcher.switchToDesignation();
            logAllSavedData();
        } else if ("unknown".equals(currentInputField)) {
            saveUnknown();
        }
    }

    /**
     * Обновляет режим клавиатуры в зависимости от текущего состояния.
     */
    private void updateKeyboardMode() {
        if (keyboardModeSwitcher != null && "designations".equals(currentInputField)) {
            if (currentState == InputState.ENTERING_DESIGNATION)
                keyboardModeSwitcher.switchToDesignation();
            else if (currentState == InputState.ENTERING_VALUE)
                keyboardModeSwitcher.switchToNumbersAndOperations();
            else if (currentState == InputState.ENTERING_UNIT)
                keyboardModeSwitcher.switchToUnits();
        }
    }

    /**
     * Обновляет отображение полей ввода.
     */
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
            if (operationBuffer.length() > 0)
                designationsText.append(operationBuffer).append("(").append(designationBuffer).append(")");
            else
                designationsText.append(designationBuffer);
            int designationEnd = designationsText.length();
            if (designationSubscriptModule != null)
                designationsText.append(designationSubscriptModule.getDisplayText());
            if (designationUsesStix != null && designationUsesStix && stixTypeface != null) {
                designationsText.setSpan(new CustomTypefaceSpan(stixTypeface), designationStart, designationEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            designationsText.append(" = ");
            int valueStart = designationsText.length();
            if (valueOperationBuffer.length() > 0)
                designationsText.append(valueOperationBuffer);
            else
                designationsText.append(valueBuffer);
            int valueEnd = designationsText.length();
            if (designationExponentModule != null)
                designationsText.append(designationExponentModule.getDisplayText());
            int unitStart = designationsText.length();
            if (unitBuffer.length() > 0)
                designationsText.append(" ").append(unitBuffer);
            else if (valueBuffer.length() > 0 || valueOperationBuffer.length() > 0)
                designationsText.append(" ?");
            if ("designations".equals(currentInputField)) {
                if (focusState == FocusState.MODULE) {
                    int moduleStart = (designationExponentModule != null && designationExponentModule.isActive()) ? valueEnd : designationEnd;
                    designationsText.setSpan(new StyleSpan(Typeface.BOLD), moduleStart, designationsText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (focusState == FocusState.DESIGNATION && designationStart < valueStart - 3) {
                    designationsText.setSpan(new StyleSpan(Typeface.BOLD), designationStart, valueStart - 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (focusState == FocusState.VALUE && valueStart < valueEnd) {
                    designationsText.setSpan(new StyleSpan(Typeface.BOLD), valueStart, valueEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (focusState == FocusState.UNIT && unitStart < designationsText.length()) {
                    designationsText.setSpan(new StyleSpan(Typeface.BOLD), unitStart + 1, designationsText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        } else {
            int start = designationsText.length();
            designationsText.append("Введите обозначение");
            int color = "designations".equals(currentInputField) ? Color.BLACK : Color.GRAY;
            designationsText.setSpan(new ForegroundColorSpan(color), start, designationsText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
            if (unknownSubscriptModule != null)
                unknownText.append(unknownSubscriptModule.getDisplayText());
            unknownText.append(" = ?");
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
        } else {
            designationsView.setTextColor(Color.parseColor("#A0A0A0"));
            unknownView.setTextColor(Color.BLACK);
        }
    }
}
