package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Blueprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostgresBlueprintRepository extends JpaRepository<Blueprint, Long> {

    List<Blueprint> findByAuthor(String author);

    Optional<Blueprint> findByAuthorAndName(String author, String name);
}