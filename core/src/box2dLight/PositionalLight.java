
package box2dLight;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;

import java.util.Iterator;

/**
 * Abstract base class for all positional lights
 *
 * <p>Extends {@link Light}
 *
 * @author kalle_h
 */
public abstract class PositionalLight extends Light {

	protected final Vector2 tmpEnd = new Vector2();
	protected final Vector2 start = new Vector2();

	protected Body body;
	protected float bodyOffsetX;
	protected float bodyOffsetY;
	protected float bodyAngleOffset;

	protected float sin[];
	protected float cos[];

	protected float endX[];
	protected float endY[];

	/**
	 * Creates new positional light and automatically adds it to the specified
	 * {@link RayHandler} instance.
	 *
	 * @param rayHandler
	 *            not null instance of RayHandler
	 * @param rays
	 *            number of rays - more rays make light to look more realistic
	 *            but will decrease performance, can't be less than MIN_RAYS
	 * @param color
	 *            light color
	 * @param distance
	 *            light distance (if applicable)
	 * @param x
	 *            horizontal position in world coordinates
	 * @param y
	 *            vertical position in world coordinates
	 * @param directionDegree
	 *            direction in degrees (if applicable)
	 */
	public PositionalLight(RayHandler rayHandler, int rays, Color color, float distance, float x, float y, float directionDegree) {
		super(rayHandler, rays, color, distance, directionDegree);
		start.x = x;
		start.y = y;

		lightMesh = new Mesh(VertexDataType.VertexArray, false, vertexNum, 0, new VertexAttribute(Usage.Position, 2,
																								  "vertex_positions"), new VertexAttribute(Usage.ColorPacked, 4, "quad_colors"),
							 new VertexAttribute(Usage.Generic, 1, "s"));



		softShadowMesh = new Mesh(VertexDataType.VertexArray, false, vertexNum * 2, 0, new VertexAttribute(Usage.Position, 2,
																										   "vertex_positions"), new VertexAttribute(Usage.ColorPacked, 4, "quad_colors"),
								  new VertexAttribute(Usage.Generic, 1, "s"));
		setMesh();
	}

	int meshIndex = 0;
	Mesh getExtraMesh()
	{
		if (meshIndex >= extraBodyMeshes.size)
		{
			Mesh mesh = new Mesh(VertexDataType.VertexArray, false, 25*6, 0,
								 new VertexAttribute(Usage.Position, 2, "vertex_positions"),
								 new VertexAttribute(Usage.ColorPacked, 4, "quad_colors"),
								 new VertexAttribute(Usage.Generic, 1, "s"));
			extraBodyMeshes.add( mesh );
		}

		return extraBodyMeshes.get( meshIndex++ );
	}

	@Override
	void update() {
		updateBody();

		if (cull()) return;
		if (staticLight && !dirty) return;

		dirty = false;
		updateMesh();
	}

	@Override
	void render() {
		if (rayHandler.culling && culled) return;

		rayHandler.lightRenderedLastFrame++;
		lightMesh.render(rayHandler.lightShader, GL20.GL_TRIANGLE_FAN, 0, vertexNum);

		for (Mesh mesh : extraBodyMeshes)
		{
			mesh.render( rayHandler.lightShader, GL20.GL_TRIANGLES );
		}

		if (soft && !xray) {
			softShadowMesh.render(
					rayHandler.lightShader,
					GL20.GL_TRIANGLE_STRIP,
					0,
					(vertexNum - 1) * 2);
		}
	}

	@Override
	public void attachToBody(Body body) {
		attachToBody(body, 0f, 0f, 0f);
	}

	/**
	 * Attaches light to specified body with relative offset
	 *
	 * @param body
	 *            that will be automatically followed, note that the body
	 *            rotation angle is taken into account for the light offset
	 *            and direction calculations
	 * @param offsetX
	 *            horizontal relative offset in world coordinates
	 * @param offsetY
	 *            vertical relative offset in world coordinates
	 *
	 */
	public void attachToBody(Body body, float offsetX, float offsetY) {
		attachToBody(body, offsetX, offsetY, 0f);
	}

	/**
	 * Attaches light to specified body with relative offset and direction
	 *
	 * @param body
	 *            that will be automatically followed, note that the body
	 *            rotation angle is taken into account for the light offset
	 *            and direction calculations
	 * @param offsetX
	 *            horizontal relative offset in world coordinates
	 * @param degrees
	 *            directional relative offset in degrees
	 */
	public void attachToBody(Body body, float offsetX, float offSetY, float degrees) {
		this.body = body;
		bodyOffsetX = offsetX;
		bodyOffsetY = offSetY;
		bodyAngleOffset = degrees;
		if (staticLight) dirty = true;
	}

	@Override
	public Vector2 getPosition() {
		tmpPosition.x = start.x;
		tmpPosition.y = start.y;
		return tmpPosition;
	}

	public Body getBody() {
		return body;
	}

	/** @return horizontal starting position of light in world coordinates **/
	@Override
	public float getX() {
		return start.x;
	}

