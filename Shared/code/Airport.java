package Shared.code;

import java.io.*;
import java.util.*;

/**
 * A simple data class holding:
 * - city name (e.g. "Los Angeles")
 * - IATA code (e.g. "LAX")
 * - latitude (double)
 * - longitude (double)
 *
 * Also contains a static loader method to read airports.txt.
 */
public class Airport {
    public final String city;
    public final String code;
    public final double latitude;
    public final double longitude;

    public Airport(String city, String code, double latitude, double longitude) {
        this.city = city;
        this.code = code;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Load all airports from a file with format:
     * CityName,IATA,Latitude,Longitude
     * (one per line, no header). Returns a List<Airport>.
     */
    public static List<Airport> loadFromFile(String airportsFilePath) throws IOException {
        List<Airport> airports = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(airportsFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                String[] parts = line.split(",");
                if (parts.length != 4)
                    continue; // skip malformed
                try {
                    String city = parts[0].trim();
                    String iata = parts[1].trim();
                    double lat = Double.parseDouble(parts[2].trim());
                    double lon = Double.parseDouble(parts[3].trim());
                    airports.add(new Airport(city, iata, lat, lon));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid airport line: " + line);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Loaded airports: " + airports.size());
        return airports;
    }
}
