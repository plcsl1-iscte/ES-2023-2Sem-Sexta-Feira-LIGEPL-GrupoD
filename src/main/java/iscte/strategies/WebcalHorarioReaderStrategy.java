package iscte.strategies;

import iscte.entities.Horario;
import iscte.interfaces.HorarioReaderStrategy;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;

import java.io.IOException;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WebcalHorarioReaderStrategy implements HorarioReaderStrategy {

    private static final String INDISPONIVEL = "Indisponível";

    /**
     *
     * @param reader
     * @return
     * @throws IOException
     * @throws ParserException
     */
    public List<Horario> read(Reader reader) throws IOException, ParserException {
        List<Horario> horarios = new ArrayList<>();
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = builder.build(reader);
        reader.close();

        for (Iterator<CalendarComponent> i = calendar.getComponents().iterator(); i.hasNext();) {
            Component component = i.next();
            if (component.getName().equals(Component.VEVENT)) {
                Horario horario = createHorarioFromComponent(component);
                if (horario != null) {
                    horarios.add(horario);
                }
            }
        }

        return horarios;
    }

    /**
     *
     * @param component
     * @return
     */
    private Horario createHorarioFromComponent(Component component) {
        Property description = component.getProperty(Property.DESCRIPTION);
        if (description == null) {
            return null;
        }

        Map<String, String> descriptionMap = parseDescriptionMap(description.getValue());
        if (!descriptionMap.containsKey("Begin") || !descriptionMap.containsKey("End")) {
            return null;
        }

        Horario horario = Horario.builder()
                .unidadeCurricular(descriptionMap.getOrDefault("Unidade de execução", ""))
                .curso(descriptionMap.getOrDefault("Execution course", ""))
                .turno(descriptionMap.getOrDefault("Turno", ""))
                .turma(descriptionMap.getOrDefault("Shift", ""))
                .sala(component.getProperty(Property.LOCATION) != null ? component.getProperty(Property.LOCATION).getValue() : "")
                .inscritos(INDISPONIVEL)
                .lotacaoSala(INDISPONIVEL)
                .build();

        LocalDateTime startDateTime = parseDateTime(descriptionMap.get("Begin"));
        LocalDateTime endDateTime = parseDateTime(descriptionMap.get("End"));
        horario.setStartTime(startDateTime);
        horario.setEndTime(endDateTime);
        horario.setDiaSemana(startDateTime.getDayOfWeek().name());

        return horario;
    }

    /**
     *
     * @param description
     * @return
     */
    private Map<String, String> parseDescriptionMap(String description) {
        String[] descriptionLines = description.split("\n");
        Map<String, String> descriptionMap = new HashMap<>();
        for (String line : descriptionLines) {
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                descriptionMap.put(parts[0].trim(), parts[1].trim());
            }
        }
        return descriptionMap;
    }

    /**
     *
     * @param dateTimeStr
     * @return
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.parse(dateTimeStr, dateFormatter);
    }

}
