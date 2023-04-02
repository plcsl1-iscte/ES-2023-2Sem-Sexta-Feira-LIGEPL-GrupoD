package iscte.timetable.management;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

public class ManageTimetable {

    public static void main(String[] args) {
        Path filePath = Paths.get("src/main/java/iscte/timetable/files/horario-exemplo.csv");

        String inputFileCSV = filePath.toAbsolutePath().toString();
        String outputFileJSON = "src/main/java/iscte/timetable/files/horario-convertido.json";
        try {
            convertCSVtoJSON(inputFileCSV, outputFileJSON);
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public static void convertCSVtoJSON(String inputFile, String outputFile) throws IOException, CsvException {
        List<String[]> lines;
        try (Reader reader = Files.newBufferedReader(Paths.get(inputFile));
             CSVReader csvReader = new CSVReader(reader)) {
            lines = csvReader.readAll();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode arrayNode = objectMapper.createArrayNode();
        String[] headers = lines.get(0);

        for (int i = 1; i < lines.size(); i++) {
            ObjectNode objectNode = objectMapper.createObjectNode();
            String[] line = lines.get(i);
            for (int j = 0; j < line.length; j++) {
                objectNode.put(headers[j], line[j]);
            }
            arrayNode.add(objectNode);
        }

        try (Writer writer = Files.newBufferedWriter(Paths.get(outputFile))) {
            objectMapper.writeValue(writer, arrayNode);
        }
    }
}