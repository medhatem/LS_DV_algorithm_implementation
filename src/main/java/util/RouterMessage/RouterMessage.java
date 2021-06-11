package util.RouterMessage;

import util.FullAddress;
import util.RouterTable.RouterTableEntry;

import java.io.Serializable;
import java.util.Map;

public class RouterMessage implements Serializable {
    private final FullAddress address;
    private final Map<FullAddress, RouterTableEntry> routingMap;

    public RouterMessage(FullAddress address, Map<FullAddress, RouterTableEntry> routingMap) {
        this.address = address;
        this.routingMap = routingMap;
    }

    public FullAddress getAddress() {
        return address;
    }

    public Map<FullAddress, RouterTableEntry> getRoutingMap() {
        return routingMap;
    }
}
