package util.HostMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class HostMessageInterpreter {
    public static byte[] toBytes(HostMessage message) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {

            out.writeObject(message);
            out.flush();

            return bos.toByteArray();
        }
    }

    public static HostMessage fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {

            return (HostMessage) in.readObject();
        }
    }
}
