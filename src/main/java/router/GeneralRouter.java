package router;

import util.FullAddress;
import util.RouterTable.RouterTableEntry;
import util.HostMessage.HostMessage;
import util.HostMessage.HostMessageInterpreter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Optional;

public abstract class GeneralRouter extends Thread {
    protected static final Integer timeout = 1000;
    protected static final Integer maximumBufferSize = 1500;

    protected final FullAddress address;
    protected final Map<FullAddress, RouterTableEntry> routingMap;

    public GeneralRouter(FullAddress address, Map<FullAddress, RouterTableEntry> routingMap) {
        this.address = address;
        this.routingMap = routingMap;
    }

    private FullAddress findNextDestination(FullAddress destination) throws IOException {
        return Optional
                .ofNullable(routingMap.get(destination))
                .orElseThrow(() -> new IOException("destination not in routing table")).getAddress();
    }

    @Override
    public void run() {
        byte[] bytes;
        FullAddress destination;
        DatagramPacket packet;
        HostMessage message;

        configure();

        try (DatagramSocket socket = new DatagramSocket(address.getPort(), address.getAddress())) {
            socket.setSoTimeout(timeout);

            while (!Thread.currentThread().isInterrupted()) {
                bytes = new byte[maximumBufferSize];
                packet = new DatagramPacket(bytes, bytes.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) { continue; }

                message = HostMessageInterpreter.fromBytes(packet.getData());
                destination = findNextDestination(message.getFinalDestination());

                System.out.println("Router at address: " + address + " received message: " + message.getMessage());

                bytes = HostMessageInterpreter.toBytes(message);
                packet = new DatagramPacket(bytes, bytes.length, destination.getAddress(), destination.getPort());
                socket.send(packet);

                System.out.println("Router at address: " + address + " sent message: " + message.getMessage());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected abstract void configure();
}
