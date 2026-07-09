package at.papo.partnerportal.product;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

// Builds a tiny solid-color PNG in pure JDK (no java.awt/ImageIO - avoids any headless
// rendering risk in a container). Used by DemoDataSeeder to seed a real, validator-passing
// raster ProductImage without shipping a binary asset, and by ProductImageResourceTest to
// build valid upload fixtures. Truecolor, 8-bit, no filter/interlace.
public final class DemoPngGenerator {

    private static final byte[] SIGNATURE = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A };

    private DemoPngGenerator() {
    }

    public static byte[] solidColorPng(int width, int height, int r, int g, int b) {
        byte[] raw = new byte[height * (1 + width * 3)];
        int pos = 0;
        for (int y = 0; y < height; y++) {
            raw[pos++] = 0; // filter type: none
            for (int x = 0; x < width; x++) {
                raw[pos++] = (byte) r;
                raw[pos++] = (byte) g;
                raw[pos++] = (byte) b;
            }
        }

        ByteArrayOutputStream png = new ByteArrayOutputStream();
        png.write(SIGNATURE, 0, SIGNATURE.length);
        writeChunk(png, "IHDR", ihdrData(width, height));
        writeChunk(png, "IDAT", deflate(raw));
        writeChunk(png, "IEND", new byte[0]);
        return png.toByteArray();
    }

    private static byte[] ihdrData(int width, int height) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeInt(out, width);
        writeInt(out, height);
        out.write(8); // bit depth
        out.write(2); // color type: truecolor (RGB)
        out.write(0); // compression method
        out.write(0); // filter method
        out.write(0); // interlace method
        return out.toByteArray();
    }

    private static byte[] deflate(byte[] raw) {
        Deflater deflater = new Deflater();
        deflater.setInput(raw);
        deflater.finish();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int n = deflater.deflate(buffer);
            out.write(buffer, 0, n);
        }
        deflater.end();
        return out.toByteArray();
    }

    private static void writeChunk(ByteArrayOutputStream out, String type, byte[] data) {
        writeInt(out, data.length);
        byte[] typeBytes = type.getBytes(StandardCharsets.US_ASCII);
        out.write(typeBytes, 0, typeBytes.length);
        out.write(data, 0, data.length);
        CRC32 crc = new CRC32();
        crc.update(typeBytes);
        crc.update(data);
        writeInt(out, (int) crc.getValue());
    }

    private static void writeInt(ByteArrayOutputStream out, int value) {
        out.write((value >>> 24) & 0xFF);
        out.write((value >>> 16) & 0xFF);
        out.write((value >>> 8) & 0xFF);
        out.write(value & 0xFF);
    }
}
