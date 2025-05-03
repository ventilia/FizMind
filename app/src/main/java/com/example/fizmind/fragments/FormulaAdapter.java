package com.example.fizmind.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.fizmind.R;
import com.example.fizmind.formulas.Formula;
import com.example.fizmind.keyboard.DisplayManager;
import com.example.fizmind.quantly.PhysicalQuantity;
import com.example.fizmind.quantly.PhysicalQuantityRegistry;

import java.util.List;

/**
 * адаптер для отображения списка формул в пользовательском интерфейсе
 */
public class FormulaAdapter extends BaseAdapter {

    private final Context context;
    private final List<Formula> formulas;
    private final DisplayManager displayManager;
    private final Typeface montserratTypeface;

    public FormulaAdapter(Context context, List<Formula> formulas, DisplayManager displayManager, Typeface montserratTypeface) {
        this.context = context;
        this.formulas = formulas;
        this.displayManager = displayManager;
        this.montserratTypeface = montserratTypeface;
    }

    @Override
    public int getCount() {
        return formulas.size();
    }

    @Override
    public Object getItem(int position) {
        return formulas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_formula, parent, false);
        }

        TextView formulaTextView = convertView.findViewById(R.id.text_formula);
        formulaTextView.setTypeface(montserratTypeface); // применение шрифта montserrat

        Formula formula = formulas.get(position);

        // получаем базовое выражение формулы (предполагаем, что такой метод есть)
        String expression = formula.getBaseExpression();
        StringBuilder description = new StringBuilder();

        description.append(expression).append("<br>где:<br>");

        // получаем список переменных формулы и их описания
        for (String variable : formula.getVariables()) {
            String displayVar = displayManager.getDisplayTextFromLogicalId(variable);
            PhysicalQuantity quantity = PhysicalQuantityRegistry.getPhysicalQuantity(variable);
            String quantityDescription = (quantity != null) ? quantity.getType() : "описание не найдено";
            description.append("<b>").append(displayVar).append("</b>").append(" - ").append(quantityDescription).append("<br>");
        }

        formulaTextView.setText(Html.fromHtml(description.toString()));
        return convertView;
    }
}