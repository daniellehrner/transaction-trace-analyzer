package net.consensys.traceextract.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ExtractedTrace {
  private final String opCode;
  private final List<String> parameters;
  private final long index;
  private final String previousOpCode;
  private final String nextOpCode;

  @JsonCreator
  public ExtractedTrace(
      @JsonProperty("opCode") final String opCode,
      @JsonProperty("parameters") final List<String> parameters,
      @JsonProperty("index") final long index,
      @JsonProperty("previousOpCode") final String previousOpCode,
      @JsonProperty("nextOpCode") final String nextOpCode) {
    this.opCode = opCode;
    this.parameters = parameters;
    this.index = index;
    this.previousOpCode = previousOpCode;
    this.nextOpCode = nextOpCode;
  }

  @JsonProperty("opCode")
  public String getOpCode() {
    return opCode;
  }

  @JsonProperty("parameters")
  public List<String> getParameters() {
    return parameters;
  }

  @JsonProperty("index")
  public long getIndex() {
    return index;
  }

  @JsonProperty("previousOpCode")
  public String getPreviousOpCode() {
    return previousOpCode;
  }

  @JsonProperty("nextOpCode")
  public String getNextOpCode() {
    return nextOpCode;
  }
}
