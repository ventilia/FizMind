package com.example.fizmind.Activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.fizmind.R;
import com.example.fizmind.fragments.DesignationsFragment;
import com.example.fizmind.fragments.FormulasFragment;
import com.example.fizmind.fragments.GuideFragment;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String type = getIntent().getStringExtra("type");
        Fragment fragment;

        if ("formulas".equals(type)) {
            fragment = new FormulasFragment();
        } else if ("designations".equals(type)) {
            fragment = new DesignationsFragment();
        } else if ("guide".equals(type)) {
            fragment = new GuideFragment();
        } else {

            fragment = new FormulasFragment();
        }


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}