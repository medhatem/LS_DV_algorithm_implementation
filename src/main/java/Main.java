import host.Host;
import router.GeneralRouter;
import router.RouterBuilder;
import util.FullAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String... args) throws UnknownHostException, InterruptedException {
        ExecutorService executorService;
        Scanner scanner;
        InetAddress localHost;
        Map<String, FullAddress> addressMap;
        Host host1, host2;
        RouterBuilder builderA, builderB, builderC, builderD, builderE, builderF;
        GeneralRouter routerA, routerB, routerC, routerD, routerE, routerF;

        System.out.println("IFT585 - TP2");
        System.out.println("By Christophe Pigeon, Marc-Antoine Dugal & Mohamed Hatem Diabi");

        scanner = new Scanner(System.in);
        localHost = InetAddress.getLocalHost();

        addressMap = Stream.of(
                new AbstractMap.SimpleImmutableEntry<>("1", new FullAddress(localHost, 50000)),
                new AbstractMap.SimpleImmutableEntry<>("2", new FullAddress(localHost, 50001)),
                new AbstractMap.SimpleImmutableEntry<>("A", new FullAddress(localHost, 50002)),
                new AbstractMap.SimpleImmutableEntry<>("B", new FullAddress(localHost, 50003)),
                new AbstractMap.SimpleImmutableEntry<>("C", new FullAddress(localHost, 50004)),
                new AbstractMap.SimpleImmutableEntry<>("D", new FullAddress(localHost, 50005)),
                new AbstractMap.SimpleImmutableEntry<>("E", new FullAddress(localHost, 50006)),
                new AbstractMap.SimpleImmutableEntry<>("F", new FullAddress(localHost, 50007)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        builderA = new RouterBuilder().with(consumer -> {
            consumer.address = addressMap.get("A");

            consumer.routingMap = new HashMap<>();
            consumer.addEntry(addressMap.get("1"), 0);
            consumer.addEntry(addressMap.get("A"), 0);
            consumer.addEntry(addressMap.get("B"), 5);
            consumer.addEntry(addressMap.get("C"), Integer.MAX_VALUE);
            consumer.addEntry(addressMap.get("D"), 45);
            consumer.addEntry(addressMap.get("E"), Integer.MAX_VALUE);
            consumer.addEntry(addressMap.get("F"), Integer.MAX_VALUE);
        });

        builderB = new RouterBuilder().with(consumer -> {
            consumer.address = addressMap.get("B");

            consumer.routingMap = new HashMap<>();
            consumer.addEntry(addressMap.get("A"), 5);
            consumer.addEntry(addressMap.get("B"), 0);
            consumer.addEntry(addressMap.get("C"), 70);
            consumer.addEntry(addressMap.get("D"), Integer.MAX_VALUE);
            consumer.addEntry(addressMap.get("E"), 3);
            consumer.addEntry(addressMap.get("F"), Integer.MAX_VALUE);
        });

        builderC = new RouterBuilder().with(consumer -> {
            consumer.address = addressMap.get("C");

            consumer.routingMap = new HashMap<>();
            consumer.addEntry(addressMap.get("A"), Integer.MAX_VALUE);
            consumer.addEntry(addressMap.get("B"), 70);
            consumer.addEntry(addressMap.get("C"), 0);
            consumer.addEntry(addressMap.get("D"), 50);
            consumer.addEntry(addressMap.get("E"), Integer.MAX_VALUE);
            consumer.addEntry(addressMap.get("F"), 78);
        });

        builderD = new RouterBuilder().with(consumer -> {
            consumer.address = addressMap.get("D");

            consumer.routingMap = new HashMap<>();
            consumer.addEntry(addressMap.get("A"), 45);
            consumer.addEntry(addressMap.get("B"), Integer.MAX_VALUE);
            consumer.addEntry(addressMap.get("C"), 50);
            consumer.addEntry(addressMap.get("D"), 0);
            consumer.addEntry(addressMap.get("E"), 8);
            consumer.addEntry(addressMap.get("F"), Integer.MAX_VALUE);
        });

        builderE = new RouterBuilder().with(consumer -> {
            consumer.address = addressMap.get("E");

            consumer.routingMap = new HashMap<>();
            consumer.addEntry(addressMap.get("A"), Integer.MAX_VALUE);
            consumer.addEntry(addressMap.get("B"), 3);
            consumer.addEntry(addressMap.get("C"), Integer.MAX_VALUE);
            consumer.addEntry(addressMap.get("D"), 8);
            consumer.addEntry(addressMap.get("E"), 0);
            consumer.addEntry(addressMap.get("F"), 7);
        });

        builderF = new RouterBuilder().with(consumer -> {
            consumer.address = addressMap.get("F");

            consumer.routingMap = new HashMap<>();
            consumer.addEntry(addressMap.get("2"), 0);
            consumer.addEntry(addressMap.get("A"), Integer.MAX_VALUE);
            consumer.addEntry(addressMap.get("B"), Integer.MAX_VALUE);
            consumer.addEntry(addressMap.get("C"), 78);
            consumer.addEntry(addressMap.get("D"), Integer.MAX_VALUE);
            consumer.addEntry(addressMap.get("E"), 7);
            consumer.addEntry(addressMap.get("F"), 0);
        });

        System.out.println("Select the algorithm:");
        System.out.println("0 - DV");
        System.out.println("1 - LS (default)");

        switch (scanner.nextInt()) {
            case 0:
                routerA = builderA.createDistanceVectorRouter();
                routerB = builderB.createDistanceVectorRouter();
                routerC = builderC.createDistanceVectorRouter();
                routerD = builderD.createDistanceVectorRouter();
                routerE = builderE.createDistanceVectorRouter();
                routerF = builderF.createDistanceVectorRouter();
                break;
            case 1:
            default:
                // I arbitrarily choose that LS was the default algorithm
                routerA = builderA.createLinkStateRouter();
                routerB = builderB.createLinkStateRouter();
                routerC = builderC.createLinkStateRouter();
                routerD = builderD.createLinkStateRouter();
                routerE = builderE.createLinkStateRouter();
                routerF = builderF.createLinkStateRouter();
                break;
        }

        host1 = new Host(addressMap.get("1"), addressMap.get("A"));
        host2 = new Host(addressMap.get("2"), addressMap.get("F"));

        executorService = Executors.newFixedThreadPool(8);
        executorService.submit(routerA);
        executorService.submit(routerB);
        executorService.submit(routerC);
        executorService.submit(routerD);
        executorService.submit(routerE);
        executorService.submit(routerF);

        System.out.println("waiting 10 seconds for the configure task (DV and LS algorithm) to finish");
        TimeUnit.SECONDS.sleep(10);

        executorService.submit(host2.receive());
        executorService.submit(host1.send("Hello World!", addressMap.get("2")));

        try {
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            System.err.println("tasks interrupted");

        } finally {
            if (!executorService.isTerminated()) {
                System.err.println("cancel non-finished tasks");
            }
            executorService.shutdownNow();
        }
    }
}
