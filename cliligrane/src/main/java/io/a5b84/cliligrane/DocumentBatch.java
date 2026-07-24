package io.a5b84.cliligrane;

import java.nio.file.Path;
import java.util.Collection;

public record DocumentBatch(Collection<Entry> entries, String watermarkText) {
    public record Entry(Path inputPath, Path outputPath) {}
}
