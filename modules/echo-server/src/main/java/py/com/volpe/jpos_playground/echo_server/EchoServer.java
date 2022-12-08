package py.com.volpe.jpos_playground.echo_server;

import py.com.volpe.jpos_playground.echo_server.CreateEcho.DelayConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Arturo Volpe
 * @since 2022-12-08
 */
public class EchoServer {

    AtomicBoolean sem = new AtomicBoolean(true);

    public static void main(String[] args) throws Exception {
        new EchoServer().start();
    }

    public void stop() {
        sem.set(false);
    }

    public void start() throws Exception {

        int port = Optional.ofNullable(System.getenv("ECHO_SERVER_PORT")).map(Integer::parseInt).orElse(10_000);
        System.out.printf("Listening on port %d%n", port);

        try (var serverSocket = new ServerSocket(port)) {
            while (sem.get())
                new EchoClientHandler(serverSocket.accept())
                        .start();
        }
    }

    public static class EchoClientHandler extends Thread {

        final Socket socket;

        public EchoClientHandler(Socket socket) {
            this.socket = socket;
        }


        @Override
        public void run() {
            try {
                CreateEcho echo = new CreateEcho(Arrays.asList(
                        new DelayConfig(95d, 1000),// 5% of the time sleep 1000 miliseconds
                        new DelayConfig(90d, 100), // 10% of the time sleep 100 miliseconds,
                        new DelayConfig(0d, 0) // the rest of the time don't sleep
                ));
                while (echo.pipe(socket.getInputStream(), true, socket.getOutputStream())) {
                    System.out.println("msg sended");
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
