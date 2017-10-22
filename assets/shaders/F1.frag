// F1.frag
uniform sampler2D texSampler;
uniform vec4 Color;
uniform int coloring;

void main(void) {
	if (coloring != 0) {
		gl_FragColor = texture(texSampler, vec2(gl_TexCoord[0])) * Color;
	} else {
		gl_FragColor = texture(texSampler, vec2(gl_TexCoord[0]));
	}
}
