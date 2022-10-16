package net.consensys.traceextract.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;

public class StructLog {
  private final int depth;
  private final long gas;
  private final long gasCost;
  private final String[] memory;
  private final String op;
  private final int pc;
  private final String[] stack;
  private final Object storage;
  private final String reason;
  private final List<String> error;

  @JsonCreator
  public StructLog(
      @JsonProperty("depth") int depth,
      @JsonProperty("gas") long gas,
      @JsonProperty("gasCost") long gasCost,
      @JsonProperty("memory") String[] memory,
      @JsonProperty("op") String op,
      @JsonProperty("pc") int pc,
      @JsonProperty("stack") String[] stack,
      @JsonProperty("storage") Object storage,
      @JsonProperty("reason") String reason,
      @JsonProperty("error") List<String> error) {
    this.depth = depth;
    this.gas = gas;
    this.gasCost = gasCost;
    this.memory = memory;
    this.op = op;
    this.pc = pc;
    this.stack = stack;
    this.storage = storage;
    this.reason = reason;
    this.error = error;
  }

  public int getDepth() {
    return depth;
  }

  public long getGas() {
    return gas;
  }

  public long getGasCost() {
    return gasCost;
  }

  public String[] getMemory() {
    return memory;
  }

  public String getOp() {
    return op;
  }

  public int getPc() {
    return pc;
  }

  public String[] getStack() {
    return stack;
  }

  public Object getStorage() {
    return storage;
  }

  public String getReason() {
    return reason;
  }

  public List<String> getError() {
    return error;
  }

  @Override
  public String toString() {
    return "StructLog{"
        + "depth="
        + depth
        + ", gas="
        + gas
        + ", gasCost="
        + gasCost
        + ", memory="
        + Arrays.toString(memory)
        + ", op='"
        + op
        + '\''
        + ", pc="
        + pc
        + ", stack="
        + Arrays.toString(stack)
        + ", storage="
        + storage
        + ", reason='"
        + reason
        + '\''
        + ", error="
        + error
        + '}';
  }
}
