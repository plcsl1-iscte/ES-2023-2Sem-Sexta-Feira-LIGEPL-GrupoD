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

/**
 * Utility class for reading Horario objects from files or webcal URLs.
 *
 * The appropriate reader strategy is selected based on the file extension or
 *
 * the URL type. The supported file formats are CSV and JSON, and the webcal URL
 *
 * format is also supported.
 *
 * @see iscte.interfaces.HorarioReaderStrategy
 *
 * @see iscte.strategies.CsvHorarioReaderStrategy
 *
 * @see iscte.strategies.JsonHorarioReaderStrategy
 *
 * @see iscte.strategies.WebcalHorarioReaderStrategy
 */
public class HorarioReader {
    private final String filePath;

    static final Logger LOGGER = (Logger) LoggerFactory.getLogger(HorarioReader.class);

    public HorarioReader(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Reads the input from a file or URL and returns a list of Horario objects
     * created from the data. The method supports both local files and webcal URLs.
     * The appropriate reader strategy is determined based on the file extension or
     * the URL type.
     *
     * @return A List of Horario objects created from the input data.
     * @throws Exception If an error occurs while reading the input or parsing the
     *                   data.
     */

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

    /**
     * Retrieves the file extension from a given file path string.
     * The method returns an empty string if no extension is found.
     *
     * @param filePath The file path string to extract the file extension from.
     * @return A String containing the file extension in lowercase, or an empty
     *         string if no extension is found.
     */

    private String getFileExtension(String filePath) {
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex > 0) {
            return filePath.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Retrieves the appropriate HorarioReaderStrategy based on the given file
     * extension.
     * The method returns a CsvHorarioReaderStrategy for "csv" files, a
     * JsonHorarioReaderStrategy
     * for "json" files, and null for any other file extension.
     *
     * @param fileExtension The file extension string to determine the appropriate
     *                      reader strategy.
     * @return A HorarioReaderStrategy corresponding to the file extension, or null
     *         if no suitable strategy is found.
     */

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
