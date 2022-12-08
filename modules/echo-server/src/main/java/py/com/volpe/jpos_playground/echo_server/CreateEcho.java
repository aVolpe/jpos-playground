package py.com.volpe.jpos_playground.echo_server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Arturo Volpe
 * @since 2022-12-08
 */
public class CreateEcho {

    private final List<DelayConfig> delayConfigs;
    private final Random random;

    public CreateEcho(List<DelayConfig> delayConfigs) {
        this.delayConfigs = delayConfigs;
        this.random = new SecureRandom();
    }

    public boolean pipe(InputStream msg, boolean includeLength, OutputStream os) throws IOException, InterruptedException {

        byte[] full;
        byte[] msgLengthAsBytes = new byte[2];
        int length;
        // if include length, the length are the two first bytes
        if (includeLength) {
            int readed = msg.read(msgLengthAsBytes);
            if (readed == -1) return false;
            if (readed != 2)
                throw new IOException("Can't read the length of the msg, expected 2, readed %d".formatted(readed));
            length = byte2int(msgLengthAsBytes);
            full = new byte[length];
            int realLength = msg.read(full);
            if (realLength != length) {
                throw new IOException("The message should has a length of %d but was %d".formatted(length, realLength));
            }
        } else {
            full = msg.readAllBytes();
        }


        double chance = random.nextDouble(100d);
        for (DelayConfig dc : this.delayConfigs) {
            if (chance > dc.percentile && dc.sleep > 0) {
                System.out.println("The percentile %f , sleeping for %d".formatted(chance, dc.sleep));
                TimeUnit.MILLISECONDS.sleep(dc.sleep);
                break;
            }
        }

        full[1] = 0x10;


        if (includeLength) {
            os.write(msgLengthAsBytes);
        }

        os.write(full, 0, 1);
        os.write(new byte[]{0x10});
        os.write(full, 2, full.length - 2);
        return true;
    }

    /**
     * Copied from ISOUtil.byte2int to remove dependency to jpos
     */
    public static int byte2int(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return 0;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        for (int i = 0; i < 4 - bytes.length; i++) {
            byteBuffer.put((byte) 0);
        }
        for (int i = 0; i < bytes.length; i++) {
            byteBuffer.put(bytes[i]);
        }
        byteBuffer.position(0);
        return byteBuffer.getInt();
    }

    public record DelayConfig(double percentile, int sleep) {
    }
}
