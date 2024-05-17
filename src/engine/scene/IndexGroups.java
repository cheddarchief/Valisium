package engine.scene;

import java.util.ArrayList;

public class IndexGroups {
    private final ArrayList<Integer> positionIndices = new ArrayList<>();
    private final ArrayList<Integer> uvIndices = new ArrayList<>();
    private final ArrayList<Integer> normalIndices = new ArrayList<>();

    public IndexGroups() {}

    public void add(int positionIndex, int uvIndex, int normalIndex) {
        positionIndices.add(positionIndex - 1);
        uvIndices.add(uvIndex - 1);
        normalIndices.add(normalIndex - 1);
    }

    public void add(String positionIndex, String uvIndex, String normalIndex) {
        int uv = 0;

        try {
            uv = Integer.parseInt(uvIndex);
        } catch (Exception ignore) {}

        add(Integer.parseInt(positionIndex), uv, Integer.parseInt(normalIndex));
    }

    // Format = "v/vt/vn"
    public void add(String data) {
        String[] indices = data.split("/");

        switch (indices.length) {
            case 3 -> add(indices[0], indices[1], indices[2]);
            case 1 -> add(indices[0], "0", "0");
            default -> throw new IllegalStateException("Unknown format of data: \"" + data + "\"");
        }
    }

    public int getIndexPos(int i) {
        return positionIndices.get(i);
    }

    public int getUvPos(int i) {
        return uvIndices.get(i);
    }

    public int getNormalPos(int i) {
        return normalIndices.get(i);
    }

    public int size() {
        return positionIndices.size();
    }
}
