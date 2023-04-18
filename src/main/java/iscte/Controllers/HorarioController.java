package iscte.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HorarioController {

    @GetMapping("/horario")
    public String horario() {
        return "horario";
    }

}
