package iscte.utils;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
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

import static java.lang.System.*;

public class HorarioWriter {

    public HorarioWriter() {

    }

    private static final String[] CSV_HEADER = {
            "curso", "unidadeCurricular", "turno", "turma", "inscritosNoTurno",
            "diaDaSemana", "horaInicioAula", "horaFimAula", "dataAula", "salaAtribuidaAula", "lotacaoSala"
    };

    /**
     *
     * @param horarios
     * @return
     * @throws JsonProcessingException
     */
    public static String listToJson(List<Horario> horarios) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(horarios);
    }

    /**
     *
     * @param horarios
     * @return
     * @throws IOException
     * @throws CsvDataTypeMismatchException
     * @throws CsvRequiredFieldEmptyException
     */
    public static String listToCsv(List<Horario> horarios) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        out.println("Size of horarios: " + horarios.size());

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
