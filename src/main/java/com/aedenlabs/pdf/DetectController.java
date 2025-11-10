package com.aedenlabs.pdf;

import com.aedenlabs.pdf.service.PdfToImageService;
import com.aedenlabs.pdf.service.YoloPythonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/detect")
@CrossOrigin("*")
public class DetectController {

  private final PdfToImageService pdfService;
  private final YoloPythonService yoloService;

  public DetectController(PdfToImageService pdfService, YoloPythonService yoloService) {
    this.pdfService = pdfService;
    this.yoloService = yoloService;
  }

  @PostMapping
  public ResponseEntity<?> detect(@RequestParam("file") MultipartFile file) {
    File tempPdf = null;
    try {
      // 1️⃣ Save the uploaded PDF
      tempPdf = Files.createTempFile("upload_", ".pdf").toFile();
      file.transferTo(tempPdf);

      // 2️⃣ Convert to images
      List<String> images = pdfService.convertPdfToImages(tempPdf);

      // 3️⃣ Run YOLO
      Map<String, List<String>> detections = yoloService.runInference(images);

      // 4️⃣ Clean up
      tempPdf.delete();

      return ResponseEntity.ok(Map.of("status", "success", "detections", detections));
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    } finally {
      if (tempPdf != null) tempPdf.delete();
    }
  }
}
