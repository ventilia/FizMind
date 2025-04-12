package com.example.fizmind.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import com.example.fizmind.PhysicalQuantityRegistry;
import com.example.fizmind.R;
import com.example.fizmind.utils.LogUtils;

/**
 * фрагмент настроек приложения
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // регистрируем слушатель изменений настроек
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener((prefs, key) -> {
            if ("gravity_value".equals(key)) {
                //  значение ускорения свободного падения
                PhysicalQuantityRegistry.updateGravityValue(requireContext());
                LogUtils.d("SettingsFragment", "значение g обновлено: " + PhysicalQuantityRegistry.getGravityValue());
            } else if ("enable_debug_features".equals(key)) {
                //  настройки отладочных функций
                LogUtils.updateSettings(requireContext());

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        PhysicalQuantityRegistry.updateGravityValue(context);
        LogUtils.updateSettings(context);
    }
}