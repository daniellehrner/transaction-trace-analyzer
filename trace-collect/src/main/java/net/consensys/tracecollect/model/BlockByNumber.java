package net.consensys.tracecollect.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockByNumber {
  private final String number;
  private final List<String> transactions;

  @JsonCreator
  public BlockByNumber(
      @JsonProperty("number") final String number,
      @JsonProperty("transactions") final List<String> transactions) {
    this.number = number;
    this.transactions = transactions;
  }

  public List<String> getTransactions() {
    return transactions;
  }

  public String getNumber() {
    return number;
  }
}
