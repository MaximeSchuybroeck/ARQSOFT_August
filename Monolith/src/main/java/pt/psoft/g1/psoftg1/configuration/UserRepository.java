package pt.psoft.g1.psoftg1.configuration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.usermanagement.model.User;

import java.util.Optional;

@Repository
interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String email);

    Optional<User> findByUsername(String email);
}
