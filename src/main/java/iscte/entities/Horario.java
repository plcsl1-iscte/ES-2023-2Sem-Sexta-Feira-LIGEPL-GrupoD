package iscte.entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.opencsv.bean.CsvBindByPosition;
import lombok.*;


/**
 The Horario class represents a schedule of an academic course, containing information about the course,
 unit, shift, class, enrolled students, weekday, start time, end time, date, classroom, and classroom capacity.
 It also provides methods for setting the start and end times of the schedule.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class Horario {
    @CsvBindByPosition(position = 0)
    private String curso;
    @CsvBindByPosition(position = 1)
    private String unidadeCurricular;
    @CsvBindByPosition(position = 2)
    private String turno;
    @CsvBindByPosition(position = 3)
    private String turma;
    @CsvBindByPosition(position = 4)
    private String inscritos;
    @CsvBindByPosition(position = 5)
    private String diaSemana;
    @CsvBindByPosition(position = 6)
    private String horaInicio;
    @CsvBindByPosition(position = 7)
    private String horaFim;
    @CsvBindByPosition(position = 8)
    private String dataAula;
    @CsvBindByPosition(position = 9)
    private String sala;
    @CsvBindByPosition(position = 10)
    private String lotacaoSala;

    /**

     Sets the start time of the schedule, using a LocalDateTime object representing the start time.

     @param startTime the LocalDateTime object representing the start time of the schedule
     */
    public void setStartTime(LocalDateTime startTime) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        setDataAula(startTime.format(dateFormatter));
        setHoraInicio(startTime.format(timeFormatter));
    }

    /**

     Sets the end time of the schedule, using a LocalDateTime object representing the end time.
     @param endTime the LocalDateTime object representing the end time of the schedule
     */
    public void setEndTime(LocalDateTime endTime) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        setHoraFim(endTime.format(timeFormatter));
    }
}

