package zechs.zplex.zplex_api.model;

public class Movie {

    private Long id;
    private String title;
    private String posterPath;
    private Double voteAverage;
    private Integer year;
    private String fileId;

    public Movie() {
    }

    public Movie(Long id, String title, String posterPath, Double voteAverage, Integer year, String fileId) {
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
        this.voteAverage = voteAverage;
        this.year = year;
        this.fileId = fileId;
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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}

