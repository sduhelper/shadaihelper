package com.shadai.shadaiHelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.about);
    }
    Life life_frg = new Life();
    Study study_frg = new Study();
    About about_frg = new About();

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item and false if the item should not be
     * selected. Consider setting non-selectable items as disabled preemptively to make them
     * appear non-interactive.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
        case R.id.life:
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view, life_frg).commit();
            return true;

            case R.id.study:
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view, study_frg).commit();
            return true;

            case R.id.about:
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view, about_frg).commit();
            return true;
    }
    return false;
    }
}