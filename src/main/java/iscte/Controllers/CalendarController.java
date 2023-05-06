package iscte.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import iscte.timetable.models.Horario;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CalendarController {

  String currentUrlOrPath = "";
  private List<Horario> horarios = new ArrayList<Horario>();
  private List<Horario> filteredHorarios = new ArrayList<Horario>();
  private Set<Pair<Horario, Horario>> horariosSobrepostos = new HashSet<>();
  private ArrayList<Horario> horariosSobrelotacao = new ArrayList<Horario>();
  private List<Horario> horariosToDisplay = new ArrayList<Horario>();
  private int overlappingPageNumber = 0;
  private int overbookedPageNumber = 0;

  @GetMapping("/")
  public String displayCalendar(Model model, String filePath) throws Exception {
    if (model != null && filePath != null) {
      HorarioReader reader = new HorarioReader(filePath);

      horarios = reader.read();

      Set<String> uniqueUCs = new HashSet<>();
      for (Horario horario : horarios) {
        uniqueUCs.add(horario.getUnidadeCurricular());
      }

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

  @PostMapping("/handleFileUpload")
  public String handleFileUpload(
    HttpServletRequest request,
    RedirectAttributes redirectAttributes,
    Model model
  ) throws Exception {
    String path = request.getParameter("path");
    if (path == null) path = "";
    currentUrlOrPath = path;
    return displayCalendar(model, currentUrlOrPath);
  }

  @PostMapping("/selectUCs")
  public String processSelectedUCs(
    @RequestParam("ucs") List<String> selectedUCs,
    Model model
  ) throws Exception {
    // Print the selected UCs to the console
    System.out.println("Selected UCs: " + selectedUCs);

    filteredHorarios.clear();

    for (Horario h : horarios) {
      Boolean found = false;
      for (String uc : selectedUCs) {
        if (h.getUnidadeCurricular().equals(uc) && found == false) {
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
    if (url == null) url = "";
    currentUrlOrPath = url;
    return displayCalendar(model, currentUrlOrPath);
  }

  @PostMapping("/download/csv")
  public ResponseEntity<byte[]> downloadCsvFile()
    throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {
    String csvContent = HorarioWriter.listToCsv(horarios);

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
    String jsonContent = HorarioWriter.listToJson(horarios);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setContentDispositionFormData("attachment", "downloadHorario.json");

    return new ResponseEntity<>(
      jsonContent.getBytes(StandardCharsets.UTF_8),
      headers,
      HttpStatus.OK
    );
  }

  private void fileToList(String filePath) throws Exception {
    if (filePath != null && !filePath.isEmpty()) {
      HorarioReader reader = new HorarioReader(filePath);
      horarios = reader.read();
    } else {
      horarios = new ArrayList<>(); // Initialize an empty list if the file path is not valid
    }
  }

  private void checkForOverlappingLessons(Model model) {
    horariosSobrepostos.clear();
    List<Horario> checkingOverlap = horariosToDisplay;
    for (int i = 0; i < checkingOverlap.size(); i++) {
      for (int j = i + 1; j < checkingOverlap.size(); j++) {
        Horario horario1 = checkingOverlap.get(i);
        Horario horario2 = checkingOverlap.get(j);
        if (horario1.getDataAula().equals(horario2.getDataAula())) {
          if (checkOverlap(horario1, horario2)) {
            Pair<Horario, Horario> sh = Pair.of(horario1, horario2);
            horariosSobrepostos.add(sh);
          }
        }
      }
    }
    System.out.println(
      "Existem " + horariosSobrepostos.size() + " horarios sobrepostos."
    );
    displayOverlappingLessonsMessage(model);
  }

  private boolean checkOverlap(Horario horario1, Horario horario2) {
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    LocalTime startTime1 = LocalTime.parse(
      horario1.getHoraInicio(),
      timeFormatter
    );
    LocalTime endTime1 = LocalTime.parse(horario1.getHoraFim(), timeFormatter);
    LocalTime startTime2 = LocalTime.parse(
      horario2.getHoraInicio(),
      timeFormatter
    );
    LocalTime endTime2 = LocalTime.parse(horario2.getHoraFim(), timeFormatter);

    boolean sameDate = horario1.getDataAula().equals(horario2.getDataAula());
    boolean startTimeOverlap = startTime1.compareTo(endTime2) < 0;
    boolean endTimeOverlap = endTime1.compareTo(startTime2) > 0;

    return sameDate && startTimeOverlap && endTimeOverlap;
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
  }

  private void checkOverbookedEvents(Model model) {
    horariosSobrelotacao.clear();
    List<Horario> checkingOverbook = horariosToDisplay;
    for (Horario h : checkingOverbook) {
      boolean areParseableToIntegers = true;

      try {
        int inscritos = Integer.parseInt(h.getInscritos());
        int lotacaoSala = Integer.parseInt(h.getLotacaoSala());

        if (inscritos > lotacaoSala) {
          horariosSobrelotacao.add(h);
        }
      } catch (NumberFormatException e) {
        areParseableToIntegers = false;
      }
    }
    System.out.println(
      "Existem " + horariosSobrelotacao.size() + " horarios sobrepostos."
    );
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
    model.addAttribute(
      "disableOverbookedNextPage",
      paginatedMessages.size() < 10
    );
  }

  private List<String> paginateMessages(List<String> messages, int pageNumber) {
    int pageSize = 10;
    int fromIndex = pageNumber * pageSize;
    int toIndex = Math.min(fromIndex + pageSize, messages.size());
    return messages.subList(fromIndex, toIndex);
  }

  @PostMapping("/overlappingPrev")
  public String handleOverlappingPrev(Model model) throws Exception {
    if (overlappingPageNumber > 0) {
      overlappingPageNumber--;
    }
    return displayCalendar(model, currentUrlOrPath);
  }

  @PostMapping("/overlappingNext")
  public String handleOverlappingNext(
    Model model,
    @RequestParam(
      "overlappingLessonsMessages"
    ) List<String> overlappingLessonsMessages
  ) throws Exception {
    int totalPages = (int) Math.ceil(overlappingLessonsMessages.size() / 10.0);
    if (overlappingPageNumber < totalPages - 1) {
      overlappingPageNumber++;
    }
    return displayCalendar(model, currentUrlOrPath);
  }

  @PostMapping("/overbookedPrev")
  public String handleOverbookedPrev(Model model) throws Exception {
    if (overbookedPageNumber > 0) {
      overbookedPageNumber--;
    }
    return displayCalendar(model, currentUrlOrPath);
  }

  @PostMapping("/overbookedNext")
  public String handleOverbookedNext(
    Model model,
    @RequestParam(
      "overbookedLessonsMessages"
    ) List<String> overbookedLessonsMessages
  ) throws Exception {
    int totalPages = (int) Math.ceil(overbookedLessonsMessages.size() / 10.0);
    if (overbookedPageNumber < totalPages - 1) {
      overbookedPageNumber++;
    }
    return displayCalendar(model, currentUrlOrPath);
  }
}
