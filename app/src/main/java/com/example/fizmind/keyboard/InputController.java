package com.example.fizmind.keyboard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.TextView;
import com.example.fizmind.SI.SIConverter;
import com.example.fizmind.database.AppDatabase;
import com.example.fizmind.database.ConcreteMeasurementEntity;
import com.example.fizmind.database.UnknownQuantityEntity;
import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.modules.InputModule;
import com.example.fizmind.modules.ModuleType;
import com.example.fizmind.modules.ModuleValidator;
import com.example.fizmind.quantly.PhysicalQuantity;
import com.example.fizmind.quantly.PhysicalQuantityRegistry;
import com.example.fizmind.utils.LogUtils;

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
    private final StringBuilder operationBuffer;
    private final StringBuilder valueOperationBuffer;
    private final TextView designationsView;
    private final TextView unknownView;
    private final View rootView;
    private final AppDatabase database;
    private final DisplayManager displayManager;
    private final SIConverter siConverter;
    private Typeface stixTypeface;
    private KeyboardModeSwitcher keyboardModeSwitcher;
    private boolean isCurrentConstant;
    private final Map<String, String> lastUnitForDesignation;
    private String currentInputField;
    private String unknownDisplayDesignation;
    private String logicalUnknownDesignation;
    private String currentUnknownDesignation;
    private Boolean designationUsesStix;
    private Boolean unknownUsesStix;
    private String logicalDesignation;
    private String displayDesignation;
    private boolean isUnknownInputAllowed = true;
    private InputModule designationSubscriptModule;
    private InputModule unknownSubscriptModule;
    private long lastDeleteTime = 0;
    private int deleteClickCount = 0;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300;
    private boolean isConversionMode = false;


    public InputController(TextView designationsView, TextView unknownView, AppDatabase database,
                           View rootView, DisplayManager displayManager) {
        this.designationsView = designationsView;
        this.unknownView = unknownView;
        this.rootView = rootView;
        this.database = database;
        this.displayManager = displayManager;
        this.siConverter = new SIConverter(database);
        this.currentState = InputState.ENTERING_DESIGNATION;
        this.focusState = FocusState.DESIGNATION;
        this.designationBuffer = new StringBuilder();
        this.valueBuffer = new StringBuilder();
        this.unitBuffer = new StringBuilder();
        this.operationBuffer = new StringBuilder();
        this.valueOperationBuffer = new StringBuilder();
        this.lastUnitForDesignation = new HashMap<>();
        this.currentInputField = "designations";
        updateDisplay();
        LogUtils.logControllerInitialized("InputController");
    }

    // установка разрешения ввода неизвестного
    public void setUnknownInputAllowed(boolean allowed) {
        this.isUnknownInputAllowed = allowed;
        LogUtils.logPropertySet("InputController", "разрешение ввода неизвестного", allowed);
    }

    // установка шрифта
    public void setStixTypeface(Typeface stixTypeface) {
        this.stixTypeface = stixTypeface;
        LogUtils.logPropertySet("InputController", "шрифт STIX", "установлен");
    }

    // установка переключателя режимов клавиатуры
    public void setKeyboardModeSwitcher(KeyboardModeSwitcher switcher) {
        this.keyboardModeSwitcher = switcher;
        LogUtils.logPropertySet("InputController", "переключатель режимов клавиатуры", "установлен");
    }

    // установка  перевода в СИ
    public void setConversionMode(boolean isConversionMode) {
        this.isConversionMode = isConversionMode;
        if (isConversionMode) {
            resetUnknownInput();
            LogUtils.d("InputController", "сброшены данные 'Введите неизвестное' в режиме 'Перевод в СИ'");
        }
        updateDisplay();
        LogUtils.logPropertySet("InputController", "режим", isConversionMode ? "перевод в СИ" : "калькулятор");
    }

    // установка текущего поля
    public void setCurrentInputField(String field) {
        if ("unknown".equals(field) && !isUnknownInputAllowed) {
            LogUtils.wWithSnackbar("InputController", "переключение на 'Введите неизвестное' заблокировано", rootView);
            return;
        }
        if (!field.equals(currentInputField)) {
            resetFieldState();
            LogUtils.d("InputController", "сброшены модули и состояния при смене поля ввода");
        }
        this.currentInputField = field;
        updateKeyboardModeForField(field);
        updateDisplay();
        LogUtils.d("InputController", "текущее поле ввода установлено: " + field);
    }

    // получение текущего обозначения
    public String getCurrentDesignation() {
        return logicalDesignation;
    }

    // получение текущего поля ввода
    public String getCurrentInputField() {
        return currentInputField;
    }

    // получение текущего неизвестного обозначения
    public String getCurrentUnknownDesignation() {
        return currentUnknownDesignation;
    }


    public boolean hasSubscript() {
        return designationSubscriptModule != null && !designationSubscriptModule.isEmpty();
    }


    public boolean hasUnknownSubscript() {
        return unknownSubscriptModule != null && !unknownSubscriptModule.isEmpty();
    }


    public void onKeyInput(String input, String sourceKeyboardMode, boolean keyUsesStix, String logicalId) {
        LogUtils.logInputProcessing("InputController", currentState.toString(), focusState.toString(), input, logicalId, isConversionMode ? "СИ" : "калькулятор");

        if ("designations".equals(currentInputField)) {
            handleDesignationsInput(input, sourceKeyboardMode, keyUsesStix, logicalId);
        } else if ("unknown".equals(currentInputField)) {
            handleUnknownInput(input, sourceKeyboardMode, keyUsesStix, logicalId);
        }
        updateDisplay();
    }


    private void handleDesignationsInput(String input, String sourceKeyboardMode, boolean keyUsesStix, String logicalId) {
        if (focusState == FocusState.MODULE && designationSubscriptModule != null && designationSubscriptModule.isActive()) {
            handleModuleInput(input, logicalId);
        } else if (currentState == InputState.ENTERING_DESIGNATION) {
            handleDesignationInput(input, sourceKeyboardMode, keyUsesStix, logicalId);
        } else if (currentState == InputState.ENTERING_VALUE) {
            handleValueInput(input, logicalId);
        } else if (currentState == InputState.ENTERING_UNIT) {
            handleUnitInput(input, logicalId);
        }
    }

    private void handleModuleInput(String input, String logicalId) {
        if (designationSubscriptModule.getType() == ModuleType.SUBSCRIPT && (input.equals("p") || input.equals("k"))) {
            LogUtils.wWithSnackbar("InputController", "символы 'p' и 'k' можно использовать только в специальных модулях", rootView);
            return;
        }
        if (ModuleValidator.canApplyInput(designationSubscriptModule.getType(), designationSubscriptModule.getContent(), input)) {
            designationSubscriptModule.apply(input);
        } else {
            LogUtils.wWithSnackbar("InputController", "нельзя добавить символ '" + input + "' к модулю", rootView);
            return;
        }
        if (!input.matches("[a-zA-Z0-9]")) {
            designationSubscriptModule.deactivate();
            switchFocusAfterModule(input, logicalId);
        }
    }


    private void switchFocusAfterModule(String input, String logicalId) {
        if (input.matches("[0-9]") || ".".equals(input) || "-".equals(input)) {
            focusState = FocusState.VALUE;
            currentState = InputState.ENTERING_VALUE;
            handleValueInput(input, logicalId);
        } else {
            focusState = FocusState.UNIT;
            currentState = InputState.ENTERING_UNIT;
            handleUnitInput(input, logicalId);
        }
    }

    // обработка ввода обозначения
    private void handleDesignationInput(String input, String sourceKeyboardMode, boolean keyUsesStix, String logicalId) {
        if (designationBuffer.length() == 0) {
            processInitialDesignation(input, sourceKeyboardMode, keyUsesStix, logicalId);
        } else {
            processSubsequentDesignationInput(input, logicalId);
        }
    }

    // обработка первого символа
    private void processInitialDesignation(String input, String sourceKeyboardMode, boolean keyUsesStix, String logicalId) {
        if (!"Designation".equals(sourceKeyboardMode)) {
            LogUtils.wWithSnackbar("InputController", "первый символ ввода поля должен быть из режима 'Обозначения'", rootView);
            return;
        }
        if (logicalId.startsWith("op_") || logicalId.startsWith("mod_")) {
            LogUtils.wWithSnackbar("InputController", "нельзя начинать ввод с индекса", rootView);
            return;
        }
        String adjustedLogicalId = "designation_E".equals(logicalId) ? "E_latin" : logicalId;
        designationBuffer.append(input);
        logicalDesignation = adjustedLogicalId;
        displayDesignation = displayManager.getDisplayTextFromLogicalId(adjustedLogicalId);
        designationUsesStix = keyUsesStix;
        initializeBuffersForDesignation();
        updateStateForPhysicalQuantity();
        LogUtils.d("InputController", "введено обозначение: " + displayDesignation + " (логическое: " + adjustedLogicalId + ")");
    }

    // инициализация буферов для обозначения
    private void initializeBuffersForDesignation() {
        if (lastUnitForDesignation.containsKey(logicalDesignation) && valueBuffer.length() > 0) {
            unitBuffer.setLength(0);
            unitBuffer.append(lastUnitForDesignation.get(logicalDesignation));
        } else {
            unitBuffer.setLength(0);
        }
    }

    private void updateStateForPhysicalQuantity() {
        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
        if (pq != null && pq.isConstant()) {
            valueBuffer.append(String.valueOf(pq.getConstantValue()));
            unitBuffer.append(pq.getSiUnit());
            isCurrentConstant = true;
            onDownArrowPressed();
        } else {
            currentState = InputState.ENTERING_VALUE;
            focusState = FocusState.VALUE;
            updateKeyboardMode();
        }
    }


    private void processSubsequentDesignationInput(String input, String logicalId) {
        if (input.matches("[0-9]") || ".".equals(input) || "-".equals(input)) {
            currentState = InputState.ENTERING_VALUE;
            focusState = FocusState.VALUE;
            handleValueInput(input, logicalId);
        } else if (logicalId.equals("op_subscript")) {
            addSubscriptModule(ModuleType.SUBSCRIPT);
        } else if (logicalId.equals("mod_subscript_p")) {
            addFixedModule(ModuleType.SUBSCRIPT_P, "p");
        } else if (logicalId.equals("mod_subscript_k")) {
            addFixedModule(ModuleType.SUBSCRIPT_K, "k");
        } else {
            LogUtils.wWithSnackbar("InputController", "обозначение уже введено, ожидается число или индекс", rootView);
        }
    }

    // добавление  подстрочного индекса
    private void addSubscriptModule(ModuleType type) {
        if (!ModuleValidator.canAddModule(type, designationSubscriptModule, logicalDesignation)) {
            LogUtils.wWithSnackbar("InputController", "нельзя добавить индекс", rootView);
            return;
        }
        designationSubscriptModule = new InputModule(type);
        designationSubscriptModule.activate();
        focusState = FocusState.MODULE;
        updateKeyboardMode();
    }


    private void addFixedModule(ModuleType type, String symbol) {
        if (!ModuleValidator.canAddModule(type, designationSubscriptModule, logicalDesignation)) {
            LogUtils.wWithSnackbar("InputController", "нельзя добавить модуль '" + symbol + "'", rootView);
            return;
        }
        designationSubscriptModule = new InputModule(type);
        designationSubscriptModule.apply(symbol);
        focusState = FocusState.VALUE;
        currentState = InputState.ENTERING_VALUE;
        updateKeyboardMode();
    }

    // обработка ввода значения
    private void handleValueInput(String input, String logicalId) {
        if (designationBuffer.length() == 0) {
            LogUtils.wWithSnackbar("InputController", "нельзя ввести число без обозначения", rootView);
            return;
        }
        if (logicalId.equals("op_subscript")) {
            addSubscriptModule(ModuleType.SUBSCRIPT);
        } else if (logicalId.equals("mod_subscript_p")) {
            addFixedModule(ModuleType.SUBSCRIPT_P, "p");
        } else if (logicalId.equals("mod_subscript_k")) {
            addFixedModule(ModuleType.SUBSCRIPT_K, "k");
        } else {
            processValueInput(input, logicalId);
        }
    }

    // обработка числового ввода
    private void processValueInput(String input, String logicalId) {
        if (input.matches("[0-9]")) {
            valueBuffer.append(input);
        } else if (".".equals(input)) {
            validateDecimalPoint(input);
        } else if (logicalId.equals("op_abs_open")) {
            valueOperationBuffer.append("|");
        } else if (logicalId.equals("op_abs_close") && valueOperationBuffer.toString().contains("|")) {
            valueOperationBuffer.append(valueBuffer).append("|");
            valueBuffer.setLength(0);
        } else {
            currentState = InputState.ENTERING_UNIT;
            focusState = FocusState.UNIT;
            onKeyInput(input, "Units_of_measurement", false, logicalId);
            return;
        }
        updateDisplay();
    }

    // валидация точки
    private void validateDecimalPoint(String input) {
        if (valueBuffer.length() == 0 || valueBuffer.toString().equals("-")) {
            LogUtils.wWithSnackbar("InputController", "число не может начинаться с точки", rootView);
        } else if (valueBuffer.indexOf(".") != -1) {
            LogUtils.wWithSnackbar("InputController", "число уже содержит точку", rootView);
        } else {
            valueBuffer.append(input);
        }
    }



    private void handleUnitInput(String input, String logicalId) {
        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
        if (pq == null) {
            LogUtils.wWithSnackbar("InputController", "неизвестная физическая величина: " + logicalDesignation, rootView);
            return;
        }
        int maxAllowedLength = pq.getAllowedUnits().stream().mapToInt(String::length).max().orElse(0);
        String potentialUnit = (unitBuffer.toString() + input).toLowerCase();
        boolean validPrefix = pq.getAllowedUnits().stream().anyMatch(allowed -> allowed.startsWith(potentialUnit));
        if (validPrefix && potentialUnit.length() <= maxAllowedLength) {
            unitBuffer.append(input);
            LogUtils.d("InputController", "добавлена единица измерения: " + input);
        } else {
            LogUtils.wWithSnackbar("InputController", "недопустимая единица измерения: " + potentialUnit, rootView);
        }
    }


    private void handleUnknownInput(String input, String sourceKeyboardMode, boolean keyUsesStix, String logicalId) {
        if (isConversionMode) {
            LogUtils.wWithSnackbar("InputController", "ввод в 'Введите неизвестное' заблокирован в режиме 'Перевод в СИ'", rootView);
            return;
        }
        if ("Numbers_and_operations".equals(sourceKeyboardMode) && !"_".equals(input) && focusState != FocusState.MODULE) {
            LogUtils.wWithSnackbar("InputController", "числа нельзя вводить в 'Введите неизвестное'", rootView);
            return;
        }
        if ("Units_of_measurement".equals(sourceKeyboardMode)) {
            LogUtils.wWithSnackbar("InputController", "единицы измерения нельзя вводить в 'Введите неизвестное'", rootView);
            return;
        }
        if (focusState == FocusState.MODULE && unknownSubscriptModule != null && unknownSubscriptModule.isActive()) {
            if (unknownSubscriptModule.getType() == ModuleType.SUBSCRIPT && (input.equals("p") || input.equals("k"))) {
                LogUtils.wWithSnackbar("InputController", "символы 'p' и 'k' можно использовать только в специальных модулях", rootView);
                return;
            }
            if (ModuleValidator.canApplyInput(unknownSubscriptModule.getType(), unknownSubscriptModule.getContent(), input)) {
                unknownSubscriptModule.apply(input);
                LogUtils.d("InputController", "добавлен символ в индекс неизвестного: " + input);
            } else {
                LogUtils.wWithSnackbar("InputController", "недопустимый символ для индекса: " + input, rootView);
            }
        } else if (logicalId.equals("op_subscript")) {
            addSubscriptModuleForUnknown();
        } else if (logicalId.equals("mod_subscript_p") || logicalId.equals("mod_subscript_k")) {
            addFixedModuleForUnknown(logicalId, input);
        } else if (unknownDisplayDesignation == null) {
            setInitialUnknown(input, keyUsesStix, logicalId);
        } else {
            LogUtils.wWithSnackbar("InputController", "в 'Введите неизвестное' можно ввести только одно обозначение", rootView);
        }
    }


    private void addSubscriptModuleForUnknown() {
        if (unknownSubscriptModule != null) {
            LogUtils.wWithSnackbar("InputController", "индекс уже введён", rootView);
            return;
        }
        if (unknownDisplayDesignation == null) {
            LogUtils.wWithSnackbar("InputController", "сначала введите обозначение перед добавлением индекса", rootView);
            return;
        }
        unknownSubscriptModule = new InputModule(ModuleType.SUBSCRIPT);
        unknownSubscriptModule.activate();
        focusState = FocusState.MODULE;
        updateKeyboardMode();
        LogUtils.d("InputController", "добавлен подстрочный индекс для неизвестного");
    }

    private void addFixedModuleForUnknown(String logicalId, String input) {
        if (!"designation_E".equals(currentUnknownDesignation) && !"E_latin".equals(currentUnknownDesignation)) {
            LogUtils.wWithSnackbar("InputController", "модули 'p' и 'k' применимы только к 'E'", rootView);
            return;
        }
        if (unknownSubscriptModule != null) {
            LogUtils.wWithSnackbar("InputController", "нельзя добавить 'p' или 'k', если уже есть индекс", rootView);
            return;
        }
        ModuleType type = logicalId.equals("mod_subscript_p") ? ModuleType.SUBSCRIPT_P : ModuleType.SUBSCRIPT_K;
        unknownSubscriptModule = new InputModule(type);
        unknownSubscriptModule.apply(input);
        focusState = FocusState.DESIGNATION;
        updateKeyboardMode();
        LogUtils.d("InputController", "добавлен фиксированный модуль '" + input + "' для неизвестного");
    }


    private void setInitialUnknown(String input, boolean keyUsesStix, String logicalId) {
        String adjustedLogicalId = "designation_E".equals(logicalId) ? "E_latin" : logicalId;
        unknownDisplayDesignation = displayManager.getDisplayTextFromLogicalId(adjustedLogicalId);
        logicalUnknownDesignation = adjustedLogicalId;
        currentUnknownDesignation = adjustedLogicalId;
        unknownUsesStix = keyUsesStix;
        focusState = FocusState.DESIGNATION;
        LogUtils.d("InputController", "введено неизвестное обозначение: " + unknownDisplayDesignation);
    }

    // сохранение неизвестного
    private void saveUnknown() {
        if (unknownDisplayDesignation == null) return;
        if (unknownSubscriptModule != null && unknownSubscriptModule.isActive() && unknownSubscriptModule.isEmpty()) {
            LogUtils.wWithSnackbar("InputController", "завершите ввод индекса или удалите его", rootView);
            return;
        }
        String subscript = (unknownSubscriptModule != null && !unknownSubscriptModule.isEmpty()) ? unknownSubscriptModule.getDisplayText().toString() : "";
        String fullLogicalDesignation = adjustLogicalDesignationForUnknown(subscript);
        SpannableStringBuilder displayText = displayManager.buildUnknownText(
                database.unknownQuantityDao().getAllUnknowns(), unknownDisplayDesignation, unknownUsesStix,
                unknownSubscriptModule, currentInputField, isConversionMode
        );
        UnknownQuantityEntity unknown = new UnknownQuantityEntity(
                unknownDisplayDesignation, fullLogicalDesignation, subscript,
                unknownUsesStix != null && unknownUsesStix, displayText.toString()
        );
        database.unknownQuantityDao().insert(unknown);
        resetUnknownInput();
        LogUtils.logSaveUnknown("InputController", unknown.toString());
    }

    // корректировка логического обозначения для неизвестного
    private String adjustLogicalDesignationForUnknown(String subscript) {
        String fullLogicalDesignation = logicalUnknownDesignation;
        if ("E_latin".equals(logicalUnknownDesignation)) {
            if ("p".equals(subscript)) {
                fullLogicalDesignation = "E_latin_p";
                subscript = "";
            } else if ("k".equals(subscript)) {
                fullLogicalDesignation = "E_latin_k";
                subscript = "";
            }
        }
        return fullLogicalDesignation;
    }

    // обработка нажатия кнопки удаления
    public void onDeletePressed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDeleteTime < DOUBLE_CLICK_TIME_DELTA) {
            deleteClickCount++;
        } else {
            deleteClickCount = 1;
        }
        lastDeleteTime = currentTime;

        if ("unknown".equals(currentInputField)) {
            handleUnknownDeletion();
        } else {
            handleDesignationsDeletion();
        }
        updateKeyboardMode();
        updateDisplay();
    }

    private void handleUnknownDeletion() {
        if (isConversionMode) {
            LogUtils.d("InputController", "удаление в 'Введите неизвестное' игнорируется в режиме 'Перевод в СИ'");
            return;
        }
        if (unknownDisplayDesignation != null) {
            resetUnknownInput();
            LogUtils.d("InputController", "удалено несохранённое обозначение в 'Введите неизвестное'");
        } else {
            database.unknownQuantityDao().deleteLastUnknown();
            LogUtils.d("InputController", "удалено последнее сохранённое неизвестное");
        }
    }

    private void handleDesignationsDeletion() {
        if (focusState == FocusState.MODULE || focusState == FocusState.VALUE) {
            performSingleDelete();
        } else if (deleteClickCount == 2) {
            database.measurementDao().deleteLastMeasurement();
            deleteClickCount = 0;
            LogUtils.logDeletion("InputController", "выполнено двойное удаление последнего сохранённого поля");
        } else {
            performSingleDelete();
        }
    }

    // выполнение  удаления
    private void performSingleDelete() {
        if (focusState == FocusState.MODULE && designationSubscriptModule != null && designationSubscriptModule.isActive()) {
            if (designationSubscriptModule.deleteChar()) {
                designationSubscriptModule = null;
                focusState = FocusState.DESIGNATION;
                LogUtils.d("InputController", "индекс удалён полностью");
            }
        } else if (currentState == InputState.ENTERING_UNIT) {
            unitBuffer.setLength(0);
            currentState = InputState.ENTERING_VALUE;
            focusState = FocusState.VALUE;
            LogUtils.d("InputController", "единицы измерения удалены");
        } else if (currentState == InputState.ENTERING_VALUE) {
            deleteFromValueOrOperation();
        } else if (currentState == InputState.ENTERING_DESIGNATION && designationBuffer.length() > 0) {
            deleteDesignationOrModule();
        }
    }

    // удаление из значения или операции
    private void deleteFromValueOrOperation() {
        if (valueBuffer.length() > 0) {
            valueBuffer.deleteCharAt(valueBuffer.length() - 1);
            LogUtils.d("InputController", "удалён символ из значения");
        } else if (valueOperationBuffer.length() > 0) {
            valueOperationBuffer.deleteCharAt(valueOperationBuffer.length() - 1);
            LogUtils.d("InputController", "удалён символ из операции");
        } else if (designationBuffer.length() > 0) {
            deleteDesignationOrModule();
        }
    }

    // удаление обозначения или модуля
    private void deleteDesignationOrModule() {
        if (designationSubscriptModule != null) {
            designationSubscriptModule = null;
            LogUtils.d("InputController", "модуль удалён");
        } else {
            resetInput();
            LogUtils.d("InputController", "обозначение удалено");
        }
    }

    // обработка нажатия стрелки влево
    public void onLeftArrowPressed() {
        if ("designations".equals(currentInputField)) {
            adjustFocusLeft();
            updateKeyboardMode();
            updateDisplay();
        } else if ("unknown".equals(currentInputField) && !isConversionMode) {
            adjustUnknownFocusLeft();
            updateDisplay();
        }
    }


    private void adjustFocusLeft() {
        if (focusState == FocusState.MODULE && designationSubscriptModule != null && designationSubscriptModule.isActive()) {
            designationSubscriptModule.deactivate();
            focusState = FocusState.DESIGNATION;
            LogUtils.d("InputController", "фокус снят с индекса на обозначение");
        } else if (focusState == FocusState.UNIT) {
            focusState = FocusState.VALUE;
            currentState = InputState.ENTERING_VALUE;
            LogUtils.d("InputController", "фокус переключен с единицы на значение");
        } else if (focusState == FocusState.VALUE && designationBuffer.length() > 0) {
            switchToDesignationOrModule();
        }
    }

    // переключение на обозначение или модуль
    private void switchToDesignationOrModule() {
        if (designationSubscriptModule != null) {
            designationSubscriptModule.activate();
            focusState = FocusState.MODULE;
            LogUtils.d("InputController", "фокус переключен на модуль");
        } else {
            focusState = FocusState.DESIGNATION;
            currentState = InputState.ENTERING_DESIGNATION;
            LogUtils.d("InputController", "фокус переключен на обозначение");
        }
    }

    // корректировка фокуса  неизвестного
    private void adjustUnknownFocusLeft() {
        if (focusState == FocusState.MODULE && unknownSubscriptModule != null && unknownSubscriptModule.isActive()) {
            unknownSubscriptModule.deactivate();
            focusState = FocusState.DESIGNATION;
            LogUtils.d("InputController", "фокус снят с индекса в 'Введите неизвестное'");
        }
    }

    // обработка нажатия стрелки вправо
    public void onRightArrowPressed() {
        if ("designations".equals(currentInputField)) {
            adjustFocusRight();
            updateKeyboardMode();
            updateDisplay();
        } else if ("unknown".equals(currentInputField) && !isConversionMode) {
            adjustUnknownFocusRight();
            updateDisplay();
        }
    }


    private void adjustFocusRight() {
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
        } else if (focusState == FocusState.VALUE && (valueBuffer.length() > 0 || valueOperationBuffer.length() > 0)) {
            focusState = FocusState.UNIT;
            currentState = InputState.ENTERING_UNIT;
            LogUtils.d("InputController", "фокус переключен на единицу измерения");
        } else if (focusState == FocusState.MODULE && designationSubscriptModule != null && designationSubscriptModule.isActive()) {
            designationSubscriptModule.deactivate();
            focusState = FocusState.VALUE;
            currentState = InputState.ENTERING_VALUE;
            LogUtils.d("InputController", "фокус снят с индекса на значение");
        }
    }

    // корректировка фокуса вправо для неизвестного
    private void adjustUnknownFocusRight() {
        if (focusState == FocusState.DESIGNATION && unknownSubscriptModule != null) {
            unknownSubscriptModule.activate();
            focusState = FocusState.MODULE;
            LogUtils.d("InputController", "фокус переключен на индекс в 'Введите неизвестное'");
        }
    }

    // обработка нажатия стрелки вниз (сохранение)
    public void onDownArrowPressed() {
        if ("designations".equals(currentInputField)) {
            saveMeasurement();
        } else if ("unknown".equals(currentInputField) && !isConversionMode) {
            saveUnknown();
        }
    }

    // сохранение измерения
    private void saveMeasurement() {
        if (!validateMeasurementInput()) return;
        String unit = getUnitForMeasurement();
        if (unit == null) return;
        double value = parseValue();
        if (Double.isNaN(value)) return;

        String baseDesignation = logicalDesignation;
        String subscript = getSubscriptForMeasurement();
        baseDesignation = adjustBaseDesignation(baseDesignation, subscript);

        if (!validateSubscriptUniqueness(baseDesignation, subscript)) return;
        PhysicalQuantity pq = validatePhysicalQuantity(baseDesignation);
        if (pq == null) return;

        double siValue = value;
        String siUnit = unit;
        String steps = "";
        boolean isSIUnit = pq.getSiUnit().equalsIgnoreCase(unit);

        if (!isSIUnit) {
            Object[] siData = siConverter.convertToSI(baseDesignation, value, unit);
            if (siData == null) {
                LogUtils.eWithSnackbar("InputController", "ошибка конвертации: " + unit + " для " + baseDesignation, rootView);
                return;
            }
            siValue = (double) siData[0];
            siUnit = (String) siData[1];
            steps = siConverter.getConversionSteps(baseDesignation, value, unit);
        } else {
            steps = siConverter.getConversionSteps(baseDesignation, value, unit);
        }

        String displayText = buildDisplayText(value, unit, steps, isSIUnit);
        ConcreteMeasurementEntity measurement = createMeasurementEntity(baseDesignation, siValue, siUnit, value, unit, steps, isSIUnit);
        saveAndResetMeasurement(measurement, baseDesignation, unit);
    }


    private boolean validateMeasurementInput() {
        if (designationSubscriptModule != null && designationSubscriptModule.isActive() && designationSubscriptModule.isEmpty()) {
            LogUtils.wWithSnackbar("InputController", "завершите ввод индекса или удалите его", rootView);
            return false;
        }
        if (designationBuffer.length() == 0) {
            LogUtils.wWithSnackbar("InputController", "введите обозначение перед сохранением", rootView);
            return false;
        }
        if (valueBuffer.length() == 0 && valueOperationBuffer.length() == 0) {
            LogUtils.wWithSnackbar("InputController", "введите числовое значение перед сохранением", rootView);
            return false;
        }
        return true;
    }


    private String getUnitForMeasurement() {
        String unit = unitBuffer.toString();
        if (unit.isEmpty()) {
            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
            if (pq == null || pq.getSiUnit().isEmpty()) {
                LogUtils.wWithSnackbar("InputController", "укажите единицу измерения перед сохранением", rootView);
                return null;
            }
            return pq.getSiUnit();
        }
        return unit;
    }

    // парсинг значения
    private double parseValue() {
        try {
            return Double.parseDouble(valueBuffer.length() > 0 ? valueBuffer.toString() : "0");
        } catch (NumberFormatException e) {
            LogUtils.eWithSnackbar("InputController", "некорректный формат числа: " + valueBuffer.toString(), rootView);
            return Double.NaN;
        }
    }


    private String getSubscriptForMeasurement() {
        return (designationSubscriptModule != null && !designationSubscriptModule.isEmpty()) ? designationSubscriptModule.getDisplayText().toString() : "";
    }


    private String adjustBaseDesignation(String baseDesignation, String subscript) {
        if ("E_latin".equals(baseDesignation)) {
            if ("p".equals(subscript)) return "E_latin_p";
            if ("k".equals(subscript)) return "E_latin_k";
        }
        return baseDesignation;
    }

    // проверка уникальности индекса
    private boolean validateSubscriptUniqueness(String baseDesignation, String subscript) {
        List<ConcreteMeasurement> measurements = convertEntitiesToMeasurements(database.measurementDao().getAllMeasurements());
        if (!ModuleValidator.isSubscriptUnique(baseDesignation, subscript, measurements)) {
            LogUtils.eWithSnackbar("InputController", "этот индекс уже используется для '" + baseDesignation + "'", rootView);
            return false;
        }
        return true;
    }

    // валидация физической величины
    private PhysicalQuantity validatePhysicalQuantity(String baseDesignation) {
        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(baseDesignation);
        if (pq == null) {
            LogUtils.eWithSnackbar("InputController", "неизвестная физическая величина: " + baseDesignation, rootView);
        }
        return pq;
    }

    // построение отображаемого текста
    private String buildDisplayText(double value, String unit, String steps, boolean isSIUnit) {
        StringBuilder displayText = new StringBuilder();
        if (operationBuffer.length() > 0) {
            displayText.append(operationBuffer).append("(").append(displayDesignation).append(")");
        } else {
            displayText.append(displayDesignation);
        }
        String subscript = getSubscriptForMeasurement();
        if (!subscript.isEmpty()) displayText.append("_").append(subscript);
        displayText.append(" = ").append(SIConverter.formatValue(value)).append(" ").append(unit);
        if (isConversionMode && !isSIUnit && !steps.isEmpty()) {
            displayText.append(" = ").append(steps);
        }
        return displayText.toString();
    }

    // создание сущности измерения
    private ConcreteMeasurementEntity createMeasurementEntity(String baseDesignation, double siValue, String siUnit,
                                                              double value, String unit, String steps, boolean isSIUnit) {
        return new ConcreteMeasurementEntity(
                baseDesignation, siValue, siUnit, operationBuffer.toString(), valueOperationBuffer.toString(),
                getSubscriptForMeasurement(), isCurrentConstant, buildDisplayText(value, unit, steps, isSIUnit),
                value, unit, steps, isSIUnit, isConversionMode, designationUsesStix != null && designationUsesStix
        );
    }


    private void saveAndResetMeasurement(ConcreteMeasurementEntity measurement, String baseDesignation, String unit) {
        database.measurementDao().insert(measurement);
        LogUtils.logSaveMeasurement("InputController", measurement.toString());
        if (!unit.isEmpty()) lastUnitForDesignation.put(baseDesignation, unit);
        resetInput();
        if (keyboardModeSwitcher != null) keyboardModeSwitcher.switchToDesignation();
        logAllSavedData();
    }



    private void updateKeyboardMode() {
        if (keyboardModeSwitcher == null) return;
        if ("designations".equals(currentInputField)) {
            switch (focusState) {
                case MODULE: keyboardModeSwitcher.switchToNumbersAndOperations(); break;
                case DESIGNATION: keyboardModeSwitcher.switchToDesignation(); break;
                case VALUE: keyboardModeSwitcher.switchToNumbersAndOperations(); break;
                case UNIT: keyboardModeSwitcher.switchToUnits(); break;
            }
        } else if ("unknown".equals(currentInputField)) {
            keyboardModeSwitcher.switchToDesignation();
            if (focusState == FocusState.MODULE) keyboardModeSwitcher.switchToNumbersAndOperations();
        }
    }

    // обновление режима клавиатуры при смене поля
    private void updateKeyboardModeForField(String field) {
        if (keyboardModeSwitcher == null) return;
        if ("unknown".equals(field)) {
            keyboardModeSwitcher.switchToDesignation();
        } else if ("designations".equals(field)) {
            updateKeyboardMode();
        }
    }


    private void updateDisplay() {
        List<ConcreteMeasurementEntity> measurements = database.measurementDao().getAllMeasurements();
        List<UnknownQuantityEntity> unknowns = database.unknownQuantityDao().getAllUnknowns();

        SpannableStringBuilder designationsText = displayManager.buildDesignationsText(
                measurements, designationBuffer, valueBuffer, unitBuffer, operationBuffer, valueOperationBuffer,
                displayDesignation, designationUsesStix, designationSubscriptModule, focusState, currentInputField
        );
        designationsView.setText(designationsText);

        SpannableStringBuilder unknownText = displayManager.buildUnknownText(
                unknowns, unknownDisplayDesignation, unknownUsesStix, unknownSubscriptModule, currentInputField, isConversionMode
        );
        unknownView.setText(unknownText);

        updateTextColors();
        LogUtils.d("InputController", "обновлён интерфейс отображения");
    }


    private void updateTextColors() {
        if ("designations".equals(currentInputField)) {
            designationsView.setTextColor(Color.BLACK);
            unknownView.setTextColor(Color.parseColor("#A0A0A0"));
        } else {
            designationsView.setTextColor(Color.parseColor("#A0A0A0"));
            unknownView.setTextColor(Color.BLACK);
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
        displayDesignation = null;
        isCurrentConstant = false;
        designationSubscriptModule = null;
        updateKeyboardMode();
        updateDisplay();
        LogUtils.d("InputController", "сброшены все буферы ввода");
    }


    private void resetFieldState() {
        designationSubscriptModule = null;
        unknownSubscriptModule = null;
        focusState = FocusState.DESIGNATION;
        currentState = InputState.ENTERING_DESIGNATION;
        currentUnknownDesignation = null;
    }


    private void resetUnknownInput() {
        unknownDisplayDesignation = null;
        logicalUnknownDesignation = null;
        currentUnknownDesignation = null;
        unknownUsesStix = null;
        unknownSubscriptModule = null;
        focusState = FocusState.DESIGNATION;
    }


    public void clearAll() {
        if ("designations".equals(currentInputField)) {
            resetInput();
            database.measurementDao().deleteAll();
            LogUtils.d("InputController", "очищены все данные для 'Введите обозначение'");
        } else if ("unknown".equals(currentInputField) && !isConversionMode) {
            resetUnknownInput();
            database.unknownQuantityDao().deleteAll();
            LogUtils.d("InputController", "очищены все данные для 'Введите неизвестное'");
        }
        updateDisplay();
        if (keyboardModeSwitcher != null) keyboardModeSwitcher.switchToDesignation();
    }

    // логирование всех сохранённых данных
    public void logAllSavedData() {
        List<ConcreteMeasurementEntity> measurements = database.measurementDao().getAllMeasurements();
        List<UnknownQuantityEntity> unknowns = database.unknownQuantityDao().getAllUnknowns();
        StringBuilder logMessage = new StringBuilder("все сохранённые данные:\n");
        logMessage.append("измерения ('Введите обозначение'):\n");
        logMessage.append(measurements.isEmpty() ? "  нет сохранённых измерений\n" : measurements.stream()
                .map(m -> "  " + m.toString() + "\n").reduce("", String::concat));
        logMessage.append("неизвестные ('Введите неизвестное'):\n");
        logMessage.append(unknowns.isEmpty() ? "  нет сохранённых неизвестных\n" : unknowns.stream()
                .map(u -> "  " + u.toString() + "\n").reduce("", String::concat));
        LogUtils.d("InputController", logMessage.toString());
    }

    // конвертация сущностей в измерения
    private List<ConcreteMeasurement> convertEntitiesToMeasurements(List<ConcreteMeasurementEntity> entities) {
        java.util.ArrayList<ConcreteMeasurement> measurements = new java.util.ArrayList<>();
        for (ConcreteMeasurementEntity entity : entities) {
            measurements.add(new ConcreteMeasurement(
                    entity.getBaseDesignation(), entity.getValue(), entity.getUnit(),
                    entity.getDesignationOperations(), entity.getValueOperations(),
                    entity.getSubscript(), entity.isConstant(),
                    new SpannableStringBuilder(entity.getOriginalDisplay()),
                    entity.getOriginalValue(), entity.getOriginalUnit(),
                    entity.getConversionSteps(), entity.isSIUnit(), entity.isConversionMode()
            ));
        }
        return measurements;
    }
}