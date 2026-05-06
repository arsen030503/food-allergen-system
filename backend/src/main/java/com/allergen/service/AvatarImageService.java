package com.allergen.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Locale;

/**
 * Decodes client avatar (data URL or raw base64), resizes, and outputs JPEG bytes (full + thumbnail).
 */
@Service
public class AvatarImageService {

    private static final int MAX_DECODED_BYTES = 5 * 1024 * 1024;
    private static final int MAX_INPUT_STRING_CHARS = 8_000_000;
    private static final int FULL_MAX_EDGE = 1024;
    private static final int THUMB_EDGE = 96;
    private static final float FULL_JPEG_QUALITY = 0.82f;
    private static final float THUMB_JPEG_QUALITY = 0.78f;

    public record ProcessedAvatar(byte[] fullJpeg, byte[] thumbJpeg) {}

    public ProcessedAvatar processFromClientPayload(String avatarPayload) {
        if (avatarPayload == null || avatarPayload.isBlank()) {
            return new ProcessedAvatar(null, null);
        }
        if (avatarPayload.length() > MAX_INPUT_STRING_CHARS) {
            throw new IllegalArgumentException("error.avatar.payloadTooLarge");
        }
        byte[] raw = decodeBase64Payload(avatarPayload.trim());
        if (raw.length > MAX_DECODED_BYTES) {
            throw new IllegalArgumentException("error.avatar.imageTooLarge");
        }
        BufferedImage src;
        try {
            src = ImageIO.read(new ByteArrayInputStream(raw));
        } catch (IOException e) {
            throw new IllegalArgumentException("error.avatar.invalidImageData");
        }
        if (src == null) {
            throw new IllegalArgumentException("error.avatar.unsupportedFormat");
        }
        BufferedImage rgb = toRgb(src);
        byte[] full = encodeJpeg(scaleMax(rgb, FULL_MAX_EDGE), FULL_JPEG_QUALITY);
        byte[] thumb = encodeJpeg(scaleCover(rgb, THUMB_EDGE), THUMB_JPEG_QUALITY);
        return new ProcessedAvatar(full, thumb);
    }

    private static byte[] decodeBase64Payload(String payload) {
        String p = payload;
        if (p.toLowerCase(Locale.ROOT).startsWith("data:")) {
            int comma = p.indexOf(',');
            if (comma < 0) {
                throw new IllegalArgumentException("error.avatar.invalidDataUrl");
            }
            p = p.substring(comma + 1);
        }
        try {
            return Base64.getDecoder().decode(p);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("error.avatar.invalidBase64");
        }
    }

    private static BufferedImage toRgb(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_RGB) {
            return src;
        }
        BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            g.drawImage(src, 0, 0, null);
        } finally {
            g.dispose();
        }
        return rgb;
    }

    private static BufferedImage scaleMax(BufferedImage src, int maxEdge) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (w <= maxEdge && h <= maxEdge) {
            return src;
        }
        double scale = Math.min((double) maxEdge / w, (double) maxEdge / h);
        int nw = Math.max(1, (int) Math.round(w * scale));
        int nh = Math.max(1, (int) Math.round(h * scale));
        return scale(src, nw, nh);
    }

    /** Center-crop to square then scale to edge x edge. */
    private static BufferedImage scaleCover(BufferedImage src, int edge) {
        int w = src.getWidth();
        int h = src.getHeight();
        int side = Math.min(w, h);
        int x = (w - side) / 2;
        int y = (h - side) / 2;
        BufferedImage cropped = src.getSubimage(x, y, side, side);
        return scale(cropped, edge, edge);
    }

    private static BufferedImage scale(BufferedImage src, int targetW, int targetH) {
        BufferedImage dst = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src, 0, 0, targetW, targetH, null);
        } finally {
            g.dispose();
        }
        return dst;
    }

    private static byte[] encodeJpeg(BufferedImage img, float quality) {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No JPEG writer available");
        }
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(img, null, null), param);
            writer.dispose();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return baos.toByteArray();
    }
}
