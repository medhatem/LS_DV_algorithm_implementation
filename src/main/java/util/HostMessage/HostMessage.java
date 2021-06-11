package util.HostMessage;

import util.FullAddress;

import java.io.Serializable;

public class HostMessage implements Serializable {
    private final FullAddress originalSender;
    private final FullAddress finalDestination;
    private final String message;

    public HostMessage(FullAddress originalSender, FullAddress finalDestination, String message) {
        this.originalSender = originalSender;
        this.finalDestination = finalDestination;
        this.message = message;
    }

    public FullAddress getOriginalSender() {
        return originalSender;
    }

    public FullAddress getFinalDestination() {
        return finalDestination;
    }

    public String getMessage() {
        return message;
    }
}
