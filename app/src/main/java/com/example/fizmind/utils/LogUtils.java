package com.example.fizmind.utils;

import android.util.Log;

import com.example.fizmind.modules.ModuleType;

/**
 * утилитный класс для централизованного логирования
 */
public class LogUtils {
    private static final boolean LOG_ENABLED = true; // логи всегда включены

    // базовые методы логирования
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

    // специальные методы для часто используемых логов
    public static void logActivityStarted(String tag, String activityName) {
        d(tag, activityName + " запущена");
    }

    public static void logButtonPressed(String tag, String buttonName) {
        d(tag, "нажата кнопка '" + buttonName + "'");
    }

    public static void logFragmentInitialized(String tag, String fragmentName, String mode) {
        d(tag, fragmentName + " инициализирован: " + mode);
    }

    public static void logFontLoadError(String tag, Exception e) {
        e(tag, "ошибка загрузки шрифта", e);
    }

    public static void logKeyPressed(String tag, String logicalId) {
        d(tag, "кнопка нажата: " + logicalId);
    }

    public static void logModuleCreated(String tag, ModuleType type) {
        d(tag, "создан модуль: " + type.getDescription());
    }

    public static void logSymbolAdded(String tag, ModuleType type, String input) {
        d(tag, "добавлено в " + type.getDescription() + ": " + input);
    }

    public static void logSymbolDeleted(String tag, ModuleType type, String remaining) {
        d(tag, "удалён символ из " + type.getDescription() + ", осталось: " + remaining);
    }

    public static void logModuleActivated(String tag, ModuleType type) {
        d(tag, "модуль активирован: " + type.getDescription());
    }

    public static void logModuleDeactivated(String tag, ModuleType type) {
        d(tag, "модуль деактивирован: " + type.getDescription());
    }

    public static void logControllerInitialized(String tag) {
        d(tag, "контроллер ввода инициализирован");
    }

    public static void logPropertySet(String tag, String property, Object value) {
        d(tag, "установлено " + property + ": " + value);
    }

    public static void logInputProcessing(String tag, String state, String focus, String input, String logicalId, String mode) {
        d(tag, "обработка ввода: состояние=" + state + ", фокус=" + focus + ", ввод='" + input + "', logicalId=" + logicalId + ", режим=" + mode);
    }

    public static void logDeletion(String tag, String action) {
        d(tag, action);
    }

    public static void logSaveMeasurement(String tag, String measurement) {
        d(tag, "сохранено измерение: " + measurement);
    }

    public static void logSaveUnknown(String tag, String unknown) {
        d(tag, "сохранено неизвестное: " + unknown);
    }

    public static void logValidationError(String tag, String error) {
        e(tag, "ошибка валидации: " + error);
    }

    public static void logConversionError(String tag, String designation, String unit) {
        e(tag, "ошибка конвертации для " + designation + " с единицей " + unit);
    }

    public static void logMeasurementCreated(String tag, String measurement) {
        d(tag, "создано измерение: " + measurement);
    }

    public static void logUnknownCreated(String tag, String designation, String subscript) {
        d(tag, "создано неизвестное: " + designation + (subscript != null && !subscript.isEmpty() ? "_" + subscript : ""));
    }
}