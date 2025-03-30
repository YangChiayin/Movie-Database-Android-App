package com.example.assignment2_yang.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.assignment2_yang.model.FavoriteMovie;
import com.example.assignment2_yang.model.MovieModel;
import com.example.assignment2_yang.utils.ApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MovieViewModel extends ViewModel {

    // LiveData for storing movie list and details
    private final MutableLiveData<List<MovieModel>> movieDataList = new MutableLiveData<>();
    private final MutableLiveData<MovieModel> movieDetailData = new MutableLiveData<>();

    // Add these fields to the class
    private final MutableLiveData<List<FavoriteMovie>> favoritesLiveData = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // Add new LiveData for description
    private final MutableLiveData<String> favoriteDescriptionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccessLiveData = new MutableLiveData<>();

    public LiveData<List<MovieModel>> getMovieDataList() {
        return movieDataList;
    }

    public LiveData<MovieModel> getMovieDetailData() {
        return movieDetailData;
    }

    public LiveData<List<FavoriteMovie>> getFavorites() {
        return favoritesLiveData;
    }

    /**
     * Fetches a list of movies based on a search query.
     *
     * @param query  The search keyword
     * @param apiKey The API key for OMDb
     */
    public void searchMovies(String query, String apiKey) {
        String urlString = "https://www.omdbapi.com/?apikey=" + apiKey + "&s=" + query;

        ApiClient.get(urlString, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                movieDataList.postValue(null);
                Log.e("API Error", "Failed to fetch movies: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) return;
                Log.d("API Response", "Raw JSON: " + "Can't be empty");

                String responseData = response.body().string();
                Log.d("API Response", "Raw JSON: " + responseData);

                List<MovieModel> movies = new ArrayList<>();

                try {
                    JSONObject json = new JSONObject(responseData);
                    if (json.has("Search")) {
                        JSONArray movieArray = json.getJSONArray("Search");
                        for (int i = 0; i < movieArray.length(); i++) {
                            JSONObject movieJson = movieArray.getJSONObject(i);
                            MovieModel movie = new MovieModel(
                                    movieJson.optString("Title"),
                                    movieJson.optString("Year"),
                                    movieJson.optString("imdbID"),
                                    movieJson.optString("Type"),
                                    movieJson.optString("Poster")
                            );
                            movies.add(movie);
                        }
                    }
                    Log.e("xxxxxxx", "onResponse: " + movies);
                    Log.d("API Response", "movies: " + movies.size());

                    movieDataList.postValue(movies);


                } catch (JSONException e) {
                    movieDataList.postValue(null);
                }
            }
        });
    }

    /**
     * Fetches detailed information for a specific movie.
     *
     * @param imdbID The IMDb ID of the movie
     * @param apiKey The API key for OMDb
     */
    public void getMovieDetail(String imdbID, String apiKey) {
        // Change from "t=" to "i=" to search by IMDB ID
        String urlString = "https://www.omdbapi.com/?apikey=" + apiKey + "&i=" + imdbID;

        ApiClient.get(urlString, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                movieDetailData.postValue(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) return;

                String responseData = response.body().string();
                try {
                    JSONObject json = new JSONObject(responseData);
                    MovieModel movie = new MovieModel(
                            json.optString("Title"),
                            json.optString("Year"),
                            json.optString("imdbID"),
                            json.optString("Type"),
                            json.optString("Poster"),
                            json.optString("Rated"),
                            json.optString("Released"),
                            json.optString("Runtime"),
                            json.optString("Genre"),
                            json.optString("Director"),
                            json.optString("Writer"),
                            json.optString("Actors"),
                            json.optString("Plot"),
                            json.optString("Language"),
                            json.optString("Country"),
                            json.optString("Awards"),
                            json.optString("Metascore"),
                            json.optString("imdbRating"),
                            json.optString("imdbVotes"),
                            json.optString("BoxOffice")
                    );
                    movieDetailData.postValue(movie);
                } catch (JSONException e) {
                    movieDetailData.postValue(null);
                }
            }
        });
    }

    public void loadFavorites() {
        if (auth.getCurrentUser() == null) {
            Log.e("MovieViewModel", "No user logged in");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<FavoriteMovie> favorites = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        FavoriteMovie favorite = document.toObject(FavoriteMovie.class);
                        favorites.add(favorite);
                    }
                    favoritesLiveData.setValue(favorites);
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Log.e("MovieViewModel", "Error loading favorites", e);
                });
    }

    public void addToFavorites(MovieModel movie) {
        if (auth.getCurrentUser() == null) {
            Log.e("MovieViewModel", "No user logged in");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        FavoriteMovie favorite = new FavoriteMovie(
                userId,
                movie.getImdbID(),
                movie.getTitle(),
                movie.getYear(),
                movie.getPoster(),
                movie.getImdbRating(),
                movie.getDirector()
        );
        // Set the plot as the default description
        favorite.setDescription(movie.getPlot());

        db.collection("favorites")
                .add(favorite)
                .addOnSuccessListener(documentReference -> {
                    Log.d("MovieViewModel", "Favorite added with ID: " + documentReference.getId());
                    loadFavorites(); // Reload the favorites list
                })
                .addOnFailureListener(e -> {
                    Log.e("MovieViewModel", "Error adding favorite", e);
                });
    }

    public void removeFromFavorites(String movieId) {
        String userId = auth.getCurrentUser().getUid();
        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("movieId", movieId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    loadFavorites(); // Reload the favorites list
                })
                .addOnFailureListener(e -> {
                    Log.e("MovieViewModel", "Error removing favorite", e);
                });
    }

    public LiveData<String> getFavoriteDescription(String movieId) {
        String userId = auth.getCurrentUser().getUid();
        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("movieId", movieId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        FavoriteMovie favorite = document.toObject(FavoriteMovie.class);
                        favoriteDescriptionLiveData.setValue(favorite.getDescription());
                        return;
                    }
                    favoriteDescriptionLiveData.setValue("");
                })
                .addOnFailureListener(e -> {
                    Log.e("MovieViewModel", "Error getting description", e);
                    favoriteDescriptionLiveData.setValue("");
                });
        return favoriteDescriptionLiveData;
    }

    public LiveData<Boolean> updateFavoriteDescription(String movieId, String newDescription) {
        String userId = auth.getCurrentUser().getUid();
        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("movieId", movieId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference()
                                .update("description", newDescription)
                                .addOnSuccessListener(aVoid -> {
                                    updateSuccessLiveData.setValue(true);
                                    loadFavorites(); // Reload favorites list
                                })
                                .addOnFailureListener(e -> {
                                    updateSuccessLiveData.setValue(false);
                                });
                        return;
                    }
                    updateSuccessLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("MovieViewModel", "Error updating description", e);
                    updateSuccessLiveData.setValue(false);
                });
        return updateSuccessLiveData;
    }
}
