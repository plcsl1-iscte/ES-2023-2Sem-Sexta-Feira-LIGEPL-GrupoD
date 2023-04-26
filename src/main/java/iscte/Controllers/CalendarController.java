package iscte.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import iscte.timetable.models.Horario;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

@Controller
public class CalendarController {

    private List<Horario> horarios = null;
    private ArrayList<Horario> horariosSobrepostos = new ArrayList<Horario>();
    private ArrayList<Horario> horariosSobrelotacao = new ArrayList<Horario>();

    @GetMapping("/")
    public String displayCalendar(Model model, String filePath) {
        if (model != null && filePath != null) {
            HorarioReader reader = new HorarioReader(filePath);
            List<Horario> horarios = reader.read();
            model.addAttribute("horarios", horarios);
        }
        return "calendar";
    }


    @PostMapping("/handleFileUpload")
    public String handleFileUpload(HttpServletRequest request, RedirectAttributes redirectAttributes, Model model) {
        String path = request.getParameter("path");
        if (path == null)
            path = "";
        return displayCalendar(model, path);
    }

    @PostMapping("/handleURLUpload")
    public String handleWebCalUrlUpload(HttpServletRequest request, RedirectAttributes redirectAttributes, Model model) {
        String url = request.getParameter("url");
        if (url == null)
            url = "";
        return  displayCalendar(model, url);
    }

    @PostMapping("/download/csv")
    public ResponseEntity<byte[]> downloadCsvFile()
            throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {
        String csvContent = HorarioWriter.listToCsv(horarios);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "downloadHorario.csv");

        return new ResponseEntity<>(csvContent.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
    }

    @PostMapping("/download/json")
    public ResponseEntity<byte[]> downloadJsonFile() throws JsonProcessingException {
        String jsonContent = HorarioWriter.listToJson(horarios);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "downloadHorario.json");

        return new ResponseEntity<>(jsonContent.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
    }

    private void fileToList(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            HorarioReader reader = new HorarioReader(filePath);
            horarios = reader.read();
        } else {
            horarios = new ArrayList<>(); // Initialize an empty list if the file path is not valid
        }
    }

    private void addHorario(Horario newhorario) {
        horarios.add(newhorario);
    }

    private void removeHorario(Horario rmHorario) {
        for (Horario h : horarios) {
            if (h.toString().equals(rmHorario.toString())) {
                horarios.remove(rmHorario);
            }
        }
    }

    private void checkForOverlappingLessons() {
        for (int i = 0; i < horarios.size(); i++) {
            for (int j = i + 1; j < horarios.size(); j++) {
                Horario horario1 = horarios.get(i);
                Horario horario2 = horarios.get(j);
                if (horario1.getDataAula().equals(horario2.getDataAula())) {
                    if (horario1.getHoraInicio().compareTo(horario2.getHoraFim()) <= 0 &&
                            horario1.getHoraFim().compareTo(horario2.getHoraInicio()) >= 0) {
                        System.out.println("Aulas sobrepostas: ");
                        System.out.println(horario1.toString());
                        System.out.println(horario2.toString());
                    }
                }
            }
        }
    }

    public void overbooked() {
        for (Horario h : horarios) {
            int capacidadeSala = Integer.parseInt(h.getLotacaoSala());
            int numInscritos = Integer.parseInt(h.getInscritos());
            if (capacidadeSala < numInscritos) {
                horariosSobrelotacao.add(h);

                // System.out.println("a sala "+ h.getSala() + "está sobrelotada na cadeira de:
                // " + h.getUnidadeCurricular());
            }
        }
    }

}