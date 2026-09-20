package com.bookstore.dto;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Offset-paginated book catalogue response")
public record BookPageResponse(
        @Schema(description = "Books in the requested page")
        List<BookResponse> content,
        @Schema(example = "0", description = "Zero-based offset used for this page")
        int offset,
        @Schema(example = "5", description = "Requested page size")
        int limit,
        @Schema(description = "Whether another page is available")
        boolean hasNext,
        @Schema(example = "9", description = "Total number of matching available books")
        long total
) {
}
