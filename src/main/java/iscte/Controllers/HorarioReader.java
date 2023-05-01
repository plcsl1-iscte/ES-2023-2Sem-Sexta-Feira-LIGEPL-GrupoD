package iscte.Controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import iscte.timetable.models.Horario;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.Description;
import java.lang.reflect.Type;

public class HorarioReader {

  private String path;

  public HorarioReader(String path) {
    this.path = path;
  }

  public static String checkStringType(String input) {
    File directory = new File(input);

    if (input == null || input.equals("")) {
      System.out.println("Is Null!");
      return "null";
    }

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
      BufferedReader reader = null;
      String stringType = checkStringType(path);
      if (stringType.equals("webcal")) {
        System.out.println("IT IS A WEBCAL!");
        String httpUrl = path.replaceFirst("webcal://", "https://");
        URL url = new URL(httpUrl);
        URLConnection connection = url.openConnection();
        InputStream inputStream = connection.getInputStream();
        reader =
          new BufferedReader(
            new InputStreamReader(inputStream, StandardCharsets.UTF_8)
          );

        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = builder.build(reader);
        reader.close();

        for (
          Iterator<CalendarComponent> i = calendar.getComponents().iterator();
          i.hasNext();
        ) {
          Component component = i.next();
          if (component.getName().equals(Component.VEVENT)) {
            Property description = component.getProperty(
              Description.DESCRIPTION
            );
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
      } else if (stringType.equals("null")) {
        System.out.println("Is Null! Returning empty list..");
        return horarios;
      } else {
        System.out.println("IT IS A DIRECTORY!");
        CharsetDetector detector = new CharsetDetector();
        byte[] fileData = Files.readAllBytes(Paths.get(path));
        detector.setText(fileData);
        CharsetMatch match = detector.detect();
        Charset detectedCharset = Charset.forName(match.getName());
    
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileData), detectedCharset))) {          String line;
          if (path.endsWith(".csv")) {
              while ((line = br.readLine()) != null) {
                  String[] tokens = line.split(",");
                  if (tokens.length != 11) {
                      System.err.println("Invalid input line: " + line);
                      continue;
                  }
                  Horario horario = new Horario();
                  horario.setCurso(tokens[0] != null ? tokens[0].replace("\"", "") : "");
                  horario.setUnidadeCurricular(tokens[1] != null ? tokens[1].replace("\"", "") : "");
                  horario.setTurno(tokens[2] != null ? tokens[2].replace("\"", "") : "");
                  horario.setTurma(tokens[3] != null ? tokens[3].replace("\"", "") : "");
                  horario.setInscritos(tokens[4] != null ? tokens[4].replace("\"", "") : "");
                  horario.setDiaSemana(tokens[5] != null ? tokens[5].replace("\"", "") : "");
                  horario.setHoraInicio(tokens[6] != null ? tokens[6].replace("\"", "") : "");
                  horario.setHoraFim(tokens[7] != null ? tokens[7].replace("\"", "") : "");
                  horario.setDataAula(tokens[8] != null ? tokens[8].replace("\"", "") : "");
                  horario.setSala(tokens[9] != null ? tokens[9].replace("\"", "") : "");
                  horario.setLotacaoSala(tokens[10] != null ? tokens[10].replace("\"", "") : "");
                  horarios.add(horario);
              }
          } else if (path.endsWith(".json")) {
              Gson gson = new Gson();
              Type type = new TypeToken<List<Horario>>() {}.getType();
              List<Horario> jsonHorarios = gson.fromJson(br, type);
              horarios.addAll(jsonHorarios);
          } else {
              System.err.println("Unsupported file type");
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
      
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return horarios;
  }
}