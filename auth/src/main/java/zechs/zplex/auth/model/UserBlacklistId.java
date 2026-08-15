package zechs.zplex.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import zechs.zplex.common.model.MediaType;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserBlacklistId implements Serializable {

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    @Column(name = "tmdb_id", nullable = false)
    private int tmdbId;

    public UserBlacklistId() {
    }

    public UserBlacklistId(String username, MediaType mediaType, int tmdbId) {
        this.username = username;
        this.mediaType = mediaType;
        this.tmdbId = tmdbId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public int getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(int tmdbId) {
        this.tmdbId = tmdbId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserBlacklistId that)) return false;
        return tmdbId == that.tmdbId
                && Objects.equals(username, that.username)
                && mediaType == that.mediaType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, mediaType, tmdbId);
    }
}
