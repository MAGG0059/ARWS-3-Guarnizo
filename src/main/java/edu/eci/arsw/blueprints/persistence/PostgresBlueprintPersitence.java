package edu.eci.arsw.blueprints.persistence;


import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.entity.BlueprintEntity;
import edu.eci.arsw.blueprints.persistence.entity.PointEntity;
import edu.eci.arsw.blueprints.persistence.repository.BlueprintJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Primary
@Transactional
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    @Autowired
    private BlueprintJpaRepository blueprintRepository;

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        try {
            // Verificar si ya existe
            Optional<BlueprintEntity> existing = blueprintRepository
                    .findByAuthorAndName(bp.getAuthor(), bp.getName());

            if (existing.isPresent()) {
                throw new BlueprintPersistenceException(
                        "Blueprint already exists: " + bp.getAuthor() + ":" + bp.getName()
                );
            }

            // Crear entidad directamente desde el modelo
            BlueprintEntity entity = new BlueprintEntity(bp);
            blueprintRepository.save(entity);

        } catch (Exception e) {
            if (e instanceof BlueprintPersistenceException) {
                throw e;
            }
            throw new BlueprintPersistenceException("Error saving blueprint: " + e.getMessage());
        }
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        Optional<BlueprintEntity> optional = blueprintRepository.findByAuthorAndName(author, name);

        if (optional.isEmpty()) {
            throw new BlueprintNotFoundException(
                    String.format("Blueprint not found: %s/%s", author, name)
            );
        }

        // La entidad se convierte a modelo directamente
        return optional.get().toBlueprint();
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        List<BlueprintEntity> entities = blueprintRepository.findByAuthor(author);

        if (entities.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author: " + author);
        }

        return entities.stream()
                .map(BlueprintEntity::toBlueprint)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        List<BlueprintEntity> entities = blueprintRepository.findAll();

        return entities.stream()
                .map(BlueprintEntity::toBlueprint)
                .collect(Collectors.toSet());
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        Optional<BlueprintEntity> optional = blueprintRepository.findByAuthorAndName(author, name);

        if (optional.isEmpty()) {
            throw new BlueprintNotFoundException(
                    String.format("Blueprint not found: %s/%s", author, name)
            );
        }

        BlueprintEntity entity = optional.get();

        // Crear y agregar el nuevo punto
        PointEntity newPoint = new PointEntity(x, y);
        entity.addPoint(newPoint);

        // Guardar (el cascade se encarga del punto)
        blueprintRepository.save(entity);
    }

    @Override
    public void updateBlueprint(Blueprint bp) throws BlueprintNotFoundException {
        Optional<BlueprintEntity> optional = blueprintRepository.findByAuthorAndName(
                bp.getAuthor(), bp.getName()
        );

        if (optional.isEmpty()) {
            throw new BlueprintNotFoundException(
                    String.format("Blueprint not found: %s/%s", bp.getAuthor(), bp.getName())
            );
        }

        BlueprintEntity entity = optional.get();
        entity.updateFromBlueprint(bp);
        blueprintRepository.save(entity);
    }

    @Override
    public void deleteBlueprint(String author, String name) throws BlueprintNotFoundException {
        Optional<BlueprintEntity> optional = blueprintRepository.findByAuthorAndName(author, name);

        if (optional.isEmpty()) {
            throw new BlueprintNotFoundException(
                    String.format("Blueprint not found: %s/%s", author, name)
            );
        }

        blueprintRepository.delete(optional.get());
    }
}