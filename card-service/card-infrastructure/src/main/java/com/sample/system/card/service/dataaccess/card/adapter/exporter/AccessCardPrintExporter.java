package com.sample.system.card.service.dataaccess.card.adapter.exporter;

import com.healthmarketscience.jackcess.*;
import com.sample.system.card.service.dataaccess.card.mapper.exporter.CardPrintMapper;
import com.sample.system.card.service.domain.entity.Card;
import com.sample.system.card.service.dataaccess.card.dto.accessFile.AccessCardPrintRow;
import com.sample.system.card.service.domain.ports.output.exporter.CardPrintExporterRepository;
import com.sample.system.card.service.domain.response.accessfile.result.AccessFileResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessCardPrintExporter implements CardPrintExporterRepository {

    private static final String ACCESS_FILE_PREFIX = "card-export";
    private static final DateTimeFormatter FILE_TS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final CardPrintMapper cardPrintMapper;

    @Value("${file.storage.path:uploads}")
    private String fileStoragePath;

    @Override
    public AccessFileResult export(List<Card> cards) {
        log.info("Starting Access 2003 MDB export for {} cards", cards.size());

        List<AccessCardPrintRow> rows = cardPrintMapper.toAccessRows(cards);

        Path filePath = prepareFilePath();
        writeAccessFile(rows, filePath);

        return AccessFileResult.builder()
                .filePath(filePath)
                .fileName(filePath.getFileName().toString())
                .downloadUrl("/uploads/" + filePath.getFileName())
                .recordCount(cards.size())
                .trackCount(cards.size())
                .customerCount(cards.size())
                .build();
    }

    private Path prepareFilePath() {
        Path dir = resolveStorageDirectory();
        String fileName = ACCESS_FILE_PREFIX + "-"
                + LocalDateTime.now().format(FILE_TS_FORMAT)
                + ".mdb";
        return dir.resolve(fileName);
    }

    private Path resolveStorageDirectory() {
        Path directory = Paths.get(
                (fileStoragePath == null || fileStoragePath.isBlank())
                        ? "uploads"
                        : fileStoragePath
        ).toAbsolutePath().normalize();

        try {
            Files.createDirectories(directory);
            return directory;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create storage directory", e);
        }
    }

    private void writeAccessFile(List<AccessCardPrintRow> rows, Path filePath) {
        try (Database db = DatabaseBuilder.create(
                Database.FileFormat.V2003,
                filePath.toFile()
        )) {

            Table table = createAccessTable(db);

            for (AccessCardPrintRow row : rows) {
                table.addRow(
                        row.getId(),
                        row.getPanPart1(),
                        row.getPanPart2(),
                        row.getPanPart3(),
                        row.getPanPart4(),
                        row.getCvv2(),
                        row.getTrack1(),
                        row.getTrack2(),
                        row.getTrack3(),
                        row.getFullName(),
                        row.getFirstName(),
                        row.getLastName(),
                        row.getExDate(),
                        row.getPan(),
                        row.getFirstStatementDate(),
                        row.getAddress(),
                        row.getAddress1(),
                        row.getAddress2(),
                        row.getAddress3(),
                        row.getZipCode(),
                        row.getBarcode(),
                        row.getCellNumber(),
                        row.getPrintCount(),
                        row.getPrintCommand(),
                        row.getLDate(),
                        row.getLTime(),
                        row.getNationalId()
                );
            }

            log.info("Access 2003 MDB file created successfully: {}", filePath);

        } catch (Exception e) {
            log.error("Failed to generate MDB file {}", filePath, e);
            throw new RuntimeException("Access MDB export failed", e);
        }
    }

    private Table createAccessTable(Database db) throws IOException {
        return new TableBuilder("card_export")

                .addColumn(col("ID", DataType.TEXT))
                .addColumn(col("PanPart1", DataType.TEXT))
                .addColumn(col("PanPart2", DataType.TEXT))
                .addColumn(col("PanPart3", DataType.TEXT))
                .addColumn(col("PanPart4", DataType.TEXT))
                .addColumn(col("Cvv2", DataType.TEXT))

                .addColumn(col("Track1", DataType.TEXT))
                .addColumn(col("Track2", DataType.TEXT))
                .addColumn(col("Track3", DataType.TEXT))

                .addColumn(col("FullName", DataType.TEXT))
                .addColumn(col("FirstName", DataType.TEXT))
                .addColumn(col("LastName", DataType.TEXT))
                .addColumn(col("ExDate", DataType.TEXT))
                .addColumn(col("Pan", DataType.TEXT))
                .addColumn(col("FirstStatementDate", DataType.TEXT))

                .addColumn(col("Address", DataType.TEXT))
                .addColumn(col("Address1", DataType.TEXT))
                .addColumn(col("Address2", DataType.TEXT))
                .addColumn(col("Address3", DataType.TEXT))

                .addColumn(col("ZipCode", DataType.TEXT))
                .addColumn(col("Barcode", DataType.TEXT))
                .addColumn(col("CellNumber", DataType.TEXT))
                .addColumn(col("PrintCount", DataType.TEXT))
                .addColumn(col("PrintCommand", DataType.TEXT))
                .addColumn(col("L_Date", DataType.TEXT))
                .addColumn(col("L_Time", DataType.TEXT))
                .addColumn(col("NationalId", DataType.TEXT))

                .toTable(db);
    }

    private ColumnBuilder col(String name, DataType type) {
        return new ColumnBuilder(name).setType(type);
    }
}
