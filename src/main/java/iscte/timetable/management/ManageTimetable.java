package iscte.timetable.management;

import java.io.*;
import java.nio.file.*;
import java.text.ParseException;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

public class ManageTimetable {

    public static void main(String[] args) {
        Path filePath = Paths.get("src/main/java/iscte/timetable/files/horario-exemplo.csv");
        String inputFileCSV = filePath.toAbsolutePath().toString();
        String outputFileJSON = "src/main/java/iscte/timetable/files/horario-convertido.json";

        String jsonFilePath = "src/main/java/iscte/timetable/files/horario-convertido.json";
        File jsonFile = new File(jsonFilePath);
        ;
        try {
            convertCSVtoJSON(inputFileCSV, outputFileJSON);
            convertJSONtoCSV(jsonFile, "teste");
        } catch (IOException | CsvException | ParseException | org.json.simple.parser.ParseException e) {
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

    public static void convertJSONtoCSV(File jsonFile, String csvFileName)
            throws IOException, ParseException, org.json.simple.parser.ParseException {
        // Initialize the JSON parser
        JSONParser parser = new JSONParser();

        // Initialize the reader for the JSON file
        FileReader reader = new FileReader(jsonFile);

        // Parse the JSON data from the file
        JSONArray jsonData = (JSONArray) parser.parse(reader);

        // Get the headers of the CSV file
        Set<String> headers = new HashSet<>();
        for (int i = 0; i < jsonData.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonData.get(i);
            headers.addAll(jsonObject.keySet());
        }

        // Initialize the CSV writer
        String currentDir = System.getProperty("user.dir");
        String csvFilePath = currentDir + "/" + csvFileName + ".csv";
        FileWriter writer = new FileWriter(csvFilePath);

        // Write the headers of the CSV file
        for (String header : headers) {
            writer.append(header).append(",");
        }
        writer.append("\n");

        // Write the data rows of the CSV file
        for (int i = 0; i < jsonData.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonData.get(i);
            for (String header : headers) {
                String value = "";
                if (jsonObject.containsKey(header)) {
                    value = (String) jsonObject.get(header);
                }
                writer.append(value).append(",");
            }
            writer.append("\n");
        }

        // Close the CSV writer and the JSON reader
        writer.close();
        reader.close();
    }

}