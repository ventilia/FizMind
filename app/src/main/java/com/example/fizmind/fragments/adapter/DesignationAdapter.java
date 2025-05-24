package com.example.fizmind.fragments.adapter;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.fizmind.R;
import com.example.fizmind.quantly.PhysicalQuantity;
import com.example.fizmind.keyboard.DisplayManager;

import java.util.List;

public class DesignationAdapter extends BaseAdapter {

    private final Context context;
    private final List<PhysicalQuantity> quantities;
    private final DisplayManager displayManager;

    public DesignationAdapter(Context context, List<PhysicalQuantity> quantities, DisplayManager displayManager) {
        this.context = context;
        this.quantities = quantities;
        this.displayManager = displayManager;
    }

    @Override
    public int getCount() {
        return quantities.size();
    }

    @Override
    public Object getItem(int position) {
        return quantities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_designation, parent, false);
        }

        TextView designationTextView = convertView.findViewById(R.id.text_designation);
        PhysicalQuantity quantity = quantities.get(position);

        SpannableStringBuilder designationText = new SpannableStringBuilder();
        String displayDesignation = displayManager.getDisplayTextFromLogicalId(quantity.getId());
        designationText.append(displayDesignation)
                .append(" - ")
                .append(quantity.getType())
                .append(" (")
                .append(String.join(", ", quantity.getAllowedUnits()))
                .append(")");

        designationTextView.setText(designationText);
        return convertView;
    }
}