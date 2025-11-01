package ma.projet.restclient.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ma.projet.restclient.R;
import ma.projet.restclient.entities.Compte;

public class CompteAdapter extends RecyclerView.Adapter<CompteAdapter.CompteViewHolder> {
    public interface OnDeleteClickListener { void onDeleteClick(Compte compte); }
    public interface OnUpdateClickListener { void onUpdateClick(Compte compte); }

    private final List<Compte> comptes;
    private final OnDeleteClickListener onDeleteClickListener;
    private final OnUpdateClickListener onUpdateClickListener;

    public CompteAdapter(OnDeleteClickListener onDeleteClickListener,
                         OnUpdateClickListener onUpdateClickListener) {
        this.comptes = new ArrayList<>();
        this.onDeleteClickListener = onDeleteClickListener;
        this.onUpdateClickListener = onUpdateClickListener;
    }

    @NonNull @Override
    public CompteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_compte, parent, false);
        return new CompteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompteViewHolder holder, int position) {
        holder.bind(comptes.get(position));
    }

    @Override
    public int getItemCount() { return comptes.size(); }

    public void updateData(List<Compte> newComptes) {
        this.comptes.clear();
        if (newComptes != null) this.comptes.addAll(newComptes);
        notifyDataSetChanged();
    }

    class CompteViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvId, tvSolde, tvType, tvDate;
        private final View btnDelete, btnUpdate;

        public CompteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvSolde = itemView.findViewById(R.id.tvSolde);
            tvType = itemView.findViewById(R.id.tvType);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnUpdate = itemView.findViewById(R.id.btnEdit);
        }

        public void bind(Compte compte) {
            tvId.setText("ID: " + (compte.getId() != null ? compte.getId() : "—"));
            tvSolde.setText(String.format(Locale.getDefault(), "Solde: %.2f", compte.getSolde()));
            tvType.setText("Type: " + (compte.getType() != null ? compte.getType() : "—"));
            tvDate.setText("Date: " + formatDateDisplay(compte.getDateCreation()));

            btnDelete.setOnClickListener(v -> {
                if (onDeleteClickListener != null) onDeleteClickListener.onDeleteClick(compte);
            });
            btnUpdate.setOnClickListener(v -> {
                if (onUpdateClickListener != null) onUpdateClickListener.onUpdateClick(compte);
            });
        }

        private String formatDateDisplay(String raw) {
            if (raw == null) return "—";
            raw = raw.trim();
            if (raw.isEmpty()) return "—";

            // Try common API formats — adjust if your backend differs.
            // 1) ISO instant/offset (e.g., 2025-10-31T22:12:34Z or +01:00) → show dd/MM/yyyy
            try {
                // Parse with java.text for broad compatibility
                // If you know it's exactly "yyyy-MM-dd", skip to the 3rd try.
                SimpleDateFormat isoZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                isoZ.setLenient(true);
                return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(isoZ.parse(raw));
            } catch (Exception ignored) { }

            try {
                // ISO with milliseconds Z
                SimpleDateFormat isoMsZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                isoMsZ.setLenient(true);
                return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(isoMsZ.parse(raw));
            } catch (Exception ignored) { }

            try {
                // Without timezone: 2025-10-31T22:12:34
                SimpleDateFormat isoNoZone = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                isoNoZone.setLenient(true);
                return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(isoNoZone.parse(raw));
            } catch (Exception ignored) { }

            try {
                // Date only: 2025-10-31
                SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                dateOnly.setLenient(true);
                return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(dateOnly.parse(raw));
            } catch (ParseException ignored) { }

            // Millis epoch (if backend sends numbers as string)
            try {
                long epoch = Long.parseLong(raw);
                return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(new java.util.Date(epoch));
            } catch (Exception ignored) { }

            // Fallback: show raw string
            return raw;
        }
    }
}
