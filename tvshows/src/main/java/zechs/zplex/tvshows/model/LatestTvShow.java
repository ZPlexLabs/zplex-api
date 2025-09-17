package zechs.zplex.tvshows.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LatestTvShow {

    private Integer tmdbId;
    private String title;
    private String posterPath;
    private String backdropPath;
    private Integer episodeCount;
    private String release;

    public LatestTvShow() {
    }

    public LatestTvShow(Integer tmdbId, String title, String posterPath, String backdropPath, Integer episodeCount, String release) {
        this.tmdbId = tmdbId;
        this.title = title;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.episodeCount = episodeCount;
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

    public Integer getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(Integer episodeCount) {
        this.episodeCount = episodeCount;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }
}


