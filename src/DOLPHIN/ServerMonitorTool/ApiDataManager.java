package DOLPHIN.ServerMonitorTool;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.*;

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

    //separation between packet upload rate in ms
    private static final int UPLOAD_RATE = 10;

    Boolean bIsAuth;
    Boolean bIsTryingToAuth;
    private String APIKey;
    WebSocketConnection serverSocket;

    //hashmap that holds an action name and its method
    private Map<String, Runnable> actions;

    private Map<String, String> parameterLatestUpdates;

    private BlockingQueue<JSONObject> sendQueue;
    ScheduledExecutorService executor ;

    ApiDataManager(){
        actions = new HashMap<>();
        serverSocket = new WebSocketConnection();

        bIsAuth = false;
        bIsTryingToAuth = false;

        sendQueue = new LinkedBlockingQueue<>();

        parameterLatestUpdates = new ConcurrentHashMap<>();

        executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() ->{
            if(!bIsAuth) return;

            try{
                //we get the latest parameter values from the hashmap and add them inside the json list we send to backend
                for(Map.Entry<String, String> parameter : parameterLatestUpdates.entrySet()){
                    String parameterName = parameter.getKey();
                    String parameterValue = parameter.getValue();

                    EnqueueParameterUpdate(parameterName, parameterValue);
                }
                List<JSONObject> batch = new ArrayList<>();
                sendQueue.drainTo(batch);

                for(JSONObject message : batch){
                    serverSocket.SendJson(message);
                }
            }
            catch(Exception ignored){

            }
        },0,UPLOAD_RATE, TimeUnit.MILLISECONDS);
    }

    private void TryAuth(){

        if(!serverSocket.IsConnected() || bIsTryingToAuth) return;

        bIsTryingToAuth = true;

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
        try {
            JSONObject jsonObject = serverSocket.GetReceivedJSON();

            while (jsonObject != null) {

                String messageType = jsonObject.getString("type");

                switch (messageType) {
                    case "run_action":
                        ActionStatus actionStatus;
                        String actionName = jsonObject.getString("actionID");
                        actionStatus = RunAction(actionName);
                        SendActionStatus(actionStatus, actionName);
                        break;

                    case "auth-status":
                        bIsTryingToAuth = false;
                        String statusMessage = jsonObject.getString("result");

                        if (Objects.equals(statusMessage, "accepted")) {
                            bIsAuth = true;
                        } else {
                            System.out.println("Failed to authentificate to server!");
                            String reason = jsonObject.getString("reason");

                            System.out.println("Reason: " + reason);

                        }
                        break;

                    default:
                        System.out.println("Unknown message type!");
                }
                jsonObject = serverSocket.GetReceivedJSON();
            }
        }
        catch (JSONException e){
            System.out.println("Failed to access json message required keys!");
        }
    }

    private void SendActionStatus(ActionStatus status, String name){
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(JsonKeys.TYPE.label, MessageType.STATUS.label);
        jsonObject.put(JsonKeys.SOURCE.label,"api");
        jsonObject.put(JsonKeys.ACTIONID.label, name);
        jsonObject.put(JsonKeys.APIKEY.label, APIKey);
        jsonObject.put(JsonKeys.STATUS.label, status.label);

        try {
            sendQueue.put(jsonObject);
        }
        catch (Exception ignored){}
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

        parameterLatestUpdates.put(nameID, value);
    }

    public void EnqueueParameterUpdate(String nameID, String value){

        JSONObject jsonObject = new JSONObject();

        jsonObject.put(JsonKeys.TYPE.label, MessageType.DATA_TYPE.label);
        jsonObject.put(JsonKeys.SOURCE.label,"api");
        jsonObject.put(JsonKeys.NAMEID.label,nameID);
        jsonObject.put(JsonKeys.VALUE.label,value);
        jsonObject.put(JsonKeys.APIKEY.label, APIKey);

        try {
            sendQueue.put(jsonObject);
        } catch (InterruptedException e) {
            System.out.println("Failed to put json message in blocking queue");
        }
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
