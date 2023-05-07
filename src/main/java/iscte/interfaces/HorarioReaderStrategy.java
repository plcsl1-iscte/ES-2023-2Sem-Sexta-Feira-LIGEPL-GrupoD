package iscte.interfaces;

import com.opencsv.exceptions.CsvValidationException;
import iscte.entities.Horario;
import net.fortuna.ical4j.data.ParserException;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

public interface HorarioReaderStrategy {
    List<Horario> read(Reader reader) throws IOException, CsvValidationException, ParserException;
}