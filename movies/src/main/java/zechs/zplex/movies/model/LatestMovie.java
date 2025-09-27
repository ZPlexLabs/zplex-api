package zechs.zplex.movies.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LatestMovie {

    private Integer tmdbId;
    private String title;
    private String posterPath;
    private String backdropPath;
    // Although this could be an Integer, we use a String to maintain consistency
    // with the response model in the LatestTvShow class.
    private String release;

    public LatestMovie() {
    }

    public LatestMovie(Integer tmdbId, String title, String posterPath, String backdropPath, String release) {
        this.tmdbId = tmdbId;
        this.title = title;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.release = release;
    }

    public Integer getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(Integer tmdbId) {
        this.tmdbId = tmdbId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }
}

