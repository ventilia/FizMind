package com.example.fizmind.keyboard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.SubscriptSpan;
import android.text.style.RelativeSizeSpan;
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

// контроллер ввода для управления данными и отображением
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

    // конструктор контроллера
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

    // установка шрифта stix
    public void setStixTypeface(Typeface stixTypeface) {
        this.stixTypeface = stixTypeface;
        LogUtils.logPropertySet("InputController", "шрифт STIX", "установлен");
    }

    // установка переключателя режимов клавиатуры
    public void setKeyboardModeSwitcher(KeyboardModeSwitcher switcher) {
        this.keyboardModeSwitcher = switcher;
        LogUtils.logPropertySet("InputController", "переключатель режимов клавиатуры", "установлен");
    }

    // установка режима перевода в си
    public void setConversionMode(boolean isConversionMode) {
        this.isConversionMode = isConversionMode;
        if (isConversionMode) {
            unknownDisplayDesignation = null;
            logicalUnknownDesignation = null;
            currentUnknownDesignation = null;
            unknownUsesStix = null;
            unknownSubscriptModule = null;
            focusState = FocusState.DESIGNATION;
            LogUtils.d("InputController", "сброшены данные 'Введите неизвестное' при переходе в режим 'Перевод в СИ'");
        }
        updateDisplay();
        LogUtils.logPropertySet("InputController", "режим", isConversionMode ? "перевод в СИ" : "калькулятор");
    }

    // установка текущего поля ввода
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
            currentUnknownDesignation = null;
            LogUtils.d("InputController", "сброшены модули и состояния при смене поля ввода");
        }
        this.currentInputField = field;
        if ("unknown".equals(field) && keyboardModeSwitcher != null) {
            keyboardModeSwitcher.switchToDesignation();
        } else if ("designations".equals(field)) {
            updateKeyboardMode();
        }
        updateDisplay();
        LogUtils.d("InputController", "текущее поле ввода установлено: " + field);
    }

    public String getCurrentDesignation() {
        return logicalDesignation;
    }

    public String getCurrentInputField() {
        return currentInputField;
    }

    public String getCurrentUnknownDesignation() {
        return currentUnknownDesignation;
    }

    public boolean hasSubscript() {
        return designationSubscriptModule != null && !designationSubscriptModule.isEmpty();
    }

    public boolean hasUnknownSubscript() {
        return unknownSubscriptModule != null && !unknownSubscriptModule.isEmpty();
    }

    // обработка ввода с клавиатуры
    public void onKeyInput(String input, String sourceKeyboardMode, boolean keyUsesStix, String logicalId) {
        LogUtils.logInputProcessing("InputController", currentState.toString(), focusState.toString(), input, logicalId, isConversionMode ? "СИ" : "калькулятор");

        if ("designations".equals(currentInputField)) {
            if (focusState == FocusState.MODULE && designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                handleModuleInput(input, logicalId);
            } else if (currentState == InputState.ENTERING_DESIGNATION) {
                handleDesignationInput(input, sourceKeyboardMode, keyUsesStix, logicalId);
            } else if (currentState == InputState.ENTERING_VALUE) {
                handleValueInput(input, logicalId);
            } else if (currentState == InputState.ENTERING_UNIT) {
                handleUnitInput(input, logicalId);
            }
        } else if ("unknown".equals(currentInputField)) {
            if (isConversionMode) {
                LogUtils.wWithSnackbar("InputController", "ввод в 'Введите неизвестное' заблокирован в режиме 'Перевод в СИ'", rootView);
                return;
            }
            if (focusState == FocusState.MODULE && unknownSubscriptModule != null && unknownSubscriptModule.isActive()) {
                if (!unknownSubscriptModule.apply(input)) {
                    LogUtils.wWithSnackbar("InputController", "недопустимый символ для индекса: " + input, rootView);
                }
            } else if (logicalId.equals("op_subscript")) {
                if (unknownSubscriptModule != null) {
                    LogUtils.wWithSnackbar("InputController", "индекс уже введен", rootView);
                    return;
                }
                unknownSubscriptModule = new InputModule(ModuleType.SUBSCRIPT);
                unknownSubscriptModule.activate();
                focusState = FocusState.MODULE;
                updateKeyboardMode();
            } else if (logicalId.equals("mod_subscript_p") || logicalId.equals("mod_subscript_k")) {
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
                focusState = FocusState.DESIGNATION;
                updateKeyboardMode();
            } else if (unknownDisplayDesignation == null) {
                String adjustedLogicalId = "designation_E".equals(logicalId) ? "E_latin" : logicalId;
                unknownDisplayDesignation = displayManager.getDisplayTextFromLogicalId(adjustedLogicalId);
                logicalUnknownDesignation = adjustedLogicalId;
                currentUnknownDesignation = adjustedLogicalId;
                unknownUsesStix = keyUsesStix;
                focusState = FocusState.DESIGNATION;
                LogUtils.d("InputController", "введено неизвестное обозначение: " + unknownDisplayDesignation + " (логическое: " + adjustedLogicalId + ")");
            } else {
                LogUtils.wWithSnackbar("InputController", "в 'Введите неизвестное' можно ввести только одно обозначение", rootView);
            }
        }
        updateDisplay();
    }

    private void handleModuleInput(String input, String logicalId) {
        if (designationSubscriptModule.getType() == ModuleType.SUBSCRIPT && input.matches("[a-zA-Z0-9]")) {
            if (!designationSubscriptModule.apply(input)) {
                LogUtils.wWithSnackbar("InputController", "недопустимый символ для индекса: " + input, rootView);
            }
        } else {
            designationSubscriptModule.deactivate();
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
    }

    private void handleDesignationInput(String input, String sourceKeyboardMode, boolean keyUsesStix, String logicalId) {
        if (designationBuffer.length() == 0) {
            if (!"Designation".equals(sourceKeyboardMode)) {
                LogUtils.wWithSnackbar("InputController", "символ обозначения должен быть из режима 'Designation'", rootView);
                return;
            }
            if (logicalId.equals("op_subscript") || logicalId.equals("mod_subscript_p") || logicalId.equals("mod_subscript_k")) {
                LogUtils.wWithSnackbar("InputController", "нельзя начинать ввод с индекса", rootView);
                return;
            }
            String adjustedLogicalId = "designation_E".equals(logicalId) ? "E_latin" : logicalId;
            designationBuffer.append(input);
            logicalDesignation = adjustedLogicalId;
            displayDesignation = displayManager.getDisplayTextFromLogicalId(adjustedLogicalId);
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
                updateKeyboardMode();
            }
            LogUtils.d("InputController", "введено обозначение: " + displayDesignation + " (логическое: " + adjustedLogicalId + ")");
        } else {
            if (input.matches("[0-9]") || ".".equals(input) || "-".equals(input)) {
                currentState = InputState.ENTERING_VALUE;
                focusState = FocusState.VALUE;
                handleValueInput(input, logicalId);
            } else if (logicalId.equals("op_subscript")) {
                if (!ModuleValidator.canAddModule(ModuleType.SUBSCRIPT, designationSubscriptModule, logicalDesignation)) {
                    LogUtils.wWithSnackbar("InputController", "нельзя добавить индекс", rootView);
                    return;
                }
                designationSubscriptModule = new InputModule(ModuleType.SUBSCRIPT);
                designationSubscriptModule.activate();
                focusState = FocusState.MODULE;
                updateKeyboardMode();
            } else if (logicalId.equals("mod_subscript_p")) {
                if (!ModuleValidator.canAddModule(ModuleType.SUBSCRIPT_P, designationSubscriptModule, logicalDesignation)) {
                    LogUtils.wWithSnackbar("InputController", "нельзя добавить модуль 'p'", rootView);
                    return;
                }
                designationSubscriptModule = new InputModule(ModuleType.SUBSCRIPT_P);
                focusState = FocusState.VALUE;
                currentState = InputState.ENTERING_VALUE;
                updateKeyboardMode();
            } else if (logicalId.equals("mod_subscript_k")) {
                if (!ModuleValidator.canAddModule(ModuleType.SUBSCRIPT_K, designationSubscriptModule, logicalDesignation)) {
                    LogUtils.wWithSnackbar("InputController", "нельзя добавить модуль 'k'", rootView);
                    return;
                }
                designationSubscriptModule = new InputModule(ModuleType.SUBSCRIPT_K);
                focusState = FocusState.VALUE;
                currentState = InputState.ENTERING_VALUE;
                updateKeyboardMode();
            } else {
                LogUtils.wWithSnackbar("InputController", "обозначение уже введено, ожидается число или индекс", rootView);
            }
        }
    }

    private void handleValueInput(String input, String logicalId) {
        if (designationBuffer.length() == 0) {
            LogUtils.wWithSnackbar("InputController", "нельзя ввести число без обозначения", rootView);
            return;
        }
        if (logicalId.equals("op_subscript")) {
            if (!ModuleValidator.canAddModule(ModuleType.SUBSCRIPT, designationSubscriptModule, logicalDesignation)) {
                LogUtils.wWithSnackbar("InputController", "нельзя добавить индекс", rootView);
                return;
            }
            designationSubscriptModule = new InputModule(ModuleType.SUBSCRIPT);
            designationSubscriptModule.activate();
            focusState = FocusState.MODULE;
            updateKeyboardMode();
            updateDisplay();
        } else if (logicalId.equals("mod_subscript_p")) {
            if (!ModuleValidator.canAddModule(ModuleType.SUBSCRIPT_P, designationSubscriptModule, logicalDesignation)) {
                LogUtils.wWithSnackbar("InputController", "нельзя добавить модуль 'p'", rootView);
                return;
            }
            designationSubscriptModule = new InputModule(ModuleType.SUBSCRIPT_P);
            focusState = FocusState.VALUE;
            updateKeyboardMode();
            updateDisplay();
        } else if (logicalId.equals("mod_subscript_k")) {
            if (!ModuleValidator.canAddModule(ModuleType.SUBSCRIPT_K, designationSubscriptModule, logicalDesignation)) {
                LogUtils.wWithSnackbar("InputController", "нельзя добавить модуль 'k'", rootView);
                return;
            }
            designationSubscriptModule = new InputModule(ModuleType.SUBSCRIPT_K);
            focusState = FocusState.VALUE;
            updateKeyboardMode();
            updateDisplay();
        } else {
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
    }

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

    // сохранение неизвестного в базе данных
    private void saveUnknown() {
        if (unknownDisplayDesignation != null) {
            if (unknownSubscriptModule != null && unknownSubscriptModule.isActive() && unknownSubscriptModule.isEmpty()) {
                LogUtils.w("InputController", "ошибка сохранения: пустой активный индекс");
                LogUtils.wWithSnackbar("InputController", "завершите ввод индекса или удалите его перед сохранением", rootView);
                return;
            }
            String subscript = (unknownSubscriptModule != null && !unknownSubscriptModule.isEmpty()) ? unknownSubscriptModule.getDisplayText().toString() : "";
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

            SpannableStringBuilder displayText = displayManager.buildUnknownText(
                    database.unknownQuantityDao().getAllUnknowns(), unknownDisplayDesignation, unknownUsesStix,
                    unknownSubscriptModule, currentInputField, isConversionMode
            );

            // преобразуем в html для сохранения форматирования
            String htmlDisplayText = Html.toHtml(displayText);

            UnknownQuantityEntity unknown = new UnknownQuantityEntity(
                    unknownDisplayDesignation, fullLogicalDesignation, subscript,
                    unknownUsesStix != null && unknownUsesStix, htmlDisplayText
            );
            database.unknownQuantityDao().insert(unknown);
            LogUtils.logSaveUnknown("InputController", unknown.toString());
            unknownDisplayDesignation = null;
            logicalUnknownDesignation = null;
            currentUnknownDesignation = null;
            unknownUsesStix = null;
            unknownSubscriptModule = null;
            focusState = FocusState.DESIGNATION;
            updateDisplay();
        }
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
            if (isConversionMode) {
                LogUtils.d("InputController", "удаление в 'Введите неизвестное' игнорируется в режиме 'Перевод в СИ'");
                return;
            }
            if (unknownDisplayDesignation != null) {
                unknownDisplayDesignation = null;
                logicalUnknownDesignation = null;
                currentUnknownDesignation = null;
                unknownUsesStix = null;
                unknownSubscriptModule = null;
                focusState = FocusState.DESIGNATION;
                LogUtils.d("InputController", "удалено несохраненное обозначение в 'Введите неизвестное'");
            } else {
                database.unknownQuantityDao().deleteLastUnknown();
                LogUtils.d("InputController", "удалено последнее сохраненное неизвестное");
            }
        } else {
            if (deleteClickCount == 2) {
                database.measurementDao().deleteLastMeasurement();
                deleteClickCount = 0;
                LogUtils.logDeletion("InputController", "выполнено двойное удаление последнего сохраненного поля");
            } else {
                performSingleDelete();
            }
        }
        updateKeyboardMode();
        updateDisplay();
    }

    private void performSingleDelete() {
        if ("designations".equals(currentInputField)) {
            if (focusState == FocusState.MODULE && designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                if (designationSubscriptModule.getType() == ModuleType.SUBSCRIPT && designationSubscriptModule.getContent().length() > 1) {
                    if (designationSubscriptModule.deleteChar()) {
                        designationSubscriptModule = null;
                        focusState = FocusState.DESIGNATION;
                        LogUtils.d("InputController", "индекс удален полностью");
                    }
                } else {
                    designationSubscriptModule.deleteEntire();
                    designationSubscriptModule = null;
                    focusState = FocusState.DESIGNATION;
                    LogUtils.d("InputController", "модуль удален целиком");
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
                    if (designationSubscriptModule != null) {
                        designationSubscriptModule = null;
                        LogUtils.d("InputController", "модуль удален");
                    } else {
                        resetInput();
                        LogUtils.d("InputController", "обозначение удалено");
                    }
                }
            } else if (currentState == InputState.ENTERING_DESIGNATION && designationBuffer.length() > 0) {
                if (designationSubscriptModule != null) {
                    designationSubscriptModule = null;
                    LogUtils.d("InputController", "модуль удален");
                } else {
                    resetInput();
                    LogUtils.d("InputController", "обозначение удалено");
                }
            }
        }
    }

    // обработка нажатия стрелки влево
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
            updateKeyboardMode();
            updateDisplay();
        } else if ("unknown".equals(currentInputField)) {
            if (isConversionMode) {
                return;
            }
            if (focusState == FocusState.MODULE && unknownSubscriptModule != null && unknownSubscriptModule.isActive()) {
                unknownSubscriptModule.deactivate();
                focusState = FocusState.DESIGNATION;
                LogUtils.d("InputController", "фокус снят с индекса в 'Введите неизвестное'");
            }
            updateDisplay();
        }
    }

    // обработка нажатия стрелки вправо
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
            if (isConversionMode) {
                return;
            }
            if (focusState == FocusState.DESIGNATION && unknownSubscriptModule != null) {
                unknownSubscriptModule.activate();
                focusState = FocusState.MODULE;
                LogUtils.d("InputController", "фокус переключен на индекс в 'Введите неизвестное'");
            }
            updateDisplay();
        }
    }

    // сохранение измерения с конвертацией в си
    public void onDownArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if (designationSubscriptModule != null && designationSubscriptModule.isActive() && designationSubscriptModule.isEmpty()) {
                LogUtils.w("InputController", "ошибка сохранения: пустой активный индекс");
                LogUtils.wWithSnackbar("InputController", "завершите ввод индекса или удалите его перед сохранением", rootView);
                return;
            }
            if (designationBuffer.length() == 0) {
                LogUtils.w("InputController", "ошибка сохранения: отсутствует обозначение");
                LogUtils.wWithSnackbar("InputController", "введите обозначение перед сохранением", rootView);
                return;
            }
            if (valueBuffer.length() == 0 && valueOperationBuffer.length() == 0) {
                LogUtils.w("InputController", "ошибка сохранения: отсутствует числовое значение");
                LogUtils.wWithSnackbar("InputController", "введите числовое значение перед сохранением", rootView);
                return;
            }

            String unit = unitBuffer.toString();
            if (unit.isEmpty()) {
                PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
                if (pq == null) {
                    LogUtils.e("InputController", "ошибка сохранения: неизвестная физическая величина: " + logicalDesignation);
                    LogUtils.eWithSnackbar("InputController", "неизвестная физическая величина: " + logicalDesignation, rootView);
                    return;
                }
                unit = pq.getSiUnit();
                if (unit.isEmpty()) {
                    LogUtils.w("InputController", "ошибка сохранения: отсутствует единица измерения");
                    LogUtils.wWithSnackbar("InputController", "укажите единицу измерения перед сохранением", rootView);
                    return;
                }
            }

            double value;
            try {
                value = Double.parseDouble(valueBuffer.length() > 0 ? valueBuffer.toString() : "0");
            } catch (NumberFormatException e) {
                LogUtils.e("InputController", "ошибка сохранения: некорректный формат числа: " + valueBuffer.toString());
                LogUtils.eWithSnackbar("InputController", "некорректный формат числа: " + valueBuffer.toString(), rootView);
                return;
            }

            String baseDesignation = logicalDesignation;
            String subscript = (designationSubscriptModule != null && !designationSubscriptModule.isEmpty()) ? designationSubscriptModule.getDisplayText().toString() : "";
            if ("E_latin".equals(baseDesignation)) {
                if ("p".equals(subscript)) {
                    baseDesignation = "E_latin_p";
                    subscript = "";
                } else if ("k".equals(subscript)) {
                    baseDesignation = "E_latin_k";
                    subscript = "";
                }
            }

            String fullDesignation = subscript.isEmpty() ? baseDesignation : baseDesignation + "_" + subscript;
            List<ConcreteMeasurement> existingMeasurements = convertEntitiesToMeasurements(database.measurementDao().getAllMeasurements());
            if (!ModuleValidator.isSubscriptUnique(baseDesignation, subscript, existingMeasurements)) {
                LogUtils.e("InputController", "ошибка сохранения: индекс '" + subscript + "' уже используется для обозначения: " + baseDesignation);
                LogUtils.eWithSnackbar("InputController", "этот индекс уже используется для '" + baseDesignation + "'. используйте другой индекс", rootView);
                return;
            }

            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(baseDesignation);
            if (pq == null) {
                LogUtils.e("InputController", "ошибка сохранения: неизвестная физическая величина: " + baseDesignation);
                LogUtils.eWithSnackbar("InputController", "неизвестная физическая величина: " + baseDesignation, rootView);
                return;
            }

            // конвертация в си, если единица не си
            double siValue = value;
            String siUnit = unit;
            String steps = "";
            boolean isSIUnit = pq.getSiUnit().equalsIgnoreCase(unit);

            if (!isSIUnit) {
                Object[] siData = siConverter.convertToSI(baseDesignation, value, unit);
                if (siData != null) {
                    siValue = (double) siData[0];
                    siUnit = (String) siData[1];
                    steps = siConverter.getConversionSteps(baseDesignation, value, unit);
                } else {
                    LogUtils.logConversionError("InputController", baseDesignation, unit);
                    LogUtils.eWithSnackbar("InputController", "ошибка конвертации: " + unit + " для " + baseDesignation, rootView);
                    return;
                }
            } else {
                steps = siConverter.getConversionSteps(baseDesignation, value, unit);
            }

            // создание отформатированного текста с нижним индексом
            SpannableStringBuilder displayText = new SpannableStringBuilder();
            if (operationBuffer.length() > 0) {
                displayText.append(operationBuffer).append("(").append(displayDesignation).append(")");
            } else {
                displayText.append(displayDesignation);
            }
            if (!subscript.isEmpty()) {
                int subscriptStart = displayText.length();
                displayText.append(subscript);
                int subscriptEnd = displayText.length();
                displayText.setSpan(new SubscriptSpan(), subscriptStart, subscriptEnd, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
                displayText.setSpan(new RelativeSizeSpan(0.75f), subscriptStart, subscriptEnd, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            displayText.append(" = ").append(SIConverter.formatValue(value)).append(" ").append(unit);
            if (isConversionMode && !isSIUnit && !steps.isEmpty()) {
                displayText.append(" = ").append(steps);
            }

            // сохранение как html для сохранения форматирования
            String htmlDisplayText = Html.toHtml(displayText);

            // сохранение в базу данных
            ConcreteMeasurementEntity measurement = new ConcreteMeasurementEntity(
                    baseDesignation, siValue, siUnit,
                    operationBuffer.toString(), valueOperationBuffer.toString(),
                    subscript, isCurrentConstant, htmlDisplayText,
                    value, unit, steps, isSIUnit, isConversionMode,
                    designationUsesStix != null && designationUsesStix
            );
            database.measurementDao().insert(measurement);
            LogUtils.logSaveMeasurement("InputController", measurement.toString());

            if (!unit.isEmpty()) {
                lastUnitForDesignation.put(baseDesignation, unit);
            }

            resetInput();
            if (keyboardModeSwitcher != null) {
                keyboardModeSwitcher.switchToDesignation();
            }
            logAllSavedData();
        } else if ("unknown".equals(currentInputField)) {
            if (isConversionMode) {
                LogUtils.d("InputController", "сохранение в 'Введите неизвестное' игнорируется в режиме 'Перевод в СИ'");
                return;
            }
            saveUnknown();
        }
    }

    // обновление режима клавиатуры
    private void updateKeyboardMode() {
        if (keyboardModeSwitcher != null) {
            if ("designations".equals(currentInputField)) {
                if (focusState == FocusState.MODULE) {
                    keyboardModeSwitcher.switchToNumbersAndOperations();
                } else if (currentState == InputState.ENTERING_DESIGNATION) {
                    keyboardModeSwitcher.switchToDesignation();
                } else if (currentState == InputState.ENTERING_VALUE) {
                    keyboardModeSwitcher.switchToNumbersAndOperations();
                } else if (currentState == InputState.ENTERING_UNIT) {
                    keyboardModeSwitcher.switchToUnits();
                }
            } else if ("unknown".equals(currentInputField)) {
                if (focusState == FocusState.MODULE) {
                    keyboardModeSwitcher.switchToNumbersAndOperations();
                } else {
                    keyboardModeSwitcher.switchToDesignation();
                }
            }
        }
    }

    // обновление отображения интерфейса
    private void updateDisplay() {
        List<ConcreteMeasurementEntity> measurements = database.measurementDao().getAllMeasurements();
        List<UnknownQuantityEntity> unknowns = database.unknownQuantityDao().getAllUnknowns();

        SpannableStringBuilder designationsText = displayManager.buildDesignationsText(
                measurements, designationBuffer, valueBuffer, unitBuffer,
                operationBuffer, valueOperationBuffer, displayDesignation,
                designationUsesStix, designationSubscriptModule, focusState, currentInputField
        );
        designationsView.setText(designationsText);

        SpannableStringBuilder unknownText = displayManager.buildUnknownText(
                unknowns, unknownDisplayDesignation, unknownUsesStix, unknownSubscriptModule, currentInputField, isConversionMode
        );
        unknownView.setText(unknownText);

        if ("designations".equals(currentInputField)) {
            designationsView.setTextColor(Color.BLACK);
            unknownView.setTextColor(Color.parseColor("#A0A0A0"));
        } else {
            designationsView.setTextColor(Color.parseColor("#A0A0A0"));
            unknownView.setTextColor(Color.BLACK);
        }
        LogUtils.d("InputController", "обновлен интерфейс отображения");
    }

    // сброс ввода
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

    // очистка всех данных
    public void clearAll() {
        if ("designations".equals(currentInputField)) {
            resetInput();
            database.measurementDao().deleteAll();
            LogUtils.d("InputController", "очищены все данные для 'Введите обозначение'");
        } else if ("unknown".equals(currentInputField)) {
            if (isConversionMode) {
                LogUtils.d("InputController", "очистка 'Введите неизвестное' игнорируется в режиме 'Перевод в СИ'");
            } else {
                unknownDisplayDesignation = null;
                logicalUnknownDesignation = null;
                currentUnknownDesignation = null;
                unknownUsesStix = null;
                unknownSubscriptModule = null;
                focusState = FocusState.DESIGNATION;
                database.unknownQuantityDao().deleteAll();
                LogUtils.d("InputController", "очищены все данные для 'Введите неизвестное'");
            }
        }
        updateDisplay();
        if (keyboardModeSwitcher != null) {
            keyboardModeSwitcher.switchToDesignation();
        }
    }

    // логирование всех сохраненных данных
    public void logAllSavedData() {
        List<ConcreteMeasurementEntity> measurements = database.measurementDao().getAllMeasurements();
        List<UnknownQuantityEntity> unknowns = database.unknownQuantityDao().getAllUnknowns();
        StringBuilder logMessage = new StringBuilder("все сохраненные данные:\n");
        logMessage.append("измерения ('Введите обозначение'):\n");
        if (measurements.isEmpty()) {
            logMessage.append("  нет сохраненных измерений\n");
        } else {
            for (ConcreteMeasurementEntity m : measurements) {
                logMessage.append("  ").append(m.toString()).append("\n");
            }
        }
        logMessage.append("неизвестные ('Введите неизвестное'):\n");
        if (unknowns.isEmpty()) {
            logMessage.append("  нет сохраненных неизвестных\n");
        } else {
            for (UnknownQuantityEntity u : unknowns) {
                logMessage.append("  ").append(u.toString()).append("\n");
            }
        }
        LogUtils.d("InputController", logMessage.toString());
    }

    // преобразование сущностей в измерения
    private List<ConcreteMeasurement> convertEntitiesToMeasurements(List<ConcreteMeasurementEntity> entities) {
        java.util.ArrayList<ConcreteMeasurement> measurements = new java.util.ArrayList<>();
        for (ConcreteMeasurementEntity entity : entities) {
            measurements.add(new ConcreteMeasurement(
                    entity.getBaseDesignation(), entity.getValue(), entity.getUnit(),
                    entity.getDesignationOperations(), entity.getValueOperations(),
                    entity.getSubscript(), entity.isConstant(),
                    new SpannableStringBuilder(Html.fromHtml(entity.getOriginalDisplay())),
                    entity.getOriginalValue(), entity.getOriginalUnit(),
                    entity.getConversionSteps(), entity.isSIUnit(), entity.isConversionMode()
            ));
        }
        return measurements;
    }
}