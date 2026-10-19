package com.hikmethankolay.template.user;

import com.hikmethankolay.template.audit.AuditEvent;
import com.hikmethankolay.template.audit.AuditLog;
import com.hikmethankolay.template.user.dto.PersonalDataExport;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Builds the personal data export from the user's profile and every {@link PersonalDataProvider}.
 */
@Service
public class PersonalDataService {

    /** Profile lookups. */
    private final UserService userService;

    /** Feature-specific data, in bean order. */
    private final List<PersonalDataProvider> providers;

    /** Records the export. */
    private final AuditLog audit;

    /**
     * Creates the service.
     *
     * @param userService profile lookups
     * @param providers   every registered data provider
     * @param audit       records the export
     */
    public PersonalDataService(UserService userService, List<PersonalDataProvider> providers, AuditLog audit) {
        this.userService = userService;
        this.providers = providers;
        this.audit = audit;
    }

    /**
     * Exports everything stored about a user.
     *
     * @param userId the user
     * @return the export
     * @throws com.hikmethankolay.template.common.ApiException 404 if the user does not exist
     */
    @Transactional(readOnly = true)
    public PersonalDataExport export(long userId) {
        Map<String, Object> related = new LinkedHashMap<>();
        providers.forEach(provider -> related.put(provider.section(), provider.export(userId)));
        PersonalDataExport export = new PersonalDataExport(Instant.now(), userService.getById(userId), related);
        audit.record(AuditEvent.DATA_EXPORTED, userId);
        return export;
    }
}
