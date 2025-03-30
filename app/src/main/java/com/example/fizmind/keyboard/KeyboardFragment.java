package com.example.fizmind.keyboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fizmind.R;

import java.util.Arrays;
import java.util.List;

/**
 * Фрагмент, содержащий клавиатуру и логику ввода для использования в разных активностях.
 * Поддерживает настройку отображения блока "Введите неизвестное" через аргументы.
 */
public class KeyboardFragment extends Fragment {

    // Ключ для аргумента, определяющего видимость блока "Введите неизвестное"
    private static final String ARG_SHOW_UNKNOWN_BLOCK = "show_unknown_block";

    private KeyboardLogic keyboardLogic;
    private InputController inputController;
    private TextView editTextDesignations;
    private TextView editTextUnknown;
    private boolean showUnknownBlock; // Флаг для отображения блока "Введите неизвестное"

    // Пустой конструктор, требуемый для фрагментов
    public KeyboardFragment() {}

    /**
     * Создает новый экземпляр фрагмента с указанием, нужно ли отображать блок "Введите неизвестное".
     * @param showUnknownBlock true - отображать блок, false - скрыть блок
     * @return новый экземпляр KeyboardFragment
     */
    public static KeyboardFragment newInstance(boolean showUnknownBlock) {
        KeyboardFragment fragment = new KeyboardFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SHOW_UNKNOWN_BLOCK, showUnknownBlock);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Извлекаем значение аргумента showUnknownBlock
        if (getArguments() != null) {
            showUnknownBlock = getArguments().getBoolean(ARG_SHOW_UNKNOWN_BLOCK, true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Загружаем разметку фрагмента
        return inflater.inflate(R.layout.fragment_keyboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация всех View-компонентов
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

        // Настройка прокрутки для поля "Введите обозначение"
        editTextDesignations.setMovementMethod(new android.text.method.ScrollingMovementMethod());

        // Скрываем блок "Введите неизвестное", если showUnknownBlock = false
        if (!showUnknownBlock) {
            editTextUnknown.setVisibility(View.GONE);
        }

        // Инициализация KeyboardLogic
        keyboardLogic = new KeyboardLogic(
                requireContext(), keyboardCells, pageNumberView, designationButton, unitsButton,
                numbersButton, prevPageButton, nextPageButton, buttonScrollDown, editTextDesignations,
                buttonLeft, buttonRight
        );
        keyboardLogic.setUseStixFont(true);

        // Инициализация InputController с учетом видимости блока "Введите неизвестное"
        inputController = new InputController(
                editTextDesignations,
                showUnknownBlock ? editTextUnknown : null // Передаем null, если блок скрыт
        );
        inputController.setStixTypeface(keyboardLogic.getStixTypeface());
        keyboardLogic.setInputController(inputController);

        // Обработчики кнопок
        ImageButton buttonSave = view.findViewById(R.id.button_save);
        ImageButton buttonClear = view.findViewById(R.id.button_clear);

        buttonSave.setOnClickListener(v -> {
            Log.d("KeyboardFragment", "Нажата кнопка SAVE");
            inputController.onDownArrowPressed();
        });

        buttonLeft.setOnClickListener(v -> {
            Log.d("KeyboardFragment", "Нажата кнопка LEFT");
            inputController.onLeftArrowPressed();
        });

        buttonRight.setOnClickListener(v -> {
            Log.d("KeyboardFragment", "Нажата кнопка RIGHT");
            inputController.onRightArrowPressed();
        });

        buttonClear.setOnClickListener(v -> {
            Log.d("KeyboardFragment", "Нажата кнопка DELETE");
            inputController.onDeletePressed();
        });

        buttonClear.setOnLongClickListener(v -> {
            Log.d("KeyboardFragment", "Длительное нажатие на DELETE");
            inputController.clearAll();
            return true;
        });

        // Переключение фокуса между полями ввода
        editTextDesignations.setOnClickListener(v -> {
            inputController.setCurrentInputField("designations");
            Log.d("KeyboardFragment", "Фокус переключен на 'Введите обозначение'");
        });

        // Устанавливаем обработчик только если блок "Введите неизвестное" виден
        if (showUnknownBlock) {
            editTextUnknown.setOnClickListener(v -> {
                inputController.setCurrentInputField("unknown");
                Log.d("KeyboardFragment", "Фокус переключен на 'Введите неизвестное'");
            });
        }
    }

    /**
     * Получение экземпляра InputController для доступа к данным ввода.
     * @return текущий InputController
     */
    public InputController getInputController() {
        return inputController;
    }
}