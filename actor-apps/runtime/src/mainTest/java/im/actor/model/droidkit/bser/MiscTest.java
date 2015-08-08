package im.actor.core.droidkit.bser;

import org.junit.Test;

import static im.actor.core.tests.Util.assertEmptyConstructor;

/**
 * Created by ex3ndr on 08.03.15.
 */
public class MiscTest {
    @Test
    public void testEmptyConstructors() throws Exception {
        assertEmptyConstructor(Utils.class);
        assertEmptyConstructor(Limits.class);
        assertEmptyConstructor(WireTypes.class);
        assertEmptyConstructor(BserParser.class);
        assertEmptyConstructor(Bser.class);
    }
}
