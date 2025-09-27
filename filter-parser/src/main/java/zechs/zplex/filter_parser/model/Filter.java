package zechs.zplex.filter_parser.model;

import zechs.zplex.filter_parser.model.enums.Playstate;
import zechs.zplex.filter_parser.model.enums.Status;
import zechs.zplex.filter_parser.utils.FilterStringBuilder;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class Filter {

    private final Set<Integer> genres;
    private final Set<String> parentalRatings;
    private final Set<Integer> studios;
    private final Set<Integer> years;
    private Playstate playstate;
    private Status status;

    public Filter() {
        this.playstate = null;
        this.genres = new LinkedHashSet<>();
        this.parentalRatings = new LinkedHashSet<>();
        this.studios = new LinkedHashSet<>();
        this.years = new LinkedHashSet<>();
        this.status = null;
    }

    public Filter(Playstate playstate, Set<Integer> genres, Set<String> parentalRatings,
                  Set<Integer> studios, Set<Integer> years, Status status) {
        this.parentalRatings = parentalRatings;
        this.studios = studios;
        this.years = years;
        this.playstate = playstate;
        this.genres = genres;
        this.status = status;
    }

    public Playstate getPlaystate() {
        return playstate;
    }

    public void setPlaystate(Playstate playstate) {
        this.playstate = playstate;
    }

    public List<Integer> getGenres() {
        return genres.stream().toList();
    }

    public void setGenres(List<Integer> genres) {
        this.genres.clear();
        this.genres.addAll(genres);
    }

    public void addGenre(Integer genre) {
        this.genres.add(genre);
    }

    public List<String> getParentalRatings() {
        return parentalRatings.stream().toList();
    }

    public void setParentalRatings(List<String> parentalRatings) {
        this.parentalRatings.clear();
        this.parentalRatings.addAll(parentalRatings);
    }

    public void addParentRating(String parentRating) {
        this.parentalRatings.add(parentRating);
    }

    public List<Integer> getStudios() {
        return studios.stream().toList();
    }

    public void setStudios(List<Integer> studios) {
        this.studios.clear();
        this.studios.addAll(studios);
    }

    public void addStudio(Integer studio) {
        this.studios.add(studio);
    }

    public List<Integer> getYears() {
        return years.stream().toList();
    }

    public void setYears(List<Integer> years) {
        this.years.clear();
        this.years.addAll(years);
    }

    public void addYears(Integer years) {
        this.years.add(years);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return FilterStringBuilder.buildFilterString(playstate, genres, parentalRatings, studios, years, status, false);
    }

    public static class Builder {
        private final Set<Integer> genres;
        private final Set<String> parentalRatings;
        private final Set<Integer> studios;
        private final Set<Integer> years;
        private Playstate playstate;
        private Status status;
        private Boolean quote;

        public Builder() {
            this.playstate = null;
            this.genres = new LinkedHashSet<>();
            this.parentalRatings = new LinkedHashSet<>();
            this.studios = new LinkedHashSet<>();
            this.years = new LinkedHashSet<>();
            this.status = null;
            this.quote = false;
        }

        public Builder setPlaystate(Playstate playstate) {
            this.playstate = playstate;
            return this;
        }

        public Builder addGenre(Integer genre) {
            this.genres.add(genre);
            return this;
        }

        public Builder addParentRating(String parentRating) {
            this.parentalRatings.add(parentRating);
            return this;
        }

        public Builder addStudio(Integer studio) {
            this.studios.add(studio);
            return this;
        }

        public Builder addYear(int year) {
            this.years.add(year);
            return this;
        }

        public Builder setStatus(Status status) {
            this.status = status;
            return this;
        }

        public String build() {
            return FilterStringBuilder.buildFilterString(playstate, genres, parentalRatings, studios, years, status, quote);
        }

        public Filter getFilter() {
            return new Filter(playstate, genres, parentalRatings, studios, years, status);
        }

        public Builder quoteValuesInList(boolean quote) {
            this.quote = quote;
            return this;
        }
    }
}