package zechs.zplex.media.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaListItem {

    private Integer tmdbId;
    private String title;
    private String posterPath;
    private Double imdbRating;
    private String release;

    public MediaListItem() {
    }

    public MediaListItem(Integer tmdbId, String title, String posterPath, Double imdbRating, String release) {
        this.tmdbId = tmdbId;
        this.title = title;
        this.posterPath = posterPath;
        this.imdbRating = imdbRating;
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

    public Double getImdbRating() {
        return imdbRating;
    }

    public void setImdbRating(Double imdbRating) {
        this.imdbRating = imdbRating;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }
}
