package co.geisyanne.netflixclone.model;

import java.util.List;

public class MovieDetailSimilar {

    private final Movie movie;
    private final List<Movie> movieSimilar;

    public MovieDetailSimilar(Movie movie, List<Movie> movieSimilar) {
        this.movie = movie;
        this.movieSimilar = movieSimilar;
    }

    public Movie getMovie() {
        return movie;
    }

    public List<Movie> getMovieSimilar() {
        return movieSimilar;
    }
}
