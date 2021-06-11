package util.RouterTable;

import util.FullAddress;

import java.io.Serializable;

public class RouterTableEntry implements Serializable {
    private FullAddress address;
    private Integer hopCost;

    public RouterTableEntry(FullAddress address, Integer hopCost) {
        this.address = address;
        this.hopCost = hopCost;
    }

    public FullAddress getAddress() {
        return address;
    }

    public void setAddress(FullAddress address) {
        this.address = address;
    }

    public Integer getHopCost() {
        return hopCost;
    }

    public void setHopCost(Integer hopCost) {
        this.hopCost = hopCost;
    }
}
