package chat;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private LocalDateTime timestamp;
    private String message;
    private String user;

    public Message(LocalDateTime timestamp, String user, String message) {
        this.timestamp = timestamp;
        this.message = message;
        this.user = user;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setLocalDateTime(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
