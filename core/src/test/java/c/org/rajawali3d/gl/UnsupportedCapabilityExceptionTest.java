package c.org.rajawali3d.gl;

import android.support.test.filters.SmallTest;
import c.org.rajawali3d.gl.Capabilities.UnsupportedCapabilityException;
import org.junit.Test;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SmallTest
public class UnsupportedCapabilityExceptionTest {

    @Test(expected = UnsupportedCapabilityException.class)
    public void constructorNoArgs() throws Exception {
        throw new UnsupportedCapabilityException();
    }

    @Test(expected = UnsupportedCapabilityException.class)
    public void constructorMessage() throws Exception {
        throw new UnsupportedCapabilityException("THIS IS AN EXCEPTION");
    }

    @Test(expected = UnsupportedCapabilityException.class)
    public void constructorMessageThrowable() throws Exception {
        throw new UnsupportedCapabilityException("MESSAGE", new Throwable("CAUSE"));
    }

    @Test(expected = UnsupportedCapabilityException.class)
    public void constructorThrowable() throws Exception {
        throw new UnsupportedCapabilityException(new Throwable("CAUSE"));
    }
}