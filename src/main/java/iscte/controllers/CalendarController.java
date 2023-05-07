package iscte.controllers;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import iscte.entities.Horario;
import iscte.utils.HorarioReader;
import iscte.utils.HorarioWriter;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Controller
public class CalendarController {
    static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CalendarController.class);
    private final List<Horario> filteredHorarios = new ArrayList<>();
    private final ArrayList<Horario> horariosSobrelotacao = new ArrayList<>();
    private List<Horario> horarios = new ArrayList<>();
    private Set<Pair<Horario, Horario>> horariosSobrepostos = new HashSet<>();
    private List<Horario> horariosToDisplay = new ArrayList<>();
    private int overlappingPageNumber = 1;
    private int overbookedPageNumber = 1;
    private String currentUrlOrPath;

    /**
     * Displays the calendar view with the course schedules from the input file.
     *
     * @param model    the Spring model used to pass data to the view
     * @param filePath the path of the input file to read the course schedules from
     * @return the name of the calendar view template
     * @throws Exception if an error occurs while reading or processing the course
     *                   schedules
     */
    @GetMapping("/calendar")
    public String displayCalendar(Model model, @RequestParam(name = "file", required = false) String filePath)
            throws Exception {
        if (model != null && filePath != null) {
            HorarioReader reader = new HorarioReader(filePath);

            horarios = reader.read();

            Set<String> uniqueUCs = horarios.stream().map(Horario::getUnidadeCurricular).collect(Collectors.toSet());

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
        return "calendar";
    }

    /**
     * Handles the uploaded course schedule input file and displays the calendar
     * view.
     *
     * @param request            the HTTP servlet request containing the uploaded
     *                           file
     * @param redirectAttributes the redirect attributes used to pass data to the
     *                           next request
     * @param model              the Spring model used to pass data to the view
     * @return the name of the calendar view template
     * @throws Exception if an error occurs while reading or processing the course
     *                   schedules
     */
    @PostMapping("/handleFileUpload")
    public String handleFileUpload(HttpServletRequest request, RedirectAttributes redirectAttributes, Model model)
            throws Exception {
        String path = request.getParameter("path");
        currentUrlOrPath = path;
        return displayCalendar(model, currentUrlOrPath);
    }

    /**
     * Filters the course schedules to display in the calendar view based on the
     * selected courses.
     *
     * @param selectedUCs the list of selected courses to filter by
     * @param model       the Spring model used to pass data to the view
     * @return the name of the calendar view template
     * @throws Exception if an error occurs while reading or processing the course
     *                   schedules
     */
    @PostMapping("/selectUCs")
    public String processSelectedUCs(@RequestParam("ucs") List<String> selectedUCs, Model model) throws Exception {
        // Print the selected UCs to the console

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

    /**
     * 
     * Handles the POST request to upload a webcal URL, sets the current URL or
     * path,
     * and returns the updated calendar view.
     * 
     * @param request            the HttpServletRequest containing the uploaded
     *                           webcal URL
     * @param redirectAttributes the RedirectAttributes to add attributes to the
     *                           redirect URL
     * @param model              the Model to add attributes to the view
     * @return the updated calendar view
     * @throws Exception if an error occurs while displaying the calendar view
     */
    @PostMapping("/handleURLUpload")
    public String handleWebCalUrlUpload(HttpServletRequest request, RedirectAttributes redirectAttributes, Model model)
            throws Exception {
        String url = request.getParameter("CalendarURL"); // Change this line
        currentUrlOrPath = url;
        return displayCalendar(model, currentUrlOrPath);
    }

    /**
     * Handles the URL of the course schedule input file and displays the calendar
     * view. This method is triggered when a POST request is made to "/download/csv"
     * endpoint. It converts the list of Horario objects to a CSV format and creates
     * a byte array response for the user to download the CSV file.
     *
     * @return ResponseEntity<byte[]> The response entity containing the byte array
     *         of the CSV file and the HTTP headers.
     * @throws CsvDataTypeMismatchException   Thrown when there is a data type
     *                                        mismatch while writing the CSV file.
     * @throws CsvRequiredFieldEmptyException Thrown when a required field is empty
     *                                        while writing the CSV file.
     * @throws IOException                    Thrown when there is an I/O error
     *                                        while writing the CSV file.
     */
    @PostMapping("/download/csv")
    public ResponseEntity<byte[]> downloadCsvFile()
            throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {
        String csvContent = HorarioWriter.listToCsv(horariosToDisplay);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "downloadHorario.csv");

        return new ResponseEntity<>(csvContent.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
    }

    /**
     * Handles the URL of the course schedule input file and displays the JSON view.
     * This method is triggered when a POST request is made to "/download/json"
     * endpoint. It converts the list of Horario objects to a JSON format and
     * creates a byte array response for the user to download the JSON file.
     *
     * @return ResponseEntity<byte[]> The response entity containing the byte array
     *         of the JSON file and the HTTP headers.
     * @throws JsonProcessingException Thrown when there is an error during JSON
     *                                 serialization.
     */
    @PostMapping("/download/json")
    public ResponseEntity<byte[]> downloadJsonFile() throws JsonProcessingException {
        String jsonContent = HorarioWriter.listToJson(horariosToDisplay);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "downloadHorario.json");

        return new ResponseEntity<>(jsonContent.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
    }

    /**
     * Checks for overlapping lessons among the Horario objects in the
     * {@link #horariosToDisplay} list and populates
     * the {@link #horariosSobrepostos} set with pairs of overlapping Horario
     * objects.
     * This method groups Horario objects by date and checks for overlaps only
     * between Horarios with the same date.
     * For each group, it sorts the Horarios by start time and then checks for
     * overlaps between each pair of adjacent Horarios.
     * 
     * @param model the Spring Model object to which the overlapping lessons message
     *              should be added
     */
    private void checkForOverlappingLessons(Model model) {
        horariosSobrepostos.clear();
        List<Horario> checkingOverlap = horariosToDisplay;

        // Group Horario objects by date
        Map<String, List<Horario>> horariosByDate = checkingOverlap.stream()
                .filter(horario -> horario.getDataAula() != null).collect(Collectors.groupingBy(Horario::getDataAula));

        horariosSobrepostos = horariosByDate.values().stream().parallel().flatMap(horariosWithSameDate -> {
            Set<Pair<Horario, Horario>> overlaps = new HashSet<>();
            // Sort the horarios by start time
            List<Horario> sortedHorarios = horariosWithSameDate.stream()
                    .sorted(Comparator.comparing(Horario::getHoraInicio)).collect(Collectors.toList());

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
        }).collect(Collectors.toSet());
        // displayOverlappingLessonsMessage(model);
    }

    /**
     * Checks if two Horario objects overlap in time.
     *
     * @param horario1 the first Horario object
     * @param horario2 the second Horario object
     * @return true if the two Horario objects overlap, false otherwise
     */
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

        return overlapDuration.compareTo(Duration.ZERO) > 0 && overlapDuration.compareTo(duration1) <= 0
                && overlapDuration.compareTo(duration2) <= 0;
    }

    /**
     * Generates and paginates a list of messages to inform the user about
     * overlapping lessons.
     *
     * @param model The Model object to which the messages will be added as
     *              attributes.
     */
    private void displayOverlappingLessonsMessage(Model model) {
        List<String> messages = new ArrayList<>();
        for (Pair<Horario, Horario> lessonPair : horariosSobrepostos) {
            Horario h1 = lessonPair.getLeft();
            Horario h2 = lessonPair.getRight();

            messages.add(h1.getUnidadeCurricular() + " " + h1.getDataAula() + " (" + h1.getHoraInicio() + " - "
                    + h1.getHoraFim() + ") [ X ] " + h2.getUnidadeCurricular() + " " + h2.getDataAula() + " ("
                    + h2.getHoraInicio() + " - " + h2.getHoraFim() + ")\n");
        }
        // List<String> paginatedMessages = paginateMessages(messages,
        // overlappingPageNumber);
        // model.addAttribute("overlappingLessonsMessages", paginatedMessages);
        model.addAttribute("disableOverlappingPrevPage", overlappingPageNumber == 0);
        // model.addAttribute("disableOverlappingNextPage", paginatedMessages.size() <
        // 10);
        int overlappingTotalPages = (int) Math.ceil((double) messages.size() / 10);
        model.addAttribute("overlappingTotalPages", overlappingTotalPages);
    }

    /**
     * Checks for events that exceed the capacity of the room.
     *
     * @param model The Model object to add the list of overbooked events to.
     */
    private void checkOverbookedEvents(Model model) {
        horariosSobrelotacao.clear();
        for (Horario h : horariosToDisplay) {
            try {
                int inscritos = Integer.parseInt(h.getInscritos());
                int lotacaoSala = Integer.parseInt(h.getLotacaoSala());
                if (inscritos > lotacaoSala) {
                    horariosSobrelotacao.add(h);
                }
            } catch (NumberFormatException e) {
                LOGGER.error("Invalid 'Inscritos' or 'LotacaoSala' value for horario: " + h, e);
            }
        }
        // displayOverbookedLessonsMessage(model);
    }

    /**
     * 
     * Displays a list of messages containing information about overlapping lessons
     * and overbooked events
     * 
     * based on the current state of the {@code horariosSobrelotacao} and
     * {@code horariosSobrepostos} lists.
     * 
     * @param page  the current page number to display
     * 
     * @param size  the number of messages to display per page
     * 
     * @param model the model object used to pass data to the view
     * 
     * @return the name of the view to display the list of messages
     */
    @GetMapping("/list")
    public String list(@RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        int start = page * size;
        int end = Math.min(start + size, horariosSobrelotacao.size() + horariosSobrepostos.size());

        List<String> messagesToShow = new ArrayList<>();
        for (int i = start; i < end; i++) {
            if (i < horariosSobrelotacao.size()) {
                Horario h = horariosSobrelotacao.get(i);
                messagesToShow.add("Existe sobrelotação na Sala " + h.getSala() + " relativamente à UC "
                        + h.getUnidadeCurricular() + " ,no dia " + h.getDataAula() + " ,entre as " + h.getHoraInicio()
                        + " e as " + h.getHoraFim() + " ,");
            } else {
                Pair<Horario, Horario> pair = horariosSobrepostos.stream().skip(i - horariosSobrelotacao.size())
                        .findFirst().orElse(null);
                if (pair != null) {
                    Horario h1 = pair.getLeft();
                    Horario h2 = pair.getRight();
                    messagesToShow.add("Sobreposição de Horários: A UC " + h1.getUnidadeCurricular() + " e a UC "
                            + h2.getUnidadeCurricular() + " têm aulas sobrepostas no dia " + h1.getDataAula()
                            + " entre as " + h1.getHoraInicio() + " e as " + h1.getHoraFim());
                }
            }
        }

        model.addAttribute("messages", messagesToShow);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (horariosSobrelotacao.size() + horariosSobrepostos.size() + size - 1) / size);

        return "list";
    }
}
