package data;

import com.xxdb.gui.data.ServerVersion;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ServerVersionTest {
    @Before
    public void setUp() {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testServerVersion() throws Exception {
        ServerVersion sv = new ServerVersion("1.00.2");
        Assert.assertEquals(sv.getValue(), 10002);
        sv = new ServerVersion("1.02.2");
        Assert.assertEquals(sv.getValue(), 10202);
        sv = new ServerVersion("1.01.12");
        Assert.assertEquals(10112,sv.getValue());
        sv = new ServerVersion("1.03.12");
        Assert.assertEquals(sv.getValue(), 10312);
        sv = new ServerVersion("2.93.12");
        Assert.assertEquals(sv.getValue(), 29312);
        sv = new ServerVersion("12.03.12");
        Assert.assertEquals(120312, sv.getValue());
    }


}
