package DOLPHIN.ServerMonitorTool;


public class SMTApi {
    ApiDataManager apiManager;

    public SMTApi() {
        apiManager = new ApiDataManager();
    }

    public SMTApi(String APIKey){
        apiManager = new ApiDataManager();
        SetApiKey(APIKey);
    }
    //if the user wants to infinitely wait until socket gets a message bFreezeUntilReceived should be true
    public void GetMessages(){
        apiManager.HandleMessages();
    }

    public void CreateAction(String actionName, Runnable code){
        apiManager.CreateAction(actionName, code);
    }

    public void SetApiKey(String APIKey){
        apiManager.SetApiKey(APIKey);
    }

    public void UpdateParameter(String nameID, int value){
        UpdateParameter(nameID, String.valueOf(value));
    }

    public void UpdateParameter(String nameID, String value){
        apiManager.UpdateParameter(nameID, value);
    }

    public void CloseApi() {
        apiManager.Close();
    }
}

