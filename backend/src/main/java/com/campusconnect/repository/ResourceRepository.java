package com.campusconnect.repository;

import com.campusconnect.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    // category and subject are both optional - a null value means "don't filter on it"
    @Query("SELECT r FROM Resource r WHERE "
            + "(:category IS NULL OR LOWER(r.category) = LOWER(:category)) AND "
            + "(:subject IS NULL OR LOWER(r.subject) = LOWER(:subject)) "
            + "ORDER BY r.createdAt DESC")
    List<Resource> search(@Param("category") String category, @Param("subject") String subject);

    // distinct values power the self-extending dropdowns in the admin form
    @Query("SELECT DISTINCT r.category FROM Resource r WHERE r.category IS NOT NULL AND r.category <> '' ORDER BY r.category")
    List<String> findDistinctCategories();

    @Query("SELECT DISTINCT r.subject FROM Resource r WHERE r.subject IS NOT NULL AND r.subject <> '' ORDER BY r.subject")
    List<String> findDistinctSubjects();

    @Query("SELECT DISTINCT r.type FROM Resource r WHERE r.type IS NOT NULL AND r.type <> '' ORDER BY r.type")
    List<String> findDistinctTypes();
}
