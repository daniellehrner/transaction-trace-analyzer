package net.consensys.tracecollect;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

public class TraceCollectMain {
  private static final ObjectMapper mapper = new ObjectMapper();

  public static void main(String[] args) {
    final Logger logger = LogManager.getLogger("TraceCollector");

    final TraceRetrieve traceRetrieve =
        new TraceRetrieve(new OkHttpClient(), mapper, logger, new TraceCollect(mapper, logger));
    new CommandLine(traceRetrieve).parseArgs(args);

    try {
      traceRetrieve.retrieve();
    } catch (RuntimeException e) {
      logger.error(e.getMessage());
      System.exit(1);
    }
  }
}
