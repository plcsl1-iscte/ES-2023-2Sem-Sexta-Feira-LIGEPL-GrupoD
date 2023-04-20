package iscte.Controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

public class HorarioWriter {

    private static final String[] CSV_HEADER = {
            "curso", "unidadeCurricular", "turno", "turma", "inscritosNoTurno",
            "diaDaSemana", "horaInicioAula", "horaFimAula", "dataAula", "salaAtribuidaAula", "lotacaoSala"
    };

    public static void listToJsonFile(List<Horario> horarios, String outputPath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputPath), horarios);
    }

    public static void listToCsvFile(List<Horario> horarios, String outputPath) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        try (FileWriter fileWriter = new FileWriter(outputPath);
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {
            csvWriter.writeNext(CSV_HEADER);

            ColumnPositionMappingStrategy<Horario> mappingStrategy = new ColumnPositionMappingStrategy<>();
            mappingStrategy.setType(Horario.class);

            StatefulBeanToCsv<Horario> beanToCsv = new StatefulBeanToCsvBuilder<Horario>(csvWriter)
                    .withMappingStrategy(mappingStrategy)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .build();

            beanToCsv.write(horarios);
        }
    }

}
