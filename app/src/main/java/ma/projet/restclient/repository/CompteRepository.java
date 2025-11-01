package ma.projet.restclient.repository;

import ma.projet.restclient.api.CompteService;
import ma.projet.restclient.entities.Compte;
import ma.projet.restclient.entities.CompteList;
import ma.projet.restclient.config.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompteRepository {
    private final CompteService compteService;
    private final String format;

    public CompteRepository(String converterType) {
        this.compteService = RetrofitClient.getClient(converterType).create(CompteService.class);
        this.format = converterType;
    }

    public void getAllCompte(Callback<List<Compte>> callback) {
        if ("JSON".equals(format)) {
            compteService.getAllCompteJson().enqueue(new Callback<List<Compte>>() {
                @Override
                public void onResponse(Call<List<Compte>> call, Response<List<Compte>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Compte> list = response.body();
                        for (Compte c : list) c.setDateCreation(normalizeDateToYMD(c.getDateCreation()));
                        callback.onResponse(call, Response.success(list));
                    } else {
                        callback.onResponse(call, Response.success(java.util.Collections.emptyList()));
                    }
                }
                @Override public void onFailure(Call<List<Compte>> call, Throwable t) {
                    callback.onFailure(call, t);
                }
            });
        } else {
            compteService.getAllCompteXml().enqueue(new Callback<CompteList>() {
                @Override
                public void onResponse(Call<CompteList> call, Response<CompteList> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Compte> comptes = response.body().getComptes();
                        for (Compte c : comptes) c.setDateCreation(normalizeDateToYMD(c.getDateCreation()));
                        callback.onResponse(null, Response.success(comptes));
                    } else {
                        callback.onResponse(null, Response.success(java.util.Collections.emptyList()));
                    }
                }
                @Override public void onFailure(Call<CompteList> call, Throwable t) {
                    callback.onFailure(null, t);
                }
            });
        }
    }


    /** Accepts ISO-8601 (with/without millis/zone), epoch millis, or yyyy-MM-dd; returns yyyy-MM-dd. */
    private String normalizeDateToYMD(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try {
            // epoch millis?
            if (raw.matches("^\\d{10,}$")) {
                long ms = Long.parseLong(raw);
                java.util.Date d = new java.util.Date(ms);
                return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(d);
            }

            // Already yyyy-MM-dd?
            if (raw.matches("^\\d{4}-\\d{2}-\\d{2}$")) return raw;

            // Try common ISO-8601 patterns (API-24 safe)
            String[] patterns = {
                    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                    "yyyy-MM-dd'T'HH:mm:ssXXX",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd'T'HH:mmXXX",
                    "yyyy-MM-dd'T'HH:mm'Z'"
            };
            for (String p : patterns) {
                try {
                    java.text.SimpleDateFormat in = new java.text.SimpleDateFormat(p, java.util.Locale.US);
                    in.setLenient(true);
                    java.util.Date d = in.parse(raw);
                    if (d != null) {
                        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(d);
                    }
                } catch (Exception ignore) { }
            }
        } catch (Exception ignoreOuter) { }
        // Fallback: return as-is (adapter must handle null/unknown)
        return raw;
    }



    public void getCompteById(Long id, Callback<Compte> callback) {
        compteService.getCompteById(id).enqueue(callback);
    }

    // ✅ Void bodies for write ops
    public void addCompte(Compte compte, Callback<Void> callback) {
        compteService.addCompte(compte).enqueue(callback);
    }

    public void updateCompte(Long id, Compte compte, Callback<Void> callback) {
        compteService.updateCompte(id, compte).enqueue(callback);
    }

    public void deleteCompte(Long id, Callback<Void> callback) {
        compteService.deleteCompte(id).enqueue(callback);
    }
}
