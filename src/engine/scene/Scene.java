package engine.scene;

import java.util.ArrayList;
import java.util.Objects;

public class Scene {
    public ArrayList<Entity> entities;
    public Flying_Camera camera;

    public String label;

    public Scene(String label, ArrayList<Entity> entities, Flying_Camera flyingCamera) {
        this.label = label;
        this.camera = flyingCamera;

        this.entities = Objects.requireNonNullElseGet(entities, ArrayList::new);
    }

    public void updateState() {

    }

    public void render() {

    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }
}
