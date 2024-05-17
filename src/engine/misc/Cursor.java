package engine.misc;

public class Cursor {
    public float x;
    public float y;
    public float dx;
    public float dy;

    public boolean isDirty;

    public Cursor(float x, float y) {
        this.x = x;
        this.y = y;

        flush();
    }

    @Override
    public String toString() {
        return "Cursor{" +
                "x=" + x +
                ", y=" + y +
                ", dx=" + dx +
                ", dy=" + dy +
                ", isDirty=" + isDirty +
                '}';
    }

    public void move(float newX, float newY) {
        dx = newX - x;
        dy = newY - y;

        x = newX;
        y = newY;

        isDirty = true;
    }

    public void flush() {
        dx = 0;
        dy = 0;

        isDirty = false;
    }
}
