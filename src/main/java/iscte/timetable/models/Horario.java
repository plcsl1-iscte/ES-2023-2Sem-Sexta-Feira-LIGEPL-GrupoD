package iscte.timetable.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.opencsv.bean.CsvBindByPosition;

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

    public Horario() {
        this.curso = "MISSING VALUE";
        this.unidadeCurricular = "MISSING VALUE";
        this.turno = "MISSING VALUE";
        this.turma = "MISSING VALUE";
        this.inscritos = "MISSING VALUE";
        this.diaSemana = "MISSING VALUE";
        this.horaInicio = "MISSING VALUE";
        this.horaFim = "MISSING VALUE";
        this.dataAula = "MISSING VALUE";
        this.sala = "MISSING VALUE";
        this.lotacaoSala = "MISSING VALUE";
    }

    public Horario(String curso, String unidadeCurricular, String turno, String turma, String inscritos,
                   String diaSemana, String horaInicio, String horaFim, String dataAula, String sala, String lotacaoSala) {
        this.curso = curso;
        this.unidadeCurricular = unidadeCurricular;
        this.turno = turno;
        this.turma = turma;
        this.inscritos = inscritos;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.dataAula = dataAula;
        this.sala = sala;
        this.lotacaoSala = lotacaoSala;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String getUnidadeCurricular() {
        return unidadeCurricular;
    }

    public void setUnidadeCurricular(String unidadeCurricular) {
        this.unidadeCurricular = unidadeCurricular;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public String getTurma() {
        return turma;
    }

    public void setTurma(String turma) {
        this.turma = turma;
    }

    public String getInscritos() {
        return inscritos;
    }

    public void setInscritos(String inscritos) {
        this.inscritos = inscritos;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(String horaFim) {
        this.horaFim = horaFim;
    }

    public String getDataAula() {
        return dataAula;
    }

    public void setDataAula(String dataAula) {
        this.dataAula = dataAula;
    }

    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public String getLotacaoSala() {
        return lotacaoSala;
    }

    public void setLotacaoSala(String lotacaoSala) {
        this.lotacaoSala = lotacaoSala;
    }

    public void setStartTime(LocalDateTime startTime) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        setDataAula(startTime.format(dateFormatter));
        setHoraInicio(startTime.format(timeFormatter));
    }

    public void setEndTime(LocalDateTime endTime) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        setHoraFim(endTime.format(timeFormatter));
    }

    @Override
    public String toString() {
        return "Horario [curso=" + curso + ", unidadeCurricular=" + unidadeCurricular + ", turno=" + turno + ", turma="
                + turma + ", inscritos=" + inscritos + ", diaSemana=" + diaSemana + ", horaInicio=" + horaInicio
                + ", horaFim=" + horaFim + ", dataAula=" + dataAula + ", sala=" + sala + ", lotacaoSala=" + lotacaoSala
                + "]";
    }

    

}
