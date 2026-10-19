package com.hikmethankolay.template.user;

import com.hikmethankolay.template.security.TokenService;
import com.hikmethankolay.template.user.dto.AdminUpdateUserRequest;
import com.hikmethankolay.template.user.dto.ChangePasswordRequest;
import com.hikmethankolay.template.user.dto.UpdateProfileRequest;
import com.hikmethankolay.template.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * The {@code /api/v1/users} resource.
 *
 * <table>
 *   <caption>Endpoints</caption>
 *   <tr><th>Method</th><th>Path</th><th>Access</th><th>Success</th></tr>
 *   <tr><td>GET</td><td>/me</td><td>authenticated</td><td>200</td></tr>
 *   <tr><td>PATCH</td><td>/me</td><td>authenticated</td><td>200</td></tr>
 *   <tr><td>PUT</td><td>/me/password</td><td>authenticated</td><td>204</td></tr>
 *   <tr><td>GET</td><td>/</td><td>ADMIN</td><td>200 (paginated)</td></tr>
 *   <tr><td>GET</td><td>/{id}</td><td>ADMIN or the user</td><td>200</td></tr>
 *   <tr><td>PATCH</td><td>/{id}</td><td>ADMIN</td><td>200</td></tr>
 *   <tr><td>DELETE</td><td>/{id}</td><td>ADMIN</td><td>204</td></tr>
 * </table>
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
public class UserController {

    /** User business logic. */
    private final UserService userService;

    /**
     * Creates the controller.
     *
     * @param userService user business logic
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Returns the authenticated user.
     *
     * @param jwt the caller's token
     * @return the current user
     */
    @GetMapping("/me")
    @Operation(summary = "Get the current user")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return userService.getById(TokenService.userId(jwt));
    }

    /**
     * Partially updates the authenticated user's username and/or email.
     *
     * @param jwt     the caller's token
     * @param request fields to change
     * @return the updated user
     */
    @PatchMapping("/me")
    @Operation(summary = "Update the current user's profile")
    public UserResponse updateMe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(TokenService.userId(jwt), request);
    }

    /**
     * Replaces the authenticated user's password.
     *
     * @param jwt     the caller's token
     * @param request current and new password
     */
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Change the current user's password")
    public void changeMyPassword(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(TokenService.userId(jwt), request);
    }

    /**
     * Lists users. Supports the standard {@code page}, {@code size} and {@code sort} query parameters,
     * e.g. {@code ?page=0&size=20&sort=createdAt,desc}.
     *
     * @param pageable requested page
     * @return one page of users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List users (admin)")
    public Page<UserResponse> list(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return userService.list(pageable);
    }

    /**
     * Returns a user by id. Administrators can read any user, other users only themselves.
     *
     * @param id user id
     * @return the user
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
    @Operation(summary = "Get a user (admin or self)")
    public UserResponse get(@PathVariable Long id) {
        return userService.getById(id);
    }

    /**
     * Partially updates any field of a user.
     *
     * @param id      user id
     * @param request fields to change
     * @return the updated user
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a user (admin)")
    public UserResponse update(@PathVariable long id, @Valid @RequestBody AdminUpdateUserRequest request) {
        return userService.update(id, request);
    }

    /**
     * Deletes a user. Administrators cannot delete themselves.
     *
     * @param id  user id
     * @param jwt the caller's token
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a user (admin)")
    public void delete(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        userService.delete(id, TokenService.userId(jwt));
    }
}
