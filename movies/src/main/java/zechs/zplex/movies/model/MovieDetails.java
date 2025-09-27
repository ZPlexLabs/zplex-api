package zechs.zplex.movies.model;

import zechs.zplex.config.model.IdNamePair;
import zechs.zplex.media.model.Cast;
import zechs.zplex.media.model.Crew;
import zechs.zplex.media.model.Studio;

import java.util.List;

public record MovieDetails(
        Integer tmdbId,
        String title,
        Integer collectionId,
        String fileId,
        String imdbId,
        String imdbRating,
        Long imdbVotes,
        String releaseDate,
        Short releaseYear,
        String parentalRating,
        Short runtime,
        String posterPath,
        String backdropPath,
        String logoPath,
        String trailerLink,
        String tagline,
        String plot,
        String director,
        List<IdNamePair> genres,
        List<Studio> studios,
        List<Cast> casts,
        List<Crew> crews,
        List<IdNamePair> collections) {
}