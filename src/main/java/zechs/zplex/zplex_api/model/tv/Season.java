package zechs.zplex.zplex_api.model.tv;

public class Season {

    private Integer id;
    private String name;
    private String posterPath;
    private Integer seasonNumber;

    public Season() {
    }

    public Season(Integer id, String name, String posterPath, Integer seasonNumber) {
        this.id = id;
        this.name = name;
        this.posterPath = posterPath;
        this.seasonNumber = seasonNumber;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public Integer getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(Integer seasonNumber) {
        this.seasonNumber = seasonNumber;
    }
}
