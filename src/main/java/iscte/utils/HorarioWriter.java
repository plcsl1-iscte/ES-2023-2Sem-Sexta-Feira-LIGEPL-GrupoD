package iscte.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import iscte.entities.Horario;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * The HorarioWriter class provides methods to convert a List of Horario objects into
 * different string representations, such as JSON and CSV.
 * It includes two public static methods:
 * <li>{@link #listToJson(List)}</li>
 * <li>{@link #listToCsv(List)}</li>
 * The class also defines a private constructor, which is empty and has no logic,
 * to ensure that no objects of the HorarioWriter class can be created.
 * The listToJson method uses the Jackson ObjectMapper to convert the List of Horario
 * objects into a JSON string.
 * The listToCsv method uses the OpenCSV library to convert the List of Horario
 * objects into a CSV string. The method expects the Horario class to have fields
 * with names that match the column headers in the CSV_HEADER constant defined in
 * the class.
 */
public class HorarioWriter {

    private static final String[] CSV_HEADER = {
            "curso", "unidadeCurricular", "turno", "turma", "inscritosNoTurno",
            "diaDaSemana", "horaInicioAula", "horaFimAula", "dataAula", "salaAtribuidaAula", "lotacaoSala"
    };

    /**
     * This constructor is empty because there are no additional operations that need to be performed
     * during the initialization of an object of the HorarioWriter class.
     * All necessary initializations are done through other constructor overloads or by setting
     * default values for class variables.
     */
    private HorarioWriter() {

    }

    /**
     * Converts a List of Horario objects into a JSON string representation.
     *
     * @param horarios The List of Horario objects to be converted into a JSON
     *                 string.
     * @return A JSON string representing the List of Horario objects.
     * @throws JsonProcessingException If an error occurs while processing the JSON.
     */
    public static String listToJson(List<Horario> horarios) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(horarios);
    }

    /**
     * Converts a List of Horario objects into a CSV string representation.
     *
     * @param horarios The List of Horario objects to be converted into a CSV
     *                 string.
     * @return A CSV string representing the List of Horario objects.
     * @throws IOException                    If an I/O error occurs while writing
     *                                        to the StringWriter.
     * @throws CsvDataTypeMismatchException   If a field of an object in the list
     *                                        cannot be converted to the required
     *                                        CSV data type.
     * @throws CsvRequiredFieldEmptyException If a required field in an object of
     *                                        the list is empty.
     */

    public static String listToCsv(List<Horario> horarios)
            throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {

        StringWriter stringWriter = new StringWriter();

        try (CSVWriter csvWriter = new CSVWriter(stringWriter)) {
            csvWriter.writeNext(CSV_HEADER);

            ColumnPositionMappingStrategy<Horario> mappingStrategy = new ColumnPositionMappingStrategy<>();
            mappingStrategy.setType(Horario.class);

            StatefulBeanToCsv<Horario> beanToCsv = new StatefulBeanToCsvBuilder<Horario>(stringWriter)
                    .withMappingStrategy(mappingStrategy)
                    .withQuotechar(ICSVWriter.NO_QUOTE_CHARACTER)
                    .withSeparator(ICSVWriter.DEFAULT_SEPARATOR)
                    .build();

            beanToCsv.write(horarios);
        }

        return stringWriter.toString();
    }

}
