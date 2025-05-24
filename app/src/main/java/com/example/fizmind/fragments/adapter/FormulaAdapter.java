package com.example.fizmind.fragments.adapter;

import android.content.Context;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
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

public class FormulaAdapter extends BaseAdapter {

    private final Context context;
    private final List<Formula> formulas;
    private final DisplayManager displayManager;
    private final android.graphics.Typeface montserratTypeface;

    // конструктор
    public FormulaAdapter(Context context,
                          List<Formula> formulas,
                          DisplayManager displayManager,
                          android.graphics.Typeface montserratTypeface) {
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
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_formula, parent, false);
        }

        TextView formulaTextView = convertView.findViewById(R.id.text_formula);
        formulaTextView.setTypeface(montserratTypeface);

        Formula formula = formulas.get(position);
        SpannableStringBuilder resultBuilder = new SpannableStringBuilder();


        String exprHtml = displayManager.getDisplayExpression(formula, null);
        Spanned exprSpanned = Html.fromHtml(exprHtml, Html.FROM_HTML_MODE_LEGACY);
        resultBuilder.append(exprSpanned).append("\n\n");
        resultBuilder.append("где:\n");


        for (String varId : formula.getVariables()) {
            String displayVar = displayManager.getDisplayTextFromLogicalId(varId);
            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(varId);
            String desc = (pq != null) ? pq.getType() : "описание не найдено";


            resultBuilder.append(displayVar)
                    .append(" — ")
                    .append(desc)
                    .append("\n");
        }

        formulaTextView.setText(resultBuilder);
        return convertView;
    }
}