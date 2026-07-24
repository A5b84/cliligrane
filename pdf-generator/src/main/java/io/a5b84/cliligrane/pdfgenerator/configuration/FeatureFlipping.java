package io.a5b84.cliligrane.pdfgenerator.configuration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FeatureFlipping {
    private final boolean useColors;
    private final boolean useDistortion;

    public boolean shouldUseColors() {
        return useColors;
    }

    public boolean shouldUseDistortion() {
        return useDistortion;
    }
}
