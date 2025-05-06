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

import java.util.List;

public class FormulaAdapter extends BaseAdapter {

    private final Context context;
    private final List<Formula> formulas;
    private final DisplayManager displayManager;
    private final Typeface montserratTypeface;
    private final Typeface stixTypeface;

    /**
     * адаптер для списка формул, где математические обозначения (v, m, etc.) рендерятся шрифтом STIX,
     * а весь остальной текст — Montserrat
     */
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
        // по умолчанию весь текст Montserrat
        formulaTextView.setTypeface(montserratTypeface);

        Formula formula = formulas.get(position);
        SpannableStringBuilder builder = new SpannableStringBuilder();

        // 1. математическое выражение
        String exprHtml = displayManager.getDisplayExpression(formula, null);
        // преобразуем HTML (sup/sub) в Spanned
        Spanned spanExpr = Html.fromHtml(exprHtml, Html.FROM_HTML_MODE_LEGACY);
        SpannableStringBuilder spanBuilder = new SpannableStringBuilder(spanExpr);
        // выделяем каждую переменную шрифтом STIX
        for (String logicalId : formula.getVariables()) {
            String displayVar = displayManager.getDisplayTextFromLogicalId(logicalId);
            int index = spanBuilder.toString().indexOf(displayVar);
            if (index >= 0) {
                spanBuilder.setSpan(
                        new CustomTypefaceSpan(stixTypeface),
                        index, index + displayVar.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }
        builder.append(spanBuilder).append("\n\n");

        // 2. описание: 'где:' + список
        builder.append("где:\n");
        for (String logicalId : formula.getVariables()) {
            String displayVar = displayManager.getDisplayTextFromLogicalId(logicalId);
            PhysicalQuantity pq = PhysicalQuantityRegistry.getPhysicalQuantity(logicalId);
            String desc = (pq != null) ? pq.getType() : "описание не найдено";

            // переменная: STIX
            int varStart = builder.length();
            builder.append(displayVar);
            builder.setSpan(
                    new CustomTypefaceSpan(stixTypeface),
                    varStart, builder.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            // разделитель и описание: Montserrat по умолчанию
            builder.append(" — ");
            builder.append(desc).append("\n");
        }

        formulaTextView.setText(builder);
        return convertView;
    }
}
