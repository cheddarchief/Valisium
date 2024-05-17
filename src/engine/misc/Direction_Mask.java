package engine.misc;

public class Direction_Mask {
    private int raw = 0;
    
    public static final int FORWARD  = 1;
    public static final int BACKWARD = 1 << 1;
    public static final int RIGHT    = 1 << 2;
    public static final int LEFT     = 1 << 3;
    public static final int UP       = 1 << 4;
    public static final int DOWN     = 1 << 5;
    
    public void reset() {
        raw = 0;
    }
    
    public void forward() {
        raw |= FORWARD;
    }

    public void backward() {
        raw |= BACKWARD;
    }

    public void right() {
        raw |= RIGHT;
    }

    public void left() {
        raw |= LEFT;
    }

    public void up() {
        raw |= UP;
    }

    public void down() {
        raw |= DOWN;
    }

    public boolean is(int direction) {
        return (raw & direction) == direction;
    }

    public int getRaw() {
        return raw;
    }
}
