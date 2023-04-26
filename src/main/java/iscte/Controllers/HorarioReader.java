package iscte.Controllers;

import iscte.timetable.models.Horario;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class HorarioReader {

    private String path;

    public HorarioReader(String path) {
        this.path = path;
    }

    public List<Horario> read() {
        List<Horario> horarios = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            if (path.endsWith(".csv")) {
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.split(",");
                    if (tokens.length != 11) {
                        System.err.println("Invalid input line: " + line);
                        continue;
                    }
                    Horario horario = new Horario();
                    horario.setCurso(tokens[0] != null ? tokens[0] : "");
                    horario.setUnidadeCurricular(tokens[1] != null ? tokens[1] : "");
                    horario.setTurno(tokens[2] != null ? tokens[2] : "");
                    horario.setTurma(tokens[3] != null ? tokens[3] : "");
                    horario.setInscritos(tokens[4] != null ? tokens[4] : "");
                    horario.setDiaSemana(tokens[5] != null ? tokens[5] : "");
                    horario.setHoraInicio(tokens[6] != null ? tokens[6] : "");
                    horario.setHoraFim(tokens[7] != null ? tokens[7] : "");
                    horario.setDataAula(tokens[8] != null ? tokens[8] : "");
                    horario.setSala(tokens[9] != null ? tokens[9] : "");
                    horario.setLotacaoSala(tokens[10] != null ? tokens[10] : "");
                    horarios.add(horario);
                }
            } else if (path.endsWith(".json")) {
                Gson gson = new Gson();
                Type type = new TypeToken<List<Horario>>() {
                }.getType();
                List<Horario> jsonHorarios = gson.fromJson(br, type);
                horarios.addAll(jsonHorarios);

            } else if (path.startsWith("https://")) {
                URL url = new URL(path);
                URLConnection connection = url.openConnection();
                InputStream inputStream = connection.getInputStream();
                CalendarBuilder builder = new CalendarBuilder();
                net.fortuna.ical4j.model.Calendar calendar = builder.build(inputStream);
                
                for (Object o : calendar.getComponents(Component.VEVENT)) {
                    if( o instanceof VEvent) {
                        VEvent vEvent = (VEvent) o;
                        Horario horario = new Horario();
                        System.out.println(  vEvent.getSummary().getValue());
                        horario.setCurso(vEvent.getClass().getName());
                        horario.setHoraInicio(vEvent.getStartDate().toString());
                        horario.setHoraFim(vEvent.getEndDate().toString());
                        horario.setDataAula(vEvent.getDateStamp().toString());
                        horarios.add(horario);
                    }
                    
                }
        





                

            } else {
                System.err.println("Unsupported file type");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return horarios;
    }
   
}