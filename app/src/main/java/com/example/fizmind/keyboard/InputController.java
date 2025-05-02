package com.example.fizmind.keyboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.TextView;

import androidx.room.Room;

import com.example.fizmind.SI.ConversionService;
import com.example.fizmind.database.AppDatabase;
import com.example.fizmind.database.ConcreteMeasurementEntity;
import com.example.fizmind.database.MeasurementDao;
import com.example.fizmind.database.UnknownQuantityEntity;
import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.measurement.UnknownQuantity;
import com.example.fizmind.modules.InputModule;
import com.example.fizmind.modules.ModuleType;
import com.example.fizmind.modules.ModuleValidator;
import com.example.fizmind.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

// контроллер ввода с полной интеграцией базы данных
public class InputController {

    private final Context context;
    private final TextView designationsView;
    private final TextView unknownView;
    private final ConversionService conversionService;
    private final View rootView;
    private final DisplayManager displayManager;
    private final MeasurementDao measurementDao;
    private Typeface stixTypeface;
    private KeyboardModeSwitcher modeSwitcher;

    private String currentInputField = "designations"; // текущее поле ввода
    private StringBuilder designationBuffer = new StringBuilder();
    private StringBuilder valueBuffer = new StringBuilder();
    private StringBuilder unitBuffer = new StringBuilder();
    private StringBuilder operationBuffer = new StringBuilder();
    private StringBuilder valueOperationBuffer = new StringBuilder();
    private String displayDesignation;
    private Boolean designationUsesStix;
    private InputModule designationSubscriptModule;
    private String unknownDisplayDesignation;
    private Boolean unknownUsesStix;
    private InputModule unknownSubscriptModule;
    private FocusState focusState = FocusState.NONE;

    // состояние фокуса
    public enum FocusState {
        NONE, DESIGNATION, VALUE, UNIT
    }

    // конструктор
    public InputController(Context context, TextView designationsView, TextView unknownView,
                           ConversionService conversionService, View rootView, DisplayManager displayManager) {
        this.context = context;
        this.designationsView = designationsView;
        this.unknownView = unknownView;
        this.conversionService = conversionService;
        this.rootView = rootView;
        this.displayManager = displayManager;

        // инициализация базы данных
        AppDatabase db = Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, "fizmind-database").allowMainThreadQueries().build();
        this.measurementDao = db.measurementDao();

