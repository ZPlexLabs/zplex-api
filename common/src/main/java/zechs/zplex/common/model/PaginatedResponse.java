package zechs.zplex.common.model;

import java.util.List;

public record PaginatedResponse<T>(
        List<T> data,
        int pageNumber,
        int pageCount
) {}
