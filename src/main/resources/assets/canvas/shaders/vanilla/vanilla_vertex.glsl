#include canvas:shaders/lib/common_header.glsl
#include canvas:shaders/lib/context.glsl
#include canvas:shaders/lib/common_varying.glsl
#include canvas:shaders/lib/common_vertex_lib.glsl
#include canvas:shaders/lib/flags.glsl
#include canvas:shaders/lib/vertex_data.glsl
#include canvas:shaders/lib/diffuse.glsl

#include canvas:apitarget

attribute vec4 in_color;
attribute vec2 in_uv;
attribute vec4 in_normal_ao;
attribute vec4 in_lightmap;

void main() {
	cv_VertexData data = cv_VertexData(
		gl_Vertex,
		textureCoord(in_uv, 0),
		in_color,
		in_normal_ao.xyz
	);

	// Adding +0.5 prevents striping or other strangeness in flag-dependent rendering
	// due to FP error on some cards/drivers.  Also made varying attribute invariant (rolls eyes at OpenGL)
	__cv_flags = in_lightmap.b + 0.5;

	cv_startVertex(data);

	vec4 viewCoord = gl_ModelViewMatrix * data.vertex;
	gl_ClipVertex = viewCoord;
	gl_FogFragCoord = length(viewCoord.xyz);

	data.vertex = gl_ModelViewProjectionMatrix * data.vertex;
	gl_Position = data.vertex;

	cv_endVertex(data);

	v_texcoord = data.spriteUV;
	v_color = data.vertexColor;

#if CONTEXT_IS_BLOCK
	v_ao = (in_normal_ao.w + 1.0) * 0.5;
#endif

#if DIFFUSE_SHADING_MODE != DIFFUSE_MODE_NONE
   v_diffuse = diffuse(diffuseNormal(viewCoord, data.vertexNormal));
#endif

#if !CONTEXT_IS_GUI
	// the lightmap texture matrix is scaled to 1/256 and then offset + 8
	// it is also clamped to repeat and has linear min/mag
	v_lightcoord = in_lightmap.rg * 0.00390625 + 0.03125;
#endif
}
