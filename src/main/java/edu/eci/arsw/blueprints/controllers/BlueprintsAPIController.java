package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.dtos.ApiResponse;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints")  // Cambio de path: /blueprints → /api/v1/blueprints
@Tag(name = "Blueprints", description = "Blueprints management API")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) {
        this.services = services;
    }

    // GET /api/v1/blueprints
    // 200 OK: Consulta exitosa
    @Operation(
            summary = "Get all blueprints",
            description = "Returns a list of all available blueprints in the system"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Blueprints retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        Set<Blueprint> blueprints = services.getAllBlueprints();
        return ResponseEntity.ok(
                new ApiResponse<>(200, "Blueprints retrieved successfully", blueprints)
        );
    }

    // GET /api/v1/blueprints/{author}
    // 200 OK: Consulta exitosa
    // 404 Not Found: Autor no tiene blueprints
    @Operation(
            summary = "Get blueprints by author",
            description = "Returns all blueprints created by a specific author"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Blueprints retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Author not found or has no blueprints"
            )
    })
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponse<Set<Blueprint>>> byAuthor(
            @Parameter(description = "Author username", required = true, example = "john")
            @PathVariable String author) {
        try {
            Set<Blueprint> blueprints = services.getBlueprintsByAuthor(author);
            return ResponseEntity.ok(
                    new ApiResponse<>(200, "Blueprints retrieved successfully", blueprints)
            );
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }

    // GET /api/v1/blueprints/{author}/{bpname}
    // 200 OK: Consulta exitosa
    // 404 Not Found: Blueprint no existe
    @Operation(
            summary = "Get specific blueprint",
            description = "Returns a specific blueprint by author and blueprint name"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Blueprint retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Blueprint not found"
            )
    })
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResponse<Blueprint>> byAuthorAndName(
            @Parameter(description = "Author username", required = true, example = "john")
            @PathVariable String author,
            @Parameter(description = "Blueprint name", required = true, example = "house")
            @PathVariable String bpname) {
        try {
            Blueprint blueprint = services.getBlueprint(author, bpname);
            return ResponseEntity.ok(
                    new ApiResponse<>(200, "Blueprint retrieved successfully", blueprint)
            );
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }

    // POST /api/v1/blueprints
    // 201 Created: Blueprint creado exitosamente
    // 400 Bad Request: Datos inválidos o blueprint ya existe
    @Operation(
            summary = "Create new blueprint",
            description = "Creates a new blueprint with the provided data"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Blueprint created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or blueprint already exists"
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Blueprint>> add(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Blueprint data to create", required = true,
                    content = @Content(schema = @Schema(implementation = NewBlueprintRequest.class))
            )
            @Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(201, "Blueprint created successfully", bp));
        } catch (BlueprintPersistenceException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)  // Cambiado de FORBIDDEN a BAD_REQUEST
                    .body(new ApiResponse<>(400, e.getMessage(), null));
        }
    }

    // PUT /api/v1/blueprints/{author}/{bpname}/points
    // 202 Accepted: Punto agregado exitosamente
    // 404 Not Found: Blueprint no existe
    @Operation(
            summary = "Add point to blueprint",
            description = "Adds a new point to an existing blueprint"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "202",
                    description = "Point added successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Blueprint not found"
            )
    })
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<ApiResponse<Void>> addPoint(
            @Parameter(description = "Author username", required = true, example = "john")
            @PathVariable String author,
            @Parameter(description = "Blueprint name", required = true, example = "house")
            @PathVariable String bpname,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Point coordinates to add", required = true,
                    content = @Content(schema = @Schema(implementation = Point.class))
            )
            @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new ApiResponse<>(202, "Point added successfully", null));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid java.util.List<Point> points
    ) { }
}