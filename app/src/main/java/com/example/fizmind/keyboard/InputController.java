package com.example.fizmind.keyboard;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.TextView;

import com.example.fizmind.PhysicalQuantity;
import com.example.fizmind.PhysicalQuantityRegistry;
import com.example.fizmind.animation.CustomTypefaceSpan;
import com.example.fizmind.measurement.ConcreteMeasurement;
import com.example.fizmind.measurement.Measurement;

import java.util.ArrayList;
import java.util.List;


public class InputController {

    public enum InputState {
        ENTERING_DESIGNATION,
        ENTERING_VALUE,
        ENTERING_UNIT
    }

    private static final int MAX_DESIGNATION_LENGTH = 1;

    private InputState currentState;
    private final StringBuilder designationBuffer;
    private final StringBuilder valueBuffer;
    private final StringBuilder unitBuffer;
    private final TextView displayView;
    private final List<Measurement> measurements;
    private final List<SpannableStringBuilder> history; // хранит историю  записей. пока плохо сделано

    private Boolean designationUsesStix;
    private String logicalDesignation;
    private android.graphics.Typeface stixTypeface;
    private KeyboardModeSwitcher keyboardModeSwitcher;

    public InputController(TextView displayView) {
        this.displayView = displayView;
        this.currentState = InputState.ENTERING_DESIGNATION;
        this.designationBuffer = new StringBuilder();
        this.valueBuffer = new StringBuilder();
        this.unitBuffer = new StringBuilder();
        this.measurements = new ArrayList<>();
        this.history = new ArrayList<>(); //  истории
        this.designationUsesStix = null;
        this.logicalDesignation = null;
        updateDisplay();
    }

    public void setStixTypeface(android.graphics.Typeface stixTypeface) {
        this.stixTypeface = stixTypeface;
    }

    public void setKeyboardModeSwitcher(KeyboardModeSwitcher switcher) {
        this.keyboardModeSwitcher = switcher;
    }

    public void onKeyInput(String input, String sourceKeyboardMode, boolean keyUsesStix, String logicalId) {
        if (currentState == InputState.ENTERING_DESIGNATION) {
            if (designationBuffer.length() == 0) {
                if (!"Designation".equals(sourceKeyboardMode)) {
                    Log.w("InputController", " символ обозначения должен быть из режима 'Designation'");
                    return;
                }
                if (input.length() > MAX_DESIGNATION_LENGTH) {
                    input = input.substring(0, MAX_DESIGNATION_LENGTH);
                }
                designationUsesStix = keyUsesStix;
                designationBuffer.append(designationUsesStix ? convertToStix(input) : input);
                logicalDesignation = logicalId;
                if (designationBuffer.length() == MAX_DESIGNATION_LENGTH) {
                    currentState = InputState.ENTERING_VALUE;
                    if (keyboardModeSwitcher != null) {
                        keyboardModeSwitcher.switchToNumbersAndOperations();
                    }
                }
            } else {
                Log.w("InputController", " обозначение уже введено, нужен ввод числа.");
            }
        } else if (currentState == InputState.ENTERING_VALUE) {
            if (input.matches("[0-9]")) {
                valueBuffer.append(input);
            } else if (".".equals(input)) {
                // Запрещаем точку, если число ещё не содержит цифр (учитывая возможный минус)
                if (valueBuffer.length() == 0 || valueBuffer.toString().equals("-")) {
                    Log.e("InputController", "Ошибка ввода: числовое значение не может начинаться с точки.");
                } else if (valueBuffer.indexOf(".") != -1) {
                    Log.e("InputController", "Ошибка ввода: числовое значение уже содержит .");
                } else {
                    valueBuffer.append(input);
                }
            } else if ("-".equals(input)) {
                if (valueBuffer.length() > 0) {
                    Log.e("InputController", "ошибка хз  почему.");
                } else {
                    valueBuffer.append(input);
                }
            } else {
                currentState = InputState.ENTERING_UNIT;
                onKeyInput(input, sourceKeyboardMode, keyUsesStix, logicalId);
                return;
            }
        } else if (currentState == InputState.ENTERING_UNIT) {
            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
            if (pq == null) {
                Log.e("InputController", "нет данных о  величине для: " + logicalDesignation);
                return;
            }
            int maxAllowedLength = 0;
            for (String allowed : pq.getAllowedUnits()) {
                if (allowed.length() > maxAllowedLength) {
                    maxAllowedLength = allowed.length();
                }
            }
            String potentialUnit = unitBuffer.toString() + input;
            boolean validPrefix = false;
            for (String allowed : pq.getAllowedUnits()) {
                if (allowed.startsWith(potentialUnit)) {
                    validPrefix = true;
                    break;
                }
            }
            if (validPrefix && potentialUnit.length() <= maxAllowedLength) {
                unitBuffer.append(input);
            } else {
                Log.w("InputController", "введен недопустимый символ для единицы измерения: " + input);
            }
        }
        updateDisplay();
    }