        updateDisplay();
    }

    // установка шрифта STIX
    public void setStixTypeface(Typeface stixTypeface) {
        this.stixTypeface = stixTypeface;
    }

    // установка переключателя режимов клавиатуры
    public void setKeyboardModeSwitcher(KeyboardModeSwitcher switcher) {
        this.modeSwitcher = switcher;
    }

    // получение текущего поля ввода
    public String getCurrentInputField() {
        return currentInputField;
    }

    // получение текущего обозначения
    public String getCurrentDesignation() {
        return displayDesignation;
    }

    // получение текущего неизвестного обозначения
    public String getCurrentUnknownDesignation() {
        return unknownDisplayDesignation;
    }

    // проверка наличия индекса
    public boolean hasSubscript() {
        return designationSubscriptModule != null && !designationSubscriptModule.isEmpty();
    }

    // проверка наличия индекса у неизвестной величины
    public boolean hasUnknownSubscript() {
        return unknownSubscriptModule != null && !unknownSubscriptModule.isEmpty();
    }

    // обработка ввода символа
    public void onKeyInput(String displayText, String mode, boolean usesStix, String logicalId) {
        if ("designations".equals(currentInputField)) {
            handleDesignationInput(displayText, mode, usesStix, logicalId);
        } else {
            handleUnknownInput(displayText, mode, usesStix, logicalId);
        }
        updateDisplay();
    }

    // обработка ввода для измерений
    private void handleDesignationInput(String displayText, String mode, boolean usesStix, String logicalId) {
        switch (mode) {
            case "Designation":
                designationBuffer.append(displayText);
                displayDesignation = logicalId;
                designationUsesStix = usesStix;
                if (modeSwitcher != null) modeSwitcher.switchToNumbersAndOperations();
                break;
            case "Numbers_and_operations":
                if (displayText.equals("_")) {
                    if (ModuleValidator.canAddModule(ModuleType.SUBSCRIPT, designationSubscriptModule, displayDesignation)) {
                        designationSubscriptModule = new InputModule(ModuleType.SUBSCRIPT);
                    }
                } else if (displayText.equals("p") && "designation_E".equals(displayDesignation)) {
                    if (ModuleValidator.canAddModule(ModuleType.SUBSCRIPT_P, designationSubscriptModule, displayDesignation)) {
                        designationSubscriptModule = new InputModule(ModuleType.SUBSCRIPT_P);
                    }
                } else if (displayText.equals("k") && "designation_E".equals(displayDesignation)) {
                    if (ModuleValidator.canAddModule(ModuleType.SUBSCRIPT_K, designationSubscriptModule, displayDesignation)) {
                        designationSubscriptModule = new InputModule(ModuleType.SUBSCRIPT_K);
                    }
                } else if (designationSubscriptModule != null && designationSubscriptModule.isActive()) {
                    designationSubscriptModule.apply(displayText);
                } else {
                    valueBuffer.append(displayText);
                    if (modeSwitcher != null) modeSwitcher.switchToUnits();
                }
                break;
            case "Units_of_measurement":
                unitBuffer.append(displayText);
                break;
        }
    }

    // обработка ввода для неизвестных величин
    private void handleUnknownInput(String displayText, String mode, boolean usesStix, String logicalId) {
        switch (mode) {
            case "Designation":
                unknownDisplayDesignation = displayText;
                unknownUsesStix = usesStix;
                break;
            case "Numbers_and_operations":
                if (displayText.equals("_")) {
                    if (ModuleValidator.canAddModule(ModuleType.SUBSCRIPT, unknownSubscriptModule, unknownDisplayDesignation)) {
                        unknownSubscriptModule = new InputModule(ModuleType.SUBSCRIPT);
                    }
                } else if (unknownSubscriptModule != null && unknownSubscriptModule.isActive()) {
                    unknownSubscriptModule.apply(displayText);
                }
                break;
        }
    }

    // сохранение при нажатии стрелки вниз
    public void onDownArrowPressed() {
        if ("designations".equals(currentInputField)) {
            saveMeasurement();
        } else {
            saveUnknown();
        }
        updateDisplay();
    }

    // сохранение измерения
    private void saveMeasurement() {
        if (designationBuffer.length() == 0 || valueBuffer.length() == 0 || unitBuffer.length() == 0) {
            LogUtils.w("InputController", "неполные данные для сохранения измерения");
            return;
        }

        String baseDesignation = displayDesignation != null ? displayDesignation : designationBuffer.toString();
        double value = Double.parseDouble(valueBuffer.toString());
        String unit = unitBuffer.toString();
        String subscript = designationSubscriptModule != null ? designationSubscriptModule.getContent() : "";

        // проверка уникальности индекса
        if (!ModuleValidator.isSubscriptUnique(baseDesignation, subscript, getAllMeasurements())) {
            LogUtils.w("InputController", "индекс " + subscript + " для " + baseDesignation + " не уникален");
            return;
        }

        SpannableStringBuilder originalDisplay = new SpannableStringBuilder(designationBuffer + " = " + value + " " + unit);
        ConcreteMeasurement measurement = new ConcreteMeasurement(
                baseDesignation, value, unit, operationBuffer.toString(), valueOperationBuffer.toString(),
                subscript, false, originalDisplay, value, unit, "", true, false
        );

        if (measurement.validate()) {
            measurementDao.insertConcreteMeasurement(measurement.toEntity());
            resetDesignationBuffers();
        } else {
            LogUtils.w("InputController", "невалидное измерение: " + measurement);
        }
    }

    // сохранение неизвестной величины
    private void saveUnknown() {
        if (unknownDisplayDesignation == null || unknownDisplayDesignation.isEmpty()) {
            LogUtils.w("InputController", "не указано обозначение неизвестной величины");
            return;
        }

        String subscript = unknownSubscriptModule != null ? unknownSubscriptModule.getContent() : "";
        SpannableStringBuilder displayText = new SpannableStringBuilder(unknownDisplayDesignation + " = ?");
        UnknownQuantity unknown = new UnknownQuantity(
                unknownDisplayDesignation, unknownDisplayDesignation, subscript,
                unknownUsesStix != null && unknownUsesStix, displayText
        );
        measurementDao.insertUnknownQuantity(unknown.toEntity());
        resetUnknownBuffers();
    }

    // удаление последнего элемента
    public void onDeletePressed() {
        if ("unknown".equals(currentInputField)) {
            measurementDao.deleteLastUnknownQuantity();
        } else {
            measurementDao.deleteLastConcreteMeasurement();
        }
        updateDisplay();
    }

    // обработка нажатия левой стрелки
    public void onLeftArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if (unitBuffer.length() > 0) {
                setFocusState(FocusState.UNIT);
            } else if (valueBuffer.length() > 0) {
                setFocusState(FocusState.VALUE);
            } else {
                setFocusState(FocusState.DESIGNATION);
            }
        }
    }

    // обработка нажатия правой стрелки
    public void onRightArrowPressed() {
        if ("designations".equals(currentInputField)) {
            if (focusState == FocusState.DESIGNATION && valueBuffer.length() > 0) {
                setFocusState(FocusState.VALUE);
            } else if (focusState == FocusState.VALUE && unitBuffer.length() > 0) {
                setFocusState(FocusState.UNIT);
            }
        }
    }

    // очистка всех данных
    public void clearAll() {
        if ("designations".equals(currentInputField)) {
            measurementDao.clearConcreteMeasurements();
        } else {
            measurementDao.clearUnknownQuantities();
        }
        updateDisplay();
    }

    // получение всех измерений
    private List<ConcreteMeasurement> getAllMeasurements() {
        List<ConcreteMeasurementEntity> entities = measurementDao.getAllConcreteMeasurements();
        List<ConcreteMeasurement> measurements = new ArrayList<>();
        for (ConcreteMeasurementEntity entity : entities) {
            measurements.add(new ConcreteMeasurement(entity));
        }
        return measurements;
    }

    // получение всех неизвестных величин
    private List<UnknownQuantity> getAllUnknowns() {
        List<UnknownQuantityEntity> entities = measurementDao.getAllUnknownQuantities();
        List<UnknownQuantity> unknowns = new ArrayList<>();
        for (UnknownQuantityEntity entity : entities) {
            unknowns.add(new UnknownQuantity(entity));
        }
        return unknowns;
    }

    // обновление отображения
    private void updateDisplay() {
        List<ConcreteMeasurement> measurements = getAllMeasurements();
        List<UnknownQuantity> unknowns = getAllUnknowns();

        SpannableStringBuilder designationsText = displayManager.buildDesignationsText(
                measurements, designationBuffer, valueBuffer, unitBuffer,
                operationBuffer, valueOperationBuffer, displayDesignation,
                designationUsesStix, designationSubscriptModule, focusState, currentInputField
        );
        designationsView.setText(designationsText);

        SpannableStringBuilder unknownText = displayManager.buildUnknownText(
                unknowns, unknownDisplayDesignation, unknownUsesStix,
                unknownSubscriptModule, currentInputField
        );
        unknownView.setText(unknownText);
    }

    // сброс буферов измерений
    private void resetDesignationBuffers() {
        designationBuffer.setLength(0);
        valueBuffer.setLength(0);
        unitBuffer.setLength(0);
        operationBuffer.setLength(0);
        valueOperationBuffer.setLength(0);
        displayDesignation = null;
        designationUsesStix = null;
        designationSubscriptModule = null;
        focusState = FocusState.NONE;
    }

    // сброс буферов неизвестных величин
    private void resetUnknownBuffers() {
        unknownDisplayDesignation = null;
        unknownUsesStix = null;
        unknownSubscriptModule = null;
    }

    // установка состояния фокуса
    public void setFocusState(FocusState state) {
        this.focusState = state;
        updateDisplay();
    }

    // переключение поля ввода
    public void setCurrentInputField(String field) {
        this.currentInputField = field;
        updateDisplay();
    }
}