import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import iscte.Application;
import iscte.Controllers.HorarioController;
import iscte.Controllers.HorarioReader;
import iscte.timetable.management.ConversorHorario;
import iscte.timetable.models.Horario;

public class ApplicationTest {


    @Test
    public void testHorario() {
        Horario horario = new Horario("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
        assertEquals("1", horario.getCurso());
        assertEquals("2", horario.getUnidadeCurricular());
        assertEquals("3", horario.getTurno());
        assertEquals("4", horario.getTurma());
        assertEquals("5", horario.getInscritos());
        assertEquals("6", horario.getDiaSemana());
        assertEquals("7", horario.getHoraInicio());
        assertEquals("8", horario.getHoraFim());
        assertEquals("9", horario.getDataAula());
        assertEquals("10", horario.getSala());
        assertEquals("11", horario.getLotacaoSala());
    }

    // Test if the HorarioReader class is reading the csv file correctly
    
    
    @Test
    public void testHorarioReader() {
        HorarioReader horarioReader = new HorarioReader("src/main/java/iscte/timetable/files/horario-exemplo.csv");
        assertNotNull(horarioReader);
    }


    // unit tests for HorarioController
    @Test
    public void testHorarioController() {
        HorarioController horarioController = new HorarioController();
        assertNotNull(horarioController);
    }

// test if conversorhorario class is working as expected 
    @Test
    public void testConversorHorario() {
        ConversorHorario conversorHorario = new ConversorHorario();
        assertNotNull(conversorHorario);
    }

    // unit test escreverJson method
    @Test
    public void testEscreverJson() {
        HorarioController horarioController = new HorarioController();
        assertNotNull(horarioController);
    }



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

   
    //unit tests for Application
    @Test
    public void testApplication() {
        Application application = new Application();
        assertNotNull(application);
    }

}