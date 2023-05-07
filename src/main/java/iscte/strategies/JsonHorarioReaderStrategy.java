package iscte.strategies;

import com.google.gson.Gson;
import iscte.entities.Horario;
import iscte.interfaces.HorarioReaderStrategy;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

public class JsonHorarioReaderStrategy implements HorarioReaderStrategy {
    /**
     * @param reader
     * @return
     * @throws IOException
     */
    @Override
    public List<Horario> read(Reader reader) throws IOException {
        Gson gson = new Gson();
        Horario[] horarios = gson.fromJson(reader, Horario[].class);
        return Arrays.asList(horarios);
    }
}


