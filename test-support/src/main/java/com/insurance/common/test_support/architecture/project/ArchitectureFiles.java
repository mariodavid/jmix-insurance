package com.insurance.common.test_support.architecture.project;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class ArchitectureFiles {

  private ArchitectureFiles() {}

  public static String read(Path path) {
    try {
      return Files.readString(path, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static List<Path> walk(Path root) {
    try (Stream<Path> paths = Files.walk(root)) {
      return paths.filter(Files::isRegularFile).toList();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static List<Path> walkIfExists(Path root) {
    if (!Files.exists(root)) {
      return List.of();
    }
    return walk(root);
  }

  public static List<Path> javaAndXmlFiles(Path root) {
    return walkIfExists(root).stream()
        .filter(path -> path.toString().endsWith(".java") || path.toString().endsWith(".xml"))
        .toList();
  }
}
