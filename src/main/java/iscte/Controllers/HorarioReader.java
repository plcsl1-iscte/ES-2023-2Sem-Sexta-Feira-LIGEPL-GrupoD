package iscte.Controllers;

import iscte.timetable.models.Horario;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Content;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;

public class HorarioReader {

  private String path;

  public HorarioReader(String path) {
    this.path = path;
  }

  public static String checkStringType(String input) {
    File directory = new File(input);
    if (directory.isDirectory()) {
      return "directory";
    }

    Pattern webcalPattern = Pattern.compile(
      "^webcal://[\\w.-]+(/.*)?$",
      Pattern.CASE_INSENSITIVE
    );
    if (webcalPattern.matcher(input).matches()) {
      return "webcal";
    }

    return "unknown";
  }

  /**
   * @return
   * @throws Exception
   */
  public List<Horario> read() throws Exception {
    List<Horario> horarios = new ArrayList<>();

    try {
      System.out.println(path);
      BufferedReader reader;
      String stringType = checkStringType(path);
      if (stringType.equals("webcal")) {
        String httpUrl = path.replaceFirst("webcal://", "https://");
        URL url = new URL(httpUrl);
        URLConnection connection = url.openConnection();
        InputStream inputStream = connection.getInputStream();
        reader =
          new BufferedReader(
            new InputStreamReader(inputStream, StandardCharsets.UTF_8)
          );
      } else if (stringType.equals("directory")) {
        reader = new BufferedReader(new FileReader(path));
      } else {
        throw new Exception("The given url has " + stringType + " state");
      }

      CalendarBuilder builder = new CalendarBuilder();
      Calendar calendar = builder.build(reader);
      reader.close();

      for (
        Iterator<CalendarComponent> i = calendar.getComponents().iterator();
        i.hasNext();
      ) {
        Component component = i.next();
        if (component.getName().equals(Component.VEVENT)) {
          Property description = component.getProperty(Description.DESCRIPTION);
          if (description != null) {
            String[] descriptionLines = description.getValue().split("\n");
            Map<String, String> descriptionMap = new HashMap<>();

            for (String line : descriptionLines) {
              String[] parts = line.split(":", 2);
              if (parts.length == 2) {
                descriptionMap.put(parts[0].trim(), parts[1].trim());
              }
            }
            if (
              descriptionMap.containsKey("Begin") &&
              descriptionMap.containsKey("End")
            ) {
              Horario horario = new Horario();

              if (descriptionMap.containsKey("Unidade de execução")) {
                horario.setUnidadeCurricular(
                  descriptionMap.get("Unidade de execução")
                );
              }

              if (descriptionMap.containsKey("Execution course")) {
                horario.setCurso(descriptionMap.get("Execution course"));
              }

              if (descriptionMap.containsKey("Turno")) {
                horario.setTurno(descriptionMap.get("Turno"));
              }

              if (descriptionMap.containsKey("Shift")) {
                horario.setTurma(descriptionMap.get("Shift"));
              }

              DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm"
              );

              String beginStr = descriptionMap.get("Begin");
              if (beginStr != null) {
                LocalDateTime startTime = LocalDateTime.parse(
                  beginStr,
                  dateFormatter
                );
                horario.setStartTime(startTime);
              }

              String endStr = descriptionMap.get("End");
              if (endStr != null) {
                LocalDateTime endTime = LocalDateTime.parse(
                  endStr,
                  dateFormatter
                );
                horario.setEndTime(endTime);
              }

              if (component.getProperty(Property.LOCATION) != null) {
                horario.setSala(
                  component.getProperty(Property.LOCATION).getValue()
                );
              }

              horario.setDiaSemana("Indisponível"); // Not provided in the iCalendar data, you might need to calculate it from DTSTART
              horario.setInscritos("Indisponível"); // Not provided in the iCalendar data
              horario.setLotacaoSala("Indisponível"); // Not provided in the iCalendar data

              horarios.add(horario);
            }
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return horarios;
  }
}
