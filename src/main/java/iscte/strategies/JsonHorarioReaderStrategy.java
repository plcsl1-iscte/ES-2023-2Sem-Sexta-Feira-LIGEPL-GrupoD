package iscte.strategies;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import iscte.entities.Horario;
import iscte.interfaces.HorarioReaderStrategy;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class JsonHorarioReaderStrategy implements HorarioReaderStrategy {

    @Override
    public List<Horario> read(Reader reader) throws IOException {
        Gson gson = new Gson();
        Horario[] horarios = gson.fromJson(reader, Horario[].class);
        return Arrays.asList(horarios);
    }
}


