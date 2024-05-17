package engine.gfx;

public class Triangle_Mesh_Layout {
    public static final int VERTEX_BUFFERS_COUNT = Triangle_Mesh.VERTEX_BUFFERS_COUNT;
    
    public final Buffer_Layout[] buffers;
    public final Attribute_Layout[] attrs;

    public Triangle_Mesh_Layout() {
        buffers = new Buffer_Layout[VERTEX_BUFFERS_COUNT];
        attrs = new Attribute_Layout[VERTEX_BUFFERS_COUNT];
    }

    public Triangle_Mesh_Layout(Buffer_Layout[] buffers, Attribute_Layout[] attrs) {
        assert buffers.length <= VERTEX_BUFFERS_COUNT;
        assert attrs.length <= VERTEX_BUFFERS_COUNT;

        this.buffers = buffers;
        this.attrs = attrs;
    }
}
