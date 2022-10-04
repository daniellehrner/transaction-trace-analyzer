package net.consensys.traceanalyzer;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TraceAnalyzer {
  private final Logger logger;

  public TraceAnalyzer(final Logger logger) {
    this.logger = logger;
  }

  public void analyzeDirectory(final String dir) throws RuntimeException {
    try (DirectoryStream<Path> resultsDir = Files.newDirectoryStream(Paths.get(dir))) {
      for (Path result : resultsDir) {
        if (Files.isDirectory(result) && !result.toFile().getName().endsWith(".zip")) {
          continue;
        }

        analyze(result.toFile());
      }
    } catch (ZipException e) {
      logger.error("Error while reading zip file");
      throw new RuntimeException(e);
    } catch (IOException e) {
      logger.error("Error while reading the trace result directory");
      throw new RuntimeException(e);
    }
  }

  private void analyze(final File resultZipFile) throws IOException {
    logger.info("Analyzing {}", resultZipFile.getName());
    final ZipFile file = new ZipFile(resultZipFile);

    file.getFileHeaders().forEach(traceFileHeader -> {
      file.extractFile(traceFileHeader, ".");
    });


    file.close();
  }
}
