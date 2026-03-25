package com.gridpadel.infrastructure.publish;

import com.gridpadel.domain.model.Tournament;
import com.gridpadel.domain.port.BracketPublishPort;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GitHubPagesPublisher implements BracketPublishPort {

    private final HtmlBracketGenerator htmlGenerator = new HtmlBracketGenerator();

    @Override
    public String publish(List<Tournament> tournaments, String repoUrl) {
        try {
            Path tempDir = Files.createTempDirectory("gh-pages-");
            try {
                generateHtmlFiles(tournaments, tempDir);
                pushToGhPages(tempDir, repoUrl);
                return buildPublicUrl(repoUrl);
            } finally {
                deleteRecursively(tempDir);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al publicar en GitHub Pages: " + e.getMessage(), e);
        }
    }

    private void generateHtmlFiles(List<Tournament> tournaments, Path outputDir) throws IOException {
        String indexHtml = htmlGenerator.generateIndexPage(tournaments);
        Files.writeString(outputDir.resolve("index.html"), indexHtml);

        // Group by tournament name
        java.util.LinkedHashMap<String, java.util.List<Tournament>> byName = new java.util.LinkedHashMap<>();
        for (Tournament t : tournaments) {
            byName.computeIfAbsent(t.name(), k -> new java.util.ArrayList<>()).add(t);
        }

        // Generate per-tournament landing pages
        for (var entry : byName.entrySet()) {
            String tournamentSlug = HtmlBracketGenerator.slugify(entry.getKey());
            String landingHtml = htmlGenerator.generateTournamentLandingPage(entry.getKey(), entry.getValue());
            Files.writeString(outputDir.resolve(tournamentSlug + ".html"), landingHtml);
        }

        // Generate per-category bracket pages
        for (Tournament t : tournaments) {
            if (t.mainBracket().rounds().isEmpty()) continue;
            String slug = HtmlBracketGenerator.slugify(t.name() + "-" + t.category());
            String html = htmlGenerator.generateTournamentPage(t);
            Files.writeString(outputDir.resolve(slug + ".html"), html);
        }

        // .nojekyll to disable Jekyll processing on GitHub Pages
        Files.writeString(outputDir.resolve(".nojekyll"), "");
    }

    private void pushToGhPages(Path contentDir, String repoUrl) throws IOException, InterruptedException {
        Path workDir = Files.createTempDirectory("gh-pages-repo-");
        try {
            // Initialize a fresh repo
            run(workDir, "git", "init");
            run(workDir, "git", "checkout", "--orphan", "gh-pages");

            // Copy generated files
            for (var file : Files.list(contentDir).toList()) {
                Files.copy(file, workDir.resolve(file.getFileName()));
            }

            // Configure git for the commit
            run(workDir, "git", "add", "-A");
            run(workDir, "git", "-c", "user.name=GridPadel", "-c", "user.email=gridpadel@publish",
                    "commit", "-m", "Actualizar cuadros de torneo");

            // Force push to gh-pages branch
            run(workDir, "git", "remote", "add", "origin", repoUrl);
            run(workDir, "git", "push", "--force", "origin", "gh-pages");
        } finally {
            deleteRecursively(workDir);
        }
    }

    private void run(Path workDir, String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command)
                .directory(workDir.toFile())
                .redirectErrorStream(true);
        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Command failed (" + String.join(" ", command) + "): " + output);
        }
    }

    String buildPublicUrl(String repoUrl) {
        // Extract owner/repo from various URL formats
        // https://github.com/Owner/Repo.git or git@github.com:Owner/Repo.git
        Pattern httpsPattern = Pattern.compile("github\\.com[/:]([^/]+)/([^/.]+)");
        Matcher matcher = httpsPattern.matcher(repoUrl);
        if (matcher.find()) {
            String owner = matcher.group(1).toLowerCase();
            String repo = matcher.group(2).toLowerCase();
            return "https://" + owner + ".github.io/" + repo + "/";
        }
        return repoUrl;
    }

    private void deleteRecursively(Path path) {
        try {
            if (Files.isDirectory(path)) {
                try (var entries = Files.walk(path)) {
                    entries.sorted(java.util.Comparator.reverseOrder())
                            .forEach(p -> {
                                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                            });
                }
            } else {
                Files.deleteIfExists(path);
            }
        } catch (IOException ignored) {}
    }
}
