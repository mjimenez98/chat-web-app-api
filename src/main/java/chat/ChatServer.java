package chat;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

public class ChatServer {
    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 8980;
        server = ServerBuilder.forPort(port)
                .addService(new ChatManagerImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    ChatServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final ChatServer server = new ChatServer();
        server.start();
        server.blockUntilShutdown();
    }

    /**
     * Our implementation of ChatManager service.
     * See chat.proto for details of the methods.
     */
    private static class ChatManagerImpl extends ChatManagerGrpc.ChatManagerImplBase {
        private ChatManager chatManager;
        static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        ChatManagerImpl() {
            chatManager = new ChatManager();
        }

        @Override
        public void postMessage(chat.Msg request,
                                io.grpc.stub.StreamObserver<chat.Response> responseObserver) {
            String message = request.getMessage();
            String user = request.getUser();
            Message newMessage = chatManager.postMessage(message, user);
            if (newMessage != null) {
                responseObserver.onNext(chat.Response.newBuilder().setResponse("200: OK").build());
            } else {
                responseObserver.onNext(chat.Response.newBuilder().setResponse("400: ERROR").build());
            }
            responseObserver.onCompleted();
        }

        @Override
        public void listMessages(Request request, StreamObserver<Msg> responseObserver) {
            LocalDateTime start = getLDT(request.getStartDate());
            LocalDateTime end = getLDT(request.getEndDate());
            ;
            LinkedList<Message> chat = chatManager.listMessages(start, end);
            for (Message message : chat) {
                Msg.Builder msg = Msg.newBuilder();
                msg.setUser(message.getUser());
                msg.setMessage(message.getMessage());
                msg.setDate(message.getTimestamp().format(formatter));
                responseObserver.onNext(msg.build());
            }
            responseObserver.onCompleted();
        }

        @Override
        public void clearChat(chat.Request request,
                              io.grpc.stub.StreamObserver<chat.Response> responseObserver) {

            LocalDateTime start = getLDT(request.getStartDate());
            LocalDateTime end = getLDT(request.getEndDate());
            chatManager.clearChat(start, end);
            responseObserver.onNext(chat.Response.newBuilder().setResponse("CHAT CLEARED!").build());
            responseObserver.onCompleted();
        }
    }

    private static LocalDateTime getLDT(String date) {
        if (date != null && date.length() > 0) {
            return LocalDateTime.parse(date);
        }
        return null;
    }
}
