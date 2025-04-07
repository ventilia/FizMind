package com.example.fizmind;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import com.example.fizmind.PhysicalQuantityRegistry;
import com.example.fizmind.R;
import com.example.fizmind.utils.LogUtils;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // Регистрируем слушатель изменений настроек
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener((prefs, key) -> {
            if ("gravity_value".equals(key)) {
                PhysicalQuantityRegistry.updateGravityValue(requireContext());
                LogUtils.d("SettingsFragment", "Значение g обновлено: " + PhysicalQuantityRegistry.getGravityValue());
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Обновляем значение g при первом запуске фрагмента
        PhysicalQuantityRegistry.updateGravityValue(context);
    }
}