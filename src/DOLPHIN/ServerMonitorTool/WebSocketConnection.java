package DOLPHIN.ServerMonitorTool;

import java.net.URI;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;


public class WebSocketConnection extends Thread {

    private final String serverURI = "ws://dolphinisaf.ddns.net:1337";
    private WebSocket webSocket;
    private HttpClient client;

    public WebSocketConnection(){
        client = HttpClient.newHttpClient();
        System.out.println("Created http client");

        URI server = URI.create(serverURI);
        try {
            webSocket = client.newWebSocketBuilder().buildAsync(server, new SMTApi.WebSocketListener()).join();
            System.out.println("Created websocket");
        }
        catch (CompletionException e){
            System.out.println("Caught CompletionException!");
        }
    }

    public void run(){

    }

}
