package zechs.zplex.auth.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import zechs.zplex.common.model.MediaType;

@Entity
@Table(name = "user_blacklist")
public class UserBlacklist {

    @EmbeddedId
    private UserBlacklistId id;

    public UserBlacklist() {
    }

    public UserBlacklist(UserBlacklistId id) {
        this.id = id;
    }

    public UserBlacklist(String username, MediaType mediaType, int tmdbId) {
        this.id = new UserBlacklistId(username, mediaType, tmdbId);
    }

    public UserBlacklistId getId() {
        return id;
    }

    public void setId(UserBlacklistId id) {
        this.id = id;
    }
}
