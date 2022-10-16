package net.consensys.traceextract.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class ExtractedTraceResult {
  private final long transactionTotal;
  private final long transferTransactionTotal;
  private final long extractedTransactionTotal;
  private final Map<String, List<ExtractedTrace>> extractedTransactions;

  @JsonCreator
  public ExtractedTraceResult(
      @JsonProperty("transactionTotal") long transactionTotal,
      @JsonProperty("transferTransactionTotal") long transferTransactionTotal,
      @JsonProperty("extractedTransactions")
          final Map<String, List<ExtractedTrace>> extractedTransactions) {
    this.transactionTotal = transactionTotal;
    this.transferTransactionTotal = transferTransactionTotal;
    this.extractedTransactionTotal = extractedTransactions.size();
    this.extractedTransactions = extractedTransactions;
  }

  @JsonProperty("transactionTotal")
  public long getTransactionTotal() {
    return transactionTotal;
  }

  @JsonProperty("transferTransactionTotal")
  public long getTransferTransactionTotal() {
    return transferTransactionTotal;
  }

  @JsonProperty("extractedTransactionTotal")
  public long getExtractedTransactionTotal() {
    return extractedTransactionTotal;
  }

  @JsonProperty("extractedTransactions")
  public Map<String, List<ExtractedTrace>> getExtractedTransactions() {
    return extractedTransactions;
  }
}
