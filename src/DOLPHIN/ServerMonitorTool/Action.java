package DOLPHIN.ServerMonitorTool;

public class Action {

    private String name;
    private Runnable method;

    public Action(String actionName){
        this.name = actionName;
        this.method = null;
    }

    public Action(String actionName, Runnable method){
        this.name = actionName;
        this.method = method;
    }

    public void Run(){
        if(method != null) {
            method.run();
            System.out.println("API called an actions method: " + name);
        }
        else{
            System.out.println("API tried running an action without having a method assigned!");
        }
    }

    public String GetName(){
        return name;
    }

    public void SetName(String newName){
        this.name = newName;
    }
    
    public void SetMethod(Runnable newMethod){
        this.method = newMethod;
    }
}
