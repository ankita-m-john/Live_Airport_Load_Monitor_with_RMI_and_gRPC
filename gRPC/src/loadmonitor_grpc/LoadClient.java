package loadmonitor_grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import loadmonitor_grpc.LoadServiceGrpc;
import loadmonitor_grpc.AirportLoad;
import loadmonitor_grpc.LoadRequest;

public class LoadClient {
    public static void main(String[] args) throws Exception {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        LoadServiceGrpc.LoadServiceStub stub = LoadServiceGrpc.newStub(channel);

        StreamObserver<LoadRequest> requestObserver = stub.monitorLoad(new StreamObserver<AirportLoad>() {
            @Override
            public void onNext(AirportLoad msg) {
                System.out.printf("[%s] 📍 %s | Arrivals: %d %s | Departures: %d %s%n",
                        java.time.LocalTime.now(),
                        msg.getAirportCode(),
                        msg.getArrivals(), msg.getArrivingFlightsList(),
                        msg.getDepartures(), msg.getDepartingFlightsList());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Stream error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Server has closed the stream.");
            }
        });

        // Scanner for terminal input
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter airport codes to subscribe (e.g. LAX JFK), or type 'exit' to quit:");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.equalsIgnoreCase("exit")) {
                break;
            }

            // List<String> codes = Arrays.asList(line.split("\\s+"));
            List<String> codes = Arrays.stream(line.split("\\s+"))
                    .map(String::toUpperCase)
                    .toList();
            LoadRequest req = LoadRequest.newBuilder()
                    .addAllAirportCodes(codes)
                    .build();
            requestObserver.onNext(req);
            System.out.println("🔄 Updated subscription to: " + codes);
        }

        // Graceful shutdown
        requestObserver.onCompleted();
        channel.shutdownNow();
        scanner.close();
    }
}
