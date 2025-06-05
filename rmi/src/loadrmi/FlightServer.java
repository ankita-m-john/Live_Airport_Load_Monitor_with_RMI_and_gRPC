package loadrmi;

import Shared.code.FlightTracker;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.*;

public class FlightServer implements FlightMonitor {
    private final FlightTracker tracker;
    private final Map<FlightSubscriber, Set<String>> subscriptions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public FlightServer(String airportsFile, String flightsFile) throws Exception {
        System.out.println("Scanning for flight data...");
        this.tracker = new FlightTracker(airportsFile, flightsFile);
        startUpdateTask();
    }

    private void startUpdateTask() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Map<String, FlightTracker.AirportStats> stats = tracker.scan();
                for (Map.Entry<FlightSubscriber, Set<String>> entry : subscriptions.entrySet()) {
                    FlightSubscriber subscriber = entry.getKey();
                    Set<String> codes = entry.getValue();
                    
                    for (String code : codes) {
                        if (stats.containsKey(code)) {
                            FlightTracker.AirportStats s = stats.get(code);
                            subscriber.update(
                                code, 
                                s.arrivals.size(), 
                                s.departures.size(),
                                new ArrayList<>(s.arrivals),
                                new ArrayList<>(s.departures)
                            );
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in update task: " + e.getMessage());
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void subscribe(List<String> airportCodes, FlightSubscriber subscriber) throws RemoteException {
        subscriptions.put(subscriber, new HashSet<>(airportCodes));
        System.out.println("New subscription: " + airportCodes + " for " + subscriber);
    }

    @Override
    public void unsubscribe(FlightSubscriber subscriber) throws RemoteException {
        subscriptions.remove(subscriber);
        System.out.println("Unsubscribed: " + subscriber);
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: FlightServer <airports.txt> <flights.txt>");
            return;
        }

        try {
            System.setProperty("java.rmi.server.hostname", "cssmpi1");
            FlightServer server = new FlightServer(args[0], args[1]);
            FlightMonitor stub = (FlightMonitor) UnicastRemoteObject.exportObject(server, 0);
            
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("FlightMonitor", stub);
            
            System.out.println("🚀 RMI FlightServer is running...");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}