package fr.dossierfacile.api.pdfgenerator.service.templates;

import com.jhlabs.image.TransformFilter;

import java.awt.image.BufferedImage;

/**
 * A filter which simulates a lens placed over an image.
 */
public class DFFilter extends TransformFilter {

    private static final int X_FREQUENCY = 12;
    private static final int Y_FREQUENCY = 8;
    private static final int MAX_DISTORSION = 28;

    private float width;
    private float height;

    public DFFilter() {
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        this.width = src.getWidth();
        this.height = src.getHeight();
        return super.filter(src, dst);
    }

    protected void transformInverse(int x, int y, float[] out) {
        out[0] = x;
        float r = (float) Math.sin(x * X_FREQUENCY / width);
        out[1] = y + MAX_DISTORSION * (float) Math.sin(y * Y_FREQUENCY / height) * r * r;
    }
}
