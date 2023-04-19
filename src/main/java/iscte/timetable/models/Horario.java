package iscte.timetable.models;

public class Horario {
    private String curso;
    private String unidadeCurricular;
    private String turno;
    private String turma;
    private String inscritos;
    private String diaSemana;
    private String horaInicio;
    private String horaFim;
    private String dataAula;
    private String sala;
    private String lotacaoSala;

    public Horario() {
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

}
