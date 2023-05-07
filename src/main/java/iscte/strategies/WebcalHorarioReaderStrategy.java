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
     * Reads the input from a given Reader and returns a list of Horario objects
     * created from the data. The method assumes the input to be in the iCalendar
     * format and parses it using the CalendarBuilder.
     *
     * @param reader The Reader object containing the input data.
     * @return A List of Horario objects created from the input data.
     * @throws IOException     If an I/O error occurs while reading from the input.
     * @throws ParserException If the input data cannot be parsed correctly.
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
     * Creates a Horario object from a given Component representing a VEVENT.
     * The method extracts relevant information from the Component's properties
     * and uses it to populate a new Horario object.
     *
     * @param component The Component object representing a VEVENT to extract data
     *                  from.
     * @return A Horario object with the extracted data, or null if required
     *         properties are missing.
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
                .sala(component.getProperty(Property.LOCATION) != null
                        ? component.getProperty(Property.LOCATION).getValue()
                        : "")
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
     * Parses a description string into a map of key-value pairs. The method
     * assumes that each line in the description string contains a key-value pair,
     * separated by a colon. The resulting map contains the keys and values
     * extracted from the description string.
     *
     * @param description The description string to parse.
     * @return A Map<String, String> containing the key-value pairs extracted from
     *         the description string.
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
     * Parses a string representing a date and time into a LocalDateTime object.
     * The method assumes that the input string is in the format "yyyy-MM-dd HH:mm".
     *
     * @param dateTimeStr The date and time string to parse.
     * @return A LocalDateTime object representing the parsed date and time.
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.parse(dateTimeStr, dateFormatter);
    }

}
