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
    // Вместо одного displayView теперь два отдельных поля для разных целей:
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

    /**
     * Конструктор KeyboardLogic.
     *
     * @param context           Контекст приложения.
     * @param keyboardCells     Список TextView для клавиш.
     * @param pageNumberView    TextView для отображения номера страницы.
     * @param designationButton Кнопка для режима обозначения.
     * @param unitsButton       Кнопка для режима единиц измерения.
     * @param numbersButton     Кнопка для режима чисел и операций.
     * @param prevPageButton    Кнопка для перехода на предыдущую страницу.
     * @param nextPageButton    Кнопка для перехода на следующую страницу.
     * @param buttonScrollDown  Кнопка для прокрутки вниз.
     * @param designationView   TextView для отображения поля "Введите обозначение".
     * @param unknownView       TextView для отображения поля "Введите неизвестное".
     * @param leftArrowButton   Кнопка навигации влево.
     * @param rightArrowButton  Кнопка навигации вправо.
     */
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
        // Инициализируем два поля отдельно
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

        // Пример заполнения keyboardData для режима "Designation"
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

        keyboardData.put("Units_of_measurement", Arrays.asList(
                Arrays.asList(
                        new SymbolKey("unit_m/s", "m/s", false),
                        new SymbolKey("unit_km/h", "km/h", false),
                        new SymbolKey("unit_mi/h", "mi/h", false),
                        new SymbolKey("unit_m/s²", "m/s²", false),
                        new SymbolKey("unit_m", "m", false),
                        new SymbolKey("unit_kg", "kg", false),
                        new SymbolKey("unit_g", "g", false),
                        new SymbolKey("unit_mg", "mg", false),
                        new SymbolKey("unit_s", "s", false),
                        new SymbolKey("unit_min", "min", false),
                        new SymbolKey("unit_h", "h", false)
                ),
                Arrays.asList(
                        new SymbolKey("unit_N", "N", false),
                        new SymbolKey("unit_Pa", "Pa", false),
                        new SymbolKey("unit_kPa", "kPa", false),
                        new SymbolKey("unit_atm", "atm", false),
                        new SymbolKey("unit_J", "J", false),
                        new SymbolKey("unit_kJ", "kJ", false),
                        new SymbolKey("unit_W", "W", false),
                        new SymbolKey("unit_kg/m³", "kg/m³", false),
                        new SymbolKey("unit_A", "A", false),
                        new SymbolKey("unit_V", "V", false),
                        new SymbolKey("unit_Ω", "Ω", false),
                        new SymbolKey("unit_F", "F", false),
                        new SymbolKey("unit_H", "H", false),
                        new SymbolKey("unit_Wb", "Wb", false),
                        new SymbolKey("unit_T", "T", false),
                        new SymbolKey("unit_m³", "m³", false),
                        new SymbolKey("unit_K", "K", false),
                        new SymbolKey("unit_°C", "°C", false)
                )
        ));

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
                        new SymbolKey("op_exponent", "^", false),  // Кнопка степени
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

        // Исправлено: создаем InputController, передавая два разных TextView: designationView и unknownView
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

        for (int i = 0; i < keyboardCells.size(); i++) {
            TextView keyView = keyboardCells.get(i);
            if (i < currentKeys.size()) {
                SymbolKey symbolKey = currentKeys.get(i);
                keyView.setText(symbolKey.getDisplayText());

                if (symbolKey.shouldUseStixFont()) {
                    keyView.setTypeface(stixTypeface);
                } else {
                    keyView.setTypeface(Typeface.DEFAULT);
                }

                if (symbolKey.isColor()) {
                    keyView.setTextColor(Color.WHITE);
                } else {
                    keyView.setTextColor(Color.BLACK);
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
