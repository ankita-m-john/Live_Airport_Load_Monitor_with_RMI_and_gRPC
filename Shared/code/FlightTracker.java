package Shared.code;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * FlightTracker periodically scans flights via FlightDataReader,
 * compares against the previous scan to detect arrivals/departures for each
 * airport,
 * and returns a map: airportCode -> AirportStats (holds sets of callSigns).
 *
 * INTERNAL STATE:
 * prevInRange: Map<flightCallSign, airportCode>
 * - If flightCallSign → null, means “last scan it was not in range of any
 * airport.”
 * - If flightCallSign → "LAX", means “last scan it was in range of LAX.”
 *
 * USAGE:
 * FlightTracker tracker = new FlightTracker("path/to/airports.txt",
 * "path/to/flights.txt");
 * Map<String, AirportStats> stats = tracker.scan(); // does one scan, returns
 * arrivals/departures
 * // next scan() uses updated prevInRange to compare
 */
public class FlightTracker {
    // A simple struct holding this scan’s arrivals & departures for a single
    // airport.
    public static class AirportStats {
        public final Set<String> arrivals = new HashSet<>();
        public final Set<String> departures = new HashSet<>();
    }

    // Path to the two input files:
    private final String airportsFile;
    private final String flightsFile;

    // In-memory list of all Airport objects (code, lat, lon).
    private final List<Airport> airports;

    // Map from flightCallSign -> last-known airportCode (or null if was not in
    // any).
    private final Map<String, String> prevInRange = new ConcurrentHashMap<>();

    // Bounding-box thresholds (10 miles → degrees).
    private static final double LAT_DEG_PER_10MI = 10.0 / 70.0; // ≈ 0.2857°
    private static final double LON_DEG_PER_10MI = 10.0 / 45.0; // ≈ 0.4444°

    public FlightTracker(String airportsFile, String flightsFile) throws IOException {
        this.airportsFile = airportsFile;
        this.flightsFile = flightsFile;
        // Load all airports once:
        this.airports = Airport.loadFromFile(airportsFile);
    }

    /**
     * Do one “10-second” scan:
     * 1) Read current flights from flightsFile (JSON) → List<FlightRecord>
     * 2) For each FlightRecord, determine which airport (if any) it is currently
     * in-range of.
     * 3) Compare with prevInRange to detect ARRIVAL or DEPARTURE:
     * - If prevInRange.get(callSign) == null but now inRangeCode != null → ARRIVAL
     * - If prevInRange.get(callSign) == someCode and now inRangeCode == null →
     * DEPARTURE
     * - If prevInRange.get(callSign) == someCode and now inRangeCode == sameCode →
     * still there (ignore)
     * - If prevInRange.get(callSign) == someCodeA and now inRangeCode == someCodeB
     * (B ≠ A):
     * → Treat as DEPARTURE from A and ARRIVAL at B.
     * 4) Build a Map<String airportCode, AirportStats> that collects all arrivals &
     * departures sets.
     * 5) Update prevInRange to reflect “current in-range” for next call.
     *
     * @return map of airportCode → AirportStats (arrivals, departures)
     */
    public Map<String, AirportStats> scan() {
        // Step 1: parse all flights
        List<FlightRecord> flights;
        try {
            flights = FlightDataReader.readAll(flightsFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read flights: " + e.getMessage(), e);
        }

        // Step 2: For each flight, find currentInRangeCode (or null if none)
        Map<String, String> currentInRange = new HashMap<>();
        for (FlightRecord fr : flights) {
            // OPTIONAL: skip if verticalRate == 0 or velocity too high (Program2 additional
            // feature)
            if (Math.abs(fr.verticalRate) < 0.1 || fr.velocity > 300) {
                // Treat as “not in-range” or skip checking
                currentInRange.put(fr.callSign, null);
                continue;
            }

            String inRangeCode = null;
            for (Airport a : airports) {
                if (isInRange(a, fr.latitude, fr.longitude)) {
                    inRangeCode = a.code;
                    break; // assume at most one airport in range
                }
            }
            currentInRange.put(fr.callSign, inRangeCode);
        }

        // Step 3: Compare with prevInRange to detect arrivals/departures
        // Initialize stats for every airport, even if empty
        Map<String, AirportStats> stats = new HashMap<>();
        for (Airport a : airports) {
            stats.put(a.code, new AirportStats());
        }

        // Also track flights that disappeared from this scan (previously known, now
        // missing entirely):
        Set<String> allCallSigns = new HashSet<>(currentInRange.keySet());
        allCallSigns.addAll(prevInRange.keySet());

        for (String callSign : allCallSigns) {
            String prevCode = prevInRange.get(callSign); // may be null
            String currCode = currentInRange.get(callSign); // may be null (if flight no longer in JSON)
            // --- ARRIVAL: was not in any airport before, now in one:
            if (prevCode == null && currCode != null) {
                stats.get(currCode).arrivals.add(callSign);
            }
            // --- DEPARTURE: was in airport before, now not in any:
            else if (prevCode != null && currCode == null) {
                stats.get(prevCode).departures.add(callSign);
            }
            // --- MOVED AIRPORTS: (prev=A, curr=B, A≠B)
            else if (prevCode != null && currCode != null && !prevCode.equals(currCode)) {
                // treat as departure from prevCode AND arrival at currCode
                stats.get(prevCode).departures.add(callSign);
                stats.get(currCode).arrivals.add(callSign);
            }
            // else: either still in same airport or still out of range → no event
        }

        // Step 4: Update prevInRange = currentInRange
        prevInRange.clear();
        // Only store “currently in-range” flights in prevInRange; skip those that are
        // null
        for (Map.Entry<String, String> e : currentInRange.entrySet()) {
            if (e.getValue() != null) {
                prevInRange.put(e.getKey(), e.getValue());
            }
        }

        return stats;
    }

    // Helper: bounding-box check (± LAT_DEG_PER_20MI / ± LON_DEG_PER_20MI)
    private boolean isInRange(Airport a, double lat, double lon) {
        return Math.abs(a.latitude - lat) <= LAT_DEG_PER_10MI
                && Math.abs(a.longitude - lon) <= LON_DEG_PER_10MI;
    }
}
