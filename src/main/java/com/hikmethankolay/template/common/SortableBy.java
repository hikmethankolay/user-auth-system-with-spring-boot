package com.hikmethankolay.template.common;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Names the properties a list endpoint can be sorted by, for the OpenAPI document: {@code OpenApiConfig}
 * writes them into the description and pattern of the endpoint's {@code sort} parameter.
 *
 * <p>This only documents the rule; enforce it with a {@link SortAllowList} built from the same constant in
 * the service, e.g. {@code @SortableBy(UserService.SORTABLE_PROPERTIES)}.</p>
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface SortableBy {

    /**
     * Returns the sortable properties.
     *
     * @return the property names, separated by {@code ", "}
     */
    String value();
}
