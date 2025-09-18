package zechs.zplex.tvshows.model;

import zechs.zplex.config.model.IdNamePair;
import zechs.zplex.media.model.Cast;
import zechs.zplex.media.model.Crew;
import zechs.zplex.media.model.Studio;

import java.util.List;

public record TvShowDetails(
        Integer tmdbId,
        String title,
        String imdbId,
        String imdbRating,
        Long imdbVotes,
        String releaseDate,
        String release,
        String parentalRating,
        String posterPath,
        String backdropPath,
        String logoPath,
        String trailerLink,
        String plot,
        String director,
        List<IdNamePair> genres,
        List<Studio> studios,
        List<Cast> casts,
        List<Crew> crews,
        Season latestSeason) {
}