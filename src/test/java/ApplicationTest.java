import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import iscte.Application;
import iscte.Controllers.HorarioController;
import iscte.Controllers.HorarioReader;
import iscte.timetable.management.ConversorHorario;
import iscte.timetable.models.Horario;

public class ApplicationTest {

    @Test
    public void runAllTests() {
        testHorarioReader();
        testHorarioController();
        testHorario();
        testConversorHorario();
        testApplication();
        
    }

    @Test
    public void test() {
        assertTrue(true);
    }

   // unit tests for HorarioReader
    @Test
    public void testHorarioReader() {
        HorarioReader horarioReader = new HorarioReader("src/main/resources/horarios.csv");
        assertNotNull(horarioReader);
    }

    // unit tests for HorarioController
    @Test
    public void testHorarioController() {
        HorarioController horarioController = new HorarioController();
        assertNotNull(horarioController);
    }

    //unit tests for Horario
    @Test
    public void testHorario() {
        Horario horario = new Horario("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
        assertNotNull(horario);
    }

    //unit tests for ConversorHorario
    @Test
    public void testConversorHorario() {
        ConversorHorario conversorHorario = new ConversorHorario();
        assertNotNull(conversorHorario);
    }
   
    //unit tests for Application
    @Test
    public void testApplication() {
        Application application = new Application();
        assertNotNull(application);
    }

}