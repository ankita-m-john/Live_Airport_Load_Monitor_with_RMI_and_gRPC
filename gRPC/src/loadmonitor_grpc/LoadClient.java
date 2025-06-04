package loadmonitor_grpc;

import loadmonitor_grpc.AirportLoad;
import loadmonitor_grpc.LoadRequest;
import loadmonitor_grpc.LoadServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.List;

public class LoadClient {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: LoadClient <airport1> <airport2> ...");
            return;
        }

        List<String> airportCodes = Arrays.asList(args);
        LoadRequest request = LoadRequest.newBuilder()
                .addAllAirportCodes(airportCodes)
                .build();

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        LoadServiceGrpc.LoadServiceStub stub = LoadServiceGrpc.newStub(channel);

        stub.monitorLoad(request, new StreamObserver<AirportLoad>() {
            @Override
            public void onNext(AirportLoad msg) {
                System.out.printf("📍 %s | Arrivals: %d %s | Departures: %d %s%n",
                        msg.getAirportCode(),
                        msg.getArrivals(), msg.getArrivingFlightsList(),
                        msg.getDepartures(), msg.getDepartingFlightsList());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("Stream closed by server.");
            }
        });

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
