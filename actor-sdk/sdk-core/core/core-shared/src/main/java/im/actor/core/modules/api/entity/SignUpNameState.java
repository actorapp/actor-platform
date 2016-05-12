package im.actor.core.modules.api.entity;

/**
 * Created by Administrator on 2016/4/18.
 */
public class SignUpNameState {
    String name;
    int state;//

    public SignUpNameState(){
    }

    public SignUpNameState(int state, String name) {
        this.state = state;
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }
}
