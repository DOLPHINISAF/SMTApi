package DOLPHIN.ServerMonitorTool;

import java.util.ArrayList;
import java.util.Objects;
import org.json.*;

public class SMTApi {

    private String APIKey;
    private WebSocketConnection serverSocket;

    private ArrayList<Action> actions;


    public SMTApi() {

        actions = new ArrayList<>();

        serverSocket = new WebSocketConnection();

        if(serverSocket.ConnectSocket()){
            System.out.println("Error connecting socket! Check errors");
        }

    }

    private void Auth(){

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("type", "auth");
        jsonObject.put("source","api");
        jsonObject.put("APIKey",APIKey);


        if(!serverSocket.SendJson(jsonObject)){
            System.out.println("Succesfully authentificated to server");
        }
        else{
            System.out.println("Failed to authentificate to server!");
        }

    }

    private void HandleReceivedJSON(){

        if(!serverSocket.queueMutex){
            serverSocket.queueMutex = true;
            JSONObject jsonObject = serverSocket.GetReceivedJSON();

            //TODO: Make this a switch for multiple types

            if(Objects.equals(jsonObject.getString("type"), "RUN_ACTION")){
                String actionName = jsonObject.getString("action_name");
                actions.forEach((action) ->{
                    if(Objects.equals(action.GetName(), actionName)){
                        action.Run();
                    }
                });
            }
            else if(Objects.equals(jsonObject.getString("type"), "AUTH_STATUS")){
                String statusMessage = jsonObject.getString("message");
                System.out.println(statusMessage);
            }

        }
        serverSocket.queueMutex = false;
    }

    public void CreateAction(String actionName, Runnable code){
        actions.add(new Action(actionName,code));
    }

    public void AddParam(String nameID, String description, String unit){


        JSONObject jsonObject = new JSONObject();

        jsonObject.put("type", "add");
        jsonObject.put("source","api");
        jsonObject.put("nameID",nameID);
        jsonObject.put("description",description);
        jsonObject.put("unit",unit);

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

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("type", "data");
        jsonObject.put("source","api");
        jsonObject.put("nameID",nameID);
        jsonObject.put("data",value);

        serverSocket.SendJson(jsonObject);
    }

    public void SendText(String text){

        serverSocket.SendText(text);
        System.out.println("Added param");
    }

    public void Test(){
        APIKey = "FEAG43FDG3";
        Auth();

        if(!serverSocket.IsConnected()){
            System.out.println("Socket is not open/doesn not exist!");
            return;
        }

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

