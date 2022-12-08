package py.com.volpe.jpos_playground.echo_server;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;
import org.junit.jupiter.api.Test;
import py.com.volpe.jpos_playground.echo_server.CreateEcho.DelayConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateEchoTest {

    @Test
    void test_echo_msg() throws Exception {

        ISOMsg msg = new ISOMsg();
        msg.setMTI("2100");
        msg.set(11, "123456123456");
        msg.set(37, "123456123456");
        msg.set(41, "123456123456");

        GenericPackager gp = new GenericPackager("jar:packager/cmf.xml");
        msg.setPackager(gp);

        msg.recalcBitMap();
        msg.dump(new PrintStream(System.out), ">>>");

        try (InputStream is = new ByteArrayInputStream(msg.pack());
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            new CreateEcho(Arrays.asList(
                    new DelayConfig(95d, 1000),// 5% of the time sleep 1000 miliseconds
                    new DelayConfig(90d, 100), // 10% of the time sleep 100 miliseconds,
                    new DelayConfig(0d, 0) // the rest of the time don't sleep
            )).pipe(is, false, os);

            ISOMsg echoed = new ISOMsg();
            echoed.setPackager(gp);

            byte[] asArr = os.toByteArray();
            System.out.println(ISOUtil.hexdump(asArr));
            gp.unpack(echoed, asArr);

            echoed.dump(new PrintStream(System.out), "<<<");

            assertEquals("2110", echoed.getMTI());
            assertEquals(msg.getString(11), echoed.getString(11));
            assertEquals(msg.getString(37), echoed.getString(37));
        }


    }

}
