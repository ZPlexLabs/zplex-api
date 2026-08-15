package zechs.zplex.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zechs.zplex.auth.model.UserBlacklist;
import zechs.zplex.auth.model.UserBlacklistId;

import java.util.List;

@Repository
public interface UserBlacklistRepository extends JpaRepository<UserBlacklist, UserBlacklistId> {
    List<UserBlacklist> findByIdUsername(String username);

    void deleteByIdUsername(String username);
}
