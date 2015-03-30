package im.actor.model.droidkit.bser;

import org.junit.Test;

/**
 * Created by ex3ndr on 08.03.15.
 */
public class ExceptionsTest {
    @Test
    public void testExceptions() throws Exception {
        new IncorrectTypeException("");
        new IncorrectTypeException("", new Exception());
        new IncorrectTypeException(new Exception());
        new IncorrectTypeException();

        new UnknownFieldException("");
        new UnknownFieldException("", new Exception());
        new UnknownFieldException(new Exception());
        new UnknownFieldException();
    }
}
