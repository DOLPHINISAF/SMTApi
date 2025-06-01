package DOLPHIN.ServerMonitorTool;

import java.net.URI;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import org.json.*;

public class SMTApi {
    private WebSocketConnection connection;
    private final String serverURI = "ws://dolphinsibiu.ddns.net:1337";
    private String APIKey;
    private WebSocket webSocket;
    private HttpClient client;

    public SMTApi() {

        client = HttpClient.newHttpClient();
        System.out.println("Created http client");

        URI server = URI.create(serverURI);
        try {
            webSocket = client.newWebSocketBuilder().buildAsync(server, new WebSocketListener()).join();
            System.out.println("Created websocket");
        }
        catch (CompletionException e){
            System.out.println("Caught CompletionException!");
        }
    }
    public void Close() {
        if(webSocket != null) {
            System.out.println("Closing WebSocket!");
            webSocket.sendClose(0, "Closeddd");
        }
    }
    public void AddParam(String nameID, String description){

    }
    public void SetApiKey(String APIKey){
        this.APIKey = APIKey;
    }
    public void  SendUpdate(String nameID, String value, String status){

    }
    public void  SendUpdate(String nameID, String value){

    }
    public void SetStatus(String nameID, String status){

    }

    public void Test(){


        if(webSocket == null){
            System.out.println("Socket is not open/doesn not exist!");
            return;
        }

        String text = "{\"test\":true,\n" +
                        "\"source\":\"api\"}";
        System.out.println("Sending text!");

        webSocket.sendText(text,true);
    }


    static class WebSocketListener implements WebSocket.Listener{

        @Override
        public void onOpen(WebSocket webSocket) {
            System.out.println("WebSocket opened");
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            System.out.println("Received message: " + data);
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            System.out.println("Received binary data");
            return WebSocket.Listener.super.onBinary(webSocket, data, last);
        }

        @Override
        public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
            System.out.println("Received ping");
            return WebSocket.Listener.super.onPing(webSocket, message);
        }

        @Override
        public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
            System.out.println("Received pong");
            return WebSocket.Listener.super.onPong(webSocket, message);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            System.out.println("WebSocket closed: " + reason);
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.err.println("WebSocket error: " + error.getMessage());
        }
    }

}


