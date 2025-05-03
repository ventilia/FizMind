package com.example.fizmind.keyboard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.view.View;
import android.widget.TextView;

import androidx.room.Room;

import com.example.fizmind.SI.ConversionService;
import com.example.fizmind.SI.SIConverter;
import com.example.fizmind.animation.CustomTypefaceSpan;
import com.example.fizmind.database.AppDatabase;
import com.example.fizmind.database.ConcreteMeasurementEntity;
import com.example.fizmind.database.UnknownQuantityEntity;
import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.measurement.UnknownQuantity;
import com.example.fizmind.modules.InputModule;
import com.example.fizmind.modules.ModuleType;
import com.example.fizmind.modules.ModuleValidator;
import com.example.fizmind.quantly.PhysicalQuantity;
import com.example.fizmind.quantly.PhysicalQuantityRegistry;
import com.example.fizmind.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// контроллер ввода, работающий с базой данных Room
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

    private InputState currentState = InputState.ENTERING_DESIGNATION;
    private FocusState focusState = FocusState.DESIGNATION;
    private final TextView designationsView;
    private final TextView unknownView;
    private final View rootView;
    private final ConversionService conversionService;
    private final DisplayManager displayManager;
    private final AppDatabase database;
    private Typeface stixTypeface;
    private KeyboardModeSwitcher keyboardModeSwitcher;
    private boolean isConversionMode = false;
    private boolean isUnknownInputAllowed = true;
    private String currentInputField = "designations";

    // временные переменные для текущего ввода
    private String currentDesignation = "";
    private String currentValue = "";
    private String currentUnit = "";
    private String currentOperation = "";
    private String currentValueOperation = "";
    private boolean isCurrentConstant = false;
    private Boolean designationUsesStix = null;
    private String logicalDesignation = null;
    private String displayDesignation = null;
    private InputModule designationSubscriptModule = null;
    private String unknownDisplayDesignation = null;
    private String logicalUnknownDesignation = null;
    private Boolean unknownUsesStix = null;
    private InputModule unknownSubscriptModule = null;
    private String currentUnknownDesignation = null;
    private final Map<String, String> lastUnitForDesignation = new HashMap<>();
    private long lastDeleteTime = 0;
    private int deleteClickCount = 0;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300;

    // конструктор
    public InputController(TextView designationsView, TextView unknownView, ConversionService conversionService, View rootView, DisplayManager displayManager) {
        this.designationsView = designationsView;
        this.unknownView = unknownView;
        this.rootView = rootView;
        this.conversionService = conversionService;
        this.displayManager = displayManager;
        this.database = Room.databaseBuilder(rootView.getContext(), AppDatabase.class, "fizmind-db")
                .allowMainThreadQueries()
                .build();
        updateDisplay();

    }

    // установка разрешения ввода неизвестного
    public void setUnknownInputAllowed(boolean allowed) {
        isUnknownInputAllowed = allowed;
        LogUtils.logPropertySet("InputController", "разрешение ввода неизвестного", allowed);
    }

    // установка шрифта STIX
    public void setStixTypeface(Typeface stixTypeface) {
        this.stixTypeface = stixTypeface;
        LogUtils.logPropertySet("InputController", "шрифт STIX", "установлен");
    }

    // установка переключателя режимов клавиатуры
    public void setKeyboardModeSwitcher(KeyboardModeSwitcher switcher) {
        this.keyboardModeSwitcher = switcher;
        LogUtils.logPropertySet("InputController", "переключатель режимов клавиатуры", "установлен");
    }

    // установка режима конверсии
    public void setConversionMode(boolean isConversionMode) {
        this.isConversionMode = isConversionMode;
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
            resetInput();
            LogUtils.d("InputController", "сброшены модули и состояния при смене поля ввода");
        }
        currentInputField = field;
        if ("unknown".equals(field) && keyboardModeSwitcher != null) {
            keyboardModeSwitcher.switchToDesignation();
        } else if ("designations".equals(field)) {
            updateKeyboardMode();
        }
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

    // проверка наличия индекса
    public boolean hasSubscript() {
        return designationSubscriptModule != null && !designationSubscriptModule.isEmpty();
    }

    // проверка наличия неизвестного индекса
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

    // обработка ввода индекса
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

    // обработка ввода обозначения
    private void handleDesignationInput(String input, String sourceKeyboardMode, boolean keyUsesStix, String logicalId) {
        if (currentDesignation.isEmpty()) {
            if (!"Designation".equals(sourceKeyboardMode)) {
                LogUtils.wWithSnackbar("InputController", "символ обозначения должен быть из режима 'Designation'", rootView);
                return;
            }
            if (logicalId.equals("op_subscript") || logicalId.equals("mod_subscript_p") || logicalId.equals("mod_subscript_k")) {
                LogUtils.wWithSnackbar("InputController", "нельзя начинать ввод с индекса", rootView);
                return;
            }
            String adjustedLogicalId = "designation_E".equals(logicalId) ? "E_latin" : logicalId;
            currentDesignation = input;
            logicalDesignation = adjustedLogicalId;
            displayDesignation = displayManager.getDisplayTextFromLogicalId(adjustedLogicalId);
            designationUsesStix = keyUsesStix;
            if (lastUnitForDesignation.containsKey(logicalDesignation) && !currentValue.isEmpty()) {
                currentUnit = lastUnitForDesignation.get(logicalDesignation);
            } else {
                currentUnit = "";
            }
            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
            if (pq != null && pq.isConstant()) {
                currentValue = String.valueOf(pq.getConstantValue());
                currentUnit = pq.getSiUnit();
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

    // обработка ввода значения
    private void handleValueInput(String input, String logicalId) {
        if (currentDesignation.isEmpty()) {
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
                currentValue += input;
            } else if (".".equals(input)) {
                if (currentValue.isEmpty() || currentValue.equals("-")) {
                    LogUtils.wWithSnackbar("InputController", "число не может начинаться с точки", rootView);
                } else if (currentValue.contains(".")) {
                    LogUtils.wWithSnackbar("InputController", "число уже содержит точку", rootView);
                } else {
                    currentValue += input;
                }
            } else if ("-".equals(input)) {
                if (!currentValue.isEmpty()) {
                    LogUtils.wWithSnackbar("InputController", "минус можно вводить только в начале", rootView);
                } else {
                    currentValue += input;
                }
            } else if (logicalId.equals("op_abs_open")) {
                currentValueOperation += "|";
                updateDisplay();
            } else if (logicalId.equals("op_abs_close") && currentValueOperation.contains("|")) {
                currentValueOperation += currentValue + "|";
                currentValue = "";
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

    // обработка ввода единицы измерения
    private void handleUnitInput(String input, String logicalId) {
        PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
        if (pq == null) {
            LogUtils.wWithSnackbar("InputController", "неизвестная физическая величина: " + logicalDesignation, rootView);
            return;
        }
        int maxAllowedLength = pq.getAllowedUnits().stream().mapToInt(String::length).max().orElse(0);
        String potentialUnit = currentUnit + input;
        boolean validPrefix = pq.getAllowedUnits().stream().anyMatch(allowed -> allowed.startsWith(potentialUnit));
        if (validPrefix && potentialUnit.length() <= maxAllowedLength) {
            currentUnit += input;
            LogUtils.d("InputController", "добавлена единица измерения: " + input);
        } else {
            LogUtils.wWithSnackbar("InputController", "недопустимая единица измерения: " + potentialUnit, rootView);
        }
        updateDisplay();
    }

    // сохранение неизвестной величины
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
                    getUnknownsAsUnknownQuantity(), unknownDisplayDesignation, unknownUsesStix, unknownSubscriptModule, currentInputField
            );
            String serializedDisplayText = Html.toHtml(displayText);

            UnknownQuantityEntity unknown = new UnknownQuantityEntity(
                    unknownDisplayDesignation, fullLogicalDesignation, subscript,
                    unknownUsesStix != null && unknownUsesStix, serializedDisplayText
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

    // проверка, пуст ли ввод
    private boolean isInputEmpty() {
        if ("designations".equals(currentInputField)) {
            return currentDesignation.isEmpty() && currentValue.isEmpty() && currentUnit.isEmpty() &&
                    currentOperation.isEmpty() && currentValueOperation.isEmpty() &&
                    designationSubscriptModule == null;
        } else if ("unknown".equals(currentInputField)) {
            return unknownDisplayDesignation == null && unknownSubscriptModule == null;
        }
        return true;
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
            if (unknownDisplayDesignation != null) {
                unknownDisplayDesignation = null;
                logicalUnknownDesignation = null;
                currentUnknownDesignation = null;
                unknownUsesStix = null;
                unknownSubscriptModule = null;
                focusState = FocusState.DESIGNATION;
                LogUtils.d("InputController", "удалено несохраненное обозначение в 'Введите неизвестное'");
            } else if (!getUnknowns().isEmpty()) {
                database.unknownQuantityDao().deleteLastUnknown();
                LogUtils.d("InputController", "удалено последнее сохраненное неизвестное");
            } else {
                LogUtils.d("InputController", "нет данных для удаления в 'Введите неизвестное'");
            }
        } else {
            if (deleteClickCount == 2) {
                deleteLastSavedField();
                deleteClickCount = 0;
                LogUtils.logDeletion("InputController", "выполнено двойное удаление последнего сохраненного поля");
            } else {
                if (isInputEmpty() && !getMeasurements().isEmpty()) {
                    ConcreteMeasurementEntity lastMeasurement = getMeasurements().get(getMeasurements().size() - 1);
                    if (lastMeasurement.isConstant()) {
                        deleteLastSavedField();
                    } else {
                        performSingleDelete();
                    }
                } else {
                    performSingleDelete();
                }
            }
        }
        updateKeyboardMode();
        updateDisplay();
    }

    // выполнение одиночного удаления
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
                currentUnit = "";
                currentState = InputState.ENTERING_VALUE;
                focusState = FocusState.VALUE;
                LogUtils.d("InputController", "единицы измерения удалены");
            } else if (currentState == InputState.ENTERING_VALUE) {
                if (!currentValue.isEmpty()) {
                    currentValue = currentValue.substring(0, currentValue.length() - 1);
                    LogUtils.d("InputController", "удален символ из значения");
                } else if (!currentValueOperation.isEmpty()) {
                    currentValueOperation = currentValueOperation.substring(0, currentValueOperation.length() - 1);
                    LogUtils.d("InputController", "удален символ из операции");
                } else if (!currentDesignation.isEmpty()) {
                    if (designationSubscriptModule != null) {
                        designationSubscriptModule = null;
                        LogUtils.d("InputController", "модуль удален");
                    } else {
                        resetInput();
                        LogUtils.d("InputController", "обозначение удалено");
                    }
                }
            } else if (currentState == InputState.ENTERING_DESIGNATION && !currentDesignation.isEmpty()) {
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

    // удаление последнего сохраненного поля
    private void deleteLastSavedField() {
        if ("designations".equals(currentInputField)) {
            if (!getMeasurements().isEmpty()) {
                database.measurementDao().deleteLastMeasurement();
                LogUtils.d("InputController", "удалено последнее измерение");
            }
        }
    }

    // обработка нажатия левой стрелки
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
            } else if (focusState == FocusState.VALUE && !currentDesignation.isEmpty()) {
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
            if (focusState == FocusState.MODULE && unknownSubscriptModule != null && unknownSubscriptModule.isActive()) {
                unknownSubscriptModule.deactivate();
                focusState = FocusState.DESIGNATION;
                LogUtils.d("InputController", "фокус снят с индекса в 'Введите неизвестное'");
            }
            updateDisplay();
        }
    }

    // обработка нажатия правой стрелки
    public void onRightArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if (focusState == FocusState.DESIGNATION) {
                if (designationSubscriptModule != null) {
                    designationSubscriptModule.activate();
                    focusState = FocusState.MODULE;
                    LogUtils.d("InputController", "фокус переключен на индекс");
                } else if (!currentDesignation.isEmpty()) {
                    focusState = FocusState.VALUE;
                    currentState = InputState.ENTERING_VALUE;
                    LogUtils.d("InputController", "фокус переключен на значение");
                }
            } else if (focusState == FocusState.VALUE) {
                if (!currentValue.isEmpty() || !currentValueOperation.isEmpty()) {
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

    // обработка нажатия кнопки сохранения
    public void onDownArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if (designationSubscriptModule != null && designationSubscriptModule.isActive() && designationSubscriptModule.isEmpty()) {
                LogUtils.w("InputController", "ошибка сохранения: пустой активный индекс");
                LogUtils.wWithSnackbar("InputController", "завершите ввод индекса или удалите его перед сохранением", rootView);
                return;
            }
            if (currentDesignation.isEmpty()) {
                LogUtils.w("InputController", "ошибка сохранения: отсутствует обозначение");
                LogUtils.wWithSnackbar("InputController", "введите обозначение перед сохранением", rootView);
                return;
            }
            if (currentValue.isEmpty() && currentValueOperation.isEmpty()) {
                LogUtils.w("InputController", "ошибка сохранения: отсутствует числовое значение");
                LogUtils.wWithSnackbar("InputController", "введите числовое значение перед сохранением", rootView);
                return;
            }

            String unit = currentUnit;
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
                value = Double.parseDouble(currentValue.isEmpty() ? "0" : currentValue);
            } catch (NumberFormatException e) {
                LogUtils.e("InputController", "ошибка сохранения: некорректный формат числа: " + currentValue);
                LogUtils.eWithSnackbar("InputController", "некорректный формат числа: " + currentValue, rootView);
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
            if (!ModuleValidator.isSubscriptUnique(baseDesignation, subscript, getMeasurementsAsConcrete())) {
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

            double siValue = value;
            String siUnit = unit;
            String steps = "";
            boolean isSIUnit = conversionService.isSiUnit(pq, unit);

            if (!isSIUnit) {
                steps = conversionService.getSteps(pq, value, unit);
                Object[] siData = conversionService.convert(pq, value, unit);
                if (siData != null) {
                    siValue = (double) siData[0];
                    siUnit = (String) siData[1];
                } else {
                    LogUtils.logConversionError("InputController", baseDesignation, unit);
                    return;
                }
            }

            SpannableStringBuilder historyEntry = new SpannableStringBuilder();
            int start = historyEntry.length();
            if (!currentOperation.isEmpty()) {
                historyEntry.append(currentOperation).append("(").append(displayDesignation).append(")");
            } else {
                historyEntry.append(displayDesignation);
            }
            int designationEnd = historyEntry.length();

            if (designationSubscriptModule != null && !designationSubscriptModule.isEmpty()) {
                String subscriptText = designationSubscriptModule.getDisplayText().toString();
                int subscriptStart = historyEntry.length();
                historyEntry.append(subscriptText);
                int subscriptEnd = historyEntry.length();
                historyEntry.setSpan(new SubscriptSpan(), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                historyEntry.setSpan(new RelativeSizeSpan(0.75f), subscriptStart, subscriptEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if (designationUsesStix != null && designationUsesStix && stixTypeface != null) {
                historyEntry.setSpan(new CustomTypefaceSpan(stixTypeface), start, designationEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            historyEntry.append(" = ").append(SIConverter.formatValue(value)).append(" ").append(unit);

            if (isConversionMode && !isSIUnit && !steps.isEmpty()) {
                int stepsStart = historyEntry.length();
                historyEntry.append(" = ").append(steps);
                int lastEqualIndex = historyEntry.toString().lastIndexOf("= ");
                if (lastEqualIndex != -1) {
                    int resultStart = lastEqualIndex + 2;
                    int resultEnd = historyEntry.length();
                    historyEntry.setSpan(new StyleSpan(Typeface.BOLD), resultStart, resultEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            String serializedHistory = Html.toHtml(historyEntry);

            ConcreteMeasurementEntity measurement = new ConcreteMeasurementEntity(
                    baseDesignation, siValue, siUnit,
                    currentOperation, currentValueOperation,
                    subscript, isCurrentConstant, serializedHistory,
                    value, unit, steps, isSIUnit, isConversionMode
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

    // обновление отображения
    private void updateDisplay() {
        List<SpannableStringBuilder> history = new ArrayList<>();
        for (ConcreteMeasurementEntity m : getMeasurements()) {
            history.add(new SpannableStringBuilder(Html.fromHtml(m.getOriginalDisplay())));
        }

        SpannableStringBuilder designationsText = displayManager.buildDesignationsText(
                getMeasurementsAsConcrete(), history, new StringBuilder(currentDesignation),
                new StringBuilder(currentValue), new StringBuilder(currentUnit),
                new StringBuilder(currentOperation), new StringBuilder(currentValueOperation),
                displayDesignation, designationUsesStix, designationSubscriptModule, focusState, currentInputField
        );
        designationsView.setText(designationsText);

        SpannableStringBuilder unknownText = displayManager.buildUnknownText(
                getUnknownsAsUnknownQuantity(), unknownDisplayDesignation, unknownUsesStix, unknownSubscriptModule, currentInputField
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
        currentDesignation = "";
        currentValue = "";
        currentUnit = "";
        currentOperation = "";
        currentValueOperation = "";
        currentState = InputState.ENTERING_DESIGNATION;
        focusState = FocusState.DESIGNATION;
        designationUsesStix = null;
        logicalDesignation = null;
        displayDesignation = null;
        isCurrentConstant = false;
        designationSubscriptModule = null;
        updateKeyboardMode();
        updateDisplay();
        LogUtils.d("InputController", "сброшены все поля ввода");
    }

    // очистка всех данных
    public void clearAll() {
        if ("designations".equals(currentInputField)) {
            resetInput();
            for (ConcreteMeasurementEntity m : getMeasurements()) {
                database.measurementDao().delete(m);
            }
            LogUtils.d("InputController", "очищены все данные для 'Введите обозначение'");
        } else if ("unknown".equals(currentInputField)) {
            unknownDisplayDesignation = null;
            logicalUnknownDesignation = null;
            currentUnknownDesignation = null;
            unknownUsesStix = null;
            unknownSubscriptModule = null;
            focusState = FocusState.DESIGNATION;
            for (UnknownQuantityEntity u : getUnknowns()) {
                database.unknownQuantityDao().delete(u);
            }
            LogUtils.d("InputController", "очищены все данные для 'Введите неизвестное'");
        }
        updateDisplay();
        if (keyboardModeSwitcher != null) {
            keyboardModeSwitcher.switchToDesignation();
        }
    }

    // получение всех измерений
    public List<ConcreteMeasurementEntity> getMeasurements() {
        return database.measurementDao().getAllMeasurements();
    }

    // конвертация измерений в объекты ConcreteMeasurement
    private List<ConcreteMeasurement> getMeasurementsAsConcrete() {
        List<ConcreteMeasurement> measurements = new ArrayList<>();
        for (ConcreteMeasurementEntity entity : getMeasurements()) {
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

    // получение всех неизвестных
    public List<UnknownQuantityEntity> getUnknowns() {
        return database.unknownQuantityDao().getAllUnknowns();
    }

    // конвертация неизвестных в объекты UnknownQuantity
    private List<UnknownQuantity> getUnknownsAsUnknownQuantity() {
        List<UnknownQuantity> unknowns = new ArrayList<>();
        for (UnknownQuantityEntity entity : getUnknowns()) {
            unknowns.add(new UnknownQuantity(
                    entity.getDisplayDesignation(), entity.getLogicalDesignation(),
                    entity.getSubscript(), entity.isUsesStix(),
                    new SpannableStringBuilder(Html.fromHtml(entity.getDisplayText()))
            ));
        }
        return unknowns;
    }

    // логирование всех сохраненных данных
    public void logAllSavedData() {
        StringBuilder logMessage = new StringBuilder("все сохраненные данные:\n");
        logMessage.append("измерения ('Введите обозначение'):\n");
        if (getMeasurements().isEmpty()) {
            logMessage.append("  нет сохраненных измерений\n");
        } else {
            for (ConcreteMeasurementEntity m : getMeasurements()) {
                logMessage.append("  ").append(m.toString()).append("\n");
            }
        }
        logMessage.append("неизвестные ('Введите неизвестное'):\n");
        if (getUnknowns().isEmpty()) {
            logMessage.append("  нет сохраненных неизвестных\n");
        } else {
            for (UnknownQuantityEntity u : getUnknowns()) {
                logMessage.append("  ").append(u.toString()).append("\n");
            }
        }
        LogUtils.d("InputController", logMessage.toString());
    }
}