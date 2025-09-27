package zechs.zplex.tvshows.model;

public record Episode(
        Integer id,
        String title,
        Short episodeNumber,
        Short seasonNumber,
        String overview,
        Short runtime,
        String release_date,
        String stillPath,
        String fileId
) {
}
