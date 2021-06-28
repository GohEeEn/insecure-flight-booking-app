package ucd.comp47660.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucd.comp47660.model.JwtToken;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken, String>{
    JwtToken findByJwtToken(String jwtToken);
}
