package com.hikmethankolay.template.mfa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Recovery codes, shown once: each replaces one authenticator code if the device is lost.
 *
 * @param recoveryCodes ten single-use codes such as {@code k3d9x-p2m7q}
 */
@Schema(requiredProperties = {"recoveryCodes"})
public record RecoveryCodesResponse(List<String> recoveryCodes) {}
