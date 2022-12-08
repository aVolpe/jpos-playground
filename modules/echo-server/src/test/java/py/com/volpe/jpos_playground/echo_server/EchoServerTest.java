package py.com.volpe.jpos_playground.echo_server;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.channel.PostChannel;
import org.jpos.iso.packager.GenericPackager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Arturo Volpe
 * @since 2022-12-08
 */
class EchoServerTest {

    private static PostChannel postChannel;

    @BeforeAll
    static void initServer() throws Exception {

        EchoServer es = new EchoServer();
        // given an ad hoc server
        var thread = new Thread(() -> {
            try {
                es.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

        // given a real jpos channel
        GenericPackager gp = new GenericPackager("jar:packager/cmf.xml");
        postChannel = new PostChannel("localhost", 10_000, gp);
        postChannel.connect();
    }

    @Test
    void testServer() throws Exception {


        for (int i = 0; i < 10_000; i++) {
            ISOMsg toSend = new ISOMsg("2100");
            toSend.set(11, ISOUtil.padleft(Integer.toString(i), 12, '0'));
            toSend.set(37, "123456123456");
            toSend.set(41, "1234561234561111");

            toSend.dump(System.out, ">>>");

            postChannel.send(toSend);


            ISOMsg response = postChannel.receive();

            response.dump(System.out, "<<<");
            assertEquals("2110", response.getMTI());
        }


    }

    @AfterAll
    static void stop() throws IOException {
        if (postChannel != null)
            postChannel.disconnect();
    }
}
