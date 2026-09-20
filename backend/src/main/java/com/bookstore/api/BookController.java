package com.bookstore.api;

import com.bookstore.application.BookCatalogService;
import com.bookstore.dto.BookResponse;
import com.bookstore.infrastructure.ApiRoutes;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.bookstore.dto.BookPageResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping(ApiRoutes.BOOKS)
@Tag(name = "Books", description = "Public book catalogue APIs")
public class BookController {
    private final BookCatalogService catalog;
    public BookController(BookCatalogService catalog) { this.catalog = catalog; }
    @GetMapping
    @Operation(
            summary = "List available books",
            description = "Returns available books with optional case-insensitive title search and offset pagination.",
            parameters = {
                    @Parameter(name = "offset", description = "Zero-based number of records to skip",
                            example = "0", schema = @Schema(type = "integer", minimum = "0", defaultValue = "0")),
                    @Parameter(name = "limit", description = "Number of records to return; maximum 50",
                            example = "5", schema = @Schema(type = "integer", minimum = "1", maximum = "50", defaultValue = "5")),
                    @Parameter(name = "search", description = "Optional case-insensitive search text matched against book title",
                            example = "clean", schema = @Schema(type = "string", defaultValue = ""))
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated book results",
                    content = @Content(schema = @Schema(implementation = BookPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public BookPageResponse listBooks(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(defaultValue = "") String search) {
        return catalog.searchAvailableBooks(search, offset, limit);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a book by ID", description = "Returns one book by its UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book found",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid UUID"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public BookResponse getBook(@PathVariable UUID id) { return catalog.getBook(id); }
}
