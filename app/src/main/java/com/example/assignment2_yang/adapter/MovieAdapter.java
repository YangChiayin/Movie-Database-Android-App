package com.example.assignment2_yang.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.assignment2_yang.R;
import com.example.assignment2_yang.model.MovieModel;

import java.util.ArrayList;
import java.util.List;

// Adapter class for managing and displaying movie items in a RecyclerView
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<MovieModel> movies = new ArrayList<>();
    private OnMovieClickListener onMovieClickListener;
    private boolean isSearchTab = true; // Default to search tab

    // Sets the movie list and refreshes the RecyclerView
    public void setMovies(List<MovieModel> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    // Sets the listener for handling item clicks (once go back button click)
    public void setOnMovieClickListener(OnMovieClickListener listener) {
        this.onMovieClickListener = listener;
    }

    // Add a method to set the flag
    public void setSearchTab(boolean isSearchTab) {
        this.isSearchTab = isSearchTab;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_movie_item, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        MovieModel movie = movies.get(position);

        // Bind movie title
        holder.title.setText(movie.getTitle());

        // Show only the year if the director (studio) is not available
        if (movie.getDirector() != null && !movie.getDirector().isEmpty()) {
            holder.info.setText(String.format("%s • %s", movie.getYear(), movie.getDirector()));
        } else {
            holder.info.setText(movie.getYear()); // Only display the year
        }

        // Show rating if available
        if (movie.getImdbRating() != null && !movie.getImdbRating().isEmpty()) {
            holder.rating.setVisibility(View.VISIBLE);
            holder.rating.setText("Rating: " + movie.getImdbRating());
        } else {
            holder.rating.setVisibility(View.GONE);
        }

        // Load the movie poster image using Glide
        Glide.with(holder.itemView.getContext())
                .load(movie.getPoster())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.poster);

        // Handle item click event
        holder.itemView.setOnClickListener(view -> {
            if (onMovieClickListener != null) {
                onMovieClickListener.onGoBackClicked(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    // ViewHolder class to hold references to the views
    static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView title, info, rating;
        ImageView poster;

        MovieViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.movie_title);
            info = itemView.findViewById(R.id.movie_info);
            rating = itemView.findViewById(R.id.movie_rating);
            poster = itemView.findViewById(R.id.movie_poster);
        }
    }

    // Interface for handling the "Go Back" button click event
    public interface OnMovieClickListener {
        void onGoBackClicked(MovieModel model);
    }
}
