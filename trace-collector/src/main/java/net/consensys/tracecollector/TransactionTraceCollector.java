package net.consensys.tracecollector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.consensys.tracecollector.model.BlockByNumber;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TransactionTraceCollector {
  private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

  private static final OkHttpClient client = new OkHttpClient();
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
  private static final Logger logger = LogManager.getLogger(TransactionTraceCollector.class);

  public static void main(String[] args) {

    if (args.length == 0) {
      logger.error("Number of blocks to trace needs to be provided as argument");
      System.exit(1);
    }

    final long blocksToTrace = Long.parseLong(args[0]);
    long successfullyTracedBlocks = 0;
    long lastTracedBlockNumber = 0;

    logger.info("Getting transaction for {} block(s)", blocksToTrace);

    while (successfullyTracedBlocks < blocksToTrace) {
      try {
        final JsonNode blockByNumberResponse = post("eth_getBlockByNumber", "\"latest\", false");
        final BlockByNumber block = mapper.treeToValue(blockByNumberResponse, BlockByNumber.class);

        if (block == null) {
          logger.error("Block is null. Response result: {}", blockByNumberResponse.toString());
          System.exit(1);
        }
        final long blockNumber = Long.parseLong(block.getNumber().substring(2), 16);

        if (lastTracedBlockNumber == blockNumber) {
          logger.info("Received block {} as latest again. Retrying in 13s.", lastTracedBlockNumber);
          Thread.sleep(13000);
          continue;
        }

        logger.info(
            "Got {} transactions from block {}", block.getTransactions().size(), blockNumber);

        final Path blockDir = Paths.get("./" + blockNumber);
        Files.createDirectory(blockDir);

        for (String transactionHash : block.getTransactions()) {
          logger.info("Getting trace for transaction {}", transactionHash);

          final JsonNode transactionTraceResponse =
              post(
                  "debug_traceTransaction",
                  "\"" + transactionHash + "\",{\"disableStorage\":true}");

          final Path transactionTraceFile = Paths.get(blockDir + "/" + transactionHash + ".json");
          writer.writeValue(transactionTraceFile.toFile(), transactionTraceResponse);
        }

        successfullyTracedBlocks++;
        lastTracedBlockNumber = blockNumber;

      } catch (JsonProcessingException e) {
        logger.error(
            "Error while converting eth_getBlockByNumber response to JSON: {}", e.getMessage());
      } catch (IOException e) {
        logger.error("Error during an RPC call to the node: {}", e.getMessage());
      } catch (InterruptedException e) {
        logger.error("Sleep of thread was interrupted. Shutting down");
        System.exit(1);
      }
    }
  }

  private static JsonNode post(final String method, final String parameters) throws IOException {
    final String requestString =
        "{\"jsonrpc\":\"2.0\",\"method\":\""
            + method
            + "\",\"params\":["
            + parameters
            + "], \"id\":1}";
    logger.debug("Request: {}", requestString);

    final RequestBody body = RequestBody.create(requestString, JSON);

    final Request request =
        new Request.Builder().url("http://192.168.1.42:8545").post(body).build();

    try (Response response = client.newCall(request).execute()) {
      if (response.code() != 200) {
        logger.error("Request was not successful: {}", response.body().string());
        System.exit(1);
      }

      final String responseString = response.body().string();
      logger.debug("Response: {}", responseString);

      final JsonNode responseNode = mapper.readTree(responseString);

      if (responseNode.get("error") != null) {
        logger.error("Error while calling {}: {}", method, responseNode.get("error"));
        System.exit(1);
      }

      return responseNode.get("result");
    }
  }
}
