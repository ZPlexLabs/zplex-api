package zechs.zplex.zplex_api.model.tv;

public class Show {

    private Long id;
    private String title;
    private String posterPath;
    private Double voteAverage;

    public Show() {
    }

    public Show(Long id, String title, String posterPath, Double voteAverage) {
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
        this.voteAverage = voteAverage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(Double voteAverage) {
        this.voteAverage = voteAverage;
    }

}

