package zechs.zplex.userdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zechs.zplex.userdata.model.Playlist;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    List<Playlist> findByUsernameOrderByUpdatedAtDesc(String username);

    Optional<Playlist> findByIdAndUsername(Long id, String username);

    long deleteByIdAndUsername(Long id, String username);
}
