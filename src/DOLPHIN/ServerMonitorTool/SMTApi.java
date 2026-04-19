package DOLPHIN.ServerMonitorTool;

import java.util.ArrayList;
import java.util.Objects;
import org.json.*;


public class SMTApi {
    Boolean bIsAuth;
    private String APIKey;
    WebSocketConnection serverSocket;

    ArrayList<Action> actions;

    private enum JsonKeys{
        TYPE("type"),
        SOURCE("source"),
        NAMEID("nameID"),
        DESCRIPTION("description"),
        APIKEY("APIKey"),
        VALUE("value"),
        UNIT("unit"),
        DATA("data");

        public final String label;

        private JsonKeys(String label) {
            this.label = label;

        }

    }
    private enum MessageType{
        AUTH("auth"),
        ADD("add"),
        DATA("data");

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

        jsonObject.put(JsonKeys.TYPE.label, MessageType.AUTH.label);
        jsonObject.put(JsonKeys.SOURCE.label,"api");
        jsonObject.put(JsonKeys.APIKEY.label,APIKey);

        serverSocket.SendJson(jsonObject);
        //we freeze until we get response from server, should be auth-status type
        HandleReceivedJSON(true);

        if(bIsAuth){
            System.out.println("Succesfully authentificated to server");
        }
        else{
            System.out.println("Failed to authentificate to server!");
        }

    }

    public void HandleReceivedJSON(){
        HandleReceivedJSON(false);
    }

    //if the user wants to infinitely wait until socket gets a message bFreezeUntilReceived should be true
    public void HandleReceivedJSON(Boolean bFreezeUntilReceived){

        if(!serverSocket.IsConnected()){
            serverSocket.ConnectSocket();
        }

        JSONObject jsonObject = null;

        //will loop only when authentificating, waiting for auth response
        do {
            jsonObject = serverSocket.GetReceivedJSON();
        }while(jsonObject == null && bFreezeUntilReceived);

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
                    bIsAuth = true;
                }
                break;
            default:

        }

    }

    public void CreateAction(String actionName, Runnable code){
        actions.add(new Action(actionName,code));
    }

    public void AddParam(String nameID, String description, String unit){
        if(!serverSocket.IsConnected() || !bIsAuth) return;

        JSONObject jsonObject = new JSONObject();

        jsonObject.put(JsonKeys.TYPE.label, MessageType.ADD.label);
        jsonObject.put(JsonKeys.SOURCE.label,"api");
        jsonObject.put(JsonKeys.NAMEID.label,nameID);
        jsonObject.put(JsonKeys.DESCRIPTION.label,description);
        jsonObject.put(JsonKeys.UNIT.label,unit);
        jsonObject.put(JsonKeys.APIKEY.label, APIKey);

        serverSocket.SendJson(jsonObject);

        System.out.println("Added param");

    }

    public void SetApiKey(String APIKey){
        this.APIKey = APIKey;
        Auth();
    }

    public void SendUpdate(String nameID, int value){
        SendUpdate(nameID,Integer.toString(value));
    }

    public void SendUpdate(String nameID, String value){

        if(!serverSocket.IsConnected() || !bIsAuth) return;

        JSONObject jsonObject = new JSONObject();

        jsonObject.put(JsonKeys.TYPE.label, MessageType.DATA.label);
        jsonObject.put(JsonKeys.SOURCE.label,"api");
        jsonObject.put(JsonKeys.NAMEID.label,nameID);
        jsonObject.put(JsonKeys.DATA.label,value);
        jsonObject.put(JsonKeys.APIKEY.label, APIKey);

        serverSocket.SendJson(jsonObject);
    }

    public void Test(){

        AddParam("TEST_NAME","MY DESCRIPTION","BYTES/SECOND");
        SendUpdate("TEST_NAME", 10);

    }

    public void ChangeActionName(String ActionName, String newActionName){
        actions.forEach((action) ->{
            if(Objects.equals(action.GetName(), ActionName)){
                action.SetName(newActionName);
            }
        });
    }

    public void ChangeActionMethod(String actionName, Runnable newActionMethod){
        actions.forEach((action) ->{
            if(Objects.equals(action.GetName(), actionName)){
                action.SetMethod(newActionMethod);
            }
        });
    }

    public void Close() {
        serverSocket.CloseSocket();
    }
}

