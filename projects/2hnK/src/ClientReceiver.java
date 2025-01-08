import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Time;

public class ClientReceiver extends Thread {
    DataInputStream inputStream;
    Socket socket;

    ClientReceiver(Socket socket) {
        try {
            this.socket = socket;
            inputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                String msg = inputStream.readUTF();

                String[] command = msg.split(" ", 2);
                if ("PONG".equalsIgnoreCase(command[0])) {
                    long ping = calculatePing(command[1]);
                    System.out.println("Ping: " + ping + "ms");
                } else {
                    System.out.println(msg);
                }
            }
        } catch (IOException e) {
            System.out.println("Disconnected from server.");
        } finally {
            closeResources();
        }
    }

    public long calculatePing(String Time) {
        long serverTime = Long.parseLong(Time);
        long currentTime = System.currentTimeMillis();
        return currentTime - serverTime;
    }

    private void closeResources() {
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Closed resources.");
    }
}
