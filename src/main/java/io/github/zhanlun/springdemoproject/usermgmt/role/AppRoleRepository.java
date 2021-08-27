package io.github.zhanlun.springdemoproject.usermgmt.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    Optional<AppRole> findByName(String name);

    @Query("" +
            "SELECT CASE WHEN COUNT(r) > 0 THEN " +
            "TRUE ELSE FALSE END " +
            "FROM AppRole r " +
            "WHERE r.name = ?1"
    )
    Boolean roleNameExists(String name);
}