	/** @return vertical starting position of light in world coordinates **/
	@Override
	public float getY() {
		return start.y;
	}

	@Override
	public void setPosition(float x, float y) {

		if (start.x != x || start.y != y)
		{
			if (staticLight) dirty = true;
		}

		start.x = x;
		start.y = y;
	}

	@Override
	public void setPosition(Vector2 position) {
		setPosition( position.x, position.y );
	}

	@Override
	public boolean contains(float x, float y) {
		// fast fail
		final float x_d = start.x - x;
		final float y_d = start.y - y;
		final float dst2 = x_d * x_d + y_d * y_d;
		if (distance * distance <= dst2) return false;

		// actual check
		boolean oddNodes = false;
		float x2 = mx[rayNum] = start.x;
		float y2 = my[rayNum] = start.y;
		float x1, y1;
		for (int i = 0; i <= rayNum; x2 = x1, y2 = y1, ++i) {
			x1 = mx[i];
			y1 = my[i];
			if (((y1 < y) && (y2 >= y)) || (y1 >= y) && (y2 < y)) {
				if ((y - y1) / (y2 - y1) * (x2 - x1) < (x - x1)) oddNodes = !oddNodes;
			}
		}
		return oddNodes;
	}

	@Override
	protected void setRayNum(int rays) {
		super.setRayNum(rays);

		sin = new float[rays];
		cos = new float[rays];
		endX = new float[rays];
		endY = new float[rays];
	}

	protected boolean cull() {
		culled = rayHandler.culling && !rayHandler.intersect(
				start.x, start.y, distance + softShadowLength);
		return culled;
	}

	protected void updateBody() {
		if (body == null || staticLight) return;

		final Vector2 vec = body.getPosition();
		float angle = body.getAngle();
		final float cos = MathUtils.cos(angle);
		final float sin = MathUtils.sin(angle);
		final float dX = bodyOffsetX * cos - bodyOffsetY * sin;
		final float dY = bodyOffsetX * sin + bodyOffsetY * cos;
		start.x = vec.x + dX;
		start.y = vec.y + dY;
		setDirection(bodyAngleOffset + angle * MathUtils.radiansToDegrees);
	}

	protected void updateMesh() {
		occluders.clear();
		for (int i = 0; i < rayNum; i++) {
			m_index = i;
			f[i] = 1f;
			tmpEnd.x = endX[i] + start.x;
			mx[i] = tmpEnd.x;
			tmpEnd.y = endY[i] + start.y;
			my[i] = tmpEnd.y;
			if (rayHandler.world != null && !xray) {
				//ray.hit = 0;
				rayHandler.world.rayCast(ray, start, tmpEnd);
			}
		}
		setMesh();
	}

	protected void setMesh() {
		// ray starting point
		int size = 0;

		segments[size++] = start.x;
		segments[size++] = start.y;
		segments[size++] = colorF;
		segments[size++] = 1;
		// rays ending points.
		for (int i = 0; i < rayNum; i++) {
			segments[size++] = mx[i];
			segments[size++] = my[i];
			segments[size++] = colorF;
			segments[size++] = 1 - f[i];
		}
		lightMesh.setVertices(segments, 0, size);

		meshIndex = 0;
		int count = 0;
		Iterator<Body> itr = occluders.iterator();
		while (count < occluders.size())
		{
			Mesh mesh = getExtraMesh();

			float[] data = new float[25*6*4];
			size = 0;
			for (int i = 0; i < 25 && itr.hasNext(); i++)
			{
				Vector2 pos = itr.next().getPosition();
				data[size++] = pos.x - 0.5f;
				data[size++] = pos.y - 0.5f;
				data[size++] = colorF;
				data[size++] = 0;

				data[size++] = pos.x - 0.5f;
				data[size++] = pos.y + 0.5f;
				data[size++] = colorF;
				data[size++] = 1;

				data[size++] = pos.x + 0.5f;
				data[size++] = pos.y - 0.5f;
				data[size++] = colorF;
				data[size++] = 0;

				data[size++] = pos.x + 0.5f;
				data[size++] = pos.y - 0.5f;
				data[size++] = colorF;
				data[size++] = 0;

				data[size++] = pos.x - 0.5f;
				data[size++] = pos.y + 0.5f;
				data[size++] = colorF;
				data[size++] = 1;

				data[size++] = pos.x + 0.5f;
				data[size++] = pos.y + 0.5f;
				data[size++] = colorF;
				data[size++] = 1;
			}

			mesh.setVertices( data, 0, size );

			count += 25;
		}

		if (!soft || xray) return;

		size = 0;
		// rays ending points.
		for (int i = 0; i < rayNum; i++) {
			segments[size++] = mx[i];
			segments[size++] = my[i];
			segments[size++] = colorF;
			final float s = (1 - f[i]);
			segments[size++] = s;
			segments[size++] = mx[i] + s * softShadowLength * cos[i];
			segments[size++] = my[i] + s * softShadowLength * sin[i];
			segments[size++] = zeroColorBits;
			segments[size++] = 0f;
		}
		softShadowMesh.setVertices(segments, 0, size);
	}

}
