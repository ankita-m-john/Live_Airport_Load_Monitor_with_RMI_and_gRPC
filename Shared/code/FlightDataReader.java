package Shared.code;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the entire flights.txt JSON file and returns a List<FlightRecord>.
 * This mirrors Program 2’s JSON-based approach (Prog 2 Report version).
 */
public class FlightDataReader {

    /**
     * Parse the given flights.txt JSON file (with a top-level "states" array)
     * into a List of FlightRecord. Malformed entries are skipped.
     *
     * @param flightsFilePath path to flights.txt (JSON)
     * @return List<FlightRecord>
     * @throws IOException if file not found or read error
     */
    public static List<FlightRecord> readAll(String flightsFilePath) throws IOException {
        List<FlightRecord> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(flightsFilePath))) {
            JSONObject root = new JSONObject(new JSONTokener(reader));
            JSONArray states = root.getJSONArray("states");

            for (int i = 0; i < states.length(); i++) {
                try {
                    JSONArray flight = states.getJSONArray(i);
                    // index 1 = call_sign
                    String callSign = flight.optString(1, "").trim();
                    if (callSign.isEmpty())
                        continue;

                    // index 5 = longitude, index 6 = latitude
                    double lon = flight.optDouble(5, Double.NaN);
                    double lat = flight.optDouble(6, Double.NaN);
                    if (Double.isNaN(lat) || Double.isNaN(lon))
                        continue;

                    // optional: index 9 = velocity, index 11 = vertical_rate
                    double velocity = flight.optDouble(9, 0.0);
                    double vRate = flight.optDouble(11, 0.0);

                    records.add(new FlightRecord(callSign, lat, lon, velocity, vRate));
                } catch (Exception e) {
                    // skip malformed record
                    // (e.g. if states.getJSONArray(i) fails or missing fields)
                }
            }
        }

        return records;
    }
}
