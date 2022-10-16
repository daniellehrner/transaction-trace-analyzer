package net.consensys.traceextract;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.consensys.traceextract.model.ExtractedTrace;
import net.consensys.traceextract.model.ExtractedTraceResult;
import net.consensys.traceextract.model.StructLog;
import net.consensys.traceextract.model.TransactionTrace;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.logging.log4j.Logger;

public class TraceExtract {
  private final Logger logger;
  private final ObjectMapper mapper;
  private final ObjectWriter writer;

  public TraceExtract(Logger logger, ObjectMapper mapper) {
    this.logger = logger;
    this.mapper = mapper;

    writer = mapper.writer(new DefaultPrettyPrinter());
  }

  public void extractFromDirectory(final String dir, final List<String> opCodes)
      throws RuntimeException {
    final File traceDir = new File(dir);
    final FileFilter traceZipFileFilter =
        file -> !file.isDirectory() && file.getName().endsWith(".zip");
    final List<File> traceZipFiles;

    try {
      traceZipFiles = Arrays.asList(Objects.requireNonNull(traceDir.listFiles(traceZipFileFilter)));
    } catch (NullPointerException e) {
      logger.error("Can't get trace zip files from {}", dir);
      throw new RuntimeException();
    }

    final long totalCountZipFiles = traceZipFiles.size();

    logger.info("Extracting OP codes {} from {} blocks", opCodes, totalCountZipFiles);

    traceZipFiles.parallelStream()
        .forEach(
            resultZipFile -> {
              try {
                extractFromBlock(resultZipFile, opCodes);
              } catch (ZipException e) {
                logger.error("Error while reading zip file");
                throw new RuntimeException(e);
              } catch (IOException e) {
                logger.error("Error while analyzing");
                throw new RuntimeException(e);
              }
            });

    logger.info("Successfully extracted transactions");
  }

  private void extractFromBlock(final File resultZipFile, final List<String> opCodes)
      throws IOException {
    logger.info("Extracting from {}", resultZipFile.getName());

    final ZipFile file = new ZipFile(resultZipFile);
    final List<FileHeader> fileHeaders = file.getFileHeaders();

    long transactionTotal = 0;
    long transferTransactionTotal = 0;
    final Map<String, List<ExtractedTrace>> extractedTransactions = new HashMap<>();

    for (int i = 0; i < fileHeaders.size(); i++) {
      transactionTotal++;

      final FileHeader traceFileHeader = file.getFileHeaders().get(i);
      file.extractFile(traceFileHeader, ".");

      final File traceFile = new File(traceFileHeader.getFileName());
      final TransactionTrace transactionTrace = mapper.readValue(traceFile, TransactionTrace.class);

      if (!traceFile.delete()) {
        throw new RuntimeException("Cannot delete file " + traceFile.getName());
      }

      if ((transactionTrace.getGas() == 21000) && (transactionTrace.getStructLogs().size() == 1)) {
        transferTransactionTotal++;
        logger.debug("Simple transfer, will be ignored");
        continue;
      }

      final List<ExtractedTrace> extractedTraces = new ArrayList<>();

      for (int j = 0; j < transactionTrace.getStructLogs().size(); j++) {
        final StructLog structLog = transactionTrace.getStructLogs().get(j);
        final StructLog previousStructLog =
            j > 0 ? transactionTrace.getStructLogs().get(j - 1) : null;
        final StructLog nextStructLog =
            j < (transactionTrace.getStructLogs().size() - 1)
                ? transactionTrace.getStructLogs().get(j + 1)
                : null;

        if (!opCodes.contains(structLog.getOp())) {
          continue;
        }

        final List<String> parameters;
        final int stackSize = structLog.getStack().length;

        switch (structLog.getOp()) {
          case "SSTORE":
            parameters =
                List.of(structLog.getStack()[stackSize - 1], structLog.getStack()[stackSize - 2]);
            break;
          case "SLOAD":
            parameters = List.of(structLog.getStack()[stackSize - 1]);
            break;
          default:
            throw new AssertionError(
                "Extraction of parameters is not defined for " + structLog.getOp());
        }

        extractedTraces.add(
            new ExtractedTrace(
                structLog.getOp(),
                parameters,
                j,
                previousStructLog != null ? previousStructLog.getOp() : null,
                nextStructLog != null ? nextStructLog.getOp() : null));
      }

      if (extractedTraces.isEmpty()) {
        continue;
      }

      extractedTransactions.put(
          traceFileHeader.getFileName().replace(".json", ""), extractedTraces);
    }

    file.close();

    final String extractedFileName =
        resultZipFile.getName().replace(".zip", "") + "_extracted_" + opCodes + ".json";
    writer.writeValue(
        Paths.get(extractedFileName).toFile(),
        new ExtractedTraceResult(
            transactionTotal, transferTransactionTotal, extractedTransactions));

    logger.info(
        "Extracted {}/{} transactions from {}",
        extractedTransactions.size(),
        transactionTotal,
        resultZipFile.getName());
  }
}
