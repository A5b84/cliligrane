package io.a5b84.cliligrane;

import io.a5b84.cliligrane.common.config.ImageIOInitializer;

import lombok.RequiredArgsConstructor;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Command(
        name = "cliligrane",
        description = "Adds a watermark to documents.",
        footer = {
            "",
            "Example:",
            "  cliligrane a.pdf b.pdf --output a-out.pdf b-out.pdf --text Watermark"
        })
@RequiredArgsConstructor
public class CliligraneCommand implements Callable<Integer> {
    
    private final CliWatermarkService watermarkService;

    @Spec private CommandSpec spec;

    @Parameters(arity = "1..*", paramLabel = "<input>", description = "Input files.")
    private List<Path> inputs;

    @Option(
            names = {"-o", "--output"},
            required = true,
            arity = "1..*",
            paramLabel = "<output>",
            description = "Output files. Must contain as many paths as there are input files.")
    private List<Path> outputs;

    @Option(
            names = {"-t", "--text"},
            required = true,
            paramLabel = "<text>",
            description = "Watermark text to apply.")
    private String watermarkText;

    @Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "Display this help message.")
    private boolean usageHelpRequested;

    @Override
    public Integer call() {
        DocumentBatch batch = createAndValidateBatch();
        ImageIOInitializer.initialize();
        boolean allSuccessful = watermarkService.processAndSaveBatch(batch);
        return allSuccessful ? 0 : 1;
    }

    private DocumentBatch createAndValidateBatch() {
        if (inputs.size() != outputs.size()) {
            throw new ParameterException(
                    spec.commandLine(),
                    "Expected the same number of input and output paths but got "
                            + inputs.size()
                            + " and "
                            + outputs.size()
                            + " respectively.");
        }

        List<DocumentBatch.Entry> entries = new ArrayList<>(inputs.size());

        for (int i = 0; i < inputs.size(); i++) {
            Path inputPath = inputs.get(i).toAbsolutePath().normalize();
            Path outputPath = outputs.get(i).toAbsolutePath().normalize();
            entries.add(new DocumentBatch.Entry(inputPath, outputPath));
        }

        DocumentBatch batch = new DocumentBatch(entries, watermarkText);
        checkInputsAndOutputsAreDisjoint(batch);
        return batch;
    }

    private void checkInputsAndOutputsAreDisjoint(DocumentBatch batch) {
        Set<Path> inputPaths =
                batch.entries().stream()
                        .map(DocumentBatch.Entry::inputPath)
                        .collect(Collectors.toUnmodifiableSet());
        List<Path> commonPaths =
                batch.entries().stream()
                        .map(DocumentBatch.Entry::outputPath)
                        .filter(inputPaths::contains)
                        .sorted()
                        .toList();

        if (!commonPaths.isEmpty()) {
            StringBuilder message =
                    new StringBuilder(
                            "The paths below are present both in inputs and outputs."
                                    + " Aborting to avoid overwriting original files.");

            for (Path path : commonPaths) {
                message.append('\n');
                message.append(path);
            }

            throw new ParameterException(spec.commandLine(), message.toString());
        }
    }
}
