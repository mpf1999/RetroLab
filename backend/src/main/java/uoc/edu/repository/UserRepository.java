package uoc.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uoc.edu.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
}
