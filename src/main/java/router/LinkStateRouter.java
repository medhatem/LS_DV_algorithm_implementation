package router;

import util.FullAddress;
import util.RouterTable.RouterTableEntry;
import util.RouterMessage.RouterMessage;
import util.RouterMessage.RouterMessageInterpreter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.stream.Collectors;

public class LinkStateRouter extends GeneralRouter {

    public LinkStateRouter(FullAddress address, Map<FullAddress, RouterTableEntry> routingMap) {
        super(address, routingMap);
    }

    @Override
    protected void configure() {
        List<FullAddress> nodes;
        List<FullAddress> routers;
        Map<FullAddress, RouterTableEntry> routingMapToSend;
        Map<FullAddress, Map<FullAddress, RouterTableEntry>> routerMessages;

        System.out.println("Router at address: " + address + " started the configuration task using LS");

        routers = routingMap
                .entrySet()
                .stream()
                .filter(e -> e.getValue().getHopCost() > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        routingMapToSend = routingMap
                .entrySet()
                .stream()
                .filter(e -> e.getValue().getHopCost() < Integer.MAX_VALUE)
                .filter(e -> !e.getKey().equals(address))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        nodes = new ArrayList<>(routers);
        routerMessages = new HashMap<>();

        try (DatagramSocket socket = new DatagramSocket(address.getPort(), address.getAddress())) {
            socket.setSoTimeout(timeout);

            for (FullAddress router : routers) {
                byte[] bytes;
                RouterMessage message;
                DatagramPacket packet;

                message = new RouterMessage(address, routingMapToSend);
                bytes = RouterMessageInterpreter.toBytes(message);
                packet = new DatagramPacket(bytes, bytes.length, router.getAddress(), router.getPort());
                socket.send(packet);
            }

            for (int i = 0; i < routers.size(); i++) {
                byte[] bytes;
                DatagramPacket packet;
                RouterMessage message;

                bytes = new byte[maximumBufferSize];
                packet = new DatagramPacket(bytes, bytes.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    System.out.println("Router at address: " + address + " got a timeout");
                    continue;
                }

                message = RouterMessageInterpreter.fromBytes(packet.getData());
                routerMessages.put(message.getAddress(), message.getRoutingMap());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        do {
            FullAddress minAddress = null;
            Integer minValue = Integer.MAX_VALUE;

            for (FullAddress address : nodes) {
                RouterTableEntry entry = routingMap.get(address);

                if (minAddress == null || entry.getHopCost() < minValue) {
                    minAddress = address;
                    minValue = entry.getHopCost();
                }
            }

            FullAddress lambdaAddress = routingMap.get(minAddress).getAddress();
            Integer lambdaHopCost = routingMap.get(minAddress).getHopCost();

            routerMessages.get(minAddress).forEach((address, tableEntry) -> {
                tableEntry.setAddress(lambdaAddress);
                tableEntry.setHopCost(tableEntry.getHopCost() + lambdaHopCost);

                routingMap.merge(address, tableEntry, (v1, v2) -> {
                    if (v1.getHopCost() <= v2.getHopCost()) {
                        return new RouterTableEntry(v1.getAddress(), v1.getHopCost());
                    } else {
                        return new RouterTableEntry(v2.getAddress(), v2.getHopCost());
                    }
                });
            });

            nodes.remove(minAddress);
        } while (nodes.size() > 0);

        System.out.println("Router at address: " + address + " finish the configuration task using LS");
    }
}
