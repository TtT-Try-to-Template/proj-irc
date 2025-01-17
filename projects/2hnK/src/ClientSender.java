import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientSender extends Thread {
    DataOutputStream outputStream;
    Socket socket;

    ClientSender(Socket socket) {
        try {
            this.socket = socket;
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                Scanner sc = new Scanner(System.in);
                String msg = sc.nextLine();
                outputStream.writeUTF(msg);

                if ("QUIT".equalsIgnoreCase(msg)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Disconnected from server.");
        } finally {
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (outputStream != null) outputStream.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("An Exception occurred during closing resources.");
        }
        System.out.println("Closed resources.");
    }
}
