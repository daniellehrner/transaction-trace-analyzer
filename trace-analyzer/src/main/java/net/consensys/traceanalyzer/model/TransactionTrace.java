package net.consensys.traceanalyzer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TransactionTrace {
  private final List<StructLog> structLogs;
  private final String returnValue;
  private final long gas;
  private final boolean failed;

  @JsonCreator
  public TransactionTrace(
      @JsonProperty("structLogs") List<StructLog> structLogs,
      @JsonProperty("returnValue") String returnValue,
      @JsonProperty("gas") long gas,
      @JsonProperty("failed") boolean failed) {
    this.structLogs = structLogs;
    this.returnValue = returnValue;
    this.gas = gas;
    this.failed = failed;
  }

  public List<StructLog> getStructLogs() {
    return structLogs;
  }

  public String getReturnValue() {
    return returnValue;
  }

  public long getGas() {
    return gas;
  }

  public boolean failed() {
    return failed;
  }
}
