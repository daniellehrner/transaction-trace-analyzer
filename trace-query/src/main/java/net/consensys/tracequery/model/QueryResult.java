package net.consensys.tracequery.model;

import java.util.List;
import java.util.Map;

public class QueryResult {
  private final long transactionTotal;
  private final long queriedOpCodeTotal;

  private final List<Integer> sameKeyPerTransaction;
  private final List<Integer> sameKeyPerBlock;

  private final Map<String, Integer> previousOpCodeCounters;
  private final Map<String, Integer> nextOpCodeCounters;

  public QueryResult(
      final long transactionTotal,
      final long queriedOpCodeTotal,
      final List<Integer> sameKeyPerTransaction,
      final List<Integer> sameKeyPerBlock,
      final Map<String, Integer> previousOpCodeCounters,
      final Map<String, Integer> nextOpCodeCounters) {
    this.transactionTotal = transactionTotal;
    this.queriedOpCodeTotal = queriedOpCodeTotal;
    this.sameKeyPerTransaction = sameKeyPerTransaction;
    this.sameKeyPerBlock = sameKeyPerBlock;
    this.previousOpCodeCounters = previousOpCodeCounters;
    this.nextOpCodeCounters = nextOpCodeCounters;
  }

  public long getTransactionTotal() {
    return transactionTotal;
  }

  public long getQueriedOpCodeTotal() {
    return queriedOpCodeTotal;
  }

  public List<Integer> getSameKeyPerTransaction() {
    return sameKeyPerTransaction;
  }

  public List<Integer> getSameKeyPerBlock() {
    return sameKeyPerBlock;
  }

  public Map<String, Integer> getPreviousOpCodeCounters() {
    return previousOpCodeCounters;
  }

  public Map<String, Integer> getNextOpCodeCounters() {
    return nextOpCodeCounters;
  }

  @Override
  public String toString() {
    return "QueryResult{"
        + "transactionTotal="
        + transactionTotal
        + ", queriedOpCodeTotal="
        + queriedOpCodeTotal
        + ", sameKeyPerTransaction="
        + sameKeyPerTransaction
        + ", sameKeyPerBlock="
        + sameKeyPerBlock
        + ", previousOpCodeCounters="
        + previousOpCodeCounters
        + ", nextOpCodeCounters="
        + nextOpCodeCounters
        + '}';
  }
}
