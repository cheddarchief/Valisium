package engine.scene;

import java.util.ArrayList;

public class SceneBuilder {
    private String label;
    private ArrayList<Entity> entities;
    private Flying_Camera flyingCamera;

    public SceneBuilder setLabel(String label) {
        this.label = label;
        return this;
    }

    public SceneBuilder setEntities(ArrayList<Entity> entities) {
        this.entities = entities;
        return this;
    }

    public SceneBuilder setFlyingCamera(Flying_Camera flyingCamera) {
        this.flyingCamera = flyingCamera;
        return this;
    }

    public Scene createScene() {
        return new Scene(label, entities, flyingCamera);
    }
}