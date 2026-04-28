package com.interviewprep.platform.repository;

import com.interviewprep.platform.entity.RoleProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleProfileRepository extends JpaRepository<RoleProfile, Long> {

    List<RoleProfile> findAllByOrderByNameAsc();

    Optional<RoleProfile> findByNameIgnoreCase(String name);
}
