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

public class FormulasFragment extends Fragment {

    private ListView listView;
    private FormulaAdapter adapter;
    private DisplayManager displayManager;
    private AppDatabase database;
    private Typeface montserratTypeface;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_formulas, container, false);

        listView = view.findViewById(R.id.list_view_formulas);

        // кнопка «назад»
        ImageView backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());


        montserratTypeface = Typeface.createFromAsset(
                requireContext().getAssets(),
                "fonts/MontserratAlternates-Regular.ttf"
        );

        // инициализируем базу данных
        database = Room.databaseBuilder(
                        requireContext(),
                        AppDatabase.class,
                        "fizmind-db"
                )
                .allowMainThreadQueries()
                .build();


        displayManager = new DisplayManager(montserratTypeface, database);


        FormulaDatabase formulaDatabase = new FormulaDatabase();
        List<Formula> formulas = formulaDatabase.getFormulas();


        adapter = new FormulaAdapter(
                requireContext(),
                formulas,
                displayManager,
                montserratTypeface
        );
        listView.setAdapter(adapter);

        LogUtils.d("FormulasFragment", "список формул загружен и отображается");
        return view;
    }
}