package Commune.Dev.Repositories;

import Commune.Dev.Models.User;
import Commune.Dev.Models.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    Optional<UserActivity> findByUser(User user);
    Optional<UserActivity> findByUserId(Long userId);
}
