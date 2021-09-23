package rafikov.nocasino;

public class BooleanSwitcher {
    private volatile boolean value = false;

    public void setTrue() {
        value = true;
    }

    public void setFalse() {
        value = false;
    }

    public boolean getValue() {
        return value;
    }
}
