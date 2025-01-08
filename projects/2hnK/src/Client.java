import java.io.IOException;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        Socket socket;

        try {
            socket = new Socket("127.0.0.1", 9910);
            System.out.println("Client connected to server on port " + socket.getPort());

            ClientSender sender = new ClientSender(socket);
            System.out.println("Sender is set");
            ClientReceiver receiver = new ClientReceiver(socket);
            System.out.println("Receiver is set");

            sender.start();
            System.out.println("Sender is started");
            receiver.start();
            System.out.println("Receiver is started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
