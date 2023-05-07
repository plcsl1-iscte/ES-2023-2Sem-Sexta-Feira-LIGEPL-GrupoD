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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
  private List<Horario> filteredHorariosByUC = new ArrayList<Horario>();
  private Set<Pair<Horario, Horario>> horariosSobrepostos = new HashSet<>();
  private ArrayList<Horario> horariosSobrelotacao = new ArrayList<Horario>();
  List<Horario> filteredHorariosByCurso = new ArrayList<Horario>();
  private String curso = "none";
  private String uc = "none";
  Set<String> uniqueUCs;
  Set<String> uniqueCursos;

  @GetMapping("/")
  public String displayCalendar(
    Model model,
    String filePath,
    @RequestParam(name = "curso", required = false) String curso,
    @RequestParam(name = "uc", required = false) String uc
  ) throws Exception {
    if (model != null) {
      this.uc = uc;
      this.curso = curso;
      System.out.println("UC " + uc);
      System.out.println("curso: " + curso);

      HorarioReader reader = new HorarioReader(filePath);

      horarios = reader.read();

      System.out.println("Horarios " + horarios.size());

      // Filter horarios based on the selected curso
      if (curso != null && !curso.equals("none")) {
        filteredHorariosByCurso =
          horarios
            .stream()
            .filter(h -> h.getCurso().equals(curso))
            .collect(Collectors.toList());
      }
      System.out.println(
        "filteredHorariosByCurso " + filteredHorariosByCurso.size()
      );

      for (Horario horario : horarios) {
        if (horario.getCurso().equals(curso)) System.out.println(
          "Horario curso: " + horario.getCurso()
        );
      }

      filteredHorariosByCurso =
        horarios
          .stream()
          .filter(h -> h.getCurso().equals(curso))
          .collect(Collectors.toList());

      System.out.println(
        "Filtered horarios by curso size: " + filteredHorariosByCurso.size()
      );

      System.out.println("filteredHorariosByUC " + filteredHorariosByUC.size());

      if (uniqueCursos == null) {
        // Extract unique Cursos and UCs
        uniqueCursos =
          horarios.stream().map(Horario::getCurso).collect(Collectors.toSet());
      }

      if (uniqueUCs == null) {
        uniqueUCs =
          horarios
            .stream()
            .map(Horario::getUnidadeCurricular)
            .collect(Collectors.toSet());
      }

      checkForOverlappingLessons(model);
      checkOverbookedEvents(model);

      System.out.println(
        "uniqueCursos " + uniqueCursos != null
          ? uniqueCursos.toString()
          : "uniqueCursos is null"
      );
      System.out.println(
        "filteredHorariosByUC " + filteredHorariosByUC != null
          ? filteredHorariosByUC.toString()
          : "filteredHorariosByUC is null"
      );
      System.out.println("Horarios " + horarios.size());
      System.out.println("filteredHorariosByUC " + filteredHorariosByUC.size());
      System.out.println(
        "filteredHorariosByCurso " + filteredHorariosByCurso.size()
      );

      List<Horario> horariosToDisplay = new ArrayList<>();

      if (
        filteredHorariosByCurso != null && !filteredHorariosByCurso.isEmpty()
      ) {
        horariosToDisplay.addAll(filteredHorariosByCurso);
      }

      if (filteredHorariosByUC != null && !filteredHorariosByUC.isEmpty()) {
        horariosToDisplay.addAll(filteredHorariosByUC);
      }

      if (horariosToDisplay.isEmpty()) {
        horariosToDisplay = horarios;
      }

      System.out.println("horariosToDisplay " + horariosToDisplay.size());

      model.addAttribute("horarios", horariosToDisplay);
      model.addAttribute("cursos", uniqueCursos);
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
    return displayCalendar(model, currentUrlOrPath, curso, uc);
  }

  @PostMapping("/selectUCs")
  public String processSelectedUCs(
    @RequestParam("ucs") List<String> selectedUCs,
    Model model
  ) throws Exception {
    if (!selectedUCs.isEmpty()) {
      uc = String.join(",", selectedUCs); // Update the 'uc' variable with the selected UCs
    } else {
      uc = "none";
    }

    return "redirect:/filtered?curso=" + curso + "&ucs=" + uc;
  }

  @PostMapping("/handleURLUpload")
  public String handleWebCalUrlUpload(
    HttpServletRequest request,
    RedirectAttributes redirectAttributes,
    Model model
  ) throws Exception {
    String url = request.getParameter("CalendarURL");
    if (url == null) url = "";
    currentUrlOrPath = url;
    return displayCalendar(model, currentUrlOrPath, null, null);
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

  private List<Horario> getFilteredHorarios() {
    if (!filteredHorariosByUC.isEmpty()) {
      return filteredHorariosByUC;
    } else if (!filteredHorariosByCurso.isEmpty()) {
      return filteredHorariosByCurso;
    } else {
      return horarios;
    }
  }

  private void checkForOverlappingLessons(Model model) {
    horariosSobrepostos.clear();
    List<Horario> checkingOverlap = filteredHorariosByUC;
    if (filteredHorariosByCurso.size() > 0) checkingOverlap =
      filteredHorariosByCurso;
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
    model.addAttribute("overlappingLessonsMessages", messages);
  }

  private void checkOverbookedEvents(Model model) {
    horariosSobrelotacao.clear();
    List<Horario> checkingOverbook = filteredHorariosByUC;
    if (filteredHorariosByCurso.size() > 0) checkingOverbook =
      filteredHorariosByCurso;
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
    model.addAttribute("overbookedLessonsMessages", messages);
  }

  @GetMapping("/filtered")
  public String displayFilteredCalendar(
    Model model,
    @RequestParam(name = "curso", required = false) String curso,
    @RequestParam(name = "ucs", required = false) String ucs
  ) throws Exception {
    this.curso = curso;

    // Filter horarios based on the selected curso
    if (curso != null && !curso.isEmpty()) {
      filteredHorariosByCurso =
        horarios
          .stream()
          .filter(h -> h.getCurso().equals(curso))
          .collect(Collectors.toList());
    } else {
      filteredHorariosByCurso = horarios;
    }

    // Filter horarios based on the selected ucs, if any ucs are selected
    if (ucs != null && !ucs.isEmpty()) {
      List<String> selectedUCs = Arrays.asList(ucs.split(","));
      filteredHorariosByUC =
        filteredHorariosByCurso
          .stream()
          .filter(h -> selectedUCs.contains(h.getUnidadeCurricular()))
          .collect(Collectors.toList());
    } else {
      filteredHorariosByUC = filteredHorariosByCurso;
    }

    // Rest of the code in displayCalendar() method, excluding the filtering part

    return "calendar";
  }
}
