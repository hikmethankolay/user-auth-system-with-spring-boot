package com.hikmethankolay.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Keeps {@code deploy/kubernetes} deploying the image this repository releases: the image the Deployment runs must
 * be the one Kustomize pins, and the pinned tag must be the latest release in {@code CHANGELOG.md}, so a release
 * cannot ship while the manifests still deploy an older one.
 */
class KubernetesManifestTest {

    private static final Path KUSTOMIZATION = Path.of("deploy", "kubernetes", "kustomization.yaml");
    private static final Path DEPLOYMENT = Path.of("deploy", "kubernetes", "deployment.yaml");
    private static final Path CHANGELOG = Path.of("CHANGELOG.md");

    @Test
    void kustomizePinsTheImageTheDeploymentRuns() throws IOException {
        String deployed = find(DEPLOYMENT, "image: (\\S+)");

        assertThat(find(KUSTOMIZATION, "- name: (\\S+)")).isEqualTo(deployed);
    }

    @Test
    void pinnedTagIsTheLatestRelease() throws IOException {
        Matcher release = Pattern.compile("^## \\[(\\d+\\.\\d+\\.\\d+)]", Pattern.MULTILINE)
                .matcher(Files.readString(CHANGELOG));
        // A project made from the template starts with an empty changelog: nothing is released yet.
        assumeTrue(release.find(), "no release in CHANGELOG.md");

        assertThat(find(KUSTOMIZATION, "newTag: \"?([^\"\\s]+)"))
                .as("newTag in %s; bump it with every release", KUSTOMIZATION)
                .isEqualTo(release.group(1));
    }

    private static String find(Path file, String regex) throws IOException {
        Matcher matcher = Pattern.compile(regex).matcher(Files.readString(file));
        assertThat(matcher.find()).as("%s in %s", regex, file).isTrue();
        return matcher.group(1);
    }
}
