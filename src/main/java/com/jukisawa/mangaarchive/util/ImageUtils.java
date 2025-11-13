package com.jukisawa.mangaarchive.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageUtils {

    public static Image resizeImage(Image image) {
        if (image == null) return null;

        int targetWidth = 420;
        int targetHeight = 560;
        double ratio = Math.min(targetWidth / image.getWidth(), targetHeight / image.getHeight());
        int newWidth = (int) (image.getWidth() * ratio);
        int newHeight = (int) (image.getHeight() * ratio);

        // Create a canvas of target size
        WritableImage finalImage = new WritableImage(targetWidth, targetHeight);
        javafx.scene.canvas.Canvas canvas = new Canvas(targetWidth, targetHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Fill background with transparent color (optional: use Color.WHITE)
        gc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        gc.fillRect(0, 0, targetWidth, targetHeight);

        // Draw the resized image centered
        double x = (targetWidth - newWidth) / 2.0;
        double y = (targetHeight - newHeight) / 2.0;
        gc.drawImage(image, x, y, newWidth, newHeight);

        // Snapshot the canvas to get the final WritableImage
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(javafx.scene.paint.Color.TRANSPARENT);
        canvas.snapshot(params, finalImage);

        return finalImage;
    }

    public static byte[] imageToBytes(Image image) throws IOException {
        if (image == null) return null;

        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bImage, "png", baos);
            return baos.toByteArray();
        }
    }

    public static Image bytesToImage(byte[] bytes) {
        if (bytes == null) return null;
        return new Image(new ByteArrayInputStream(bytes));
    }
}
