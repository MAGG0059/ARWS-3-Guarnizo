
package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Repository
@Primary
@Transactional
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    @Autowired
    private PostgresBlueprintRepository blueprintRepository;

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        try {
            Optional<Blueprint> existing = blueprintRepository.findByAuthorAndName(
                    bp.getAuthor(), bp.getName()
            );

            if (existing.isPresent()) {
                throw new BlueprintPersistenceException(
                        "Blueprint already exists: " + bp.getAuthor() + ":" + bp.getName()
                );
            }
            blueprintRepository.save(bp);

        } catch (Exception e) {
            if (e instanceof BlueprintPersistenceException) {
                throw e;
            }
            throw new BlueprintPersistenceException("Error saving blueprint: " + e.getMessage());
        }
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        return blueprintRepository.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException(
                        String.format("Blueprint not found: %s/%s", author, name)
                ));
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        Set<Blueprint> blueprints = new HashSet<>(blueprintRepository.findByAuthor(author));

        if (blueprints.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author: " + author);
        }

        return blueprints;
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        return new HashSet<>(blueprintRepository.findAll());
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        Blueprint bp = getBlueprint(author, name);
        bp.addPoint(new Point(x, y));
        blueprintRepository.save(bp);
    }
}