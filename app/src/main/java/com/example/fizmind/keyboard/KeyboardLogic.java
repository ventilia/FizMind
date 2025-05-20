package com.example.fizmind.keyboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.fizmind.R;
import com.example.fizmind.SI.ConversionService;
import com.example.fizmind.animation.KeyboardAnimation;
import com.example.fizmind.database.AppDatabase;
import com.example.fizmind.quantly.PhysicalQuantity;
import com.example.fizmind.quantly.PhysicalQuantityRegistry;
import com.example.fizmind.utils.LogUtils;

import java.util.ArrayList;
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
    private final TextView designationView;
    private final TextView unknownView;
    private final View rootView;
    private InputController inputController;
    private String currentMode = "Designation";
    private int currentPage = 0;
    private final Map<String, List<List<SymbolKey>>> keyboardData;
    private Typeface stixTypeface;
    private final ImageButton leftArrowButton;
    private final ImageButton rightArrowButton;
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
            ImageButton rightArrowButton,
            View rootView,
            AppDatabase database
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
        this.rootView = rootView;

        try {
            stixTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/stix_two_text_italic.ttf");
        } catch (Exception e) {
            LogUtils.logFontLoadError("KeyboardLogic", e);
            stixTypeface = Typeface.DEFAULT;
        }

        designationButton.setTag("MODE_BUTTON");
        unitsButton.setTag("MODE_BUTTON");
        numbersButton.setTag("MODE_BUTTON");

        keyboardData = new HashMap<>();
        unitIdToUnitMap = new HashMap<>();

        keyboardData.put("Designation", Arrays.asList(
                Arrays.asList(
                        new SymbolKey("s_latin", "s", true),
                        new SymbolKey("designation_t", "t", false),
                        new SymbolKey("v_latin", "v", true),
                        new SymbolKey("a_latin", "a", true),
                        new SymbolKey("m_latin", "m", true),
                        new SymbolKey("F_latin", "F", true),
                        new SymbolKey("designation_P", "P", false),
                        new SymbolKey("designation_ρ", "ρ", false),
                        new SymbolKey("designation_p", "p", false),
                        new SymbolKey("designation_A", "A", false),
                        new SymbolKey("designation_N", "N", false),
                        new SymbolKey("E_latin", "E", true),
                        new SymbolKey("designation_T", "T", false),
                        new SymbolKey("designation_Q", "Q", false),
                        new SymbolKey("designation_I", "I", false),
                        new SymbolKey("U_latin", "U", true),
                        new SymbolKey("R_latin", "R", true),
                        new SymbolKey("P_power", "P", false),
                        new SymbolKey("designation_c", "c", false),
                        new SymbolKey("designation_λ", "λ", false),
                        new SymbolKey("designation_f", "f", false)
                ),
                Arrays.asList(
                        new SymbolKey("designation_V", "V", false),
                        new SymbolKey("S_latin", "S", true),
                        new SymbolKey("h_latin", "h", true),
                        new SymbolKey("c_latin", "c", true),
                        new SymbolKey("designation_g", "g", false)
                )
        ));

        List<List<SymbolKey>> unitsPages = Arrays.asList(
                Arrays.asList(
                        new SymbolKey("unit_cm", "cm", false),
                        new SymbolKey("unit_m", "m", false),
                        new SymbolKey("unit_km", "km", false),
                        new SymbolKey("unit_ms", "ms", false),
                        new SymbolKey("unit_s", "s", false),
                        new SymbolKey("unit_min", "min", false),
                        new SymbolKey("unit_cm/s", "cm/s", false),
                        new SymbolKey("unit_m/s", "m/s", false),
                        new SymbolKey("unit_km/h", "km/h", false),
                        new SymbolKey("unit_cm/s²", "cm/s²", false),
                        new SymbolKey("unit_m/s²", "m/s²", false),
                        new SymbolKey("unit_km/h²", "km/h²", false),
                        new SymbolKey("unit_g", "g", false),
                        new SymbolKey("unit_kg", "kg", false),
                        new SymbolKey("unit_t", "t", false),
                        new SymbolKey("unit_dyne", "dyne", false),
                        new SymbolKey("unit_N", "n", false),
                        new SymbolKey("unit_kN", "kn", false),
                        new SymbolKey("unit_g/cm³", "g/cm³", false),
                        new SymbolKey("unit_kg/m³", "kg/m³", false),
                        new SymbolKey("unit_t/m³", "t/m³", false)
                ),
                Arrays.asList(
                        new SymbolKey("unit_mmHg", "mmhg", false),
                        new SymbolKey("unit_Pa", "pa", false),
                        new SymbolKey("unit_atm", "atm", false),
                        new SymbolKey("unit_erg", "erg", false),
                        new SymbolKey("unit_j", "j", false), // изменено с "J" на "j"
                        new SymbolKey("unit_kj", "kj", false), // изменено с "kJ" на "kj"
                        new SymbolKey("unit_erg/s", "erg/s", false),
                        new SymbolKey("unit_W", "w", false),
                        new SymbolKey("unit_kW", "kw", false),
                        new SymbolKey("unit_°C", "°c", false),
                        new SymbolKey("unit_K", "k", false),
                        new SymbolKey("unit_°F", "°f", false),
                        new SymbolKey("unit_cal", "cal", false),
                        new SymbolKey("unit_mA", "ma", false),
                        new SymbolKey("unit_A", "a", false),
                        new SymbolKey("unit_kA", "ka", false),
                        new SymbolKey("unit_mV", "mv", false),
                        new SymbolKey("unit_V", "v", false),
                        new SymbolKey("unit_kV", "kv", false),
                        new SymbolKey("unit_mΩ", "mω", false),
                        new SymbolKey("unit_Ω", "ω", false)
                ),
                Arrays.asList(
                        new SymbolKey("unit_kΩ", "kω", false),
                        new SymbolKey("unit_mW", "mw", false),
                        new SymbolKey("unit_km/s", "km/s", false),
                        new SymbolKey("unit_nm", "nm", false),
                        new SymbolKey("unit_mHz", "mhz", false),
                        new SymbolKey("unit_Hz", "hz", false),
                        new SymbolKey("unit_kHz", "khz", false),
                        new SymbolKey("unit_cm³", "cm³", false),
                        new SymbolKey("unit_m³", "m³", false),
                        new SymbolKey("unit_L", "l", false),
                        new SymbolKey("unit_cm²", "cm²", false),
                        new SymbolKey("unit_m²", "m²", false),
                        new SymbolKey("unit_km²", "km²", false),
                        new SymbolKey("unit_km/s²", "km/s²", false)
                )
        );

        keyboardData.put("Units_of_measurement", unitsPages);

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
                        new SymbolKey(" ", " ", false),
                        new SymbolKey("op_subscript", "_", false),
                        new SymbolKey(" ", " ", false),
                        new SymbolKey(" ", " ", false),
                        new SymbolKey("num_7", "7", false),
                        new SymbolKey("num_8", "8", false),
                        new SymbolKey("num_9", "9", false),
                        new SymbolKey(" ", " ", false),
                        new SymbolKey("num_0", "0", false)
                )
        ));

        for (List<SymbolKey> page : unitsPages) {
            for (SymbolKey key : page) {
                unitIdToUnitMap.put(key.getLogicalId(), key.getDisplayText());
            }
        }

        DisplayManager displayManager = new DisplayManager(stixTypeface, database);
        inputController = new InputController(designationView, unknownView, database, rootView, displayManager);
        inputController.setStixTypeface(stixTypeface);
        inputController.setKeyboardModeSwitcher(this);

        setupScrollButton();
        setModeListeners();
        setPageListeners();
        setupArrowButtons();
        applyModeButtonAnimations();
        updateModeButtonStyles();
        updateKeyboard();
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
            LogUtils.logButtonPressed("KeyboardLogic", "прокрутка вниз");
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
                LogUtils.d("KeyboardLogic", "прокручено к низу, scrollY = " + scrollY);
            }
        });
    }

    private void updateScrollButtonVisibility() {
        if (designationView.getLayout() == null) {
            buttonScrollDown.setVisibility(View.GONE);
            return;
        }
        int scrollRange = designationView.getLayout().getHeight() - designationView.getHeight();
        buttonScrollDown.setVisibility(scrollRange > 0 ? View.VISIBLE : View.GONE);
    }

    public Typeface getStixTypeface() {
        return stixTypeface;
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

        List<SymbolKey> baseKeys = pages.get(currentPage);
        List<SymbolKey> displayKeys = new ArrayList<>(baseKeys);

        String currentField = inputController.getCurrentInputField();
        String currentDesignation = null;
        if ("designations".equals(currentField)) {
            currentDesignation = inputController.getCurrentDesignation();
        } else if ("unknown".equals(currentField)) {
            currentDesignation = inputController.getCurrentUnknownDesignation();
        }

        if ("Numbers_and_operations".equals(currentMode) &&
                ("designation_E".equals(currentDesignation) || "E_latin".equals(currentDesignation))) {
            boolean hasSubscript = false;
            if ("designations".equals(currentField)) {
                hasSubscript = inputController.hasSubscript();
            } else if ("unknown".equals(currentField)) {
                hasSubscript = inputController.hasUnknownSubscript();
            }
            if (!hasSubscript) {
                displayKeys.add(new SymbolKey("mod_subscript_p", "p", false));
                displayKeys.add(new SymbolKey("mod_subscript_k", "k", false));
            }
        }

        List<String> allowedUnits = null;
        if ("Units_of_measurement".equals(currentMode) && currentDesignation != null) {
            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(currentDesignation);
            if (pq != null) {
                allowedUnits = pq.getAllowedUnits();
            }
        }

        for (int i = 0; i < keyboardCells.size(); i++) {
            TextView keyView = keyboardCells.get(i);
            if (i < displayKeys.size()) {
                SymbolKey symbolKey = displayKeys.get(i);
                keyView.setText(symbolKey.getDisplayText());

                if (symbolKey.shouldUseStixFont()) {
                    keyView.setTypeface(stixTypeface);
                } else {
                    keyView.setTypeface(Typeface.DEFAULT);
                }

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
                    keyView.setTextColor(symbolKey.isColor() ? Color.WHITE : Color.BLACK);
                }

                keyView.setOnClickListener(view -> {
                    String displayText = symbolKey.getDisplayText();
                    String logicalId = symbolKey.getLogicalId();
                    LogUtils.logKeyPressed("KeyboardLogic", logicalId);
                    if (inputController != null) {
                        inputController.onKeyInput(displayText, currentMode, symbolKey.shouldUseStixFont(), logicalId);
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
        designationButton.setBackgroundResource("Designation".equals(currentMode) ?
                R.drawable.ic_back_black : R.drawable.ic_back);
        designationButton.setTextColor("Designation".equals(currentMode) ? Color.WHITE : Color.BLACK);
        designationButton.setSelected("Designation".equals(currentMode));

        unitsButton.setBackgroundResource("Units_of_measurement".equals(currentMode) ?
                R.drawable.ic_back_black : R.drawable.ic_back);
        unitsButton.setTextColor("Units_of_measurement".equals(currentMode) ? Color.WHITE : Color.BLACK);
        unitsButton.setSelected("Units_of_measurement".equals(currentMode));

        numbersButton.setBackgroundResource("Numbers_and_operations".equals(currentMode) ?
                R.drawable.ic_back_black : R.drawable.ic_back);
        numbersButton.setTextColor("Numbers_and_operations".equals(currentMode) ? Color.WHITE : Color.BLACK);
        numbersButton.setSelected("Numbers_and_operations".equals(currentMode));
    }

    private void setModeListeners() {
        View.OnClickListener modeClickListener = view -> {
            String selectedMode = "";
            int viewId = view.getId();
            if (viewId == R.id.Designation) selectedMode = "Designation";
            else if (viewId == R.id.Units_of_measurement) selectedMode = "Units_of_measurement";
            else if (viewId == R.id.Numbers_and_operations) selectedMode = "Numbers_and_operations";

            if (!currentMode.equals(selectedMode)) {
                currentMode = selectedMode;
                currentPage = 0;
                updateModeButtonStyles();
                updateKeyboard();
            }
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
                if (pages != null && !pages.isEmpty()) currentPage = pages.size() - 1;
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