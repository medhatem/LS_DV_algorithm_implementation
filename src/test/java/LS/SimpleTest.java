package LS;

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

public class SimpleTest {
    public static void main(String... args) throws UnknownHostException, InterruptedException {
        ExecutorService executorService;
        InetAddress localHost;
        Map<String, FullAddress> addressMap;
        Host host1, host2;
        GeneralRouter routerA, routerB;

        localHost = InetAddress.getLocalHost();

        addressMap = Stream.of(
                new AbstractMap.SimpleImmutableEntry<>("1", new FullAddress(localHost, 50000)),
                new AbstractMap.SimpleImmutableEntry<>("2", new FullAddress(localHost, 50001)),
                new AbstractMap.SimpleImmutableEntry<>("A", new FullAddress(localHost, 50002)),
                new AbstractMap.SimpleImmutableEntry<>("B", new FullAddress(localHost, 50003)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        host1 = new Host(addressMap.get("1"), addressMap.get("A"));
        host2 = new Host(addressMap.get("2"), addressMap.get("B"));
        routerA = new RouterBuilder().with(consumer -> {
            consumer.address = addressMap.get("A");

            consumer.routingMap = new HashMap<>();
            consumer.addEntry(addressMap.get("1"), 0);
            consumer.addEntry(addressMap.get("A"), 0);
            consumer.addEntry(addressMap.get("B"), 1);
        }).createLinkStateRouter();
        routerB = new RouterBuilder().with(consumer -> {
            consumer.address = addressMap.get("B");

            consumer.routingMap = new HashMap<>();
            consumer.addEntry(addressMap.get("2"), 0);
            consumer.addEntry(addressMap.get("A"), 1);
            consumer.addEntry(addressMap.get("B"), 0);
        }).createLinkStateRouter();

        executorService = Executors.newFixedThreadPool(4);
        executorService.submit(routerA);
        executorService.submit(routerB);

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
