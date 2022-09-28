package net.consensys.traceanalyzer;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TraceAnalyzer {
  public static void main(String[] args) {
    System.out.println("Hello world!");

    //    final TransactionTrace transactionTrace =
    //        mapper.treeToValue(transactionTraceResponse, TransactionTrace.class);
    //
    //    if (transactionTrace == null) {
    //      logger.error("Trace result empty, block most probably too old");
    //      System.exit(1);
    //    }
    //
    //    if (transactionTrace.getStructLogs() == null) {
    //      logger.info("\tNo struct log available");
    //      continue;
    //    }

    //    for (StructLog structLog : transactionTrace.getStructLogs()) {
    //      opCodeCounter.put(structLog.getOp(), opCodeCounter.getOrDefault(structLog.getOp(), 0) +
    // 1);
    //      logger.debug("\t{}: {}", structLog.getOp(), opCodeCounter.get(structLog.getOp()));
    //
    //      if (!selectedOpCodes.isEmpty() && selectedOpCodes.contains(structLog.getOp())) {
    //        final int stackSize = structLog.getStack().length;
    //
    //        switch (structLog.getOp()) {
    //          case "SLOAD":
    //            logger.info("SLOAD:  key: {}", structLog.getStack()[stackSize - 1]);
    //            break;
    //          case "SSTORE":
    //            logger.info("SSTORE: key: {}, value: {}", structLog.getStack()[stackSize - 1],
    // structLog.getStack()[stackSize - 2]);
    //            break;
    //          default:
    //            logger.info(structLog.toString());
    //        }
    //      }
    //    }

    //    try {
    //      final FileWriter csvFile = new FileWriter(args[0] + "_op_codes.csv");
    //      final Map<String, Integer> sortedOpCodeCounter = sortByValue(opCodeCounter);
    //      sortedOpCodeCounter.forEach(
    //          (opCode, totalCount) -> {
    //            try {
    //              csvFile.write(opCode + ";" + totalCount + "\n");
    //            } catch (IOException e) {
    //              throw new RuntimeException(e);
    //            }
    //            logger.info("{}: {}", opCode, totalCount);
    //          });
    //      csvFile.close();
    //    } catch (IOException e) {
    //      logger.error("Cannot write to file: {}", e.getMessage());
    //    }
  }

  private static Map<String, Integer> sortByValue(Map<String, Integer> unsortedMap) {
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
