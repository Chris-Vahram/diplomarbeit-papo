package at.papo.partnerportal.product;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

// Tiny solid-color PNG for DemoDataSeeder and ProductImageResourceTest, so the repo ships
// no binary asset. Plain JDK ImageIO; works headless in the ubi8/openjdk-21 image.
public final class DemoPngGenerator {

    private DemoPngGenerator() {
    }

    public static byte[] solidColorPng(int width, int height, int r, int g, int b) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int rgb = new Color(r, g, b).getRGB();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, rgb);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }
}
