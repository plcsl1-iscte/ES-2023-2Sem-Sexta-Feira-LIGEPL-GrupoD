package iscte.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

@Controller
public class CalendarController {

    @GetMapping("/")
    public String displayCalendar() {
        return "calendar";
    }

    @PostMapping("/handleFileUpload")
    public String handleFileUpload(HttpServletRequest request, RedirectAttributes redirectAttributes)
            throws IOException, ServletException {
        // Get the file uploaded from the form data
        Part filePart = request.getPart("file");
        if (filePart != null) {
            String fileName = filePart.getSubmittedFileName();
            InputStream fileContent = filePart.getInputStream();

            // Handle the file upload here
            // ...

            // Set a success message to be displayed in the HTML page
            redirectAttributes.addFlashAttribute("message", "File uploaded successfully!");
        }

        // Redirect back to the HTML page
        return "redirect:/";
    }

}