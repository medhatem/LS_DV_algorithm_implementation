package router;

import util.FullAddress;
import util.RouterTable.RouterTableEntry;
import util.RouterMessage.RouterMessage;
import util.RouterMessage.RouterMessageInterpreter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class DistanceVectorRouter extends GeneralRouter {
    private static final long duration = 5000;

    public DistanceVectorRouter(FullAddress address, Map<FullAddress, RouterTableEntry> routingMap) {
        super(address, routingMap);
    }

    private void updateRoutingMap(RouterTableEntry entry, Map<FullAddress, RouterTableEntry> map) {
        map.forEach((address, tableEntry) -> {
            tableEntry.setAddress(entry.getAddress());
            tableEntry.setHopCost(tableEntry.getHopCost() + entry.getHopCost());

            routingMap.merge(address, tableEntry, (v1, v2) -> {
                if (v1.getHopCost() <= v2.getHopCost()) {
                    return new RouterTableEntry(v1.getAddress(), v1.getHopCost());
                } else {
                    return new RouterTableEntry(v2.getAddress(), v2.getHopCost());
                }
            });
        });
    }

    @Override
    protected void configure() {
        Instant start;
        Instant finish;
        List<FullAddress> neighbors;
        Map<FullAddress, RouterTableEntry> routingMapToSend;

        System.out.println("Router at address: " + address + " started the configuration task using DV");

        start = Instant.now();
        neighbors = routingMap
                .entrySet()
                .stream()
                .filter(e -> e.getValue().getHopCost() > 0)
                .filter(e -> e.getValue().getHopCost() < Integer.MAX_VALUE)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        try (DatagramSocket socket = new DatagramSocket(address.getPort(), address.getAddress())) {
            socket.setSoTimeout(timeout);

            do {
                routingMapToSend = routingMap
                        .entrySet()
                        .stream()
                        .filter(e -> e.getValue().getHopCost() < Integer.MAX_VALUE)
                        .filter(e -> !e.getKey().equals(address))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                for (FullAddress neighbor : neighbors) {
                    byte[] bytes;
                    RouterMessage message;
                    DatagramPacket packet;

                    message = new RouterMessage(address, routingMapToSend);
                    bytes = RouterMessageInterpreter.toBytes(message);
                    packet = new DatagramPacket(bytes, bytes.length, neighbor.getAddress(), neighbor.getPort());
                    socket.send(packet);
                }

                for (int i = 0, count = 0; i < neighbors.size(); i++) {
                    byte[] bytes;
                    DatagramPacket packet;
                    RouterMessage message;
                    RouterTableEntry entry;

                    bytes = new byte[maximumBufferSize];
                    packet = new DatagramPacket(bytes, bytes.length);

                    try {
                        socket.receive(packet);
                    } catch (SocketTimeoutException e) {
                        System.out.println("Router at address: " + address + " got a timeout");
                        continue;
                    }

                    message = RouterMessageInterpreter.fromBytes(packet.getData());
                    entry = Optional
                            .ofNullable(routingMap.get(message.getAddress()))
                            .orElseThrow(() -> new IOException("received routing table from unknown address"));

                    updateRoutingMap(entry, message.getRoutingMap());
                }
                finish = Instant.now();
            } while (Duration.between(start, finish).toMillis() < duration);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("Router at address: " + address + " finish the configuration task using DV");
    }
}
