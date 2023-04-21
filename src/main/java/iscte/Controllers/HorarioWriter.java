package iscte.Controllers;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import iscte.timetable.models.Horario;

public class HorarioWriter {

    private static final String[] CSV_HEADER = {
            "curso", "unidadeCurricular", "turno", "turma", "inscritosNoTurno",
            "diaDaSemana", "horaInicioAula", "horaFimAula", "dataAula", "salaAtribuidaAula", "lotacaoSala"
    };

    public static String listToJson(List<Horario> horarios) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(horarios);
    }

    public static String listToCsv(List<Horario> horarios) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        System.out.println("Size of horarios: " + horarios.size());

        //remover o header
        horarios.remove(0);

        StringWriter stringWriter = new StringWriter();

        try (CSVWriter csvWriter = new CSVWriter(stringWriter)) {
            csvWriter.writeNext(CSV_HEADER);

            ColumnPositionMappingStrategy<Horario> mappingStrategy = new ColumnPositionMappingStrategy<>();
            mappingStrategy.setType(Horario.class);

            StatefulBeanToCsv<Horario> beanToCsv = new StatefulBeanToCsvBuilder<Horario>(stringWriter)
                    .withMappingStrategy(mappingStrategy)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .build();

            beanToCsv.write(horarios);
        }

        return stringWriter.toString();
    }

}
