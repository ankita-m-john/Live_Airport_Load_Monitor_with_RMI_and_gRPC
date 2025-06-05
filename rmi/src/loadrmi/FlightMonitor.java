package loadrmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface FlightMonitor extends Remote {
    void subscribe(List<String> airportCodes, FlightSubscriber subscriber) throws RemoteException;
    void unsubscribe(FlightSubscriber subscriber) throws RemoteException;
}