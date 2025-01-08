import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ChannelManager {
    private HashMap<String, HashSet<DataOutputStream>> channels; // channel name, outputStream
    private HashMap<String, DataOutputStream> users; // nickname, outputStream

    ChannelManager() {
        channels = new HashMap<>();
        users = new HashMap<>();
        Collections.synchronizedMap(channels);
        Collections.synchronizedMap(users);
    }

    public synchronized void joinChannel(String channelName, DataOutputStream outputStream) {
        channels.computeIfAbsent(channelName, k -> new HashSet<>()).add(outputStream);
    }

    public synchronized void leaveChannel(String channelName, DataOutputStream outputStream) {
        if (channelName != null && channels.containsKey(channelName)) {
            channels.get(channelName).remove(outputStream);

            if (channels.get(channelName).isEmpty()) {
                channels.remove(channelName);
            }
        }
    }

    public synchronized Set<String> getChannel() {
        return channels.keySet();
    }

    public synchronized void broadcast(String channelName, String msg) {
        if (channels.containsKey(channelName)) {
            for (DataOutputStream outputStream : channels.get(channelName)) {
                try {
                    outputStream.writeUTF(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void registerNickname(String nickname, DataOutputStream outputStream) {
        users.put(nickname, outputStream);
    }

    public synchronized void unregisterNickname(String nickname) {
        users.remove(nickname);
    }

    public synchronized boolean isNicknameAvailable(String nickname) {
        return !users.containsKey(nickname);
    }

    public synchronized void whisper(String from, String to, String content, ChannelReceiver channelReceiver) {


        if (!users.containsKey("from")) {
            channelReceiver.sendToClient("User '" + from + "' does not exist.");
        }
        if (!users.containsKey("to")) {
                channelReceiver.sendToClient("User '" + to + "' does not exist.");
        }
        if (users.containsKey("to") && !users.containsKey("from")) {
            try {
                DataOutputStream outReceiver = users.get(to);
                outReceiver.writeUTF("[Whisper from " + to + "]  " + content);
                channelReceiver.sendToClient("Sent message to " + to);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

