package com.example.fizmind.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import com.example.fizmind.quantly.PhysicalQuantityRegistry;
import com.example.fizmind.R;
import com.example.fizmind.utils.LogUtils;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener((prefs, key) -> {
            if ("gravity_value".equals(key)) {
                //  значение g
                PhysicalQuantityRegistry.updateGravityValue(requireContext());
                LogUtils.d("SettingsFragment", "значение g обновлено: " + PhysicalQuantityRegistry.getGravityValue());
            } else if ("enable_debug_features".equals(key)) {
                //  настройки отладки
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