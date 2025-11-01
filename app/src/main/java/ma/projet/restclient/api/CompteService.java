package ma.projet.restclient.api;

import ma.projet.restclient.entities.Compte;
import ma.projet.restclient.entities.CompteList;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CompteService {
    // READ JSON
    @GET("banque/comptes")
    @Headers("Accept: application/json")
    Call<List<Compte>> getAllCompteJson();

    // READ XML
    @GET("banque/comptes")
    @Headers("Accept: application/xml")
    Call<CompteList> getAllCompteXml();

    // READ by id (JSON)
    @GET("banque/comptes/{id}")
    @Headers("Accept: application/json")
    Call<Compte> getCompteById(@Path("id") Long id);

    // WRITE JSON (server may return empty body -> Void)
    @POST("banque/comptes")
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    Call<Void> addCompte(@Body Compte compte);

    @PUT("banque/comptes/{id}")
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    Call<Void> updateCompte(@Path("id") Long id, @Body Compte compte);

    @DELETE("banque/comptes/{id}")
    Call<Void> deleteCompte(@Path("id") Long id);
}
