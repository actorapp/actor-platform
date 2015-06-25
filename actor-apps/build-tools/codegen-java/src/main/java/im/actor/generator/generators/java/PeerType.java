package im.actor.generator.generators.java;
import java.io.IOException;

public enum PeerType {

    PRIVATE(1),
    GROUP(2);

    private int value;

    PeerType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PeerType parse(int value) throws IOException {
        switch(value) {
            case 1: return PeerType.PRIVATE;
            case 2: return PeerType.GROUP;
        }
        throw new IOException();
    }
}
