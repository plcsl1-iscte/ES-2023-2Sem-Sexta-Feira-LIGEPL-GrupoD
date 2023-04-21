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

    @GetMapping("/")
    public String displayCalendar(Model model, String filePath) {
        if (model != null) {
            fileToList(filePath);
            model.addAttribute("horarios", horarios);
        }
        return "calendar";
    }

    @PostMapping("/handleFileUpload")
    public String handleFileUpload(HttpServletRequest request, RedirectAttributes redirectAttributes, Model model)
            throws IOException, ServletException {
        Part filePart = request.getPart("file");
        String path = "";

        if (filePart != null) {
            path = request.getParameter("path");
        }

        return displayCalendar(model, path);
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

}