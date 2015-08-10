package im.actor.core.droidkit.bser;

import im.actor.core.tests.EmptyBserObj;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by ex3ndr on 08.03.15.
 */
public class BserObjectTest {
    @Test
    public void testObj() throws Exception {
        new EmptyBserObj().toByteArray();

        try {
            new BserObject() {

                @Override
                public void parse(BserValues values) throws IOException {

                }

                @Override
                public void serialize(BserWriter writer) throws IOException {
                    throw new IOException();
                }
            }.toByteArray();
            throw new AssertionError();
        } catch (Exception e) {
            // It is Ok
        }

    }
}
