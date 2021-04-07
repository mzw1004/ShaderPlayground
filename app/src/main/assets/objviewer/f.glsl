#version 300 es
precision mediump float;

in vec3 v_ViewPosition;
in vec3 v_ViewNormal;
in vec2 v_TexCoord;
in vec3 v_Light;

out vec4 outColor;

void main() {
    vec3 objColor = vec3(0., 1., 0.);
    // ambient
    float ambient = .2;
    // diff
    vec3 lightDir = normalize(v_Light-v_ViewPosition);
    float diff = max(dot(lightDir, v_ViewNormal), 0.);
    // specular
    vec3 viewDir = normalize(-v_ViewPosition);
    vec3 reflectDir = -lightDir+2.*dot(v_ViewNormal, lightDir)*v_ViewNormal;
    float spec = pow(max(dot(viewDir, reflectDir), 0.), 16.);
    float specularStrength = .5;
    float specular = specularStrength * spec;
    vec3 specularColor = vec3(1.);
    // total
    vec3 result = (ambient+diff)*objColor+specular*specularColor;
    outColor = vec4(result, 1.);
}
