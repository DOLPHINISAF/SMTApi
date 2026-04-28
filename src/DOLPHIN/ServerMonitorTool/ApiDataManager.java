package DOLPHIN.ServerMonitorTool;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ApiDataManager {

    private enum JsonKeys{
        TYPE("type"),
        SOURCE("source"),
        NAMEID("nameID"),
        APIKEY("APIKey"),
        VALUE("data"),
        ACTIONID("actionID"),
        STATUS("status");

        public final String label;

        private JsonKeys(String label) {
            this.label = label;

        }

    }

    private enum MessageType{
        AUTH_TYPE("auth-api"),
        DATA_TYPE("data"),
        STATUS("action_status");

        public final String label;

        private MessageType(String label) {
            this.label = label;

        }

    }

    private enum ActionStatus{
        FINISHED("Completed"),
        FAIL("Failed"),
        ERROR("Error");

        public final String label;

        private ActionStatus(String label) {
            this.label = label;

        }
    }

    Boolean bIsAuth;
    private String APIKey;
    WebSocketConnection serverSocket;

    //hashmap that holds an action name and its method
    private Map<String, Runnable> actions;

    private Map<String, String> parameterLatestUpdates;

    ApiDataManager(){
        actions = new HashMap<>();
        serverSocket = new WebSocketConnection();

        bIsAuth = false;

        parameterLatestUpdates = new HashMap<>();
    }

    private void TryAuth(){

        if(!serverSocket.IsConnected()) return;

        JSONObject jsonObject = new JSONObject();

        jsonObject.put(JsonKeys.TYPE.label, MessageType.AUTH_TYPE.label);
        jsonObject.put(JsonKeys.APIKEY.label,APIKey);

        serverSocket.SendJson(jsonObject);

    }

    //if the user wants to infinitely wait until socket gets a message bFreezeUntilReceived should be true
    public void HandleMessages(){

        //if we are not connected to the server for any reason we retry
        //maybe server restarted and we only need to reconnectW
        if(!serverSocket.IsConnected()){
            serverSocket.ConnectSocket();
            TryAuth();
        }

        JSONObject jsonObject = serverSocket.GetReceivedJSON();

        if(jsonObject == null) return;


        String messageType = jsonObject.getString("type");

        switch (messageType){
            case "run_action":
                ActionStatus actionStatus;
                String actionName = jsonObject.getString("actionID");
                actionStatus = RunAction(actionName);
                SendActionStatus(actionStatus, actionName);
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

    private void SendActionStatus(ActionStatus status, String name){
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(JsonKeys.TYPE.label, MessageType.STATUS.label);
        jsonObject.put(JsonKeys.SOURCE.label,"api");
        jsonObject.put(JsonKeys.ACTIONID.label, name);
        jsonObject.put(JsonKeys.APIKEY.label, APIKey);
        jsonObject.put(JsonKeys.STATUS.label, status.label);

        serverSocket.SendJson(jsonObject);
    }

    private ActionStatus RunAction(String actionName){
        //we lookup if the action the api is requested to run exists in our list
        Runnable action = actions.get(actionName);
        if (action == null) {
            return ActionStatus.ERROR;
        }
        try {
            action.run();
        }
        catch (Exception ignored){
            return ActionStatus.FAIL;
        }
        return ActionStatus.FINISHED;
    }

    public void UpdateParameter(String nameID, String value){
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

    public void SetApiKey(String APIKey){
        this.APIKey = APIKey;
        TryAuth();
    }

    public void CreateAction(String actionName, Runnable code){
        actions.put(actionName,code);
    }

    public void Close() {
        serverSocket.CloseSocket();
    }
}
