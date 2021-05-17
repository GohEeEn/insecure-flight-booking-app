package ucd.comp40660.user.repository;

import ucd.comp40660.user.model.Role;
import ucd.comp40660.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}
