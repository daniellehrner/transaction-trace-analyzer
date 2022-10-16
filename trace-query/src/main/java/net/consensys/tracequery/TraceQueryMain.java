package net.consensys.tracequery;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TraceQueryMain {

  public static void main(String[] args) {
    final Logger logger = LogManager.getLogger("TraceQuery");
    final ObjectMapper mapper = new ObjectMapper();

    if (args.length == 0) {
      logger.error("Path to directory with results from trace-extract is needed as 1. parameter");
      System.exit(1);
    }

    if (args.length == 1) {
      logger.error("OP code to query nis needed as 2. parameter");
      System.exit(1);
    }

    try {
      new TraceQuery(logger, mapper).queryDirectory(args[0], args[1]);
    } catch (RuntimeException e) {
      logger.error("trace-query failed with error: {}", e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }
}
