package dev.mikita.userservice.util;

import com.google.cloud.firestore.Query;
import lombok.Getter;

@Getter
public class Pageable {
    private final int page;
    private final int size;
    private final String sortBy;
    private final Query.Direction sortDirection;

    public Pageable() {
        this.page = 0;
        this.size = 20;
        this.sortBy = null;
        this.sortDirection = Query.Direction.DESCENDING;
    }

    public Pageable(int page, int size, String sortBy, Query.Direction sortDirection) {
        this.page = Math.max(page, 0);
        this.size = Math.max(size, 1);
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }

    public Pageable(int page, int size, String sortBy) {
        this(page, size, sortBy, Query.Direction.DESCENDING);
    }

    public Pageable(int page, int size, Query.Direction sortDirection) {
        this(page, size, null, sortDirection);
    }

    public Pageable(int page, int size) {
        this(page, size, null, Query.Direction.DESCENDING);
    }

    public int getOffset() {
        return Math.max(0, page) * size;
    }
}
