package iscte.utils;

import ch.qos.logback.classic.Logger;
import iscte.entities.Horario;
import iscte.interfaces.HorarioReaderStrategy;
import iscte.strategies.CsvHorarioReaderStrategy;
import iscte.strategies.JsonHorarioReaderStrategy;
import iscte.strategies.WebcalHorarioReaderStrategy;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HorarioReader {
    private final String filePath;

    static final Logger LOGGER = (Logger) LoggerFactory.getLogger(HorarioReader.class);


    public HorarioReader(String filePath) {
        this.filePath = filePath;
    }

    public List<Horario> read() throws Exception {
        String fileExtension = getFileExtension(filePath);
        if (filePath.toLowerCase().startsWith("webcal")) {
            String httpUrl = filePath.replaceFirst("webcal://", "https://");
            URL url = new URL(httpUrl);
            try (InputStream inputStream = url.openStream()) {
                Reader reader = new InputStreamReader(inputStream);
                WebcalHorarioReaderStrategy webcalReaderStrategy = new WebcalHorarioReaderStrategy();
                return webcalReaderStrategy.read(reader);
            }
        } else {
            HorarioReaderStrategy readerStrategy = getReaderStrategy(fileExtension);
            if (readerStrategy == null) {
                return new ArrayList<>();
            }

            try (Reader reader = new FileReader(filePath)) {
                return readerStrategy.read(reader);
            } catch (FileNotFoundException e) {
                LOGGER.error("File not found: " + filePath, e);
                return new ArrayList<>();
            } catch (Exception e) {
                LOGGER.error("Error reading file: " + filePath, e);
                return new ArrayList<>();
            }

        }
    }

    private String getFileExtension(String filePath) {
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex > 0) {
            return filePath.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    @javax.annotation.Nullable
    private HorarioReaderStrategy getReaderStrategy(String fileExtension) {
        switch (fileExtension) {
            case "csv":
                return new CsvHorarioReaderStrategy();
            case "json":
                return new JsonHorarioReaderStrategy();
            default:
                return null;
        }
    }
}

