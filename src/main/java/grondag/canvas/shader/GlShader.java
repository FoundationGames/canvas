/*******************************************************************************
 * Copyright 2019 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package grondag.canvas.shader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.io.CharStreams;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL21;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import net.fabricmc.loader.api.FabricLoader;

import grondag.canvas.CanvasMod;
import grondag.canvas.Configurator;
import grondag.canvas.Configurator.AoMode;
import grondag.canvas.Configurator.DiffuseMode;
import grondag.canvas.varia.CanvasGlHelper;

public class GlShader {
	private static boolean isErrorNoticeComplete = false;

	public final Identifier shaderSource;

	private final int shaderType;
	private final ShaderContext context;

	private int glId = -1;
	private boolean needsLoad = true;
	private boolean isErrored = false;

	GlShader(Identifier shaderSource, int shaderType, ShaderContext context) {
		this.shaderSource = shaderSource;
		this.shaderType = shaderType;
		this.context = context;
	}

	/**
	 * Call after render / resource refresh to force shader reload.
	 */
	public final void forceReload() {
		needsLoad = true;
	}

	public final int glId() {
		if (needsLoad) {
			load();
		}

		return isErrored ? -1 : glId;
	}

	private final void load() {
		needsLoad = false;
		isErrored = false;
		String source = null;
		String error = null;

		try {
			if (glId <= 0) {
				glId = GL21.glCreateShader(shaderType);
				if (glId == 0) {
					glId = -1;
					isErrored = true;
					return;
				}
			}

			source = getSource();

			GL21.glShaderSource(glId, source);
			GL21.glCompileShader(glId);

			if (GL21.glGetShaderi(glId, GL21.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
				isErrored = true;
				error = CanvasGlHelper.getShaderInfoLog(glId);
				if(error.isEmpty()) {
					error = "Unknown OpenGL Error.";
				}
			}

		} catch (final Exception e) {
			isErrored = true;
			error = e.getMessage();
		}

		if(isErrored) {
			if(glId > 0) {
				GL21.glDeleteShader(glId);
				glId = -1;
			}

			if(Configurator.conciseErrors) {
				if(!isErrorNoticeComplete) {
					CanvasMod.LOG.error(I18n.translate("error.canvas.fail_create_any_shader"));
					isErrorNoticeComplete = true;
				}
			} else {
				CanvasMod.LOG.error(I18n.translate("error.canvas.fail_create_shader", shaderSource.toString(), context.name, error));
			}
			outputDebugSource(source, error);

		} else if(Configurator.shaderDebug) {
			outputDebugSource(source, null);
		}
	}

	public static void forceReloadErrors() {
		isErrorNoticeComplete = false;
		clearDebugSource();
	}

	private static void clearDebugSource() {
		try {
			final File gameDir = FabricLoader.getInstance().getGameDirectory();
			File shaderDir = new File(gameDir.getAbsolutePath().replace(".", "canvas_shader_debug"));

			if(shaderDir.exists()) {
				final File files[] = shaderDir.listFiles();
				for(final File f : files) {
					if (f.toString().endsWith(".glsl")) {
						f.delete();
					}
				}
			}

			shaderDir = new File(gameDir.getAbsolutePath().replace(".", "canvas_shader_debug/failed"));
			if(shaderDir.exists()) {
				final File files[] = shaderDir.listFiles();
				for(final File f : files) {
					if (f.toString().endsWith(".glsl")) {
						f.delete();
					}
				}
			}
		} catch(final Exception e){
			// TODO: log failure to output source
		}
	}

	private static boolean needsDebugOutputWarning = true;

	private void outputDebugSource(String source, String error) {
		final String key = shaderSource.toString().replace("/", "-") + "."  + context.name;
		final File gameDir = FabricLoader.getInstance().getGameDirectory();
		File shaderDir = new File(gameDir.getAbsolutePath().replace(".", "canvas_shader_debug"));

		if (!shaderDir.exists()) {
			shaderDir.mkdir();
			CanvasMod.LOG.info("Created shader debug output folder" + shaderDir.toString());
		}

		if(error != null) {
			shaderDir = new File(gameDir.getAbsolutePath().replace(".", "canvas_shader_debug/failed"));

			if (!shaderDir.exists()) {
				shaderDir.mkdir();
				CanvasMod.LOG.info("Created shader debug output failure folder" + shaderDir.toString());
			}

			source += "\n\n///////// ERROR ////////\n" + error + "\n////////////////////////\n";
		}

		if (shaderDir.exists()) {
			try(FileWriter writer = new FileWriter(shaderDir.getAbsolutePath() + File.separator + key + ".glsl", false)) {
				writer.write(source);
				writer.close();
			} catch (final IOException e) {
				if (needsDebugOutputWarning) {
					CanvasMod.LOG.error(I18n.translate("error.canvas.fail_create_shader_output", shaderDir), e);
					needsDebugOutputWarning = false;
				}
			}
		}
	}

	public String getSource() {
		String result = getShaderSource();

		if (shaderType == GL21.GL_FRAGMENT_SHADER) {
			result = result.replaceAll("#define SHADER_TYPE SHADER_TYPE_VERTEX", "#define SHADER_TYPE SHADER_TYPE_FRAGMENT");
		}

		if(context.materialContext.isBlock) {
			result = result.replaceAll("#define CONTEXT_IS_BLOCK FALSE", "#define CONTEXT_IS_BLOCK TRUE");
		}

		if(context.materialContext.isItem) {
			result = result.replaceAll("#define CONTEXT_IS_ITEM FALSE", "#define CONTEXT_IS_ITEM TRUE");
		}

		if(context.materialContext.isGui) {
			result = result.replaceAll("#define CONTEXT_IS_GUI FALSE", "#define CONTEXT_IS_GUI TRUE");
		}

		if(Configurator.subtleFog && !context.materialContext.isGui) {
			result = result.replaceAll("#define SUBTLE_FOG FALSE", "#define SUBTLE_FOG TRUE");
		}

		if(!context.materialContext.isBlock) {
			result = result.replaceAll("#define CONTEXT_IS_BLOCK TRUE", "#define CONTEXT_IS_BLOCK FALSE");
		}

		if(Configurator.lightmapNoise && Configurator.hdLightmaps) {
			result = result.replaceAll("#define ENABLE_LIGHT_NOISE FALSE", "#define ENABLE_LIGHT_NOISE TRUE");
		}

		if(Configurator.aoShadingMode != AoMode.NORMAL) {
			result = result.replaceAll("#define AO_SHADING_MODE AO_MODE_NORMAL",
					"#define AO_SHADING_MODE AO_MODE_" + Configurator.aoShadingMode.name());
		}

		if(Configurator.diffuseShadingMode != DiffuseMode.NORMAL) {
			result = result.replaceAll("#define DIFFUSE_SHADING_MODE DIFFUSE_MODE_NORMAL",
					"#define DIFFUSE_SHADING_MODE DIFFUSE_MODE_" + Configurator.diffuseShadingMode.name());
		}

		if(CanvasGlHelper.useGpuShader4() ) {
			result = result.replaceAll("#define USE_FLAT_VARYING FALSE", "#define USE_FLAT_VARYING TRUE");
		} else {
			result = result.replaceAll("#extension GL_EXT_gpu_shader4 : enable", "");
		}

		return result;
	}

	private static final HashSet<String> INCLUDED = new HashSet<>();
	static final Pattern PATTERN = Pattern.compile("^#include\\s+([\\w]+:[\\w/\\.]+)[ \\t]*.*", Pattern.MULTILINE);

	private String getShaderSource() {
		final ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();

		INCLUDED.clear();


		if (shaderType == GL21.GL_FRAGMENT_SHADER) {
			return getShaderSourceInner(resourceManager, Configurator.hdLightmaps ? ShaderData.HD_FRAGMENT : ShaderData.VANILLA_FRAGMENT);
		} else {
			return getShaderSourceInner(resourceManager, Configurator.hdLightmaps ? ShaderData.HD_VERTEX : ShaderData.VANILLA_VERTEX);
		}
	}

	private Identifier remapTargetId(Identifier id) {
		return id.equals(ShaderData.API_TARGET) ? shaderSource : id;
	}

	private String getShaderSourceInner(ResourceManager resourceManager, Identifier shaderSource) {
		shaderSource  = remapTargetId(shaderSource);

		try(Resource resource = resourceManager.getResource(shaderSource)) {
			try (Reader reader = new InputStreamReader(resource.getInputStream())) {
				String result = CharStreams.toString(reader);
				final Matcher m = PATTERN.matcher(result);

				while(m.find()) {
					final String id = m.group(1);

					if (INCLUDED.contains(id)) {
						result = StringUtils.replace(result, m.group(0), "");
					} else {
						INCLUDED.add(id);
						final String src = getShaderSourceInner(resourceManager, new Identifier(id));
						result = StringUtils.replace(result, m.group(0), src);
					}
				}

				return result;
			}
		} catch (final FileNotFoundException e) {
			CanvasMod.LOG.warn("Unable to load shader resource " + shaderSource.toString() + ". File was not found.");
			return "";
		} catch (final IOException e) {
			CanvasMod.LOG.warn("Unable to load shader resource " + shaderSource.toString() + " due to exception.", e);
			return "";
		}
	}
}
