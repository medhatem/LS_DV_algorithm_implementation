import host.Host;
import util.FullAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class P2PTest {
    public static void main(String... args) throws UnknownHostException {
        ExecutorService executorService;
        InetAddress localHost;
        Map<String, FullAddress> addressMap;
        Host host1, host2;

        localHost = InetAddress.getLocalHost();

        addressMap = Stream.of(
                new AbstractMap.SimpleImmutableEntry<>("1", new FullAddress(localHost, 50000)),
                new AbstractMap.SimpleImmutableEntry<>("2", new FullAddress(localHost, 50001)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        host1 = new Host(addressMap.get("1"), addressMap.get("2"));
        host2 = new Host(addressMap.get("2"), addressMap.get("1"));

        executorService = Executors.newFixedThreadPool(2);
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
