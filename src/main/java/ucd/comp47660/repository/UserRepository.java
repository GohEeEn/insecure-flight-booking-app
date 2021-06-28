package ucd.comp47660.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucd.comp47660.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findAllByUsernameAndPassword(String username, String password);

    User findByUsername(String username);

    User findByEmail(String email);

    User findByPhone(String phone);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