    public void onKeyInput(String input) {
        onKeyInput(input, "Designation", false, "");
    }

   //новое
   private void updateDisplay() {
       SpannableStringBuilder displayText = new SpannableStringBuilder();

       // Добавляем записи истории с двумя новыми строками между ними
       for (int i = 0; i < history.size(); i++) {
           displayText.append(history.get(i));
           if (i < history.size() - 1) {
               displayText.append("\n\n");  // Два символа новой строки между записями
           }
       }

       // Если есть история, добавляем два символа новой строки перед текущим вводом или подсказкой
       if (history.size() > 0) {
           displayText.append("\n\n");
       }

       // Проверяем, есть ли текущий ввод
       if (designationBuffer.length() == 0 && valueBuffer.length() == 0 && unitBuffer.length() == 0) {
           // Если буферы пусты, добавляем подсказку "Введите обозначение"
           int start = displayText.length();
           displayText.append("Введите обозначение");
           // Применяем серый цвет для подсказки
           displayText.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.GRAY),
                   start, displayText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
       } else {
           // Если есть текущий ввод, отображаем его
           int start = displayText.length();
           displayText.append(designationBuffer.toString());
           if (designationUsesStix != null && designationUsesStix && designationBuffer.length() > 0 && stixTypeface != null) {
               displayText.setSpan(new CustomTypefaceSpan(stixTypeface), start, displayText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
           }
           if (designationBuffer.length() > 0) {
               displayText.append(" = ");
           }
           displayText.append(valueBuffer.toString());
           if (unitBuffer.length() > 0) {
               displayText.append(" ").append(unitBuffer.toString());
           }
       }

       displayView.setText(displayText);

       // Прокручиваем вниз, чтобы показать последнюю введенную область
       displayView.post(new Runnable() {
           @Override
           public void run() {
               if (displayView.getLayout() != null) {
                   int scrollAmount = displayView.getLayout().getHeight() - displayView.getHeight();
                   if (scrollAmount > 0) {
                       displayView.scrollTo(0, scrollAmount);
                   } else {
                       displayView.scrollTo(0, 0);
                   }
               }
           }
       });
   }

