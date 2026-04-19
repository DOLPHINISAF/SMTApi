package DOLPHIN.ServerMonitorTool;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.util.concurrent.*;


public class WebSocketConnection {

    private final String serverURI;
    private WebSocket webSocket;
    private HttpClient client;
    private boolean bConnected;
    BlockingQueue<JSONObject> blockingreceivedJsonQueue;

    public WebSocketConnection(){
        serverURI = "ws://dolphinsibiu.ddns.net:1337";
        webSocket = null;
        client = null;
        blockingreceivedJsonQueue = new LinkedBlockingQueue<>();

        ConnectSocket();

    }

    public void ConnectSocket(){
        client = HttpClient.newHttpClient();
        System.out.println("Created http client");

        URI server = URI.create(serverURI);
        try {

            webSocket = client.newWebSocketBuilder().buildAsync(server, new WebSocketConnection.WebSocketListener()).join();
            System.out.println("Created websocket");
            bConnected = true;
        }
        catch (CompletionException e){
            System.out.println("Caught CompletionException when creating websocket!");
            bConnected = false;
        }

    }

    //method to send json object that is already parsed to string
    public boolean SendJson(String message){

        if(!bConnected) return true;

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
        if(!bConnected) return;
        try{
            webSocket.sendText(text,true);
        }
        catch (IllegalStateException e){
            System.out.println("ERROR sending text!!");
        }
    }

    public JSONObject GetReceivedJSON(){
        return blockingreceivedJsonQueue.poll();
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
            System.out.println("WebSocket opened");
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            System.out.println("Received message: " + data);

            String receivedData = data.toString();
            JSONObject receivedJson = new JSONObject(receivedData);
            System.out.println("Parsed received text to JSON");


            try {
                blockingreceivedJsonQueue.put(receivedJson);
            } catch (InterruptedException e) {
                System.out.println("Websocket failed to put json message in blocking queue");
            }


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
            bConnected = false;
            System.out.println("WebSocket closed: " + reason);
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.err.println("WebSocket error: " + error.getMessage());
            bConnected = false;
        }
    }

}
