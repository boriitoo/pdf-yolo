package com.aedenlabs.pdf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class YoloPythonService {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public Map<String, List<String>> runInference(List<String> imagePaths) throws Exception {
    ProcessBuilder pb = new ProcessBuilder("python3", "scripts/detect_crops.py");
    pb.redirectErrorStream(true);
    Process process = pb.start();

    // Send image paths to Python
    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
      Map<String, Object> input = Map.of("images", imagePaths);
      writer.write(objectMapper.writeValueAsString(input));
      writer.flush();
    }

    // Capture Python output
    StringBuilder resultJson = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        resultJson.append(line);
      }
    }

    int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new RuntimeException("Python process failed with code " + exitCode);
    }

    // Parse and return result
    return objectMapper.readValue(resultJson.toString(), Map.class);
  }
}
