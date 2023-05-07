import iscte.Application;
import iscte.controllers.CalendarController;
import iscte.entities.Horario;
import iscte.utils.HorarioReader;
import iscte.utils.HorarioWriter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class HorarioReaderTest {
    File file = new File("src/test/resources/horario_exemplo.csv");
    File file2 = new File("src/test/resources/downloadHorario.json");
    String webcal = "webcal://fenix.iscte-iul.pt/publico/publicPersonICalendar.do?method=iCalendar&username=tccfs@iscte.pt&password=9KOym6IKNdXZiyC3uEt7vyZg6ROTWIHFoF3SKvirI1Vm6BHEUmvPEyxKqjpjxBjOWYUulJCGSJXk2QF8A7311CzXWovUiCeoxE5xtO1ck6wgW3tgzUvBgM8mX26Ekh48";
    @Test
    void testReadAndWrite() throws Exception {
        // Arrange
        HorarioReader reader = new HorarioReader(file.getPath());
        List<Horario> horarios = reader.read();

        HorarioReader reader2 = new HorarioReader(file2.getPath());
        List<Horario> horarios2 = reader2.read();

        HorarioReader reader3 = new HorarioReader(webcal);
        List<Horario> horarios3 = reader2.read();

        HorarioWriter writer1 = new HorarioWriter();
        String download1 = writer1.listToJson(horarios);

        HorarioWriter writer2 = new HorarioWriter();
        String download2 = writer2.listToCsv(horarios);

        assertNotNull(download1, "JSON output should not be null");
        assertNotNull(download2, "CSV output should not be null");

    }

    @Test
    void testDisplayCsvCalendar() throws Exception {

        // Mock the Model object
        Model model = mock(Model.class);

        // Mock the HorarioReader and return an empty list of horarios
        HorarioReader reader = mock(HorarioReader.class);
        when(reader.read()).thenReturn(new ArrayList<>());

        // Call the method being tested
        CalendarController controller = new CalendarController();
        String viewName = controller.displayCalendar(model, String.valueOf(file), 0,0);

        // Verify that the list of horarios returned by the controller is not null
        assertThat(controller.getHorariosToDisplay()).isNotNull();

        // Verify that the correct view was returned
        assertThat(viewName).isEqualTo("calendar");
    }

    @Test
    void testDisplayJsonCalendar() throws Exception {

        // Mock the Model object
        Model model = mock(Model.class);

        // Mock the HorarioReader and return an empty list of horarios
        HorarioReader reader = mock(HorarioReader.class);
        when(reader.read()).thenReturn(new ArrayList<>());

        // Call the method being tested
        CalendarController controller = new CalendarController();
        String viewName = controller.displayCalendar(model, String.valueOf(file2), 0,0);

        // Verify that the list of horarios returned by the controller is not null
        assertThat(controller.getHorariosToDisplay()).isNotNull();

        // Verify that the correct view was returned
        assertThat(viewName).isEqualTo("calendar");
    }

    @Test
    void testDisplayWebcalCalendar() throws Exception {

        // Mock the Model object
        Model model = mock(Model.class);

        // Mock the HorarioReader and return an empty list of horarios
        HorarioReader reader = mock(HorarioReader.class);
        when(reader.read()).thenReturn(new ArrayList<>());

        // Call the method being tested
        CalendarController controller = new CalendarController();
        String viewName = controller.displayCalendar(model, String.valueOf(webcal), 0,0);

        // Verify that the list of horarios returned by the controller is not null
        assertThat(controller.getHorariosToDisplay()).isNotNull();

        // Verify that the correct view was returned
        assertThat(viewName).isEqualTo("calendar");
    }

    @Test
    void testProcessSelectedUCs() throws Exception {
        // Load test data from a CSV file
        List<Horario> horarios = new HorarioReader("src/test/resources/test_horarios.csv").read();

        // Split the test data in half
        int midpoint = horarios.size() / 2;
        List<Horario> selectedHorarios = horarios.subList(0, midpoint);

        // Mock the Model object
        Model model = mock(Model.class);

        // Call the method being tested
        CalendarController controller = new CalendarController();
        String viewName = controller.processSelectedUCs(selectedHorarios.stream().map(Horario::getUnidadeCurricular).collect(Collectors.toList()), model);

        // Verify that the filteredHorarios list was updated correctly
        assertThat(controller.getFilteredHorarios()).containsExactlyElementsOf(selectedHorarios);

        // Verify that the correct view was returned
        assertThat(viewName).isEqualTo("calendar");
    }

    @Test
    void testDownloadCsvFile() throws Exception {
        // Read the test data from the CSV file
        String filePath = "src/test/resources/horario_exemplo.csv";
        HorarioReader reader = new HorarioReader(filePath);
        List<Horario> horarios = reader.read();

        // Mock the Model object
        Model model = mock(Model.class);

        // Set up the controller and call the method being tested
        CalendarController controller = new CalendarController();
        controller.setHorarios(horarios);
        controller.setHorariosToDisplay(horarios);
        ResponseEntity<byte[]> response = controller.downloadCsvFile();

        // Verify the response headers
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("text/csv"));
        assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("downloadHorario.csv");

        // Verify the response body
        String expectedCsvContent = HorarioWriter.listToCsv(horarios);
        String actualCsvContent = new String(response.getBody(), StandardCharsets.UTF_8);
        assertThat(actualCsvContent).isEqualTo(expectedCsvContent);
    }

    @Test
    void testDownloadJsonFile() throws Exception {
        // Read the test data from the JSON file
        String filePath = "src/test/resources/horario_exemplo.json";
        HorarioReader reader = new HorarioReader(filePath);
        List<Horario> horarios = reader.read();

        // Mock the Model object
        Model model = mock(Model.class);

        // Set up the controller and call the method being tested
        CalendarController controller = new CalendarController();
        controller.setHorarios(horarios);
        controller.setHorariosToDisplay(horarios);
        ResponseEntity<byte[]> response = controller.downloadJsonFile();

        // Verify the response headers
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("downloadHorario.json");

        // Verify the response body
        String expectedJsonContent = HorarioWriter.listToJson(horarios);
        String actualJsonContent = new String(response.getBody(), StandardCharsets.UTF_8);
        assertThat(actualJsonContent).isEqualTo(expectedJsonContent);
    }

    @Test
    void startApp() {
        String[] args = {};
        assertDoesNotThrow(() -> Application.main(args));
    }

}

