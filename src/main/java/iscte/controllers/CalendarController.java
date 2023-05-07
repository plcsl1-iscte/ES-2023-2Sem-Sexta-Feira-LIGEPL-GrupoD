package iscte.controllers;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import iscte.entities.Horario;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import iscte.utils.HorarioReader;
import iscte.utils.HorarioWriter;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static java.lang.System.*;

@Controller
public class CalendarController {

    static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CalendarController.class);
    private List<Horario> horarios = new ArrayList<>();
    private List<Horario> filteredHorarios = new ArrayList<>();
    private Set<Pair<Horario, Horario>> horariosSobrepostos = new HashSet<>();
    private ArrayList<Horario> horariosSobrelotacao = new ArrayList<>();
    private List<Horario> horariosToDisplay = new ArrayList<>();
    private int overlappingPageNumber = 1;
    private int overbookedPageNumber = 1;
    private String currentUrlOrPath;

    @GetMapping("/calendar")
    public String displayCalendar(Model model, @RequestParam(name = "file", required = false) String filePath) throws Exception {
        long startTime = nanoTime();
        if (model != null && filePath != null) {
            HorarioReader reader = new HorarioReader(filePath);

            horarios = reader.read();

            Set<String> uniqueUCs = horarios
                    .stream()
                    .map(Horario::getUnidadeCurricular)
                    .collect(Collectors.toSet());

            if (filteredHorarios.size() > 0) {
                horariosToDisplay = filteredHorarios;
            } else {
                horariosToDisplay = horarios;
            }

            checkForOverlappingLessons(model);
            checkOverbookedEvents(model);

            model.addAttribute("horarios", horariosToDisplay);
            model.addAttribute("ucs", uniqueUCs);


        }
        long endTime = nanoTime();
        long duration = (endTime - startTime) / 1000000; // Convert to milliseconds
        out.println("File reading duration: " + duration + " ms");
        return "calendar";
    }

    @PostMapping("/handleFileUpload")
    public String handleFileUpload(
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Model model
    ) throws Exception {
        String path = request.getParameter("path");
        currentUrlOrPath = path;
        return displayCalendar(model, currentUrlOrPath);
    }

    @PostMapping("/selectUCs")
    public String processSelectedUCs(
            @RequestParam("ucs") List<String> selectedUCs,
            Model model
    ) throws Exception {
        // Print the selected UCs to the console
        out.println("Selected UCs: " + selectedUCs);

        filteredHorarios.clear();

        for (Horario h : horarios) {
            for (String uc : selectedUCs) {
                if (h.getUnidadeCurricular().equals(uc)) {
                    filteredHorarios.add(h);
                }
            }
        }

        return displayCalendar(model, currentUrlOrPath);
    }

    @PostMapping("/handleURLUpload")
    public String handleWebCalUrlUpload(
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Model model
    ) throws Exception {
        String url = request.getParameter("CalendarURL"); // Change this line
        currentUrlOrPath = url;
        return displayCalendar(model, currentUrlOrPath);
    }

    @PostMapping("/download/csv")
    public ResponseEntity<byte[]> downloadCsvFile()
            throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {
        String csvContent = HorarioWriter.listToCsv(horariosToDisplay);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "downloadHorario.csv");

        return new ResponseEntity<>(
                csvContent.getBytes(StandardCharsets.UTF_8),
                headers,
                HttpStatus.OK
        );
    }

    @PostMapping("/download/json")
    public ResponseEntity<byte[]> downloadJsonFile()
            throws JsonProcessingException {
        String jsonContent = HorarioWriter.listToJson(horariosToDisplay);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "downloadHorario.json");

        return new ResponseEntity<>(
                jsonContent.getBytes(StandardCharsets.UTF_8),
                headers,
                HttpStatus.OK
        );
    }

    private void checkForOverlappingLessons(Model model) {
        long startTime = nanoTime();

        horariosSobrepostos.clear();
        List<Horario> checkingOverlap = horariosToDisplay;

        // Group Horario objects by date
        Map<String, List<Horario>> horariosByDate = checkingOverlap.stream()
                .filter(horario -> horario.getDataAula() != null)
                .collect(Collectors.groupingBy(Horario::getDataAula));


        horariosSobrepostos = horariosByDate.values().stream()
                .parallel()
                .flatMap(horariosWithSameDate -> {
                    Set<Pair<Horario, Horario>> overlaps = new HashSet<>();
                    // Sort the horarios by start time
                    List<Horario> sortedHorarios = horariosWithSameDate.stream()
                            .sorted(Comparator.comparing(Horario::getHoraInicio))
                            .collect(Collectors.toList());

                    for (int i = 0; i < sortedHorarios.size(); i++) {
                        Horario horario1 = sortedHorarios.get(i);
                        for (int j = i + 1; j < sortedHorarios.size(); j++) {
                            Horario horario2 = sortedHorarios.get(j);
                            if (checkOverlap(horario1, horario2)) {
                                overlaps.add(Pair.of(horario1, horario2));
                            } else {
                                // The horarios are sorted by start time, so we can stop the inner loop
                                // as soon as we find a non-overlapping horario
                                break;
                            }
                        }
                    }
                    return overlaps.stream();
                })
                .collect(Collectors.toSet());

        out.println("Existem " + horariosSobrepostos.size() + " horarios sobrepostos.");

        long endTime = nanoTime();
        long duration = (endTime - startTime) / 1000000; // Convert to milliseconds
        out.println("checkForOverlappingLessons reading duration: " + duration + " ms");

        displayOverlappingLessonsMessage(model);
    }

    private boolean checkOverlap(Horario horario1, Horario horario2) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime startTime1 = LocalTime.parse(horario1.getHoraInicio(), timeFormatter);
        LocalTime endTime1 = LocalTime.parse(horario1.getHoraFim(), timeFormatter);
        LocalTime startTime2 = LocalTime.parse(horario2.getHoraInicio(), timeFormatter);
        LocalTime endTime2 = LocalTime.parse(horario2.getHoraFim(), timeFormatter);

        Duration duration1 = Duration.between(startTime1, endTime1);
        Duration duration2 = Duration.between(startTime2, endTime2);

        LocalTime latestStartTime = startTime1.isBefore(startTime2) ? startTime2 : startTime1;
        LocalTime earliestEndTime = endTime1.isBefore(endTime2) ? endTime1 : endTime2;

        Duration overlapDuration = Duration.between(latestStartTime, earliestEndTime);

        return overlapDuration.compareTo(Duration.ZERO) > 0 && overlapDuration.compareTo(duration1) <= 0 && overlapDuration.compareTo(duration2) <= 0;
    }


    private void displayOverlappingLessonsMessage(Model model) {
        List<String> messages = new ArrayList<>();
        for (Pair<Horario, Horario> lessonPair : horariosSobrepostos) {
            Horario h1 = lessonPair.getLeft();
            Horario h2 = lessonPair.getRight();
            StringBuilder message = new StringBuilder();

            message
                    .append(h1.getUnidadeCurricular())
                    .append(" ")
                    .append(h1.getDataAula())
                    .append(" (")
                    .append(h1.getHoraInicio())
                    .append(" - ")
                    .append(h1.getHoraFim())
                    .append(") [ X ] ")
                    .append(h2.getUnidadeCurricular())
                    .append(" ")
                    .append(h2.getDataAula())
                    .append(" (")
                    .append(h2.getHoraInicio())
                    .append(" - ")
                    .append(h2.getHoraFim())
                    .append(")\n");

            messages.add(message.toString());
        }
        List<String> paginatedMessages = paginateMessages(
                messages,
                overlappingPageNumber
        );
        model.addAttribute("overlappingLessonsMessages", paginatedMessages);
        model.addAttribute(
                "disableOverlappingPrevPage",
                overlappingPageNumber == 0
        );
        model.addAttribute(
                "disableOverlappingNextPage",
                paginatedMessages.size() < 10
        );
        int overlappingTotalPages = (int) Math.ceil((double) messages.size() / 10);
        model.addAttribute("overlappingTotalPages", overlappingTotalPages);
    }

    private void checkOverbookedEvents(Model model) {
        long startTime = nanoTime();
        horariosSobrelotacao.clear();
        for (Horario h : horariosToDisplay) {
            try {
                int inscritos = Integer.parseInt(h.getInscritos());
                int lotacaoSala = Integer.parseInt(h.getLotacaoSala());
                if (inscritos > lotacaoSala) {
                    horariosSobrelotacao.add(h);
                }
            } catch (NumberFormatException e) {
                LOGGER.error("Invalid 'Inscritos' or 'LotacaoSala' value for horario: " + h.toString(), e);
            }
        }
        long endTime = nanoTime();
        long duration = (endTime - startTime) / 1000000; // Convert to milliseconds
        out.println("checkOverbookedEvents reading duration: " + duration + " ms");
        displayOverbookedLessonsMessage(model);
    }


    private void displayOverbookedLessonsMessage(Model model) {
        List<String> messages = new ArrayList<>();
        for (Horario h : horariosSobrelotacao) {
            StringBuilder message = new StringBuilder();

            message
                    .append("Existe sobrelotação na Sala ")
                    .append(h.getSala())
                    .append("relativamente à UC ")
                    .append(h.getUnidadeCurricular())
                    .append(" ,no dia ")
                    .append(h.getDataAula())
                    .append(" ,entre as ")
                    .append(h.getHoraInicio())
                    .append(" e as ")
                    .append(h.getHoraFim())
                    .append(" ,");

            messages.add(message.toString());
        }
        List<String> paginatedMessages = paginateMessages(
                messages,
                overbookedPageNumber
        );
        model.addAttribute("overbookedLessonsMessages", paginatedMessages);
        model.addAttribute("disableOverbookedPrevPage", overbookedPageNumber == 0);
        int overbookedTotalPages = (int) Math.ceil((double) messages.size() / 10);
        model.addAttribute("overbookedTotalPages", overbookedTotalPages);
        model.addAttribute(
                "disableOverbookedNextPage",
                paginatedMessages.size() < 10
        );
    }

    private List<String> paginateMessages(List<String> messages, int pageNumber) {
        int pageSize = 10;
        int fromIndex = pageNumber * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, messages.size());

        // Check if there are messages in the specified range
        if (fromIndex >= messages.size() || fromIndex > toIndex) {
            return new ArrayList<>();
        }

        return messages.subList(fromIndex, toIndex);
    }

    @PostMapping("/overlappingPrev")
    public String handleOverlappingPrev(
            Model model,
            @RequestParam("page") int page
    ) throws Exception {
        overlappingPageNumber = Math.max(page, 0);
        return displayCalendar(model, currentUrlOrPath);
    }


    @PostMapping("/overlappingNext")
    public String handleOverlappingNext(
            Model model,
            @RequestParam("page") int page,
            @ModelAttribute("overlappingLessonsMessages") List<String> overlappingLessonsMessages
    ) throws Exception {
        int totalPages = (int) Math.ceil(overlappingLessonsMessages.size() / 10.0);
        overlappingPageNumber = Math.min(page, totalPages - 1);
        return displayCalendar(model, currentUrlOrPath);
    }

    @PostMapping("/overbookedNext")
    public String handleOverbookedNext(
            Model model,
            @RequestParam("page") int page,
            @ModelAttribute("overbookedLessonsMessages") List<String> overbookedLessonsMessages
    ) throws Exception {
        int totalPages = (int) Math.ceil(overbookedLessonsMessages.size() / 10.0);
        overbookedPageNumber = Math.min(page, totalPages - 1);
        return displayCalendar(model, currentUrlOrPath);
    }

    @PostMapping("/overbookedPrev")
    public String handleOverbookedPrev(
            Model model,
            @RequestParam("page") int page
    ) throws Exception {
        overbookedPageNumber = Math.max(page, 0);
        return displayCalendar(model, currentUrlOrPath);
    }

}
