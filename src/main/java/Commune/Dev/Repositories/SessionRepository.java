package Commune.Dev.Repositories;

import Commune.Dev.Models.Session;
import Commune.Dev.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByUserAndStatus(User user, Session.SessionStatus status);
}
