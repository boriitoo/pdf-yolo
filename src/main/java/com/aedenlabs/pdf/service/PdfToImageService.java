package com.aedenlabs.pdf.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class PdfToImageService {

  public List<String> convertPdfToImages(File pdfFile) throws IOException {
    List<String> imagePaths = new ArrayList<>();
    Path tempDir = Files.createTempDirectory("yolo_images_");

    try (PDDocument document = PDDocument.load(pdfFile)) {
      PDFRenderer renderer = new PDFRenderer(document);
      for (int i = 0; i < document.getNumberOfPages(); i++) {
        BufferedImage image = renderer.renderImageWithDPI(i, 200);
        File imageFile = tempDir.resolve("page_" + (i + 1) + ".png").toFile();
        ImageIO.write(image, "png", imageFile);
        imagePaths.add(imageFile.getAbsolutePath());
      }
    }

    return imagePaths;
  }
}
