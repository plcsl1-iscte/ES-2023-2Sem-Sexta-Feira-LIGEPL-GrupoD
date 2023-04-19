package iscte.Controllers;

import iscte.timetable.models.Horario;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HorarioReader {

  private String path;

  public HorarioReader(String path) {
    this.path = path;
  }

  public List<Horario> read() {
    List<Horario> horarios = new ArrayList<>();
    try (
        BufferedReader reader = new BufferedReader(new FileReader(this.path))
    ) {
        String line = reader.readLine(); // skip first line with headers
        while ((line = reader.readLine()) != null) {
            // Remove trailing comma before splitting the line
            line = line.trim();
            if (line.endsWith(",")) {
                line = line.substring(0, line.length() - 1);
            }
            // Split line using regex to ignore commas between quotes
            String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            // If the length of the data array is less than 11, resize it and fill the missing fields with empty strings
            if (data.length < 11) {
                String[] resizedData = new String[11];
                System.arraycopy(data, 0, resizedData, 0, data.length);
                for (int i = data.length; i < 11; i++) {
                    resizedData[i] = "";
                }
                data = resizedData;
            }

            Horario horario = new Horario(
                data[0],
                data[1],
                data[2],
                data[3],
                data[4],
                data[5],
                data[6],
                data[7],
                data[8],
                data[9],
                data[10]
            );
            horarios.add(horario);
        }
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
    return horarios;
}

}
