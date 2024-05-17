package engine.gfx;

public record Attribute_Layout(int bufferIndex, Vertex_Type type) {
    public boolean exists() {
        return type.glType != 0;
    }
}
