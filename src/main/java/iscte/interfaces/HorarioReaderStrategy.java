package iscte.interfaces;

import com.opencsv.exceptions.CsvValidationException;
import iscte.entities.Horario;
import net.fortuna.ical4j.data.ParserException;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

public interface HorarioReaderStrategy {
    /**
     * 
     * Reads a list of Horario objects from the provided Reader, which can be in any
     * supported format (CSV, JSON, etc).
     * 
     * @param reader the Reader object to read from
     * @return a List of Horario objects
     * @throws IOException            if there is an error reading from the Reader
     * @throws CsvValidationException if there is an error parsing the CSV data
     * @throws ParserException        if there is an error parsing the JSON data
     */
    List<Horario> read(Reader reader) throws IOException, CsvValidationException, ParserException;
}
