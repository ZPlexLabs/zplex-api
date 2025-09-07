package zechs.zplex.config.model;

import java.util.List;

public class FilterConfig {
    private String type;
    private List<IdNamePair> genres;
    private List<IdNamePair> studios;
    private List<Integer> years;
    private List<String> parentalRatings;

    public FilterConfig() {
    }

    public FilterConfig(String type, List<IdNamePair> genres, List<IdNamePair> studios, List<Integer> years, List<String> parentalRatings) {
        this.type = type;
        this.genres = genres;
        this.studios = studios;
        this.years = years;
        this.parentalRatings = parentalRatings;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<IdNamePair> getGenres() {
        return genres;
    }

    public void setGenres(List<IdNamePair> genres) {
        this.genres = genres;
    }

    public List<IdNamePair> getStudios() {
        return studios;
    }

    public void setStudios(List<IdNamePair> studios) {
        this.studios = studios;
    }

    public List<Integer> getYears() {
        return years;
    }

    public void setYears(List<Integer> years) {
        this.years = years;
    }

    public List<String> getParentalRatings() {
        return parentalRatings;
    }

    public void setParentalRatings(List<String> parentalRatings) {
        this.parentalRatings = parentalRatings;
    }
}
