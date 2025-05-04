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
import com.example.fizmind.formulas.Formula;
import com.example.fizmind.formulas.FormulaDatabase;
import com.example.fizmind.keyboard.DisplayManager;
import com.example.fizmind.utils.LogUtils;

import java.util.List;

// фрагмент для отображения списка формул
public class FormulasFragment extends Fragment {

    private ListView listView;
    private Typeface montserratTypeface;
    private FormulaAdapter adapter;
    private DisplayManager displayManager;
    private AppDatabase database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_formulas, container, false);
        listView = view.findViewById(R.id.list_view_formulas);

        // настройка кнопки "назад"
        ImageView backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> getActivity().onBackPressed());

        // загрузка шрифтов
        Typeface stixTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/stix_two_text_italic.ttf");
        montserratTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/MontserratAlternates-Regular.ttf");

        // инициализация базы данных
        database = Room.databaseBuilder(getContext(), AppDatabase.class, "fizmind-db")
                .allowMainThreadQueries() // разрешить запросы в основном потоке (для простоты)
                .build();

        // инициализация менеджера отображения
        displayManager = new DisplayManager(stixTypeface, database);

        // получение списка формул
        FormulaDatabase formulaDatabase = new FormulaDatabase();
        List<Formula> formulas = formulaDatabase.getFormulas();

        // настройка адаптера для списка
        adapter = new FormulaAdapter(getContext(), formulas, displayManager, montserratTypeface);
        listView.setAdapter(adapter);

        LogUtils.d("FormulasFragment", "фрагмент создан, список формул загружен");
        return view;
    }
}