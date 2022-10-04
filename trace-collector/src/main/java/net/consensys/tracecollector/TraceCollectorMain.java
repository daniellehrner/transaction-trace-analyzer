package net.consensys.tracecollector;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TraceCollectorMain {
  private static final ObjectMapper mapper = new ObjectMapper();

  public static void main(String[] args) {
    final Logger logger = LogManager.getLogger("TraceCollector");

    if (args.length == 0) {
      logger.error("Number of blocks to trace needs to be provided as argument");
      System.exit(1);
    }

    final long blocksToTrace = Long.parseLong(args[0]);

    if (args.length > 2) {
      logger.error("Too many arguments. Maximum 2 are allowed");
      System.exit(1);
    }

    final String url = args.length == 2 ? args[1] : "http://127.0.0.1:8545";

    final TraceRetriever traceRetriever =
        new TraceRetriever(new OkHttpClient(), mapper, logger, new TraceCollector(mapper, logger));

    try {
      traceRetriever.retrieve(url, blocksToTrace);
    } catch (RuntimeException e) {
      logger.error(e.getMessage());
      System.exit(1);
    }
  }
}
