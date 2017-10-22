// V1.vert
in vec3 vert;
in vec4 texcoord;
uniform mat4 view;
uniform mat4 model;
uniform mat4 projection;

void main(void) {
	gl_Position = projection * view * model * vec4(vert, 1);
	gl_TexCoord[0] = texcoord;
}
