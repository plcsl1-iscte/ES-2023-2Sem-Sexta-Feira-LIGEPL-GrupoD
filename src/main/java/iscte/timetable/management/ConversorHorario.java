package iscte.timetable.management;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.csv.*;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonPropertyOrder({"curso", "unidadeCurricular", "turno", "turma", "inscritosNoTurno", "diaDaSemana", "horaInicioAula", "horaFimAula", "dataAula", "salaAtribuidaAula", "lotacaoSala"})
class Horario {
    @JsonProperty("curso")
    public String curso;
    @JsonProperty("unidadeCurricular")
    public String unidadeCurricular;
    @JsonProperty("turno")
    public String turno;
    @JsonProperty("turma")
    public String turma;
    @JsonProperty("inscritosNoTurno")
    public int inscritosNoTurno;
    @JsonProperty("diaDaSemana")
    public String diaDaSemana;
    @JsonProperty("horaInicioAula")
    public String horaInicioAula;
    @JsonProperty("horaFimAula")
    public String horaFimAula;
    @JsonProperty("dataAula")
    public String dataAula;
    @JsonProperty("salaAtribuidaAula")
    public String salaAtribuidaAula;
    @JsonProperty("lotacaoSala")
    public int lotacaoSala;
}


public class ConversorHorario {

    public static void main(String[] args) {
        try {
            String Repo = "src/main/java/iscte/timetable/files/";

            List<Horario> horarios = lerCSV(Repo + "horario-exemplo.csv");
            escreverJSON(Repo + "horario-exemplo.json", horarios);

            List<Horario> horariosJson = lerJSON(Repo + "horario-exemplo.json");
            escreverCSV(Repo + "horario-exemplo-convertido.csv", horariosJson);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Horario> lerCSV(String arquivoCSV) throws IOException {
        File csvFile = new File(arquivoCSV);
        List<Horario> horarios = new ArrayList<>();
    
        try (Reader fileReader = new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(','))) {
    
            for (CSVRecord record : csvParser) {
                Horario horario = new Horario();
                horario.curso = record.get(0);
                horario.unidadeCurricular = record.get(1);
                horario.turno = record.get(2);
                horario.turma = record.get(3);
                String inscritosNoTurnoStr = record.get(4);
                horario.inscritosNoTurno = inscritosNoTurnoStr.isEmpty() ? 0 : Integer.parseInt(inscritosNoTurnoStr);
                horario.diaDaSemana = record.get(5);
                horario.horaInicioAula = record.get(6);
                horario.horaFimAula = record.get(7);
                horario.dataAula = record.get(8);
                horario.salaAtribuidaAula = record.get(9);
                String lotacaoSalaStr = record.get(10);
                horario.lotacaoSala = lotacaoSalaStr.isEmpty() ? 0 : Integer.parseInt(lotacaoSalaStr);
    
                horarios.add(horario);
            }
        }
    
        return horarios;
    }
    

    private static void escreverJSON(String arquivoJSON, List<Horario> horarios) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.writeValue(new File(arquivoJSON), horarios);
    }

    private static List<Horario> lerJSON(String arquivoJSON) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        return Arrays.asList(jsonMapper.readValue(new File(arquivoJSON), Horario[].class));
    }

    private static void escreverCSV(String arquivoCSV, List<Horario> horarios) throws IOException {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema.Builder schemaBuilder = CsvSchema.builder();
        schemaBuilder.addColumn("curso");
        schemaBuilder.addColumn("unidadeCurricular");
        schemaBuilder.addColumn("turno");
        schemaBuilder.addColumn("turma");
        schemaBuilder.addColumn("inscritosNoTurno", CsvSchema.ColumnType.NUMBER);
        schemaBuilder.addColumn("diaDaSemana");
        schemaBuilder.addColumn("horaInicioAula");
        schemaBuilder.addColumn("horaFimAula");
        schemaBuilder.addColumn("dataAula");
        schemaBuilder.addColumn("salaAtribuidaAula");
        schemaBuilder.addColumn("lotacaoSala", CsvSchema.ColumnType.NUMBER);

        CsvSchema schema = schemaBuilder.build().withHeader().withColumnSeparator(',');

        csvMapper.writer(schema).writeValue(new File(arquivoCSV), horarios);
    }
}
