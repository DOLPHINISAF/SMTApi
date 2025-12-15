package DOLPHIN.ServerMonitorTool;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;


public class WebSocketConnection {

    private final String serverURI;
    private WebSocket webSocket;
    private HttpClient client;
    private boolean bConnected;

    Queue<JSONObject> receivedJsonQueue;
    public boolean queueMutex;

    public WebSocketConnection(){
        //serverURI = "ws://dolphinisaf.ddns.net:1337";
        serverURI = "ws://localhost:1337";
        webSocket = null;
        client = null;
        bConnected = false;
        receivedJsonQueue = new LinkedList<>();
        queueMutex = false;
    }

    public boolean ConnectSocket(){
        client = HttpClient.newHttpClient();
        System.out.println("Created http client");

        URI server = URI.create(serverURI);
        try {
            webSocket = client.newWebSocketBuilder().buildAsync(server, new WebSocketConnection.WebSocketListener()).join();
            System.out.println("Created websocket");

        }
        catch (CompletionException e){
            System.out.println("Caught CompletionException when creating websocket!");
            bConnected = false;
            return true;
        }
        bConnected = true;
        return false;
    }

    //method to send json object that is already parsed to string
    public boolean SendJson(String message){
        try {
            webSocket.sendText(message, true);
            System.out.println("Sent json data!");
        }
        catch (IllegalStateException e){
            System.out.println("Failed to send json!");
            return true;
        }

        return false;
    }

    //method for sending json object without the need to parse it to string
    public boolean SendJson(JSONObject jsonObject){
        return SendJson(jsonObject.toString());
    }

    public void SendText(String text){
        try{
            webSocket.sendText(text,true);
        }
        catch (IllegalStateException e){
            System.out.println("ERROR sending text!!");
        }
    }

    public JSONObject GetReceivedJSON(){
        return receivedJsonQueue.poll();
    }

    public void CloseSocket(){
        if(webSocket != null) {
            System.out.println("Closing WebSocket!");
            webSocket.sendClose(0, "Closeddd");
        }

    }

    public boolean IsConnected(){
        return bConnected;
    }

    class WebSocketListener implements WebSocket.Listener{

        @Override
        public void onOpen(WebSocket webSocket) {
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            System.out.println("Received message: " + data);

            String receivedData = data.toString();
            JSONObject receivedJson = new JSONObject(receivedData);
            System.out.println("Parsed received text to JSON");

            //while main thread uses the JSON queue we block websockets access to modifying it
            while(queueMutex){

            }
            queueMutex = true;

            receivedJsonQueue.offer(receivedJson);

            queueMutex = false;

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
