package net.consensys.tracequery;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.consensys.traceextract.model.ExtractedTrace;
import net.consensys.traceextract.model.ExtractedTraceResult;
import net.consensys.tracequery.model.QueryResult;
import org.apache.logging.log4j.Logger;

public class TraceQuery {
  private final Logger logger;
  private final ObjectMapper mapper;

  public TraceQuery(final Logger logger, final ObjectMapper mapper) {
    this.logger = logger;
    this.mapper = mapper;
  }

  public void queryDirectory(final String dir, final String opCode) {
    final File extractionDir = new File(dir);
    final FileFilter traceExtractionFileFilter =
        file ->
            !file.isDirectory()
                && file.getName().contains("_extracted_")
                && file.getName().endsWith(".json");
    final List<File> traceExtractionFiles;

    try {
      traceExtractionFiles =
          Arrays.asList(Objects.requireNonNull(extractionDir.listFiles(traceExtractionFileFilter)));
    } catch (NullPointerException e) {
      logger.error("Can't get extraction JSON files from {}", dir);
      throw new RuntimeException();
    }

    final long totalCountTraceFiles = traceExtractionFiles.size();

    logger.info("Querying traces from {} files", totalCountTraceFiles);

    final List<QueryResult> queryResults =
        traceExtractionFiles.stream()
            .map(
                traceFile -> {
                  try {
                    return query(traceFile, opCode);
                  } catch (IOException e) {
                    logger.error("Error while accessing file");
                    throw new RuntimeException(e);
                  }
                })
            .collect(Collectors.toList());

    queryResults.forEach(
        queryResult -> {
          // queryResult.getSameKeyPerTransaction().sort(Comparator.reverseOrder());

          logger.info("Total transactions: {}", queryResult.getTransactionTotal());
          logger.info("Queried OP code count: {}", queryResult.getQueriedOpCodeTotal());
          logger.info(
              "Same key per transaction count: {}", queryResult.getSameKeyPerTransaction().size());
          logger.info("Same key count per transaction: {}", queryResult.getSameKeyPerTransaction());
          logger.info("Previous OP codes: {}", queryResult.getPreviousOpCodeCounters());
          logger.info("Next OP codes: {}", queryResult.getNextOpCodeCounters());
        });

    logger.info("Successfully queried extracted transactions");
  }

  private QueryResult query(final File traceFile, final String opCode) throws IOException {
    final ExtractedTraceResult traceResult =
        mapper.readValue(traceFile, ExtractedTraceResult.class);

    long queriedOpCodeCounter = 0;
    final List<Integer> sameKeyPerTransaction = new ArrayList<>();
    final Map<String, Integer> keysPerBlockCounter = new HashMap<>();

    final Map<String, Integer> previousOpCodeCounter = new HashMap<>();
    final Map<String, Integer> nextOpCodeCounter = new HashMap<>();

    for (List<ExtractedTrace> traces : traceResult.getExtractedTransactions().values()) {
      final Map<String, Integer> keysPerTransactionCounter = new HashMap<>();

      for (ExtractedTrace trace : traces) {
        if (!trace.getOpCode().equals(opCode)) {
          continue;
        }

        queriedOpCodeCounter++;

        final String key = trace.getParameters().get(0);
        keysPerTransactionCounter.put(key, keysPerTransactionCounter.getOrDefault(key, 0) + 1);
        keysPerBlockCounter.put(key, keysPerBlockCounter.getOrDefault(key, 0) + 1);

        previousOpCodeCounter.put(
            trace.getPreviousOpCode(),
            previousOpCodeCounter.getOrDefault(trace.getPreviousOpCode(), 0) + 1);
        nextOpCodeCounter.put(
            trace.getNextOpCode(), nextOpCodeCounter.getOrDefault(trace.getNextOpCode(), 0) + 1);
      }

      for (Integer keyCount : keysPerTransactionCounter.values()) {
        if (keyCount <= 1) {
          continue;
        }

        sameKeyPerTransaction.add(keyCount);
      }
    }

    final List<Integer> sameKeyPerBlock = new ArrayList<>();
    for (Integer keyCount : keysPerBlockCounter.values()) {
      if (keyCount <= 1) {
        continue;
      }

      sameKeyPerBlock.add(keyCount);
    }

    return new QueryResult(
        traceResult.getTransactionTotal(),
        queriedOpCodeCounter,
        sameKeyPerTransaction,
        sameKeyPerBlock,
        previousOpCodeCounter,
        nextOpCodeCounter);
  }

  private Map<String, Integer> sortByValue(Map<String, Integer> unsortedMap) {
    List<Map.Entry<String, Integer>> list = new LinkedList<>(unsortedMap.entrySet());

    // Sorting the list based on values
    list.sort(
        (o1, o2) ->
            o2.getValue().compareTo(o1.getValue()) == 0
                ? o1.getKey().compareTo(o2.getKey())
                : o2.getValue().compareTo(o1.getValue()));
    return list.stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
  }
}
