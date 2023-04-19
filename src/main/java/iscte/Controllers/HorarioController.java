package iscte.Controllers;

import iscte.timetable.models.Horario;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HorarioController {

    private final String REPO = "src/main/java/iscte/timetable/files/";

    @GetMapping("/horario")
    public String horario(Model model) {
        String jsonFilePath = REPO + "horario-exemplo.csv";
        HorarioReader reader = new HorarioReader(jsonFilePath);
        List<Horario> horarios = reader.read();
        model.addAttribute("horarios", horarios);
        return "horario";
    }
}
