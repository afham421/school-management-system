package com.example.school.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    public ApiResponse(boolean success, String message, T data, PaginationMetadata pagination) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.pagination = pagination;
    }

    private boolean success;
    private String message;
    private T data;
    private PaginationMetadata pagination;
    private LocalDateTime timestamp = LocalDateTime.now();

    // Success response with data
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation successful");
    }

    // Success response with data and message
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<T>(true, message, data, (PaginationMetadata) null);
    }

    // Success response for paginated data (returns List<T>)
    public static <T> ApiResponse<List<T>> success(Page<T> page) {
        PaginationMetadata pagination = new PaginationMetadata(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()
        );
        return new ApiResponse<>(true, "Data retrieved successfully", page.getContent(), pagination);
    }
    
    // Success response for Page<T> that maintains the Page object
    public static <T> ApiResponse<Page<T>> successPage(Page<T> page) {
        PaginationMetadata pagination = new PaginationMetadata(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()
        );
        return new ApiResponse<>(true, "Data retrieved successfully", page, pagination);
    }

    // Error response
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null);
    }

    // Error response with data
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data, null);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationMetadata {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }
}
