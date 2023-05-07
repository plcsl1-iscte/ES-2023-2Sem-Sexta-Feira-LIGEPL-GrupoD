package iscte.strategies;

import iscte.entities.Horario;
import iscte.interfaces.HorarioReaderStrategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

public class CsvHorarioReaderStrategy implements HorarioReaderStrategy {
    /**
     * Reads a list of Horario objects from a CSV input stream.
     *
     * @param reader the reader object for the CSV input stream
     * @return the list of Horario objects read from the input stream
     * @throws IOException if an I/O error occurs while reading from the input
     *                     stream
     */
    @Override
    public List<Horario> read(Reader reader) throws IOException {
        try (BufferedReader br = new BufferedReader(reader)) {
            return br.lines()
                    .map(line -> line.split(";"))
                    .filter(tokens -> tokens.length == 11)
                    .map(this::parseHorarioFromTokens)
                    .collect(Collectors.toList());
        }
    }

    /**
     * 
     * Parses a Horario object from an array of tokens.
     * 
     * @param tokens an array of string tokens representing a Horario object
     * @return the parsed Horario object
     */
    private Horario parseHorarioFromTokens(String[] tokens) {
        return Horario.builder()
                .curso(removeQuotes(tokens[0]))
                .unidadeCurricular(removeQuotes(tokens[1]))
                .turno(removeQuotes(tokens[2]))
                .turma(removeQuotes(tokens[3]))
                .inscritos(removeQuotes(tokens[4]))
                .diaSemana(removeQuotes(tokens[5]))
                .horaInicio(removeQuotes(tokens[6]))
                .horaFim(removeQuotes(tokens[7]))
                .dataAula(removeQuotes(tokens[8]))
                .sala(removeQuotes(tokens[9]))
                .lotacaoSala(removeQuotes(tokens[10]))
                .build();
    }

    /**
     * 
     * Removes any double quotes from a given string.
     * 
     * @param s the string to remove quotes from
     * @return the string without quotes
     */
    private String removeQuotes(String s) {
        return s.replace("\"", "");
    }
}
