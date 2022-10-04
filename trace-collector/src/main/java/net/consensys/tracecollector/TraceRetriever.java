package net.consensys.tracecollector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import net.consensys.tracecollector.model.BlockByNumber;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.logging.log4j.Logger;

public class TraceRetriever {
  private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

  private final OkHttpClient client;
  private final ObjectMapper mapper;

  private final Logger logger;
  private final TraceCollector traceCollector;

  public TraceRetriever(
      final OkHttpClient client,
      final ObjectMapper mapper,
      final Logger logger,
      final TraceCollector traceCollector) {
    this.client = client;
    this.mapper = mapper;
    this.logger = logger;
    this.traceCollector = traceCollector;
  }

  public void retrieve(final String url, final long blocksToTrace) throws RuntimeException {
    long successfullyTracedBlocks = 0;
    long lastTracedBlockNumber = 0;

    logger.info("Getting transaction for {} block(s)", blocksToTrace);

    while (successfullyTracedBlocks < blocksToTrace) {
      try {
        final JsonNode blockByNumberResponse =
            post(url, "eth_getBlockByNumber", "\"latest\", false");
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
        traceCollector.newBlock(blockNumber);

        for (int i = 0; i < block.getTransactions().size(); i++) {
          final String transactionHash = block.getTransactions().get(i);
          logger.info("Getting trace for transaction {}", transactionHash);

          final JsonNode transactionTrace =
              post(
                  url,
                  "debug_traceTransaction",
                  "\"" + transactionHash + "\",{\"disableStorage\":true}");

          traceCollector.addTransactionTrace(i, transactionHash, transactionTrace);
        }

        successfullyTracedBlocks++;
        lastTracedBlockNumber = blockNumber;
      } catch (ConnectException e) {
        logger.error("Cannot connect to node");
        throw new RuntimeException(e);
      } catch (SocketTimeoutException e) {
        logger.error("Timeout while calling the node");
        throw new RuntimeException(e);
      } catch (JsonProcessingException e) {
        logger.error(
            "Error while converting eth_getBlockByNumber response to JSON: {}", e.getMessage());
      } catch (IOException e) {
        logger.error("Error during an RPC call to the node: {}", e.getMessage());
      } catch (InterruptedException e) {
        logger.error("Sleep of thread was interrupted. Shutting down");
        throw new RuntimeException(e);
      }
    }
  }

  private JsonNode post(final String url, final String method, final String parameters)
      throws IOException {
    final String requestString =
        "{\"jsonrpc\":\"2.0\",\"method\":\""
            + method
            + "\",\"params\":["
            + parameters
            + "], \"id\":1}";
    logger.debug("Request: {}", requestString);

    final RequestBody body = RequestBody.create(requestString, JSON);

    final Request request = new Request.Builder().url(url).post(body).build();

    try (Response response = client.newCall(request).execute()) {
      if (response.code() != 200) {
        logger.error("Request was not successful: {}", response.body().string());
        throw new RuntimeException();
      }

      final String responseString = response.body().string();
      logger.debug("Response: {}", responseString);

      final JsonNode responseNode = mapper.readTree(responseString);

      if (responseNode.get("error") != null) {
        logger.error("Error while calling {}: {}", method, responseNode.get("error"));
        throw new RuntimeException();
      }

      return responseNode.get("result");
    }
  }
}
