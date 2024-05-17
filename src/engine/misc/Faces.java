package engine.misc;

import java.util.ArrayList;

public class Faces {
    public ArrayList<Integer> positionIds = new ArrayList<>();
    public ArrayList<Integer> uvIds = new ArrayList<>();
    public ArrayList<Integer> normalIds = new ArrayList<>();

    public void add(int position, int uv, int normal) {
        positionIds.add(position);
        uvIds.add(uv);
        normalIds.add(normal);
    }

    public void add(int position, int normal) {
        positionIds.add(position);
        normalIds.add(normal);
    }
}
