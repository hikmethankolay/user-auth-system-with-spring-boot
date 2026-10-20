package com.hikmethankolay.template.common;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Restricts the {@code sort} parameter of a paginated request to an explicit list of properties.
 *
 * <p>Spring Data accepts any property of the entity, including ones the API never returns. Sorting by a
 * hidden column such as a password hash reveals information about values the caller cannot see, so every
 * list endpoint should only allow sorting by fields that are already visible in its response.</p>
 */
public final class SortAllowList {

    /** Properties that may be sorted by. */
    private final Set<String> allowed;

    /** Allowed properties in declaration order, for the error message. */
    private final String description;

    /**
     * Creates an allow-list.
     *
     * @param properties entity property names that may be sorted by
     */
    public SortAllowList(List<String> properties) {
        this.allowed = Set.copyOf(properties);
        this.description = String.join(", ", properties);
    }

    /**
     * Checks every sort property of a request.
     *
     * @param pageable the requested page
     * @return the same page request, for chaining
     * @throws ApiException 400 naming the rejected property and the allowed ones
     */
    public Pageable check(Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            if (!allowed.contains(order.getProperty())) {
                throw ApiException.badRequest(ProblemType.INVALID_SORT,
                        "Cannot sort by '" + order.getProperty() + "'. Allowed properties: " + description + ".");
            }
        }
        return pageable;
    }
}
