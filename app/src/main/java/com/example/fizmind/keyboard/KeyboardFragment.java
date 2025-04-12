package com.example.fizmind.keyboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.fizmind.ConversionService;
import com.example.fizmind.R;
import com.example.fizmind.SolutionActivity;
import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.measurement.UnknownQuantity;
import com.example.fizmind.utils.LogUtils;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyboardFragment extends Fragment {

    private KeyboardLogic keyboardLogic;
    private InputController inputController;
    private TextView editTextDesignations;
    private TextView editTextUnknown;
    private boolean isUnknownInputAllowed = true;
    private boolean isConversionMode = false;

    public KeyboardFragment() {
    }

    public static KeyboardFragment newInstance(boolean isConversionMode, boolean isUnknownInputAllowed) {
        KeyboardFragment fragment = new KeyboardFragment();
        Bundle args = new Bundle();
        args.putBoolean("isConversionMode", isConversionMode);
        args.putBoolean("isUnknownInputAllowed", isUnknownInputAllowed);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isConversionMode = getArguments().getBoolean("isConversionMode", false);
            isUnknownInputAllowed = getArguments().getBoolean("isUnknownInputAllowed", true);
        }
        LogUtils.d("KeyboardFragment", "создан фрагмент, режим: " + (isConversionMode ? "конвертация" : "калькулятор"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_keyboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<TextView> keyboardCells = Arrays.asList(
                view.findViewById(R.id.key_1), view.findViewById(R.id.key_2), view.findViewById(R.id.key_3),
                view.findViewById(R.id.key_4), view.findViewById(R.id.key_5), view.findViewById(R.id.key_6),
                view.findViewById(R.id.key_7), view.findViewById(R.id.key_8), view.findViewById(R.id.key_9),
                view.findViewById(R.id.key_10), view.findViewById(R.id.key_11), view.findViewById(R.id.key_12),
                view.findViewById(R.id.key_13), view.findViewById(R.id.key_14), view.findViewById(R.id.key_15),
                view.findViewById(R.id.key_16), view.findViewById(R.id.key_17), view.findViewById(R.id.key_18),
                view.findViewById(R.id.key_19), view.findViewById(R.id.key_20), view.findViewById(R.id.key_21)
        );

        TextView pageNumberView = view.findViewById(R.id.page_number);
        TextView designationButton = view.findViewById(R.id.Designation);
        TextView unitsButton = view.findViewById(R.id.Units_of_measurement);
        TextView numbersButton = view.findViewById(R.id.Numbers_and_operations);
        ImageButton prevPageButton = view.findViewById(R.id.button_prev_page);
        ImageButton nextPageButton = view.findViewById(R.id.button_next_page);
        ImageButton buttonScrollDown = view.findViewById(R.id.button_scroll_down);
        editTextDesignations = view.findViewById(R.id.editText_designations);
        editTextUnknown = view.findViewById(R.id.editText_unknown);
        ImageButton buttonLeft = view.findViewById(R.id.button_left);
        ImageButton buttonRight = view.findViewById(R.id.button_right);
        ImageButton buttonSave = view.findViewById(R.id.button_save);
        ImageButton buttonClear = view.findViewById(R.id.button_clear);
        ImageButton buttonCycle = view.findViewById(R.id.button_cycle);

        editTextDesignations.setMovementMethod(new ScrollingMovementMethod());

        keyboardLogic = new KeyboardLogic(
                requireContext(),
                keyboardCells,
                pageNumberView,
                designationButton,
                unitsButton,
                numbersButton,
                prevPageButton,
                nextPageButton,
                buttonScrollDown,
                editTextDesignations,
                editTextUnknown,
                buttonLeft,
                buttonRight,
                view
        );
        keyboardLogic.setUseStixFont(true);

        inputController = new InputController(editTextDesignations, editTextUnknown, new ConversionService(), view);
        inputController.setConversionMode(isConversionMode);
        inputController.setUnknownInputAllowed(isUnknownInputAllowed);
        inputController.setStixTypeface(keyboardLogic.getStixTypeface());
        inputController.setKeyboardModeSwitcher(keyboardLogic);
        keyboardLogic.setInputController(inputController);

        buttonSave.setOnClickListener(v -> {
            LogUtils.logButtonPressed("KeyboardFragment", "SAVE");
            inputController.onDownArrowPressed();
        });

        buttonLeft.setOnClickListener(v -> {
            LogUtils.logButtonPressed("KeyboardFragment", "LEFT");
            inputController.onLeftArrowPressed();
        });

        buttonRight.setOnClickListener(v -> {
            LogUtils.logButtonPressed("KeyboardFragment", "RIGHT");
            inputController.onRightArrowPressed();
        });

        buttonClear.setOnClickListener(v -> {
            LogUtils.logButtonPressed("KeyboardFragment", "DELETE");
            inputController.onDeletePressed();
        });

        buttonClear.setOnLongClickListener(v -> {
            LogUtils.d("KeyboardFragment", "длительное нажатие на DELETE");
            inputController.clearAll();
            return true;
        });

        editTextDesignations.setOnClickListener(v -> {
            inputController.setCurrentInputField("designations");
            LogUtils.d("KeyboardFragment", "фокус на 'Введите обозначение'");
        });

        editTextUnknown.setOnClickListener(v -> {
            inputController.setCurrentInputField("unknown");
            LogUtils.d("KeyboardFragment", "фокус на 'Введите неизвестное'");
        });

        // Добавляем обработчик для button_cycle
        buttonCycle.setOnClickListener(v -> {
            LogUtils.logButtonPressed("KeyboardFragment", "CYCLE");
            handleCycleButtonPress();
        });
    }

    /**
     * Обрабатывает нажатие на кнопку button_cycle
     */
    private void handleCycleButtonPress() {
        List<UnknownQuantity> unknowns = inputController.getUnknowns();
        if (unknowns.isEmpty()) {
            // Если неизвестная не введена, показываем Snackbar
            if (getView() != null) {
                Snackbar.make(getView(), "Пожалуйста, введите неизвестное", Snackbar.LENGTH_SHORT).show();
            }
            return;
        }

        // Собираем данные для передачи
        Map<String, Double> knownValues = new HashMap<>();
        for (ConcreteMeasurement measurement : inputController.getMeasurements()) {
            knownValues.put(measurement.getDesignation(), measurement.getValue());
        }

        // Берем первую неизвестную (пока реализуем только одно неизвестное)
        String unknown = unknowns.get(0).getDisplayDesignation();

        // Переход к SolutionActivity
        Intent intent = new Intent(getActivity(), SolutionActivity.class);
        intent.putExtra("knownValues", (java.io.Serializable) knownValues);
        intent.putExtra("unknown", unknown);
        startActivity(intent);
    }
}