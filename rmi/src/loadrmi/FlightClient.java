package loadrmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class FlightClient implements FlightSubscriber {
    private FlightMonitor monitor;
    private boolean running = true;

    public FlightClient() throws RemoteException {
        UnicastRemoteObject.exportObject(this, 0);
    }

    public void connect() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("cssmpi1");
        monitor = (FlightMonitor) registry.lookup("FlightMonitor");
    }

    @Override
    public void update(String airportCode, int arrivals, int departures, 
                     List<String> arrivingFlights, List<String> departingFlights) throws RemoteException {
        System.out.printf("[%s] 📍 %s | Arrivals: %d %s | Departures: %d %s%n",
                new Date(),
                airportCode,
                arrivals, arrivingFlights,
                departures, departingFlights);
    }

    public void subscribe(List<String> airportCodes) throws RemoteException {
        monitor.subscribe(airportCodes, this);
    }

    public void unsubscribe() throws RemoteException {
        monitor.unsubscribe(this);
    }

    public void shutdown() {
        running = false;
    }

    public static void main(String[] args) {
        try {
            FlightClient client = new FlightClient();
            client.connect();

            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter airport codes to subscribe (e.g. LAX JFK), or type 'exit' to quit:");

            while (client.running) {
                System.out.print("> ");
                String line = scanner.nextLine().trim();
                if (line.equalsIgnoreCase("exit")) {
                    client.unsubscribe();
                    client.shutdown();
                    break;
                }

                List<String> codes = Arrays.stream(line.split("\\s+"))
                        .map(String::toUpperCase)
                        .toList();
                client.subscribe(codes);
                System.out.println("🔄 Updated subscription to: " + codes);
            }

            scanner.close();
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}