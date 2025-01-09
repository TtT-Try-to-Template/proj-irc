import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class ChannelReceiver extends Thread {
    Socket socket;
    DataInputStream inputStream;
    DataOutputStream outputStream;
    ChannelManager channelManager;
    String currentChannel = "null";
    String nickname;

    ChannelReceiver(Socket socket, ChannelManager channelManager) {
        this.socket = socket;
        this.channelManager = channelManager;
        nickname = "unknown";

        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        initializeConnection();
    }

    public void sendToClient(String msg) {
        try {
            outputStream.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processingCommand(String msg) {
        String[] command = msg.split(" ", 2);

        switch (command[0].toUpperCase()) {
            case "HELP":
                // TODO: [HELP] get command
                getHelp();
                break;

            case "LIST":
                //TODO: [LIST] get channel list
                Set<String> channels = channelManager.getChannel();
                sendToClient("<Channel List>");
                if (!channels.isEmpty()) {
                    int i = 1;
                    for (String ch : channels) {
                        sendToClient(i++ + ". " + ch);
                    }
                } else {
                    sendToClient("There is no channel.");
                    sendToClient("Create a channel using the 'JOIN <channel>' command.");
                }
                break;

            case "JOIN":
                // TODO: [JOIN] join channel
                if (!(command.length == 2)) {
                    sendToClient("JOIN <channel>");
                } else if (nickname.equals("unknown")) {
                    sendToClient("Please set a nickname");
                } else {
                    if (currentChannel != null) {
                        channelManager.leaveChannel(currentChannel, outputStream);
                    }

                    currentChannel = command[1].trim();
                    channelManager.joinChannel(currentChannel, outputStream);
                    sendToClient("Joined channel: " + currentChannel);
                }
                break;

            case "PART":
                // TODO: [PART] leave channel
                if (currentChannel != null) {
                    channelManager.leaveChannel(currentChannel, outputStream);
                    sendToClient("Left channel: " + currentChannel);
                    currentChannel = null;
                } else {
                    sendToClient("Current channel is null.");
                }
                channelManager.leaveChannel(currentChannel, outputStream);
                break;

            case "NICK":
                // TODO: [NICK] set nickname
                if (command.length == 2) {
                    String newNickname = command[1].trim();

                    if (channelManager.isNicknameAvailable(newNickname)) {
                        channelManager.unregisterNickname(nickname);
                        channelManager.registerNickname(newNickname, outputStream);
                        nickname = newNickname;
                        sendToClient("Nickname has set to '" + nickname + "'");
                    } else {
                        sendToClient(nickname + " is already use.");
                    }
                } else {
                    sendToClient("NICK <nickname>");
                }
                break;

            case "USER":
                // TODO: [USER] show userinfo
                getUserInfo();
                break;

            case "PING":
                // TODO: [PING] ping test
                try {
                    long serverTime = System.currentTimeMillis();
                    outputStream.writeUTF("PONG " + serverTime);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "PRIVMSG":
                // TODO: [PRIVMSG] whisper
                if (command.length != 2) {
                    sendToClient("PRIVMSG <user> <message>");
                    break;
                }

                String[] whisper = command[1].split(" ", 2);
                if (whisper.length > 1) {
                    String receiver = whisper[0];
                    String content = whisper[1];
                    channelManager.whisper(nickname, receiver, content, this);
                } else {
                    sendToClient("PRIVMSG <user> <message>");
                }

                break;

            case "QUIT":
                // TODO: [QUIT] leave server
                try {
                    sendToClient("See you later!");
                    channelManager.leaveChannel(currentChannel, outputStream);
                    channelManager.unregisterNickname(nickname);
                } finally {
                    try {
                        outputStream.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;

            default:
                // TODO: send message
                if (currentChannel != null) {
                    String message = "[" + nickname + "] " + msg;
                    channelManager.broadcast(currentChannel, message);
                } else {
                    sendToClient("Current channel is null.");
                }
                break;
        }

    }

    public void run() {
        try {
            while (true) {
                String msg = inputStream.readUTF();
                processingCommand(msg);

                if ("QUIT".equalsIgnoreCase(msg)) {
                    closeResources();
                    break;
                }
            }
        } catch (IOException e) {
            closeResources();
        }
    }

    private void closeResources() {
        try {
            System.out.println("[" + socket.getInetAddress() + "/" + socket.getPort() + "] has left");
            channelManager.unregisterNickname(nickname);
            if (outputStream != null) outputStream.close();
            if (inputStream != null) inputStream.close();
            if (!socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.out.println("An Exception occurred during closing resources.");
        }
        System.out.println("Closed resources.");
    }

    public void getHelp() {
        sendToClient("===============<Command List>===============\n");
        sendToClient("0. HELP: get commands");
        sendToClient("1. LIST: get lists");
        sendToClient("2. JOIN <cahnnel>: join channel");
        sendToClient("3. PART: leave channel");
        sendToClient("4. QUIT: leave server");
        sendToClient("5. PING: ping test");
        sendToClient("6. NICK <nickname>: set nickname");
        sendToClient("7. PRIVMSG <user> <message>: send a whisper");
        sendToClient("8. USER: show user info");
        sendToClient("\n============================================");
    }

    public void getUserInfo() {
        sendToClient("* Current nickname: " + nickname);
        sendToClient("* Current channel: " + currentChannel);
    }

    public void initializeConnection() {
        sendToClient("============================================\n");
        sendToClient(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        sendToClient("Have a nice day!");
        getUserInfo();
        sendToClient("\n");
        getHelp();
    }
}
