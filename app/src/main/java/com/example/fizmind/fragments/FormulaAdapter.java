package com.example.fizmind.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.fizmind.R;
import com.example.fizmind.animation.CustomTypefaceSpan;
import com.example.fizmind.formulas.Formula;
import com.example.fizmind.keyboard.DisplayManager;
import com.example.fizmind.quantly.PhysicalQuantity;
import com.example.fizmind.quantly.PhysicalQuantityRegistry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FormulaAdapter extends BaseAdapter {

    private final Context context;
    private final List<Formula> formulas;
    private final DisplayManager displayManager;
    private final Typeface montserratTypeface;
    private final Typeface stixTypeface;

    //  должны рендериться курсивом (STIX)
    private static final Set<String> ITALIC_IDS = new HashSet<>(Arrays.asList(
            "s_latin", "v_latin", "a_latin", "m_latin", "F_latin",
            "E_latin", "U_latin", "R_latin", "S_latin", "h_latin", "c_latin"
    ));

    //ПОТОМ ПОМЕНЯТЬ!!!!!!!!!!!!!!!!!!!!!!!!!!!!


    public FormulaAdapter(Context context,
                          List<Formula> formulas,
                          DisplayManager displayManager,
                          Typeface montserratTypeface,
                          Typeface stixTypeface) {
        this.context = context;
        this.formulas = formulas;
        this.displayManager = displayManager;
        this.montserratTypeface = montserratTypeface;
        this.stixTypeface = stixTypeface;
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

        // дроби через HTML
        String exprHtml = displayManager.getDisplayExpression(formula, null);
        Spanned exprSpanned = Html.fromHtml(exprHtml, Html.FROM_HTML_MODE_LEGACY);
        SpannableStringBuilder exprBuilder = new SpannableStringBuilder(exprSpanned);

        //  нужно ли применять шрифт STIX
        for (String varId : formula.getVariables()) {
            if (ITALIC_IDS.contains(varId)) {
                String displayVar = displayManager.getDisplayTextFromLogicalId(varId);
                String text = exprBuilder.toString();
                int start = text.indexOf(displayVar);
                if (start >= 0) {
                    exprBuilder.setSpan(
                            new CustomTypefaceSpan(stixTypeface),
                            start, start + displayVar.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                }
            }
        }
        resultBuilder.append(exprBuilder).append("\n\n");


        resultBuilder.append("где:\n");
        for (String varId : formula.getVariables()) {
            String displayVar = displayManager.getDisplayTextFromLogicalId(varId);
            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(varId);
            String desc = (pq != null) ? pq.getType() : "описание не найдено";


            int varStart = resultBuilder.length();
            resultBuilder.append(displayVar);
            if (ITALIC_IDS.contains(varId)) {
                resultBuilder.setSpan(
                        new CustomTypefaceSpan(stixTypeface),
                        varStart, resultBuilder.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            // остальной текст — Montserrat
            resultBuilder.append(" — ").append(desc).append("\n");
        }

        formulaTextView.setText(resultBuilder);
        return convertView;
    }
}
