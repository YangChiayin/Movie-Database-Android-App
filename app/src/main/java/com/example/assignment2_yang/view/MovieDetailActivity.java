package com.example.assignment2_yang.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.assignment2_yang.R;
import com.example.assignment2_yang.databinding.ActivityMovieDetailBinding;
import com.example.assignment2_yang.model.MovieModel;
import com.example.assignment2_yang.viewmodel.MovieViewModel;

public class MovieDetailActivity extends AppCompatActivity {
    private static final String KEY_MOVIE_DATA = "KeyMovieData";
    private static final String KEY_IS_FROM_FAVORITES = "KeyIsFromFavorites";
    private ActivityMovieDetailBinding binding;
    private MovieModel model;
    private MovieViewModel viewModel;
    private boolean isFromFavorites;

    static public void enter(Activity activity, MovieModel model, boolean isFromFavorites) {
        Intent intent = new Intent(activity, MovieDetailActivity.class);
        intent.putExtra(KEY_MOVIE_DATA, model);
        intent.putExtra(KEY_IS_FROM_FAVORITES, isFromFavorites);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MovieViewModel.class);
        binding = ActivityMovieDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        model = (MovieModel) getIntent().getSerializableExtra(KEY_MOVIE_DATA);
        isFromFavorites = getIntent().getBooleanExtra(KEY_IS_FROM_FAVORITES, false);

        setupUI();
        checkFavoriteStatus();
    }

    private void setupUI() {
        binding.goBackButton.setOnClickListener(view -> finish());
        binding.movieTitle.setText(model.getTitle());
        binding.movieRating.setText(model.getImdbRating());
        binding.movieInfo.setText(model.getYear() + " " + model.getRuntime() + " " + model.getGenre());

        // Show/hide elements based on source
        if (isFromFavorites) {
            binding.editMovieDescription.setVisibility(View.VISIBLE);
            binding.updateButton.setVisibility(View.VISIBLE);
            binding.movieDescription.setVisibility(View.GONE);

            // Get the current description from Firestore
            viewModel.getFavoriteDescription(model.getImdbID()).observe(this, description -> {
                if (description != null && !description.isEmpty()) {
                    binding.editMovieDescription.setText(description);
                } else {
                    // If no custom description is set, use the plot as default
                    binding.editMovieDescription.setText(model.getPlot());
                }
            });
        } else {
            binding.editMovieDescription.setVisibility(View.GONE);
            binding.updateButton.setVisibility(View.GONE);
            binding.movieDescription.setVisibility(View.VISIBLE);
            binding.movieDescription.setText(model.getPlot());
        }

        Glide.with(getApplicationContext())
                .load(model.getPoster())
                .placeholder(R.mipmap.ic_launcher)
                .into(binding.moviePoster);

        binding.favoriteButton.setOnClickListener(view -> toggleFavorite());

        binding.updateButton.setOnClickListener(view -> {
            if (!isFromFavorites) return;

            String updatedDescription = binding.editMovieDescription.getText().toString().trim();
            if (!updatedDescription.isEmpty()) {
                viewModel.updateFavoriteDescription(model.getImdbID(), updatedDescription)
                        .observe(this, success -> {
                            if (success) {
                                Toast.makeText(this, "Description updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Failed to update description", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkFavoriteStatus() {
        viewModel.getFavorites().observe(this, favorites -> {
            boolean isFavorite = favorites != null && favorites.stream()
                    .anyMatch(fav -> fav.getMovieId().equals(model.getImdbID()));
            updateFavoriteButton(isFavorite);
        });
        viewModel.loadFavorites();
    }

    private void toggleFavorite() {
        if (binding.favoriteButton.getText().toString().equals("Remove from Favorites")) {
            viewModel.removeFromFavorites(model.getImdbID());
            updateFavoriteButton(false);
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
        } else {
            viewModel.addToFavorites(model);
            updateFavoriteButton(true);
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFavoriteButton(boolean isFavorite) {
        binding.favoriteButton.setText(isFavorite ? "Remove from Favorites" : "Add to Favorites");
    }
}
