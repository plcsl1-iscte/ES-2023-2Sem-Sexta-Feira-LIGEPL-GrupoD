package iscte.strategies;

import com.google.gson.Gson;
import iscte.entities.Horario;
import iscte.interfaces.HorarioReaderStrategy;

import java.io.Reader;
import java.util.Arrays;
import java.util.List;

public class JsonHorarioReaderStrategy implements HorarioReaderStrategy {
    /**
     * 
     * Reads Horario objects from a JSON file using the Gson library.
     * 
     * @param reader the reader for the JSON file
     * @return a list of Horario objects parsed from the JSON file
     */
    @Override
    public List<Horario> read(Reader reader) {
        Gson gson = new Gson();
        Horario[] horarios = gson.fromJson(reader, Horario[].class);
        return Arrays.asList(horarios);
    }
}
