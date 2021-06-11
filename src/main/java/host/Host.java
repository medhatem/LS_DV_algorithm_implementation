package host;

import util.FullAddress;
import util.HostMessage.HostMessage;
import util.HostMessage.HostMessageInterpreter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class Host {
    private static final Integer maximumBufferSize = 1500;
    private static final Integer timeout = 5000;

    private final FullAddress address;
    private final FullAddress gatewayAddress;

    public Host(FullAddress address, FullAddress gatewayAddress) {
        this.address = address;
        this.gatewayAddress = gatewayAddress;
    }
    
    public Runnable receive() {
        return () -> {
            byte[] bytes;
            DatagramPacket packet;
            HostMessage message;

            try (DatagramSocket socket = new DatagramSocket(address.getPort(), address.getAddress())) {
                socket.setSoTimeout(timeout);

                bytes = new byte[maximumBufferSize];
                packet = new DatagramPacket(bytes, bytes.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    System.err.println("Host at address: " + address + " got a timeout");
                    return;
                }

                if (packet.getAddress().equals(gatewayAddress.getAddress()) && packet.getPort() == gatewayAddress.getPort()) {
                    message = HostMessageInterpreter.fromBytes(packet.getData());
                    System.out.println("Host at address: " + address + " received message: " + message.getMessage());
                } else {
                    System.err.println("Host at address: " + address + " received message from unknown address");
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        };
    }

    public Runnable send(String messageString, FullAddress destination) {
        return () -> {
            byte[] bytes;
            DatagramPacket packet;
            HostMessage message;

            try (DatagramSocket socket = new DatagramSocket(address.getPort(), address.getAddress())) {
                message = new HostMessage(address, destination, messageString);
                bytes = HostMessageInterpreter.toBytes(message);
                packet = new DatagramPacket(bytes, bytes.length, gatewayAddress.getAddress(), gatewayAddress.getPort());

                socket.send(packet);
                System.out.println("Host at address: " + address + " sent HostMessage: " + messageString);

            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }
}
