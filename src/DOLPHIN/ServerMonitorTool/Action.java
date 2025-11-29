package DOLPHIN.ServerMonitorTool;

public class Action {

    private String name;
    private Runnable code;


    public Action(String name, Runnable code){
        this.name = name;
        this.code = code;
    }

    public void Run(){
        code.run();
    }

    public String GetName(){
        return name;
    }
}
