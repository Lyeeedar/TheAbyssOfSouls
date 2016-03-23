package shaders;

import box2dLight.RayHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public final class LightShader {
	static final public ShaderProgram createLightShader() {
		String gamma = "";
		if (RayHandler.getGammaCorrection())
			gamma = "sqrt";

		final String vertexShader =
				"attribute vec4 vertex_positions;\n" //
				+ "attribute vec4 quad_colors;\n" //
				+ "attribute float s;\n"
				+ "uniform mat4 u_projTrans;\n" //
				+ "uniform vec3 u_lightData;\n" //
				+ "varying vec4 v_color;\n" //
				+ "void main()\n" //
				+ "{\n" //
				+ "   float dist = length(u_lightData.xy-vertex_positions.xy);\n" //
				+ "   float scale = 1 - dist / u_lightData.z;\n" //
				+ "   v_color = scale * quad_colors;\n" //
				+ "   gl_Position =  u_projTrans * vertex_positions;\n" //
				+ "}\n";
		final String fragmentShader = "#ifdef GL_ES\n" //
									  + "precision lowp float;\n" //
									  + "#define MED mediump\n"
									  + "#else\n"
									  + "#define MED \n"
									  + "#endif\n" //
									  + "varying vec4 v_color;\n" //
									  + "void main()\n"//
									  + "{\n" //
									  + "  gl_FragColor = "+gamma+"(v_color);\n" //
									  + "}";

		ShaderProgram.pedantic = false;
		ShaderProgram lightShader = new ShaderProgram(vertexShader,
													  fragmentShader);
		if (lightShader.isCompiled() == false) {
			Gdx.app.log("ERROR", lightShader.getLog());
		}

		return lightShader;
	}
}


//package shaders;
//
//		import box2dLight.RayHandler;
//
//		import com.badlogic.gdx.Gdx;
//		import com.badlogic.gdx.graphics.glutils.ShaderProgram;
//
//public final class LightShader {
//	static final public ShaderProgram createLightShader() {
//		String gamma = "";
//		if (RayHandler.getGammaCorrection())
//			gamma = "sqrt";
//
//		final String vertexShader =
//				"attribute vec2 vertex_positions;\n" //
//				+ "attribute vec4 quad_colors;\n" //
//				+ "uniform vec3 u_lightData;\n" //
//				+ "uniform mat4 u_projTrans;\n" //
//				+ "varying vec4 v_color;\n" //
//				+ "void main()\n" //
//				+ "{\n" //
//				+ "   float dist = length(u_lightData.xy-vertex_positions.xy);\n"
//				+ "   float scale = dist / u_lightData.z;\n" //
//				+ "   v_color = vec4(1,1,1,1);\n" //
//				+ "   gl_Position =  u_projTrans * vec4(vertex_positions, 0, 0);\n" //
//				+ "}\n";
//		final String fragmentShader = "#ifdef GL_ES\n" //
//									  + "precision lowp float;\n" //
//									  + "#define MED mediump\n"
//									  + "#else\n"
//									  + "#define MED \n"
//									  + "#endif\n" //
//									  + "varying vec4 v_color;\n" //
//									  + "void main()\n"//
//									  + "{\n" //
//									  + "  gl_FragColor = "+gamma+"(v_color);\n" //
//									  + "}";
//
//		ShaderProgram.pedantic = true;
//		ShaderProgram lightShader = new ShaderProgram(vertexShader,
//													  fragmentShader);
//		if (lightShader.isCompiled() == false) {
//			Gdx.app.log("ERROR", lightShader.getLog());
//		}
//
//		return lightShader;
//	}
//}
