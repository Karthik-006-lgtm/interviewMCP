package com.interviewprep.platform.repository;

import com.interviewprep.platform.entity.Company;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    @Query("select distinct c from Company c join c.supportedRoles r where lower(r) in :roles order by c.name asc")
    List<Company> findMatchingCompanies(@Param("roles") Collection<String> roles);
}
