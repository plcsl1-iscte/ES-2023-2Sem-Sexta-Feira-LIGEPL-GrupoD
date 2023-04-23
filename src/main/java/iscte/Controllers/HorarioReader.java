package iscte.Controllers;

import iscte.timetable.models.Horario;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
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
            } else {
                System.err.println("Unsupported file type");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return horarios;
    }

}