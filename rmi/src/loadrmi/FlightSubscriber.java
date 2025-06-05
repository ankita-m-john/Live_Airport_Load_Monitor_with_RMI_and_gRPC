package loadrmi;

import java.util.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FlightSubscriber extends Remote {
    void update(String airportCode, int arrivals, int departures, 
                List<String> arrivingFlights, List<String> departingFlights) throws RemoteException;
}