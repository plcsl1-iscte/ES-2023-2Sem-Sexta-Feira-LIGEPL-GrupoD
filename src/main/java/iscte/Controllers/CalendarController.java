package iscte.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import iscte.timetable.models.Horario;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

@Controller
public class CalendarController {

    @GetMapping("/")
    public String displayCalendar(Model model) {
        if (model != null) {
            String jsonFilePath = "src/main/java/iscte/timetable/files/horario-exemplo.csv";
            HorarioReader reader = new HorarioReader(jsonFilePath);
            List<Horario> horarios = reader.read();
            model.addAttribute("horarios", horarios);
        }
        return "calendar";
    }

    @PostMapping("/handleFileUpload")
    public String handleFileUpload(HttpServletRequest request, RedirectAttributes redirectAttributes, Model model)
            throws IOException, ServletException {
        // Get the file uploaded from the form data
        Part filePart = request.getPart("file");
        if (filePart != null) {
            String fileName = filePart.getSubmittedFileName();
            InputStream fileContent = filePart.getInputStream();
            String jsonFilePath = "src/main/java/iscte/timetable/files/horario-exemplo.csv";
            HorarioReader reader = new HorarioReader(jsonFilePath);
            List<Horario> horarios = reader.read();

            // Set a success message to be displayed in the HTML page
        }

        // Redirect back to the HTML page
        return displayCalendar(model);
    }
}