package edu.eci.arsw.blueprints;

import edu.eci.arsw.blueprints.filters.BlueprintsFilter;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistence;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlueprintsServicesTest {

    @Mock
    private BlueprintPersistence persistence;

    @Mock
    private BlueprintsFilter filter;

    @InjectMocks
    private BlueprintsServices services;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddNewBlueprint() throws BlueprintPersistenceException {
        Blueprint bp = new Blueprint("john", "house", Arrays.asList(new Point(0, 0), new Point(1, 1)));
        services.addNewBlueprint(bp);
        verify(persistence, times(1)).saveBlueprint(bp);
    }

    @Test
    void testGetAllBlueprints() {
        Set<Blueprint> mockBlueprints = new HashSet<>(Arrays.asList(
                new Blueprint("john", "house", Arrays.asList(new Point(0, 0))),
                new Blueprint("jane", "garden", Arrays.asList(new Point(1, 1)))
        ));

        when(persistence.getAllBlueprints()).thenReturn(mockBlueprints);
        when(filter.apply(any(Blueprint.class))).thenAnswer(i -> i.getArgument(0));

        Set<Blueprint> result = services.getAllBlueprints();

        assertEquals(2, result.size());
        verify(persistence, times(1)).getAllBlueprints();
        verify(filter, times(2)).apply(any(Blueprint.class));
    }

    @Test
    void testGetBlueprint() throws BlueprintNotFoundException {
        Blueprint mockBlueprint = new Blueprint("john", "house", Arrays.asList(new Point(0, 0), new Point(1, 1)));

        when(persistence.getBlueprint("john", "house")).thenReturn(mockBlueprint);
        when(filter.apply(mockBlueprint)).thenReturn(mockBlueprint);

        Blueprint result = services.getBlueprint("john", "house");

        assertNotNull(result);
        assertEquals("john", result.getAuthor());
        assertEquals("house", result.getName());
        verify(persistence, times(1)).getBlueprint("john", "house");
        verify(filter, times(1)).apply(mockBlueprint);
    }

    @Test
    void testGetBlueprintNotFound() throws BlueprintNotFoundException {
        when(persistence.getBlueprint("john", "ghost")).thenThrow(new BlueprintNotFoundException("Not found"));
        assertThrows(BlueprintNotFoundException.class, () -> {
            services.getBlueprint("john", "ghost");
        });
    }

    @Test
    void testGetBlueprintsByAuthor() throws BlueprintNotFoundException {
        Set<Blueprint> mockBlueprints = new HashSet<>(Arrays.asList(
                new Blueprint("john", "house", Arrays.asList(new Point(0, 0))),
                new Blueprint("john", "garage", Arrays.asList(new Point(1, 1)))
        ));

        when(persistence.getBlueprintsByAuthor("john")).thenReturn(mockBlueprints);
        when(filter.apply(any(Blueprint.class))).thenAnswer(i -> i.getArgument(0));

        Set<Blueprint> result = services.getBlueprintsByAuthor("john");

        assertEquals(2, result.size());
        verify(persistence, times(1)).getBlueprintsByAuthor("john");
    }

    @Test
    void testAddPoint() throws BlueprintNotFoundException {
        services.addPoint("john", "house", 10, 20);
        verify(persistence, times(1)).addPoint("john", "house", 10, 20);
    }
}