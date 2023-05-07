package iscte.strategies;

import iscte.entities.Horario;
import iscte.interfaces.HorarioReaderStrategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

public class CsvHorarioReaderStrategy implements HorarioReaderStrategy {

    @Override
    public List<Horario> read(Reader reader) throws IOException {
        try (BufferedReader br = new BufferedReader(reader)) {
            br.skip(2);
            return br.lines()
                    .map(line -> line.split(";"))
                    .filter(tokens -> tokens.length == 11)
                    .map(this::parseHorarioFromTokens)
                    .collect(Collectors.toList());
        }
    }

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

    private String removeQuotes(String s) {
        return s.replace("\"", "");
    }
}
