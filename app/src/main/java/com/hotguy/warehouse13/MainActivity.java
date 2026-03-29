package com.hotguy.warehouse13;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hotguy.warehouse13.databinding.ActivityMainBinding;
import com.hotguy.warehouse13.view.AddFragment;
import com.hotguy.warehouse13.view.ListFragment;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_ADD = "tag_add";
    private static final String TAG_LIST = "tag_list";
    private static final String TAG_SETTING = "tag_setting";
    private ActivityMainBinding binding;
    private Toolbar toolbar;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(binding.main.getId()), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        toolbar = binding.appBarMain.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        if (savedInstanceState == null) {
            loadFragment(TAG_ADD);
        }

        bottomNav = binding.contentMain.bottomNav;
        bottomNav.setSelectedItemId(R.id.nav1_add);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav1_add) {
                loadFragment(TAG_ADD);
            } else if (item.getItemId() == R.id.nav2_lists) {
                loadFragment(TAG_LIST);
            } else if (item.getItemId() == R.id.nav2_lists) {
                loadFragment(TAG_SETTING);
            }
            return true;
        });
    }

    private void loadFragment(String tag) {
        Fragment target = getSupportFragmentManager().findFragmentByTag(tag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (target == null) {
            target = tag.equals(TAG_ADD) ? new AddFragment() : new ListFragment();
            transaction.add(binding.contentMain.fragmentContainerView.getId(), target, tag);
        }

        // Only hide the known
        for (String knownTag : new String[]{TAG_ADD, TAG_LIST, TAG_SETTING}) {
            Fragment f = getSupportFragmentManager().findFragmentByTag(knownTag);
            if (f != null && !knownTag.equals(tag)) {
                transaction.hide(f);
            }
        }

        transaction.show(target).commit();
    }
}
