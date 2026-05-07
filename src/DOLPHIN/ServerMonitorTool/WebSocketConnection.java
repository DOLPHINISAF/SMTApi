package DOLPHIN.ServerMonitorTool;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.util.concurrent.*;


public class WebSocketConnection {

    private final String serverURI;
    private WebSocket webSocket;
    private HttpClient client;
    private boolean bConnected;
    private boolean bTryingToConnect;
    BlockingQueue<JSONObject> blockingReceivedJsonQueue;

    public WebSocketConnection(){
        serverURI = "wss://dolphinsibiu.ddns.net:1337";
        webSocket = null;
        client = null;
        blockingReceivedJsonQueue = new LinkedBlockingQueue<>();

        bTryingToConnect = false;

        bConnected = false;

        ConnectSocket();

    }

    public void ConnectSocket(){

        if(bTryingToConnect){
            return;
        }
        bTryingToConnect = true;
        try {
            client = HttpClient.newHttpClient();

            URI server = URI.create(serverURI);

            webSocket = client.newWebSocketBuilder().buildAsync(server, new WebSocketConnection.WebSocketListener()).join();
            bConnected = true;
        }
        catch (UncheckedIOException e){
            System.out.println("Cannot create httpclient!");
        }
        catch (Exception e){
            System.out.println("Caught unexpected error when creating websocket!");
        }

        bTryingToConnect = false;
    }

    //method for sending json object without the need to parse it to string
    public void SendJson(JSONObject jsonObject){
        String message = jsonObject.toString();

        if(!bConnected) return;

        try {
            webSocket.sendText(message, true);
        }
        catch (IllegalStateException e){
            System.out.println("Failed to send json!");
        }

    }

    public JSONObject GetReceivedJSON(){
        return blockingReceivedJsonQueue.poll();
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
            System.out.println("Api webSocket opened");
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {

            try {
                String receivedData = data.toString();
                JSONObject receivedJson = new JSONObject(receivedData);

                blockingReceivedJsonQueue.put(receivedJson);
            }
            catch (JSONException e){
                System.out.println("Failed to parse received message to json!");
            }
            catch (Exception e) {
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
            System.err.println("WebSocket closed unexpectedly. Error: " + error.getMessage());
            bConnected = false;
        }
    }

}
