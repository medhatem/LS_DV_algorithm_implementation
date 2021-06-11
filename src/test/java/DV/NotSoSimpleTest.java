package DV;

import host.Host;
import router.GeneralRouter;
import router.RouterBuilder;
import util.FullAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NotSoSimpleTest {
    public static void main(String... args) throws UnknownHostException, InterruptedException {
        ExecutorService executorService;
        InetAddress localHost;
        Map<String, FullAddress> addressMap;
        Host host1, host2;
        GeneralRouter routerA, routerB, routerC;

        localHost = InetAddress.getLocalHost();

        addressMap = Stream.of(
                new AbstractMap.SimpleImmutableEntry<>("1", new FullAddress(localHost, 50000)),
                new AbstractMap.SimpleImmutableEntry<>("2", new FullAddress(localHost, 50001)),
                new AbstractMap.SimpleImmutableEntry<>("A", new FullAddress(localHost, 50002)),
                new AbstractMap.SimpleImmutableEntry<>("B", new FullAddress(localHost, 50003)),
                new AbstractMap.SimpleImmutableEntry<>("C", new FullAddress(localHost, 50004)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        host1 = new Host(addressMap.get("1"), addressMap.get("A"));
        host2 = new Host(addressMap.get("2"), addressMap.get("C"));
        routerA = new RouterBuilder().with(consumer -> {
            consumer.address = addressMap.get("A");

            consumer.routingMap = new HashMap<>();
            consumer.addEntry(addressMap.get("1"), 0);
            consumer.addEntry(addressMap.get("A"), 0);
            consumer.addEntry(addressMap.get("B"), 3);
            consumer.addEntry(addressMap.get("C"), Integer.MAX_VALUE);
        }).createDistanceVectorRouter();
        routerB = new RouterBuilder().with(consumer -> {
            consumer.address = addressMap.get("B");

            consumer.routingMap = new HashMap<>();
            consumer.addEntry(addressMap.get("A"), 3);
            consumer.addEntry(addressMap.get("B"), 0);
            consumer.addEntry(addressMap.get("C"), 1);
        }).createDistanceVectorRouter();
        routerC = new RouterBuilder().with(consumer -> {
            consumer.address = addressMap.get("C");

            consumer.routingMap = new HashMap<>();
            consumer.addEntry(addressMap.get("2"), 0);
            consumer.addEntry(addressMap.get("A"), Integer.MAX_VALUE);
            consumer.addEntry(addressMap.get("B"), 1);
            consumer.addEntry(addressMap.get("C"), 0);
        }).createDistanceVectorRouter();

        executorService = Executors.newFixedThreadPool(5);
        executorService.submit(routerA);
        executorService.submit(routerB);
        executorService.submit(routerC);

        // Wait for configure (DV and LS algorithm) to finish
        TimeUnit.SECONDS.sleep(10);

        executorService.submit(host2.receive());
        executorService.submit(host1.send("Hello World!", addressMap.get("2")));

        try {
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.SECONDS);

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
