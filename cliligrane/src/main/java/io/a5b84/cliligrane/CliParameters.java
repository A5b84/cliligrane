package io.a5b84.cliligrane;

import java.nio.file.Path;
import java.util.Optional;

record CliParameters(Path inputPath, Path outputPath, String watermarkText) {
    public static CliParameters parseOrExit(String[] args) {
        Optional<CliParameters> result = tryParse(args);
        if (result.isPresent()) {
            return result.get();
        } else {
            System.err.println("Usage: cliligrane-cli.jar <input> <output> <cliligrane>");
            System.exit(1);
            return null;
        }
    }

    public static Optional<CliParameters> tryParse(String[] args) {
        if (args.length == 3) {
            return Optional.of(
                    new CliParameters(
                            Path.of(args[0]).toAbsolutePath(),
                            Path.of(args[1]).toAbsolutePath(),
                            args[2]));
        } else {
            return Optional.empty();
        }
    }
}
