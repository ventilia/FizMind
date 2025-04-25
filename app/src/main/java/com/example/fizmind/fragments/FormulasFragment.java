package com.example.fizmind.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import com.example.fizmind.R;
import com.example.fizmind.formulas.Formula;
import com.example.fizmind.formulas.FormulaDatabase;
import com.example.fizmind.keyboard.DisplayManager;

import java.util.List;

public class FormulasFragment extends Fragment {

    private ListView listView;
    private Typeface montserratTypeface;
    private FormulaAdapter adapter;
    private DisplayManager displayManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_formulas, container, false);
        listView = view.findViewById(R.id.list_view_formulas);


        ImageView backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> getActivity().onBackPressed());


        Typeface stixTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/stix_two_text_italic.ttf");
        displayManager = new DisplayManager(stixTypeface);

        //  список формул
        FormulaDatabase database = new FormulaDatabase();
        List<Formula> formulas = database.getFormulas();


        adapter = new FormulaAdapter(getContext(), formulas, displayManager, montserratTypeface);
        listView.setAdapter(adapter);

        return view;
    }
}