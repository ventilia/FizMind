package com.example.fizmind.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import androidx.room.Room;
import com.example.fizmind.R;
import com.example.fizmind.database.AppDatabase;
import com.example.fizmind.quantly.PhysicalQuantity;
import com.example.fizmind.quantly.PhysicalQuantityRegistry;
import com.example.fizmind.keyboard.DisplayManager;
import com.example.fizmind.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

// фрагмент для отображения списка физических величин
public class DesignationsFragment extends Fragment {

    private ListView listView;
    private DesignationAdapter adapter;
    private DisplayManager displayManager;
    private AppDatabase database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_designations, container, false);
        listView = view.findViewById(R.id.list_view_designations);

        // настройка кнопки "назад"
        ImageView backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> getActivity().onBackPressed());

        // загрузка шрифта
        Typeface stixTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/stix_two_text_italic.ttf");

        // инициализация базы данных
        database = Room.databaseBuilder(getContext(), AppDatabase.class, "fizmind-db")
                .allowMainThreadQueries() // разрешить запросы в основном потоке (для простоты)
                .build();

        // инициализация менеджера отображения
        displayManager = new DisplayManager(stixTypeface, database);

        // получение списка всех физических величин
        List<PhysicalQuantity> quantities = new ArrayList<>(PhysicalQuantityRegistry.getAllQuantities());

        // настройка адаптера для списка
        adapter = new DesignationAdapter(getContext(), quantities, displayManager);
        listView.setAdapter(adapter);

        LogUtils.d("DesignationsFragment", "фрагмент создан, список физических величин загружен");
        return view;
    }
}