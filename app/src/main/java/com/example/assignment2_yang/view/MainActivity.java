package com.example.assignment2_yang.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.assignment2_yang.R;
import com.example.assignment2_yang.adapter.MovieAdapter;
import com.example.assignment2_yang.databinding.ActivityMainBinding;
import com.example.assignment2_yang.model.MovieModel;
import com.example.assignment2_yang.viewmodel.MovieViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    MovieViewModel viewModel; // ViewModel for managing UI-related data
    ActivityMainBinding binding; // ViewBinding for accessing UI elements
    private MovieAdapter movieAdapter; // Adapter for RecyclerView
    private final String API_KEY = "3ddf33ee"; // API key for OMDb API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MovieViewModel.class);

        // Setup RecyclerView, Bottom Navigation, Observers, and Search Button
        setupRecyclerView();
        setupBottomNavigation();
        setupObservers();
        setupSearchButton();
    }

    private void setupRecyclerView() {
        // Initialize the adapter and set it to the RecyclerView
        movieAdapter = new MovieAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(movieAdapter);

        // Handle movie item click to fetch detailed information
        movieAdapter.setOnMovieClickListener(movie -> {
            viewModel.getMovieDetail(movie.getImdbID(), API_KEY);
        });
    }

    private void setupBottomNavigation() {
        // Handle navigation between Search and Favorites tabs
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_search) {
                // Show Search UI
                binding.titleText.setText("Search Movies");
                binding.searchEditText.setVisibility(android.view.View.VISIBLE);
                binding.searchButton.setVisibility(android.view.View.VISIBLE);

                // Load previous search results or clear the adapter
                if (viewModel.getMovieDataList().getValue() == null || viewModel.getMovieDataList().getValue().isEmpty()) {
                    movieAdapter.setMovies(new ArrayList<>());
                } else {
                    movieAdapter.setMovies(viewModel.getMovieDataList().getValue());
                }
                return true;
            } else if (itemId == R.id.navigation_favorites) {
                // Show Favorites UI
                binding.titleText.setText("Favorite Movies");
                binding.searchEditText.setVisibility(android.view.View.GONE);
                binding.searchButton.setVisibility(android.view.View.GONE);
                viewModel.loadFavorites();
                return true;
            }
            return false;
        });
    }

    private void setupObservers() {
        // Observe movie search results and update the RecyclerView
        viewModel.getMovieDataList().observe(this, movies -> {
            if (movies != null && !movies.isEmpty()) {
                movieAdapter.setMovies(movies);
            } else {
                Toast.makeText(this, "No movies found", Toast.LENGTH_SHORT).show();
            }
        });

        // Observe detailed movie data and navigate to the detail screen
        viewModel.getMovieDetailData().observe(this, movie -> {
            if (movie != null) {
                boolean isFromFavorites = binding.bottomNavigation.getSelectedItemId() == R.id.navigation_favorites;
                MovieDetailActivity.enter(this, movie, isFromFavorites);
            }
        });

        // Observe favorite movies and update the RecyclerView
        viewModel.getFavorites().observe(this, favorites -> {
            if (favorites != null && !favorites.isEmpty()) {
                List<MovieModel> movieModels = favorites.stream()
                        .map(fav -> {
                            MovieModel model = new MovieModel(
                                    fav.getTitle(),
                                    fav.getYear(),
                                    fav.getMovieId(),
                                    "",
                                    fav.getPoster()
                            );
                            model.setImdbRating(fav.getRating());
                            model.setDirector(fav.getStudio());
                            return model;
                        })
                        .collect(Collectors.toList());
                movieAdapter.setMovies(movieModels);
            } else {
                movieAdapter.setMovies(new ArrayList<>());
                Toast.makeText(this, "No favorites found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchButton() {
        // Handle search button click to fetch movies from the API
        binding.searchButton.setOnClickListener(view -> {
            String query = binding.searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                viewModel.searchMovies(query, API_KEY);
            } else {
                Toast.makeText(this, "Please enter a movie title", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
