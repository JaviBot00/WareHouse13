package com.hotguy.warehouse13;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hotguy.warehouse13.databinding.ActivityMainBinding;
import com.hotguy.warehouse13.view.AddFragment;
import com.hotguy.warehouse13.view.FiltersFragment;
import com.hotguy.warehouse13.view.ListFragment;
import com.hotguy.warehouse13.view.SettingsFragment;

import java.util.Objects;

/**
 * MainActivity — Activity principal de la app.
 * <p>
 * Responsabilidad MVC:
 * · Inicializa el Controlador (Singleton) y carga datos desde fichero.
 * · Gestiona la navegación entre los 4 Fragments.
 * · No contiene lógica de negocio.
 * <p>
 * Carga de datos:
 * Al arrancar, intenta cargar products.json desde el almacenamiento
 * interno. Si el fichero no existe (primera ejecución), el Controlador
 * mantendrá la lista vacía. El usuario puede cargar datos de ejemplo
 * desde Ajustes o añadirlos manualmente.
 * <p>
 * ALTERNATIVA: puedes cambiar la línea de loadDataFromFile() por
 * DataAccess.loadData() en el Constructor del Controlador para
 * cargar siempre los datos de prueba hardcodeados en DataAccess.
 */
public class MainActivity extends AppCompatActivity {

    // Tags para el BackStack de Fragments
    private static final String TAG_ADD = "tag_add";
    private static final String TAG_LIST = "tag_list";
    private static final String TAG_FILTERS = "tag_filters";
    private static final String TAG_SETTINGS = "tag_settings";
    private static final String[] ALL_TAGS = {TAG_ADD, TAG_LIST, TAG_FILTERS, TAG_SETTINGS};

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ── Toolbar ──
        Toolbar toolbar = binding.appBarMain.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // ── Carga inicial de datos desde fichero (si existe) ──
        // Si el fichero no existe, el catch interno de DataAccess
        // devuelve lista vacía sin crashear.
//        Controller.getSingleton().loadProductList(this);

        // ── Fragment inicial ──
        if (savedInstanceState == null) {
            loadFragment(TAG_ADD);
        }

        // ── BottomNav ──
        BottomNavigationView bottomNav = binding.contentMain.bottomNav;
        bottomNav.setSelectedItemId(R.id.nav_add);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_add)
                loadFragment(TAG_ADD);
            else if (id == R.id.nav_list)
                loadFragment(TAG_LIST);
            else if (id == R.id.nav_filters)
                loadFragment(TAG_FILTERS);
            else if (id == R.id.nav_settings)
                loadFragment(TAG_SETTINGS);
            return true;
        });
    }

    /**
     * Carga (o muestra) el fragment correspondiente al tag.
     * Si no existe, lo crea y lo añade al back stack.
     * Los demás fragments se ocultan (no se destruyen).
     */
    private void loadFragment(String tag) {
        Fragment target = getSupportFragmentManager().findFragmentByTag(tag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (target == null) {
            target = createFragment(tag);
            transaction.add(binding.contentMain.fragmentContainerView.getId(), target, tag);
        }

        // Ocultar todos los demás fragments conocidos
        for (String knownTag : ALL_TAGS) {
            Fragment f = getSupportFragmentManager().findFragmentByTag(knownTag);
            if (f != null && !knownTag.equals(tag)) {
                transaction.hide(f);
            }
        }

        transaction.show(target).commit();
    }

    private Fragment createFragment(String tag) {
        return switch (tag) {
            case TAG_LIST -> new ListFragment();
            case TAG_FILTERS -> new FiltersFragment();
            case TAG_SETTINGS -> new SettingsFragment();
            default -> new AddFragment();
        };
    }
}
