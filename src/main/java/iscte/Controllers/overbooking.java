package iscte.Controllers;

import java.util.ArrayList;

import iscte.timetable.models.Horario;
import scala.collection.immutable.List;

public class overbooking {
    private ArrayList<Horario> horarios = new ArrayList<Horario>();
    private ArrayList<Horario> horariosSobrepostos = new ArrayList<Horario>();
    private ArrayList<Horario> horariosSobrelotacao = new ArrayList<Horario>();
    public void sobreposicaoHorarios(Horario a) {
        for (Horario h : horarios) {
            if (h.getHoraInicio().equals(a.getHoraInicio()) || h.getHoraFim().equals(a.getHoraFim())) {
                System.out.println("Horarios sobrepostos");
            }
        }
    }

    public void overbooked() {
        for (Horario h : horarios) {
            int capacidadeSala = Integer.parseInt(h.getLotacaoSala());
            int numInscritos = Integer.parseInt(h.getInscritos());
            if (capacidadeSala < numInscritos) {
                horariosSobrelotacao.add(h);
                System.out.println("a sala "+ h.getSala() + "está sobrelotada na cadeira de: " + h.getUnidadeCurricular());
            }
        }
    }

}
