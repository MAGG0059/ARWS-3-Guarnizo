package edu.eci.arsw.blueprints;


import edu.eci.arsw.blueprints.controllers.BlueprintsAPIController;
import edu.eci.arsw.blueprints.dtos.ApiResponse;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlueprintsAPIControllerTest {

    @Mock
    private BlueprintsServices services;

    @InjectMocks
    private BlueprintsAPIController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllBlueprints() {
        Set<Blueprint> mockBlueprints = new HashSet<>(Arrays.asList(
                new Blueprint("john", "house", Arrays.asList(new Point(0, 0))),
                new Blueprint("jane", "garden", Arrays.asList(new Point(1, 1)))
        ));
        when(services.getAllBlueprints()).thenReturn(mockBlueprints);

        ResponseEntity<ApiResponse<Set<Blueprint>>> response = controller.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().code());
        assertEquals("Blueprints retrieved successfully", response.getBody().message());
        assertEquals(2, response.getBody().data().size());
    }

    @Test
    void testGetBlueprintByAuthor() throws BlueprintNotFoundException {
        Set<Blueprint> mockBlueprints = new HashSet<>(Arrays.asList(
                new Blueprint("john", "house", Arrays.asList(new Point(0, 0))),
                new Blueprint("john", "garage", Arrays.asList(new Point(1, 1)))
        ));
        when(services.getBlueprintsByAuthor("john")).thenReturn(mockBlueprints);

        ResponseEntity<ApiResponse<Set<Blueprint>>> response = controller.byAuthor("john");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().code());
        assertEquals(2, response.getBody().data().size());
    }

    @Test
    void testGetBlueprintByAuthorNotFound() throws BlueprintNotFoundException {
        when(services.getBlueprintsByAuthor("ghost")).thenThrow(new BlueprintNotFoundException("Not found"));

        ResponseEntity<ApiResponse<Set<Blueprint>>> response = controller.byAuthor("ghost");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().code());
        assertNull(response.getBody().data());
    }

    @Test
    void testGetBlueprintByAuthorAndName() throws BlueprintNotFoundException {
        Blueprint mockBlueprint = new Blueprint("john", "house", Arrays.asList(new Point(0, 0), new Point(10, 10)));
        when(services.getBlueprint("john", "house")).thenReturn(mockBlueprint);

        ResponseEntity<ApiResponse<Blueprint>> response = controller.byAuthorAndName("john", "house");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().code());
        assertEquals("john", response.getBody().data().getAuthor());
        assertEquals("house", response.getBody().data().getName());
        assertEquals(2, response.getBody().data().getPoints().size());
    }

    @Test
    void testGetBlueprintNotFound() throws BlueprintNotFoundException {
        when(services.getBlueprint("john", "ghost")).thenThrow(new BlueprintNotFoundException("Not found"));

        ResponseEntity<ApiResponse<Blueprint>> response = controller.byAuthorAndName("john", "ghost");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().code());
        assertNull(response.getBody().data());
    }

    @Test
    void testCreateBlueprint() throws BlueprintPersistenceException {
        BlueprintsAPIController.NewBlueprintRequest request =
                new BlueprintsAPIController.NewBlueprintRequest(
                        "maria",
                        "casa",
                        Arrays.asList(new Point(0, 0), new Point(5, 5))
                );

        ResponseEntity<ApiResponse<Blueprint>> response = controller.add(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(201, response.getBody().code());
        assertEquals("Blueprint created successfully", response.getBody().message());
        assertEquals("maria", response.getBody().data().getAuthor());
        assertEquals("casa", response.getBody().data().getName());
    }

    @Test
    void testCreateBlueprintDuplicate() throws BlueprintPersistenceException {
        BlueprintsAPIController.NewBlueprintRequest request =
                new BlueprintsAPIController.NewBlueprintRequest(
                        "john",
                        "house",
                        Arrays.asList(new Point(0, 0))
                );

        doThrow(new BlueprintPersistenceException("Blueprint already exists"))
                .when(services).addNewBlueprint(any(Blueprint.class));

        ResponseEntity<ApiResponse<Blueprint>> response = controller.add(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().code());
        assertNull(response.getBody().data());
    }

    @Test
    void testAddPoint() throws BlueprintNotFoundException {
        Point point = new Point(15, 15);

        ResponseEntity<ApiResponse<Void>> response = controller.addPoint("john", "house", point);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals(202, response.getBody().code());
        assertEquals("Point added successfully", response.getBody().message());
        verify(services, times(1)).addPoint("john", "house", 15, 15);
    }

    @Test
    void testAddPointBlueprintNotFound() throws BlueprintNotFoundException {
        Point point = new Point(15, 15);
        doThrow(new BlueprintNotFoundException("Not found"))
                .when(services).addPoint("john", "ghost", 15, 15);

        ResponseEntity<ApiResponse<Void>> response = controller.addPoint("john", "ghost", point);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().code());
        assertNull(response.getBody().data());
    }
}