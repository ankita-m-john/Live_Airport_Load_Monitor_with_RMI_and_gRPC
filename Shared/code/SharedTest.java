package Shared.code;

import java.util.*;

public class SharedTest {
    public static void main(String[] args) throws Exception {
        // String airportsFile = "/home/NETID/amj59/final_flight_RMI_gRPC/Shared/resources/airports.txt";
        // String flightsFile = "/home/NETID/amj59/final_flight_RMI_gRPC/Shared/resources/flights.txt";
        String airportsFile = "/home/NETID/jsasid/lib/testingGroupProject/Live_Airport_Load_Monitor_with_RMI_and_gRPC/Shared/resources/airports.txt";
        String flightsFile = "/home/NETID/jsasid/lib/testingGroupProject/Live_Airport_Load_Monitor_with_RMI_and_gRPC/Shared/resources/flights.txt";
       ///home/NETID/jsasid/lib/testingGroupProject/Live_Airport_Load_Monitor_with_RMI_and_gRPC/Shared/resources/airports.txt

        FlightTracker tracker = new FlightTracker(airportsFile, flightsFile);

        // Do three successive scans, sleeping 10s between (or manually call scan
        // thrice).
        for (int i = 0; i < 3; i++) {
            Map<String, FlightTracker.AirportStats> stats = tracker.scan();
            System.out.println("=== Scan " + (i + 1) + " ===");
            for (Map.Entry<String, FlightTracker.AirportStats> entry : stats.entrySet()) {
                String code = entry.getKey();
                Set<String> arr = entry.getValue().arrivals;
                Set<String> dep = entry.getValue().departures;
                if (!arr.isEmpty() || !dep.isEmpty()) {
                    System.out.printf("Airport %s: Arrived=%s | Departed=%s%n", code, arr, dep);
                }
            }
            Thread.sleep(10000);
        }
    }
}
