package net.consensys.traceextract;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TraceExtractMain {
  public static void main(String[] args) {
    final Logger logger = LogManager.getLogger("TraceExtract");
    final ObjectMapper mapper = new ObjectMapper();

    if (args.length == 0) {
      logger.error("Path to directory with results from trace-collect is needed as 1. parameter");
      System.exit(1);
    }

    if (args.length == 1) {
      logger.error("OP codes separated by coma are needed as 2. parameter");
      System.exit(1);
    }

    try {
      new TraceExtract(logger, mapper)
          .extractFromDirectory(args[0], Arrays.asList(args[1].split(",")));
    } catch (RuntimeException e) {
      logger.error("Extraction failed with error: {}", e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }
}
