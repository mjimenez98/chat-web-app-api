package chat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChatManager implements Serializable {
    private LinkedList<Message> chat;

    public Message postMessage(String user, String message) {
        if (chat == null)
            chat = new LinkedList<>();

        // Message parameter is required. If message is empty, do not process it
        if (message.isEmpty())
            return null;

        LocalDateTime timestamp = LocalDateTime.now();
        String thisUser = (user.isEmpty()) ? "anonymous" : user;
        Message newMessage = new Message(timestamp, thisUser, message);

        chat.add(newMessage);

        return newMessage;
    }
}
