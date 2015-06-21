package im.actor.model.droidkit.bser;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.model.droidkit.bser.util.SparseArray;
import im.actor.model.tests.EmptyBserObj;
import im.actor.model.tests.MockSerialization;

import static im.actor.model.tests.Util.assertContent;
import static im.actor.model.tests.Util.assertSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by ex3ndr on 08.03.15.
 */
public class BserWriterTest {

    private final MockSerialization DEFAULT = new MockSerialization() {
        @Override
        protected void perform(BserWriter writer) throws IOException {
            writer.writeInt(1, 1);
            writer.writeInt(1, 2);
            writer.writeInt(1, 3);
            writer.writeInt(1, 4);
            writer.writeInt(1, 5);

            writer.writeInt(2, 6);

            writer.writeBytes(3, new byte[0]);
            writer.writeBytes(3, new byte[0]);

            writer.writeLongFixed(4, 0);
            writer.writeIntFixed(5, 0);

            writer.writeString(6, "???");

            writer.writeBool(7, true);
            writer.writeBool(7, false);

            writer.writeDouble(8, 0);

            writer.writeLong(9, 0);

            writer.writeObject(10, new EmptyBserObj());

            List<EmptyBserObj> objs = new ArrayList<EmptyBserObj>();
            objs.add(new EmptyBserObj());
            writer.writeRepeatedObj(11, objs);
        }
    };

    private final MockSerialization ERROR = new MockSerialization() {
        @Override
        protected void perform(BserWriter writer) throws IOException {
            writer.writeRaw(new byte[]{1 << 3 | 3});
        }
    };

    @Test
    public void testWriter() throws Exception {
        SparseArray<Object> values = DEFAULT.buildArray();

        assertEquals(11, values.size());

        assertTrue(values.get(1) instanceof List);
        assertContent(values.get(1), Long.class);
        assertSize(values.get(1), 5);

        assertTrue(values.get(2) instanceof Long);

        assertTrue(values.get(3) instanceof List);
        assertContent(values.get(3), byte[].class);
        assertSize(values.get(3), 2);

        assertTrue(values.get(4) instanceof Long);
        assertTrue(values.get(5) instanceof Long);

        try {
            ERROR.buildArray();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        new MockSerialization() {
            @Override
            protected void perform(BserWriter writer) throws IOException {
                writer.writeLong(1, -1);
            }
        }.buildArray();

        new MockSerialization() {
            @Override
            protected void perform(BserWriter writer) throws IOException {
                List<Boolean> b = new ArrayList<Boolean>();
                b.add(true);
                writer.writeRepeatedBool(1, b);

                List<Long> l = new ArrayList<Long>();
                l.add(0L);
                writer.writeRepeatedLong(1, l);

                List<Integer> i = new ArrayList<Integer>();
                i.add(0);
                writer.writeRepeatedInt(1, i);
            }
        }.buildArray();
    }

    @Test
    public void testIncorrect() throws Exception {

        try {
            new BserWriter(null);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            new MockSerialization() {
                @Override
                protected void perform(BserWriter writer) throws IOException {
                    writer.writeInt(0, 0);
                }
            }.buildArray();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            new MockSerialization() {
                @Override
                protected void perform(BserWriter writer) throws IOException {
                    writer.writeBytes(1, null);
                }
            }.buildArray();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            new MockSerialization() {
                @Override
                protected void perform(BserWriter writer) throws IOException {
                    writer.writeString(1, null);
                }
            }.buildArray();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            new MockSerialization() {
                @Override
                protected void perform(BserWriter writer) throws IOException {
                    writer.writeRepeatedLong(1, null);
                }
            }.buildArray();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            new MockSerialization() {
                @Override
                protected void perform(BserWriter writer) throws IOException {
                    writer.writeRepeatedInt(1, null);
                }
            }.buildArray();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            new MockSerialization() {
                @Override
                protected void perform(BserWriter writer) throws IOException {
                    writer.writeRepeatedBool(1, null);
                }
            }.buildArray();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            new MockSerialization() {
                @Override
                protected void perform(BserWriter writer) throws IOException {
                    writer.writeRepeatedObj(1, null);
                }
            }.buildArray();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            new MockSerialization() {
                @Override
                protected void perform(BserWriter writer) throws IOException {
                    writer.writeObject(1, null);
                }
            }.buildArray();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            new MockSerialization() {
                @Override
                protected void perform(BserWriter writer) throws IOException {
                    writer.writeRaw(null);
                }
            }.buildArray();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            new MockSerialization() {
                @Override
                protected void perform(BserWriter writer) throws IOException {
                    List list = new ArrayList();
                    for (int i = 0; i < 2000; i++) {
                        list.add(null);
                    }
                    writer.writeRepeatedLong(1, list);
                }
            }.buildArray();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            new MockSerialization() {
                @Override
                protected void perform(BserWriter writer) throws IOException {
                    List list = new ArrayList();
                    for (int i = 0; i < 2000; i++) {
                        list.add(null);
                    }
                    writer.writeRepeatedInt(1, list);
                }
            }.buildArray();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            new MockSerialization() {
                @Override
                protected void perform(BserWriter writer) throws IOException {
                    List list = new ArrayList();
                    for (int i = 0; i < 2000; i++) {
                        list.add(null);
                    }
                    writer.writeRepeatedObj(1, list);
                }
            }.buildArray();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            new MockSerialization() {
                @Override
                protected void perform(BserWriter writer) throws IOException {
                    List list = new ArrayList();
                    for (int i = 0; i < 2000; i++) {
                        list.add(null);
                    }
                    writer.writeRepeatedBool(1, list);
                }
            }.buildArray();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            new MockSerialization() {
                @Override
                protected void perform(BserWriter writer) throws IOException {
                    writer.writeBytes(1, new byte[2048 * 2048 + 1]);
                }
            }.buildArray();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }
    }

    @Test
    public void testLimitsBool() throws Exception {
        DataOutput dataOutput = new DataOutput();
        BserWriter writer = new BserWriter(dataOutput);
        writer.writeBool(32, true);
        writer.writeBytes(33, new byte[26]);
        byte[] data = dataOutput.toByteArray();
        BserParser.deserialize(new DataInput(data));
    }
}
