import java.net.ServerSocket;
import java.net.Socket;

public class MainController {
    public static void main(String[] args) {
        ChannelManager channelManager = new ChannelManager();
        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(9910);
            System.out.println("Server is running on port " + serverSocket.getLocalPort());

            while (true) {
                socket = serverSocket.accept();
                System.out.println("[" + socket.getLocalSocketAddress() + "/" + socket.getPort() + "] has joined");
                new ChannelReceiver(socket, channelManager).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}