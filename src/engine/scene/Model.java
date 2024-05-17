package engine.scene;

import engine.gfx.Texture2D;
import engine.gfx.Triangle_Mesh;

public class Model {
    private final Triangle_Mesh mesh;
    private final Texture2D texture;

    public Model(Triangle_Mesh mesh, Texture2D texture) {
        this.mesh = mesh;
        this.texture = texture;
    }

    // it's user's job to set all the matrices the right way
    public void render() {
        texture.bind(0);
        mesh.render();
    }

    public void delete() {
        texture.delete();
        mesh.deleteWithBuffers();
    }
}
