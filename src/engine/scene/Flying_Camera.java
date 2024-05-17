package engine.scene;

import engine.misc.Direction_Mask;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Flying_Camera {
    public final Vector3f position;
    public final Vector3f direction;
    public final Vector3f right;

    public float yaw;
    public float pitch;

    public float sensitivity;
    public float speed;

    private final Matrix4f viewMatrix;

    public Flying_Camera(Vector3f position, float yaw, float pitch, float sensitivity, float speed) {
        assert pitch >= 89.0 || pitch <= -89.0;

        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
        this.sensitivity = sensitivity;
        this.speed = speed;

        this.viewMatrix = new Matrix4f();

        this.direction = new Vector3f();
        this.right = new Vector3f();

        recalculateBasis();
    }

    private void recalculateBasis() {
        float yaw_rad = Math.toRadians(yaw);
        float pitch_rad = Math.toRadians(pitch);

        direction.x = Math.cos(yaw_rad) * Math.cos(pitch_rad);
        direction.y = Math.sin(pitch_rad);
        direction.z = Math.sin(yaw_rad) * Math.cos(pitch_rad);

        direction.cross(0.0f, 1.0f, 0.0f, right);

        recalculateViewMatrix();
    }

    private void recalculateViewMatrix() {
        viewMatrix
                .identity()
                .setLookAt(
                        position.x, position.y, position.z,
                        position.x + direction.x, position.y + direction.y, position.z + direction.z,
                        0.0f, 1.0f, 0.0f
                );
    }

    // Only used inside updatePosition
    // Note: it's better than allocating it over and over in a loop
    private final Vector3f finalDirectionBuff = new Vector3f();

    public void updatePosition(Direction_Mask directionMask, float deltaTime) {
        finalDirectionBuff.zero();

        if (directionMask.is(Direction_Mask.FORWARD))
            finalDirectionBuff.add(direction);
        if (directionMask.is(Direction_Mask.BACKWARD))
            finalDirectionBuff.sub(direction);
        if (directionMask.is(Direction_Mask.RIGHT))
            finalDirectionBuff.add(right);
        if (directionMask.is(Direction_Mask.LEFT))
            finalDirectionBuff.sub(right);
        if (directionMask.is(Direction_Mask.UP))
            finalDirectionBuff.y += 1;
        if (directionMask.is(Direction_Mask.DOWN))
            finalDirectionBuff.y -= 1;

        finalDirectionBuff.normalize();

        if (Float.isNaN(finalDirectionBuff.x) |
            Float.isNaN(finalDirectionBuff.y) |
            Float.isNaN(finalDirectionBuff.z))
                return;

        position.add(finalDirectionBuff.mul(deltaTime * speed));

        recalculateViewMatrix();
    }

    public void updateDirection(float dx, float dy) {
        yaw += dx * sensitivity;
        pitch = Math.clamp(-89.0f, 89.0f, pitch - dy);

        recalculateBasis();
    }

    public Matrix4f viewMatrix() {
        return viewMatrix;
    }
}

