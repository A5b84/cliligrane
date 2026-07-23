package fr.dossierfacile.api.pdfgenerator.configuration;

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
