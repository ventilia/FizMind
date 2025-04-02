package com.example.fizmind.keyboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.fizmind.PhysicalQuantity;
import com.example.fizmind.PhysicalQuantityRegistry;
import com.example.fizmind.R;
import com.example.fizmind.animation.KeyboardAnimation;
import com.example.fizmind.ConversionService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyboardLogic implements KeyboardModeSwitcher {
    private final Context context;
    private final List<TextView> keyboardCells;
    private final TextView pageNumberView;
    private final TextView designationButton;
    private final TextView unitsButton;
    private final TextView numbersButton;
    private final ImageButton prevPageButton;
    private final ImageButton nextPageButton;
    private final ImageButton buttonScrollDown;
    private final TextView designationView; // Для поля "Введите обозначение"
    private final TextView unknownView;     // Для поля "Введите неизвестное"
    private InputController inputController;
    private boolean useStixFont;
    private String currentMode = "Designation";
    private int currentPage = 0;
    private final Map<String, List<List<SymbolKey>>> keyboardData;
    private Typeface stixTypeface;
    private final ImageButton leftArrowButton;
    private final ImageButton rightArrowButton;
    // Маппинг логических ID кнопок единиц измерения на их отображаемый текст
    private final Map<String, String> unitIdToUnitMap;

    public KeyboardLogic(
            Context context,
            List<TextView> keyboardCells,
            TextView pageNumberView,
            TextView designationButton,
            TextView unitsButton,
            TextView numbersButton,
            ImageButton prevPageButton,
            ImageButton nextPageButton,
            ImageButton buttonScrollDown,
            TextView designationView,
            TextView unknownView,
            ImageButton leftArrowButton,
            ImageButton rightArrowButton
    ) {
        this.context = context;
        this.keyboardCells = keyboardCells;
        this.pageNumberView = pageNumberView;
        this.designationButton = designationButton;
        this.unitsButton = unitsButton;
        this.numbersButton = numbersButton;
        this.prevPageButton = prevPageButton;
        this.nextPageButton = nextPageButton;
        this.buttonScrollDown = buttonScrollDown;
        this.designationView = designationView;
        this.unknownView = unknownView;
        this.leftArrowButton = leftArrowButton;
        this.rightArrowButton = rightArrowButton;

        try {
            stixTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/stix_two_text_italic.ttf");
        } catch (Exception e) {
            Log.e("KeyboardLogic", "Ошибка загрузки шрифта", e);
            stixTypeface = Typeface.DEFAULT;
        }
        designationButton.setTag("MODE_BUTTON");
        unitsButton.setTag("MODE_BUTTON");
        numbersButton.setTag("MODE_BUTTON");

        keyboardData = new HashMap<>();
        unitIdToUnitMap = new HashMap<>();

        // Режим "Designation" (без изменений)
        keyboardData.put("Designation", Arrays.asList(
                Arrays.asList(
                        new SymbolKey("a_latin", "a", true),
                        new SymbolKey("v_latin", "v", true),
                        new SymbolKey("s_latin", "s", true),
                        new SymbolKey("t_latin", "t", true),
                        new SymbolKey("m_latin", "m", true),
                        new SymbolKey("F_latin", "F", true),
                        new SymbolKey("designation_g", "g", false),
                        new SymbolKey("P_latin", "P", true),
                        new SymbolKey("E_latin", "E", true),
                        new SymbolKey("designation_W", "W", false),
                        new SymbolKey("designation_ρ", "ρ", false),
                        new SymbolKey("N_latin", "N", true),
                        new SymbolKey("S_latin", "S", true),
                        new SymbolKey("h_latin", "h", true),
                        new SymbolKey("designation_I", "I", false),
                        new SymbolKey("U_latin", "U", true),
                        new SymbolKey("R_latin", "R", true),
                        new SymbolKey("C_latin", "C", true),
                        new SymbolKey("L_latin", "L", true),
                        new SymbolKey("designation_Φ", "Φ", true),
                        new SymbolKey("B_latin", "B", true)
                ),
                Arrays.asList(
                        new SymbolKey("designation_rho", "ρ", false),
                        new SymbolKey("designation_V", "V", false),
                        new SymbolKey("designation_N", "N", false),
                        new SymbolKey("designation_W", "W", false),
                        new SymbolKey("designation_E", "E", false),
                        new SymbolKey("designation_k", "k", false),
                        new SymbolKey("designation_T", "T", false)
                )
        ));

        // Режим "Units_of_measurement" с разделением на страницы
        List<List<SymbolKey>> unitsPages = Arrays.asList(
                Arrays.asList( // Страница 1
                        new SymbolKey("unit_m/s", "m/s", false),
                        new SymbolKey("unit_km/h", "km/h", false),
                        new SymbolKey("unit_cm/s", "cm/s", false),
                        new SymbolKey("unit_m/s²", "m/s²", false),
                        new SymbolKey("unit_cm/s²", "cm/s²", false),
                        new SymbolKey("unit_km/h²", "km/h²", false),
                        new SymbolKey("unit_m", "m", false),
                        new SymbolKey("unit_km", "km", false),
                        new SymbolKey("unit_cm", "cm", false),
                        new SymbolKey("unit_s", "s", false),
                        new SymbolKey("unit_min", "min", false),
                        new SymbolKey("unit_h", "h", false),
                        new SymbolKey("unit_kg", "kg", false),
                        new SymbolKey("unit_g", "g", false),
                        new SymbolKey("unit_t", "t", false),
                        new SymbolKey("unit_N", "N", false),
                        new SymbolKey("unit_kN", "kN", false),
                        new SymbolKey("unit_dyne", "dyne", false),
                        new SymbolKey("unit_Pa", "Pa", false),
                        new SymbolKey("unit_kPa", "kPa", false),
                        new SymbolKey("unit_atm", "atm", false)
                ),
                Arrays.asList( // Страница 2
                        new SymbolKey("unit_J", "J", false),
                        new SymbolKey("unit_kJ", "kJ", false),
                        new SymbolKey("unit_cal", "cal", false),
                        new SymbolKey("unit_W", "W", false),
                        new SymbolKey("unit_kW", "kW", false),
                        new SymbolKey("unit_hp", "hp", false),
                        new SymbolKey("unit_kg/m³", "kg/m³", false),
                        new SymbolKey("unit_g/cm³", "g/cm³", false),
                        new SymbolKey("unit_g/mL", "g/mL", false),
                        new SymbolKey("unit_m²", "m²", false),
                        new SymbolKey("unit_cm²", "cm²", false),
                        new SymbolKey("unit_km²", "km²", false),
                        new SymbolKey("unit_A", "A", false),
                        new SymbolKey("unit_mA", "mA", false),
                        new SymbolKey("unit_kA", "kA", false),
                        new SymbolKey("unit_V", "V", false),
                        new SymbolKey("unit_kV", "kV", false),
                        new SymbolKey("unit_mV", "mV", false),
                        new SymbolKey("unit_Ω", "Ω", false),
                        new SymbolKey("unit_kΩ", "kΩ", false),
                        new SymbolKey("unit_MΩ", "MΩ", false)
                ),
                Arrays.asList( // Страница 3
                        new SymbolKey("unit_F", "F", false),
                        new SymbolKey("unit_μF", "μF", false),
                        new SymbolKey("unit_nF", "nF", false),
                        new SymbolKey("unit_H", "H", false),
                        new SymbolKey("unit_mH", "mH", false),
                        new SymbolKey("unit_μH", "μH", false),
                        new SymbolKey("unit_Wb", "Wb", false),
                        new SymbolKey("unit_Mx", "Mx", false),
                        new SymbolKey("unit_T·m²", "T·m²", false),
                        new SymbolKey("unit_T", "T", false),
                        new SymbolKey("unit_mT", "mT", false),
                        new SymbolKey("unit_G", "G", false),
                        new SymbolKey("unit_m³", "m³", false),
                        new SymbolKey("unit_L", "L", false),
                        new SymbolKey("unit_cm³", "cm³", false),
                        new SymbolKey("unit_K", "K", false),
                        new SymbolKey("unit_°C", "°C", false),
                        new SymbolKey("unit_°F", "°F", false)
                )
        );
        keyboardData.put("Units_of_measurement", unitsPages);

        // Инициализация маппинга для единиц измерения
        for (List<SymbolKey> page : unitsPages) {
            for (SymbolKey key : page) {
                unitIdToUnitMap.put(key.getLogicalId(), key.getDisplayText());
            }
        }

        // Режим "Numbers_and_operations" (без изменений)
        keyboardData.put("Numbers_and_operations", Arrays.asList(
                Arrays.asList(
                        new SymbolKey("num_1", "1", false),
                        new SymbolKey("num_2", "2", false),
                        new SymbolKey("num_3", "3", false),
                        new SymbolKey(" ", " ", false),
                        new SymbolKey("num_dot", ".", false),
                        new SymbolKey(" ", " ", false),
                        new SymbolKey(" ", " ", false),
                        new SymbolKey("num_4", "4", false),
                        new SymbolKey("num_5", "5", false),
                        new SymbolKey("num_6", "6", false),
                        new SymbolKey(" ", "", false),
                        new SymbolKey("op_exponent", "^", false),
                        new SymbolKey(" ", " ", false),
                        new SymbolKey(" ", " ", false),
                        new SymbolKey("num_7", "7", false),
                        new SymbolKey("num_8", "8", false),
                        new SymbolKey("num_9", "9", false),
                        new SymbolKey(" ", " ", false),
                        new SymbolKey("op_subscript", "_", false)
                )
        ));

        setupScrollButton();
        setModeListeners();
        setPageListeners();
        setupArrowButtons();
        applyModeButtonAnimations();
        updateModeButtonStyles();
        updateKeyboard();

        inputController = new InputController(designationView, unknownView, new ConversionService());
        inputController.setStixTypeface(stixTypeface);
        inputController.setKeyboardModeSwitcher(this);
    }

    private void setupArrowButtons() {
        leftArrowButton.setOnClickListener(v -> {
            if (inputController != null) {
                inputController.onLeftArrowPressed();
            }
        });

        rightArrowButton.setOnClickListener(v -> {
            if (inputController != null) {
                inputController.onRightArrowPressed();
            }
        });
    }

    public void setInputController(InputController inputController) {
        this.inputController = inputController;
        inputController.setKeyboardModeSwitcher(this);
        inputController.setStixTypeface(stixTypeface);
    }

    @Override
    public void switchToNumbersAndOperations() {
        if (!"Numbers_and_operations".equals(currentMode)) {
            currentMode = "Numbers_and_operations";
            currentPage = 0;
            updateModeButtonStyles();
            updateKeyboard();
        }
    }

    @Override
    public void switchToDesignation() {
        if (!"Designation".equals(currentMode)) {
            currentMode = "Designation";
            currentPage = 0;
            updateModeButtonStyles();
            updateKeyboard();
        }
    }

    @Override
    public void switchToUnits() {
        if (!"Units_of_measurement".equals(currentMode)) {
            currentMode = "Units_of_measurement";
            currentPage = 0;
            updateModeButtonStyles();
            updateKeyboard();
        }
    }

    private void setupScrollButton() {
        buttonScrollDown.setOnClickListener(v -> {
            Log.d("KeyboardLogic", "Нажата кнопка прокрутки вниз");
            scrollToBottom();
        });

        designationView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateScrollButtonVisibility();
            }
        });

        designationView.getViewTreeObserver().addOnScrollChangedListener(this::updateScrollButtonVisibility);
    }

    private void scrollToBottom() {
        designationView.post(() -> {
            int scrollY = designationView.getLayout().getHeight() - designationView.getHeight();
            if (scrollY > 0) {
                designationView.scrollTo(0, scrollY);
                Log.d("KeyboardLogic", "Прокручено к низу, scrollY = " + scrollY);
            } else {
                Log.d("KeyboardLogic", "Прокрутка не требуется, scrollY = " + scrollY);
            }
        });
    }

    private void updateScrollButtonVisibility() {
        if (designationView.getLayout() == null) {
            buttonScrollDown.setVisibility(View.GONE);
            Log.d("KeyboardLogic", "Layout не готов, кнопка скрыта");
            return;
        }
        int scrollRange = designationView.getLayout().getHeight() - designationView.getHeight();
        if (scrollRange > 0) {
            buttonScrollDown.setVisibility(View.VISIBLE);
            Log.d("KeyboardLogic", "Кнопка прокрутки видна, scrollRange = " + scrollRange);
        } else {
            buttonScrollDown.setVisibility(View.GONE);
            Log.d("KeyboardLogic", "Кнопка прокрутки скрыта, scrollRange = " + scrollRange);
        }
    }

    public Typeface getStixTypeface() {
        return stixTypeface;
    }

    public void setUseStixFont(boolean useStixFont) {
        this.useStixFont = useStixFont;
    }

    private void applyModeButtonAnimations() {
        KeyboardAnimation.applyButtonAnimation(designationButton);
        KeyboardAnimation.applyButtonAnimation(unitsButton);
        KeyboardAnimation.applyButtonAnimation(numbersButton);
    }

    private void updateKeyboard() {
        List<List<SymbolKey>> pages = keyboardData.get(currentMode);
        if (pages == null || pages.isEmpty()) return;

        if (currentPage >= pages.size()) {
            currentPage = pages.size() - 1;
        }

        List<SymbolKey> currentKeys = pages.get(currentPage);

        // Получаем текущее обозначение и список допустимых единиц измерения
        String currentDesignation = inputController != null ? inputController.getCurrentDesignation() : null;
        List<String> allowedUnits = null;
        if ("Units_of_measurement".equals(currentMode) && currentDesignation != null) {
            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(currentDesignation);
            if (pq != null) {
                allowedUnits = pq.getAllowedUnits();
                Log.d("KeyboardLogic", "Допустимые единицы для " + currentDesignation + ": " + allowedUnits);
            }
        }

        for (int i = 0; i < keyboardCells.size(); i++) {
            TextView keyView = keyboardCells.get(i);
            if (i < currentKeys.size()) {
                SymbolKey symbolKey = currentKeys.get(i);
                keyView.setText(symbolKey.getDisplayText());

                // Установка шрифта
                if (symbolKey.shouldUseStixFont()) {
                    keyView.setTypeface(stixTypeface);
                } else {
                    keyView.setTypeface(Typeface.DEFAULT);
                }

                // Применение стилей в режиме "Units_of_measurement"
                if ("Units_of_measurement".equals(currentMode) && allowedUnits != null) {
                    String unit = unitIdToUnitMap.get(symbolKey.getLogicalId());
                    if (unit != null && allowedUnits.contains(unit)) {
                        keyView.setTextColor(Color.BLACK);
                        keyView.setTypeface(Typeface.DEFAULT_BOLD);
                    } else {
                        keyView.setTextColor(Color.GRAY);
                        keyView.setTypeface(Typeface.DEFAULT);
                    }
                } else {
                    // Обычные стили для других режимов или если нет обозначения
                    if (symbolKey.isColor()) {
                        keyView.setTextColor(Color.WHITE);
                    } else {
                        keyView.setTextColor(Color.BLACK);
                    }
                    keyView.setTypeface(Typeface.DEFAULT);
                }

                keyView.setOnClickListener(view -> {
                    String displayText = symbolKey.getDisplayText();
                    String logicalId = symbolKey.getLogicalId();
                    Log.d("KeyboardLogic", "Кнопка нажата: " + logicalId);
                    if (inputController != null) {
                        boolean keyUsesStix = symbolKey.shouldUseStixFont();
                        inputController.onKeyInput(displayText, currentMode, keyUsesStix, logicalId);
                    }
                });
            } else {
                keyView.setText("");
                keyView.setOnClickListener(null);
            }
        }

        pageNumberView.setText(String.format("%d | %d", currentPage + 1, pages.size()));
    }

    private void updateModeButtonStyles() {
        if ("Designation".equals(currentMode)) {
            designationButton.setBackgroundResource(R.drawable.ic_back_black);
            designationButton.setTextColor(Color.WHITE);
            designationButton.setSelected(true);
        } else {
            designationButton.setBackgroundResource(R.drawable.ic_back);
            designationButton.setTextColor(Color.BLACK);
            designationButton.setSelected(false);
        }

        if ("Units_of_measurement".equals(currentMode)) {
            unitsButton.setBackgroundResource(R.drawable.ic_back_black);
            unitsButton.setTextColor(Color.WHITE);
            unitsButton.setSelected(true);
        } else {
            unitsButton.setBackgroundResource(R.drawable.ic_back);
            unitsButton.setTextColor(Color.BLACK);
            unitsButton.setSelected(false);
        }

        if ("Numbers_and_operations".equals(currentMode)) {
            numbersButton.setBackgroundResource(R.drawable.ic_back_black);
            numbersButton.setTextColor(Color.WHITE);
            numbersButton.setSelected(true);
        } else {
            numbersButton.setBackgroundResource(R.drawable.ic_back);
            numbersButton.setTextColor(Color.BLACK);
            numbersButton.setSelected(false);
        }
    }

    private void setModeListeners() {
        View.OnClickListener modeClickListener = view -> {
            String selectedMode = "";
            int viewId = view.getId();
            if (viewId == R.id.Designation) {
                selectedMode = "Designation";
            } else if (viewId == R.id.Units_of_measurement) {
                selectedMode = "Units_of_measurement";
            } else if (viewId == R.id.Numbers_and_operations) {
                selectedMode = "Numbers_and_operations";
            }

            if (currentMode.equals(selectedMode)) {
                return;
            }
            currentMode = selectedMode;
            currentPage = 0;
            updateModeButtonStyles();
            updateKeyboard();
        };

        designationButton.setOnClickListener(modeClickListener);
        unitsButton.setOnClickListener(modeClickListener);
        numbersButton.setOnClickListener(modeClickListener);
    }

    private void setPageListeners() {
        prevPageButton.setOnClickListener(view -> {
            if (currentPage > 0) {
                currentPage--;
            } else {
                List<List<SymbolKey>> pages = keyboardData.get(currentMode);
                if (pages != null && !pages.isEmpty()) {
                    currentPage = pages.size() - 1;
                }
            }
            updateKeyboard();
        });

        nextPageButton.setOnClickListener(view -> {
            List<List<SymbolKey>> pages = keyboardData.get(currentMode);
            if (pages != null && !pages.isEmpty()) {
                currentPage = (currentPage < pages.size() - 1) ? currentPage + 1 : 0;
            }
            updateKeyboard();
        });
    }
}