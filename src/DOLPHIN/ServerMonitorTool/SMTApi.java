package DOLPHIN.ServerMonitorTool;

import java.util.ArrayList;
import java.util.Objects;
import org.json.*;

//TODO: add class that holds all parameter updates and sends regularly to backend so we don't spam it
public class SMTApi {
    Boolean bIsAuth;
    private String APIKey;
    WebSocketConnection serverSocket;

    ArrayList<Action> actions;

    private enum JsonKeys{
        TYPE("type"),
        SOURCE("source"),
        NAMEID("nameID"),
        APIKEY("APIKey"),
        VALUE("data"),
        UNIT("unit");

        public final String label;

        private JsonKeys(String label) {
            this.label = label;

        }

    }
    private enum MessageType{
        AUTH_TYPE("auth-api"),
        DATA_TYPE("data");

        public final String label;

        private MessageType(String label) {
            this.label = label;

        }

    }

    public SMTApi() {
        bIsAuth = false;
        actions = new ArrayList<>();

        serverSocket = new WebSocketConnection();

    }

    private void Auth(){

        if(!serverSocket.IsConnected()) return;

        JSONObject jsonObject = new JSONObject();

        jsonObject.put(JsonKeys.TYPE.label, MessageType.AUTH_TYPE.label);
        jsonObject.put(JsonKeys.APIKEY.label,APIKey);

        serverSocket.SendJson(jsonObject);

    }

    //if the user wants to infinitely wait until socket gets a message bFreezeUntilReceived should be true
    public void HandleReceivedJSON(){

        //if we are not connected to the server for any reason we retry
        //maybe server restarted and we only need to reconnectW
        if(!serverSocket.IsConnected()){
            serverSocket.ConnectSocket();
            Auth();
        }

        JSONObject jsonObject = serverSocket.GetReceivedJSON();

        if(jsonObject == null) return;


        String messageType = jsonObject.getString("type");

        switch (messageType){
            case "run_action":
                String actionName = jsonObject.getString("actionID");
                actions.forEach((action) -> {
                    if (Objects.equals(action.GetName(), actionName)) {
                        action.Run();
                    }
                });
                break;

            case "auth-status":
                String statusMessage = jsonObject.getString("result");
                System.out.println(statusMessage);

                if (Objects.equals(statusMessage, "accepted")) {
                    System.out.println("Succesfully authentificated to server");
                    bIsAuth = true;
                }
                else{
                    System.out.println("Failed to authentificate to server!");
                }
                break;
            default:

        }

    }

    public void CreateAction(String actionName, Runnable code){
        actions.add(new Action(actionName,code));
    }

    public void SetApiKey(String APIKey){
        this.APIKey = APIKey;
        Auth();
    }

    public void SendUpdate(String nameID, String value){
        //we don't try to send anything if we aren't connected
        //or authentificated
        if(!serverSocket.IsConnected() || !bIsAuth) return;

        JSONObject jsonObject = new JSONObject();

        jsonObject.put(JsonKeys.TYPE.label, MessageType.DATA_TYPE.label);
        jsonObject.put(JsonKeys.SOURCE.label,"api");
        jsonObject.put(JsonKeys.NAMEID.label,nameID);
        jsonObject.put(JsonKeys.VALUE.label,value);
        jsonObject.put(JsonKeys.APIKEY.label, APIKey);

        serverSocket.SendJson(jsonObject);
    }

    public void Close() {
        serverSocket.CloseSocket();
    }
}

