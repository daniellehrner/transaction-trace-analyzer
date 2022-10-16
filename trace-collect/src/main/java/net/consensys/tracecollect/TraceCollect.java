package net.consensys.tracecollect;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.logging.log4j.Logger;

public class TraceCollect {
  private final ObjectWriter writer;
  private final Logger logger;

  private ZipFile currentZipFile = null;

  public TraceCollect(final ObjectMapper mapper, final Logger logger) {
    this.logger = logger;
    writer = mapper.writer(new DefaultPrettyPrinter());
  }

  public void newBlock(final long block) throws IOException {
    currentZipFile = new ZipFile(block + ".zip");
    logger.debug("Created new file {}", currentZipFile.getFile().getName());
  }

  public void addTransactionTrace(
      final int transactionIndex, final String transactionHash, final JsonNode transactionTrace)
      throws IOException {
    final String fileName =
        String.format("%03d", transactionIndex) + "_" + transactionHash + ".json";
    final Path transactionTraceFile = Paths.get(fileName);
    writer.writeValue(transactionTraceFile.toFile(), transactionTrace);

    logger.info("Adding {} to {}", fileName, currentZipFile.getFile().getName());
    try {
      currentZipFile.addFile(transactionTraceFile.toFile());
    } catch (ZipException e) {
      logger.error(
          "Can't add file {} to {}: {}",
          fileName,
          currentZipFile.getFile().getName(),
          e.getMessage());
      throw new RuntimeException(e);
    }

    if (!transactionTraceFile.toFile().delete()) {
      throw new RuntimeException("Cannot delete file " + fileName);
    }
  }
}
