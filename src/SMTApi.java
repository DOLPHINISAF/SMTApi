import java.net.http.*;

public class SMTApi {

    private String APIKey;

    private WebSocket webSocket;

    private WebSocket.Listener webSocketListener;
    SMTApi() {

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




}
