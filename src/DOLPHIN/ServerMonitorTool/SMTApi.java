package DOLPHIN.ServerMonitorTool;

import java.net.URI;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import org.json.*;

public class SMTApi {
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

    private void Auth(){


        JSONObject jsonObject = new JSONObject();

        jsonObject.put("type", "auth");
        jsonObject.put("source","api");
        jsonObject.put("APIKey",APIKey);
        String message = jsonObject.toString();


        try {
            webSocket.sendText(message, true);
            System.out.println("Sent auth json!");
        }
        catch (IllegalStateException e){
            System.out.println("Failed to send text to auth!");
        }
    }

    public void AddParam(String nameID, String description, String unit){


        JSONObject jsonObject = new JSONObject();

        jsonObject.put("type", "add");
        jsonObject.put("source","api");
        jsonObject.put("nameID",nameID);
        jsonObject.put("description",description);
        jsonObject.put("unit",unit);

        String message = jsonObject.toString();


        try {
            webSocket.sendText(message, true);
            System.out.println("Sent addParam json");
        }
        catch (IllegalStateException e){
            System.out.println("Failed to send text to add param");
        }

    }

    public void SetApiKey(String APIKey){
        this.APIKey = APIKey;
        Auth();
    }

    public void SendUpdate(String nameID, int value){
        SendUpdate(nameID,Integer.toString(value));
    }

    public void  SendUpdate(String nameID, String value){


        JSONObject jsonObject = new JSONObject();

        jsonObject.put("type", "data");
        jsonObject.put("source","api");
        jsonObject.put("nameID",nameID);
        jsonObject.put("data",value);

        String message = jsonObject.toString();


        try {
            webSocket.sendText(message, true);
            System.out.println("Sent Update json");
        }
        catch (IllegalStateException e){
            System.out.println("Failed to send text to update");
        }
    }

    public void SendText(String text){

        try{
            webSocket.sendText(text,true);
        }
        catch (IllegalStateException e){
            System.out.println("ERROR!!");
        }
    }

    public void Test(){


        if(webSocket == null){
            System.out.println("Socket is not open/doesn not exist!");
            return;
        }

        Auth();
        AddParam("TEST_NAME","MY DESCRIPTION","BYTES/SECOND");
        SendUpdate("TEST_NAME", 10);
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


