package com.example.fizmind.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import androidx.preference.PreferenceManager;
import com.example.fizmind.modules.ModuleType;
import com.google.android.material.snackbar.Snackbar;

public class LogUtils {
    private static boolean LOG_ENABLED = true; // флаг для управления логами
    private static boolean SNACKBAR_ENABLED = true; // флаг для управления snackbar

    // обновление настроек из shared preferences
    public static void updateSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean debugEnabled = prefs.getBoolean("enable_debug_features", true);
        LOG_ENABLED = debugEnabled;
        SNACKBAR_ENABLED = debugEnabled;
        Log.d("LogUtils", "Настройки обновлены: LOG_ENABLED=" + LOG_ENABLED + ", SNACKBAR_ENABLED=" + SNACKBAR_ENABLED);
    }

    // проверка, включены ли snackbar
    public static boolean isSnackbarEnabled() {
        return SNACKBAR_ENABLED;
    }

    public static void d(String tag, String message) {
        if (LOG_ENABLED) {
            Log.d(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (LOG_ENABLED) {
            Log.i(tag, message);
        }
    }

    public static void w(String tag, String message) {
        if (LOG_ENABLED) {
            Log.w(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (LOG_ENABLED) {
            Log.e(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable tr) {
        if (LOG_ENABLED) {
            Log.e(tag, message, tr);
        }
    }

    // методы с отображением snackbar
    public static void wWithSnackbar(String tag, String message, View view) {
        w(tag, message);
        if (SNACKBAR_ENABLED) {
            showSnackbar(view, message);
        }
    }

    public static void eWithSnackbar(String tag, String message, View view) {
        e(tag, message);
        if (SNACKBAR_ENABLED) {
            showSnackbar(view, message);
        }
    }

    // отображение snackbar
    private static void showSnackbar(View view, String message) {
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
        } else {
            Log.w("LogUtils", "Не удалось отобразить Snackbar: view == null");
        }
    }

    // специальные методы для часто используемых логов
    public static void logActivityStarted(String tag, String activityName) {
        d(tag, activityName + " запущена");
    }

    public static void logButtonPressed(String tag, String buttonName) {
        d(tag, "Нажата кнопка '" + buttonName + "'");
    }

    public static void logFragmentInitialized(String tag, String fragmentName, String mode) {
        d(tag, fragmentName + " инициализирован: " + mode);
    }

    public static void logFontLoadError(String tag, Exception e) {
        e(tag, "Ошибка загрузки шрифта", e);
    }

    public static void logKeyPressed(String tag, String logicalId) {
        d(tag, "Кнопка нажата: " + logicalId);
    }

    public static void logModuleCreated(String tag, ModuleType type) {
        d(tag, "Создан модуль: " + type.getDescription());
    }

    public static void logSymbolAdded(String tag, ModuleType type, String input) {
        d(tag, "Добавлено в " + type.getDescription() + ": " + input);
    }

    public static void logSymbolDeleted(String tag, ModuleType type, String remaining) {
        d(tag, "Удалён символ из " + type.getDescription() + ", осталось: " + remaining);
    }

    public static void logModuleActivated(String tag, ModuleType type) {
        d(tag, "Модуль активирован: " + type.getDescription());
    }

    public static void logModuleDeactivated(String tag, ModuleType type) {
        d(tag, "Модуль деактивирован: " + type.getDescription());
    }

    public static void logControllerInitialized(String tag) {
        d(tag, "Контроллер ввода инициализирован");
    }

    public static void logPropertySet(String tag, String property, Object value) {
        d(tag, "Установлено " + property + ": " + value);
    }

    public static void logInputProcessing(String tag, String state, String focus, String input, String logicalId, String mode) {
        d(tag, "Обработка ввода: состояние=" + state + ", фокус=" + focus + ", ввод='" + input + "', logicalId=" + logicalId + ", режим=" + mode);
    }

    public static void logDeletion(String tag, String action) {
        d(tag, action);
    }

    public static void logSaveMeasurement(String tag, String measurement) {
        d(tag, "Сохранено измерение: " + measurement);
    }

    public static void logSaveUnknown(String tag, String unknown) {
        d(tag, "Сохранено неизвестное: " + unknown);
    }

    public static void logValidationError(String tag, String error) {
        e(tag, "Ошибка валидации: " + error);
    }

    public static void logConversionError(String tag, String designation, String unit) {
        e(tag, "Ошибка конвертации для " + designation + " с единицей " + unit);
    }

    public static void logMeasurementCreated(String tag, String measurement) {
        d(tag, "Создано измерение: " + measurement);
    }

    public static void logUnknownCreated(String tag, String designation, String subscript) {
        d(tag, "Создано неизвестное: " + designation + (subscript != null && !subscript.isEmpty() ? "_" + subscript : ""));
    }
}