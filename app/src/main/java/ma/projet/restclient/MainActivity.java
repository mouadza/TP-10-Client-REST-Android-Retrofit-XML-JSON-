package ma.projet.restclient;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ma.projet.restclient.adapter.CompteAdapter;
import ma.projet.restclient.entities.Compte;
import ma.projet.restclient.repository.CompteRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements CompteAdapter.OnDeleteClickListener, CompteAdapter.OnUpdateClickListener {

    private RecyclerView recyclerView;
    private CompteAdapter adapter;
    private RadioGroup formatGroup;
    private FloatingActionButton addbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        setupFormatSelection();
        setupAddButton();

        loadData("JSON");
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        formatGroup = findViewById(R.id.formatGroup);
        addbtn = findViewById(R.id.fabAdd);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CompteAdapter(this, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupFormatSelection() {
        formatGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String format = checkedId == R.id.radioJson ? "JSON" : "XML";
            loadData(format);
        });
    }

    private void setupAddButton() {
        addbtn.setOnClickListener(v -> showAddCompteDialog());
    }

    private void showAddCompteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_compte, null);

        TextInputLayout tilSolde = dialogView.findViewById(R.id.tilSolde);
        EditText etSolde = dialogView.findViewById(R.id.etSolde);
        RadioGroup typeGroup = dialogView.findViewById(R.id.typeGroup);

        builder.setView(dialogView)
                .setTitle("Ajouter un compte")
                .setPositiveButton("Ajouter", null)
                .setNegativeButton("Annuler", (d, w) -> d.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String soldeStr = etSolde.getText() != null ? etSolde.getText().toString().trim() : "";
                if (soldeStr.isEmpty()) {
                    if (tilSolde != null) tilSolde.setError("Solde requis");
                    return;
                } else if (tilSolde != null) {
                    tilSolde.setError(null);
                }

                Double solde = parseDoubleLocaleAware(soldeStr);
                if (solde == null) {
                    if (tilSolde != null) tilSolde.setError("Format invalide (ex: 123.45)");
                    return;
                } else if (tilSolde != null) {
                    tilSolde.setError(null);
                }

                String type = (typeGroup.getCheckedRadioButtonId() == R.id.radioEpargne) ? "EPARGNE" : "COURANT";
                String formattedDate = getCurrentDateFormatted(); // "yyyy-MM-dd"

                Compte compte = new Compte(null, solde, type, formattedDate);
                addCompte(compte);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private String getCurrentDateFormatted() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return formatter.format(calendar.getTime());
    }

    private void addCompte(Compte compte) {
        CompteRepository compteRepository = new CompteRepository("JSON");
        compteRepository.addCompte(compte, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast("Compte ajouté");
                    loadData("JSON");
                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "";
                        Log.e("ADD_COMPTE", "HTTP " + response.code() + " " + err);
                    } catch (Exception ignore) {}
                    showToast("Échec ajout (code " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ADD_COMPTE", "POST failed", t);
                showToast("Erreur lors de l'ajout: " + (t.getMessage() != null ? t.getMessage() : t.toString()));
            }
        });
    }

    private void loadData(String format) {
        CompteRepository compteRepository = new CompteRepository(format);
        compteRepository.getAllCompte(new Callback<List<Compte>>() {
            @Override
            public void onResponse(Call<List<Compte>> call, Response<List<Compte>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Compte> comptes = response.body();
                    adapter.updateData(comptes);
                } else {
                    showToast("Échec chargement (code " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<List<Compte>> call, Throwable t) {
                Log.e("LOAD_COMPTE", "GET failed", t);
                showToast("Erreur: " + (t.getMessage() != null ? t.getMessage() : t.toString()));
            }
        });
    }

    @Override
    public void onUpdateClick(Compte compte) {
        showUpdateCompteDialog(compte);
    }

    private void showUpdateCompteDialog(Compte compte) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_compte, null);

        TextInputLayout tilSolde = dialogView.findViewById(R.id.tilSolde);
        EditText etSolde = dialogView.findViewById(R.id.etSolde);
        RadioGroup typeGroup = dialogView.findViewById(R.id.typeGroup);

        etSolde.setText(String.valueOf(compte.getSolde()));
        if ("COURANT".equalsIgnoreCase(compte.getType())) {
            typeGroup.check(R.id.radioCourant);
        } else if ("EPARGNE".equalsIgnoreCase(compte.getType())) {
            typeGroup.check(R.id.radioEpargne);
        }

        builder.setView(dialogView)
                .setTitle("Modifier un compte")
                .setPositiveButton("Modifier", null)
                .setNegativeButton("Annuler", (d, w) -> d.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String soldeStr = etSolde.getText() != null ? etSolde.getText().toString().trim() : "";
                if (soldeStr.isEmpty()) {
                    if (tilSolde != null) tilSolde.setError("Solde requis");
                    return;
                } else if (tilSolde != null) {
                    tilSolde.setError(null);
                }

                Double solde = parseDoubleLocaleAware(soldeStr);
                if (solde == null) {
                    if (tilSolde != null) tilSolde.setError("Format invalide (ex: 123.45)");
                    return;
                } else if (tilSolde != null) {
                    tilSolde.setError(null);
                }

                String type = (typeGroup.getCheckedRadioButtonId() == R.id.radioEpargne) ? "EPARGNE" : "COURANT";

                compte.setSolde(solde);
                compte.setType(type);
                updateCompte(compte);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void updateCompte(Compte compte) {
        CompteRepository compteRepository = new CompteRepository("JSON");
        compteRepository.updateCompte(compte.getId(), compte, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast("Compte modifié");
                    loadData("JSON");
                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "";
                        Log.e("UPDATE_COMPTE", "HTTP " + response.code() + " " + err);
                    } catch (Exception ignore) {}
                    showToast("Échec modification (code " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("UPDATE_COMPTE", "PUT failed", t);
                showToast("Erreur lors de la modification: " + (t.getMessage() != null ? t.getMessage() : t.toString()));
            }
        });
    }

    @Override
    public void onDeleteClick(Compte compte) {
        showDeleteConfirmationDialog(compte);
    }

    private void showDeleteConfirmationDialog(Compte compte) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Voulez-vous vraiment supprimer ce compte ?")
                .setPositiveButton("Oui", (dialog, which) -> deleteCompte(compte))
                .setNegativeButton("Non", null)
                .show();
    }

    private void deleteCompte(Compte compte) {
        CompteRepository compteRepository = new CompteRepository("JSON");
        compteRepository.deleteCompte(compte.getId(), new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast("Compte supprimé");
                    loadData("JSON");
                } else {
                    showToast("Échec suppression (code " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("DELETE_COMPTE", "DELETE failed", t);
                showToast("Erreur lors de la suppression: " + (t.getMessage() != null ? t.getMessage() : t.toString()));
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    /** Parse double respecting locale (accepts 12,34 and 12.34). */
    private Double parseDoubleLocaleAware(String raw) {
        try {
            NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
            Number n = nf.parse(raw);
            return n.doubleValue();
        } catch (ParseException e) {
            try {
                return Double.parseDouble(raw.replace(',', '.'));
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
