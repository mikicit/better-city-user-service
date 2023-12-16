package dev.mikita.userservice.util;

import java.util.List;

public record PagedResult<T>(List<T> items, int currentPage, long totalItems, int totalPages) { }
