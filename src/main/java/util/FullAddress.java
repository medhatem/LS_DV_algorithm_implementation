package util;

import java.io.Serializable;
import java.net.InetAddress;

public final class FullAddress implements Serializable {
    private final InetAddress address;
    private final Integer port;

    public FullAddress(InetAddress address, Integer port) {
        this.address = address;
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public Integer getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FullAddress) {
            FullAddress temp = (FullAddress) o;
            return getAddress().equals(temp.getAddress()) && getPort().equals(temp.getPort());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return port.hashCode();
    }

    @Override
    public String toString() {
        return getAddress() + " , " + getPort();
    }
}