   // save
   public void onDownArrowPressed() {
       if (designationBuffer.length() > 0 && valueBuffer.length() > 0) {
           String unit = unitBuffer.toString();
           // Если единица измерения не введена, подставляем SI-единицу
           if (unit.isEmpty()) {
               PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalDesignation);
               if (pq == null) {
                   Log.e("InputController", "Не найдена информация для физической величины: " + logicalDesignation);
                   return;
               }
               unit = pq.getSiUnit();
               Log.d("InputController", "Единица измерения не указана, подставлена SI-единица: " + unit);
           }

           try {
               double value = Double.parseDouble(valueBuffer.toString());
               ConcreteMeasurement measurement = new ConcreteMeasurement(logicalDesignation, value, unit);
               if (!measurement.validate()) {
                   Log.e("InputController", "Ошибка: измерение не прошло проверку: " + measurement.toString());
               } else {
                   measurements.add(measurement);
                   Log.d("InputController", "Измерение сохранено: " + measurement.toString());

                   // Создание записи для истории
                   SpannableStringBuilder historyEntry = new SpannableStringBuilder();
                   int start = historyEntry.length();
                   historyEntry.append(designationBuffer.toString());
                   // Применяем шрифт STIX для обозначения, если требуется
                   if (designationUsesStix != null && designationUsesStix && stixTypeface != null) {
                       historyEntry.setSpan(new CustomTypefaceSpan(stixTypeface), start, historyEntry.length(), 0);
                   }
                   historyEntry.append(" = ").append(valueBuffer.toString());
                   // Добавляем единицу измерения, если она есть (SI или введенная)
                   if (!unit.isEmpty()) {
                       historyEntry.append(" ").append(unit);
                   }
                   history.add(historyEntry);

                   resetInput();
                   if (keyboardModeSwitcher != null) {
                       keyboardModeSwitcher.switchToDesignation();
                   }
                   updateDisplay(); // Обновляем отображение после сохранения
               }
           } catch (NumberFormatException e) {
               Log.e("InputController", "Ошибка формата числа: " + valueBuffer.toString(), e);
           }
       } else {
           Log.d("InputController", "Недостаточно данных для сохранения измерения.");
       }
   }

    public void onLeftArrowPressed() {
        Log.d("InputController", "Действие не определено.");
    }

    public void onRightArrowPressed() {
        Log.d("InputController", "Нажата кнопка RIGHT – действие не определено.");
    }

    public void onDeletePressed() {
        if (currentState == InputState.ENTERING_UNIT) {
            if (unitBuffer.length() > 0) {
                unitBuffer.setLength(0);
                currentState = InputState.ENTERING_VALUE;
            } else if (valueBuffer.length() > 0) {
                valueBuffer.setLength(valueBuffer.length() - 1);
            } else if (designationBuffer.length() > 0) {
                designationBuffer.setLength(designationBuffer.length() - 1);
                currentState = InputState.ENTERING_DESIGNATION;
                designationUsesStix = null;
                logicalDesignation = null;
            }
        } else if (currentState == InputState.ENTERING_VALUE) {
            if (valueBuffer.length() > 0) {
                valueBuffer.setLength(valueBuffer.length() - 1);
            } else if (designationBuffer.length() > 0) {
                designationBuffer.setLength(designationBuffer.length() - 1);
                currentState = InputState.ENTERING_DESIGNATION;
                designationUsesStix = null;
                logicalDesignation = null;
            }
        } else if (currentState == InputState.ENTERING_DESIGNATION) {
            if (designationBuffer.length() > 0) {
                designationBuffer.setLength(designationBuffer.length() - 1);
                designationUsesStix = null;
                logicalDesignation = null;
            }
        }
        updateDisplay();

        if (currentState == InputState.ENTERING_DESIGNATION && keyboardModeSwitcher != null) {
            keyboardModeSwitcher.switchToDesignation();
        }
    }

    public void clearAll() {
        designationBuffer.setLength(0);
        valueBuffer.setLength(0);
        unitBuffer.setLength(0);
        currentState = InputState.ENTERING_DESIGNATION;
        designationUsesStix = null;
        logicalDesignation = null;
        history.clear(); //чистка имтории
        updateDisplay();
        if (keyboardModeSwitcher != null) {
            keyboardModeSwitcher.switchToDesignation();
        }
    }

    private void resetInput() {
        designationBuffer.setLength(0);
        valueBuffer.setLength(0);
        unitBuffer.setLength(0);
        currentState = InputState.ENTERING_DESIGNATION;
        designationUsesStix = null;
        logicalDesignation = null;
        updateDisplay();
    }

    private String convertToStix(String input) {
        return input;
    }
}
