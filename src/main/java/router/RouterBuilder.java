package router;

import util.FullAddress;
import util.RouterTable.RouterTableEntry;

import java.util.Map;
import java.util.function.Consumer;

public class RouterBuilder {
    public FullAddress address;
    public Map<FullAddress, RouterTableEntry> routingMap;

    public RouterBuilder with(Consumer<RouterBuilder> consumer) {
        consumer.accept(this);
        return this;
    }

    public DistanceVectorRouter createDistanceVectorRouter() {
        return new DistanceVectorRouter(address, routingMap);
    }

    public LinkStateRouter createLinkStateRouter() {
        return new LinkStateRouter(address, routingMap);
    }

    public void addEntry(FullAddress address, Integer hopCost) {
        routingMap.put(address, new RouterTableEntry(address, hopCost));
    }
}
