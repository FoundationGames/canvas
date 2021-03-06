/*******************************************************************************
 * Copyright 2019, 2020 grondag
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
package grondag.canvas;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

import blue.endless.jankson.Comment;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import grondag.canvas.apiimpl.Canvas;

@Environment(EnvType.CLIENT)
public class Configurator {

	@SuppressWarnings("hiding")
	static class ConfigData {
		@Comment("Applies material properties and shaders to items. (WIP)")
		boolean itemShaderRender = false;

		@Comment("Makes terrain fog a little less foggy.")
		boolean subtleFog = false;

		@Comment("Truly smoothh lighting. Some impact to memory use, chunk loading and frame rate.")
		boolean hdLightmaps = false;

		@Comment("Slight variation in light values - may prevent banding. Slight performance impact and not usually necessary.")
		boolean lightmapNoise = false;

		@Comment("Mimics directional light.")
		DiffuseMode diffuseShadingMode = DiffuseMode.NORMAL;

		@Comment("Makes light sources less cross-shaped. Chunk loading a little slower. Overall light levels remain similar.")
		boolean lightSmoothing = false;

		@Comment("Mimics light blocked by nearby objects.")
		AoMode aoShadingMode = AoMode.NORMAL;

		@Comment("Setting > 0 may give slightly better FPS at cost of potential flickering when lighting changes.")
		int maxLightmapDelayFrames = 0;

		@Comment("Extra lightmap capacity. Ensure enabled if you are getting `unable to create HD lightmap(s) - out of space' messages.")
		boolean moreLightmap = true;

		@Comment("Draws multiple chunks with same view transformation. Much faster, but try without if you see visual defects.")
		boolean batchedChunkRender = true;

		//        @Comment("TODO")
		//        boolean disableVanillaChunkMatrix = true;

		@Comment("Adjusts quads on some vanilla models (like iron bars) to avoid z-fighting with neighbor blocks.")
		boolean preventDepthFighting = true;

		@Comment("Forces game to allow up to this many nanoseconds for chunk loading each frame. May prevent chunk load delay at high FPS.")
		long minChunkBudgetNanos = 100000;

		@Comment("Treats model geometry outside of block boundaries as on the block for lighting purposes. Helps prevent bad lighting outcomes.")
		boolean clampExteriorVertices = true;

		@Comment("Prevent Glowstone and other blocks that emit light from casting shade on nearby blocks.")
		boolean fixLuminousBlockShading = true;

		@Comment("Distant terrain will omit back-facing polygons. Experimental.  May or may not improve performance.")
		boolean terrainBackfaceCulling = false;

		@Comment("Terrain setup done off the main render thread. Increases FPS when moving. May see occasional flashes of blank chunks")
		boolean terrainSetupOffThread = true;

		@Comment("Max number of unique render material states. Change only if errors occur without. Causes small amount of memory use.")
		int maxMaterialStates = 0x10000;

		// DEBUG
		@Comment("Output runtime per-material shader source. For shader development debugging.")
		boolean shaderDebug = false;

		@Comment("Shows HD lightmap pixels for debug purposes. Also looks cool.")
		boolean lightmapDebug = false;

		@Comment("Summarizes multiple errors and warnings to single-line entries in the log.")
		boolean conciseErrors = true;

		@Comment("Writes information useful for bug reports to the game log at startup.")
		boolean logMachineInfo = true;

		@Comment("Writes OpenGL state changes to log.  *VERY SPAMMY - KILLS FRAME RATE*  Used only for debugging.")
		boolean logGlStateChanges = false;

		@Comment("Enables LWJGL memory allocation tracking.  Will harm performance. Use for debugging memory leaks. Requires restart.")
		boolean debugNativeMemoryAllocation = false;

		@Comment("Uses slower and safer memory allocation method for GL buffers.  Use only if having problems. Requires restart.")
		boolean safeNativeMemoryAllocation = false;

		@Comment("Output performance trade data to log. Will have significant performance impact. Requires restart.")
		boolean enablePerformanceTrace = false;

		@Comment("Output periodic snapshots of terrain occlusion raster. Will have performance impact.")
		boolean debugOcclusionRaster = false;

		@Comment("Render active occlusion boxes of targeted render region. Will have performance impact and looks strange.")
		boolean debugOcclusionBoxes = false;
	}

	static final ConfigData DEFAULTS = new ConfigData();
	private static final Gson GSON = new GsonBuilder().create();
	private static final Jankson JANKSON = Jankson.builder().build();

	public static boolean itemShaderRender = DEFAULTS.itemShaderRender;
	public static boolean subtleFog = DEFAULTS.subtleFog;
	public static boolean shaderDebug = DEFAULTS.shaderDebug;
	public static int maxLightmapDelayFrames = DEFAULTS.maxLightmapDelayFrames;

	public static boolean hdLightmaps = DEFAULTS.hdLightmaps;
	public static boolean lightmapNoise = DEFAULTS.lightmapNoise;
	public static DiffuseMode diffuseShadingMode = DEFAULTS.diffuseShadingMode;
	public static boolean lightSmoothing = DEFAULTS.lightSmoothing;
	public static AoMode aoShadingMode = DEFAULTS.aoShadingMode;
	public static boolean moreLightmap = DEFAULTS.moreLightmap;

	public static long minChunkBudgetNanos = DEFAULTS.minChunkBudgetNanos;

	public static boolean batchedChunkRender = DEFAULTS.batchedChunkRender;
	public static boolean disableVanillaChunkMatrix = false; //DEFAULTS.disableVanillaChunkMatrix;
	public static boolean preventDepthFighting = DEFAULTS.preventDepthFighting;
	public static boolean clampExteriorVertices = DEFAULTS.clampExteriorVertices;
	public static boolean fixLuminousBlockShading = DEFAULTS.fixLuminousBlockShading;
	public static boolean terrainBackfaceCulling = DEFAULTS.terrainBackfaceCulling;
	public static boolean terrainSetupOffThread = DEFAULTS.terrainSetupOffThread;
	public static int maxMaterialStates = DEFAULTS.maxMaterialStates;

	public static boolean lightmapDebug = DEFAULTS.lightmapDebug;
	public static boolean conciseErrors = DEFAULTS.conciseErrors;
	public static boolean logMachineInfo = DEFAULTS.logMachineInfo;
	public static boolean logGlStateChanges = DEFAULTS.logGlStateChanges;
	public static boolean debugNativeMemoryAllocation = DEFAULTS.debugNativeMemoryAllocation;
	public static boolean safeNativeMemoryAllocation = DEFAULTS.safeNativeMemoryAllocation;
	public static boolean enablePerformanceTrace = DEFAULTS.enablePerformanceTrace;
	public static boolean debugOcclusionRaster = DEFAULTS.debugOcclusionRaster;
	public static boolean debugOcclusionBoxes = DEFAULTS.debugOcclusionBoxes;

	/** use to stash parent screen during display */
	private static Screen screenIn;

	private static File configFile;

	public static void init() {
		configFile = new File(FabricLoader.getInstance().getConfigDirectory(), "canvas.json5");
		if(configFile.exists()) {
			loadConfig();
		} else {
			saveConfig();
		}
	}

	private static void loadConfig() {
		ConfigData config = new ConfigData();
		try {
			final JsonObject configJson = JANKSON.load(configFile);
			final String regularized = configJson.toJson(false, false, 0);
			config = GSON.fromJson(regularized, ConfigData.class);
		} catch (final Exception e) {
			e.printStackTrace();
			CanvasMod.LOG.error("Unable to load config. Using default values.");
		}
		itemShaderRender = config.itemShaderRender;
		subtleFog = config.subtleFog;
		shaderDebug = config.shaderDebug;
		minChunkBudgetNanos = config.minChunkBudgetNanos;
		maxLightmapDelayFrames = config.maxLightmapDelayFrames;
		moreLightmap = config.moreLightmap;

		hdLightmaps = config.hdLightmaps;
		lightmapNoise = config.lightmapNoise;
		diffuseShadingMode = config.diffuseShadingMode;
		lightSmoothing = config.lightSmoothing;
		aoShadingMode = config.aoShadingMode;

		batchedChunkRender = config.batchedChunkRender;
		//        disableVanillaChunkMatrix = config.disableVanillaChunkMatrix;
		preventDepthFighting = config.preventDepthFighting;
		clampExteriorVertices = config.clampExteriorVertices;
		fixLuminousBlockShading = config.fixLuminousBlockShading;
		terrainBackfaceCulling = config.terrainBackfaceCulling;
		terrainSetupOffThread = config.terrainSetupOffThread;
		maxMaterialStates =  MathHelper.clamp(config.maxMaterialStates, 0x10000, 0x100000);

		lightmapDebug = config.lightmapDebug;
		conciseErrors = config.conciseErrors;
		logMachineInfo = config.logMachineInfo;
		logGlStateChanges = config.logGlStateChanges;
		debugNativeMemoryAllocation = config.debugNativeMemoryAllocation;
		safeNativeMemoryAllocation = config.safeNativeMemoryAllocation;
		enablePerformanceTrace = config.enablePerformanceTrace;
		debugOcclusionBoxes = config.debugOcclusionBoxes;
		debugOcclusionRaster = config.debugOcclusionRaster;
	}

	private static void saveConfig() {
		final ConfigData config = new ConfigData();
		config.itemShaderRender = itemShaderRender;
		config.subtleFog = subtleFog;
		config.shaderDebug = shaderDebug;
		config.minChunkBudgetNanos = minChunkBudgetNanos;
		config.maxLightmapDelayFrames = maxLightmapDelayFrames;

		config.hdLightmaps = hdLightmaps;
		config.lightmapNoise = lightmapNoise;
		config.diffuseShadingMode = diffuseShadingMode;
		config.lightSmoothing = lightSmoothing;
		config.aoShadingMode = aoShadingMode;
		config.moreLightmap = moreLightmap;

		config.batchedChunkRender = batchedChunkRender;
		//        config.disableVanillaChunkMatrix = disableVanillaChunkMatrix;
		config.preventDepthFighting = preventDepthFighting;
		config.clampExteriorVertices = clampExteriorVertices;
		config.fixLuminousBlockShading = fixLuminousBlockShading;
		config.terrainBackfaceCulling = terrainBackfaceCulling;
		config.terrainSetupOffThread = terrainSetupOffThread;
		config.maxMaterialStates = maxMaterialStates;

		config.lightmapDebug = lightmapDebug;
		config.conciseErrors = conciseErrors;
		config.logMachineInfo = logMachineInfo;
		config.logGlStateChanges = logGlStateChanges;
		config.debugNativeMemoryAllocation = debugNativeMemoryAllocation;
		config.safeNativeMemoryAllocation = safeNativeMemoryAllocation;
		config.enablePerformanceTrace = enablePerformanceTrace;
		config.debugOcclusionBoxes = debugOcclusionBoxes;
		config.debugOcclusionRaster = debugOcclusionRaster;

		try {
			final String result = JANKSON.toJson(config).toJson(true, true, 0);
			if (!configFile.exists()) {
				configFile.createNewFile();
			}

			try(
					FileOutputStream out = new FileOutputStream(configFile, false);
					) {
				out.write(result.getBytes());
				out.flush();
				out.close();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			CanvasMod.LOG.error("Unable to save config.");
			return;
		}
	}

	static boolean reload = false;

	public enum AoMode {
		NORMAL,
		SUBTLE_ALWAYS,
		SUBTLE_BLOCK_LIGHT,
		NONE;

		@Override
		public String toString() {
			return I18n.translate("config.canvas.enum.ao_mode." + name().toLowerCase());
		}
	}

	public enum DiffuseMode {
		NORMAL,
		SKY_ONLY,
		NONE;

		@Override
		public String toString() {
			return I18n.translate("config.canvas.enum.diffuse_mode." + name().toLowerCase());
		}
	}

	private static ConfigEntryBuilder ENTRY_BUILDER = ConfigEntryBuilder.create();

	static Text[] parse(String key) {
		return Arrays.stream(I18n.translate(key).split(";")).map(s ->  new LiteralText(s)).collect(Collectors.toList()).toArray(new Text[0]);
	}

	public static Screen display(Screen screen) {
		screenIn = screen;
		reload = false;

		final ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(screenIn).setTitle(new TranslatableText("config.canvas.title")).setSavingRunnable(Configurator::saveUserInput);

		// FEATURES
		final ConfigCategory features = builder.getOrCreateCategory(new TranslatableText("config.canvas.category.features"));

		features.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.item_render"), itemShaderRender)
				.setDefaultValue(DEFAULTS.itemShaderRender)
				.setTooltip(parse("config.canvas.help.item_render"))
				.setSaveConsumer(b -> itemShaderRender = b)
				.build());

		features.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.subtle_fog"), subtleFog)
				.setDefaultValue(DEFAULTS.subtleFog)
				.setTooltip(parse("config.canvas.help.subtle_fog"))
				.setSaveConsumer(b -> {subtleFog = b; reload = true;})
				.build());

		// LIGHTING
		final ConfigCategory lighting = builder.getOrCreateCategory(new TranslatableText("config.canvas.category.lighting"));

		lighting.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.light_smoothing"), lightSmoothing)
				.setDefaultValue(DEFAULTS.lightSmoothing)
				.setTooltip(parse("config.canvas.help.light_smoothing"))
				.setSaveConsumer(b -> {lightSmoothing = b; reload = true;})
				.build());

		lighting.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.hd_lightmaps"), hdLightmaps)
				.setDefaultValue(DEFAULTS.hdLightmaps)
				.setTooltip(parse("config.canvas.help.hd_lightmaps"))
				.setSaveConsumer(b -> {hdLightmaps = b; reload = true;})
				.build());

		lighting.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.more_lightmap"), moreLightmap)
				.setDefaultValue(DEFAULTS.moreLightmap)
				.setTooltip(parse("config.canvas.help.more_lightmap"))
				.setSaveConsumer(b -> moreLightmap = b)
				.build());

		lighting.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.lightmap_noise"), lightmapNoise)
				.setDefaultValue(DEFAULTS.lightmapNoise)
				.setTooltip(parse("config.canvas.help.lightmap_noise"))
				.setSaveConsumer(b -> {lightmapNoise = b; reload = true;})
				.build());

		lighting.addEntry(ENTRY_BUILDER
				.startEnumSelector(new TranslatableText("config.canvas.value.diffuse_shading"), DiffuseMode.class, diffuseShadingMode)
				.setDefaultValue(DEFAULTS.diffuseShadingMode)
				.setTooltip(parse("config.canvas.help.diffuse_shading"))
				.setSaveConsumer(b -> {diffuseShadingMode = b; reload = true;})
				.build());

		lighting.addEntry(ENTRY_BUILDER
				.startEnumSelector(new TranslatableText("config.canvas.value.ao_shading"), AoMode.class, aoShadingMode)
				.setDefaultValue(DEFAULTS.aoShadingMode)
				.setTooltip(parse("config.canvas.help.ao_shading"))
				.setSaveConsumer(b -> {aoShadingMode = b; reload = true;})
				.build());

		lighting.addEntry(ENTRY_BUILDER
				.startIntSlider(new TranslatableText("config.canvas.value.lightmap_delay_frames"), maxLightmapDelayFrames, 0, 20)
				.setDefaultValue(DEFAULTS.maxLightmapDelayFrames)
				.setTooltip(parse("config.canvas.help.lightmap_delay_frames"))
				.setSaveConsumer(b -> maxLightmapDelayFrames = b)
				.build());

		// TWEAKS
		final ConfigCategory tweaks = builder.getOrCreateCategory(new TranslatableText("config.canvas.category.tweaks"));

		tweaks.addEntry(ENTRY_BUILDER
				.startLongField(new TranslatableText("config.canvas.value.min_chunk_budget"), minChunkBudgetNanos)
				.setDefaultValue(DEFAULTS.minChunkBudgetNanos)
				.setTooltip(parse("config.canvas.help.min_chunk_budget"))
				.setSaveConsumer(b -> minChunkBudgetNanos = b)
				.build());

		tweaks.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.batch_chunk_render"), batchedChunkRender)
				.setDefaultValue(DEFAULTS.batchedChunkRender)
				.setTooltip(parse("config.canvas.help.batch_chunk_render"))
				.setSaveConsumer(b -> batchedChunkRender = b)
				.build());

		//        tweaks.addOption(new BooleanListEntry("config.canvas.value.vanilla_chunk_matrix", disableVanillaChunkMatrix, "config.canvas.reset",
		//                () -> DEFAULTS.disableVanillaChunkMatrix, b -> disableVanillaChunkMatrix = b,
		//                () -> Optional.of(parse("config.canvas.help.vanilla_chunk_matrix"))));

		tweaks.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.adjust_vanilla_geometry"), preventDepthFighting)
				.setDefaultValue(DEFAULTS.preventDepthFighting)
				.setTooltip(parse("config.canvas.help.adjust_vanilla_geometry"))
				.setSaveConsumer(b -> {preventDepthFighting = b; reload = true;})
				.build());

		tweaks.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.clamp_exterior_vertices"), clampExteriorVertices)
				.setDefaultValue(DEFAULTS.clampExteriorVertices)
				.setTooltip(parse("config.canvas.help.clamp_exterior_vertices"))
				.setSaveConsumer(b -> {clampExteriorVertices = b; reload = true;})
				.build());

		tweaks.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.fix_luminous_block_shade"), fixLuminousBlockShading)
				.setDefaultValue(DEFAULTS.fixLuminousBlockShading)
				.setTooltip(parse("config.canvas.help.fix_luminous_block_shade"))
				.setSaveConsumer(b -> {fixLuminousBlockShading = b; reload = true;})
				.build());

		tweaks.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.terrain_backface_culling"), terrainBackfaceCulling)
				.setDefaultValue(DEFAULTS.terrainBackfaceCulling)
				.setTooltip(parse("config.canvas.help.terrain_backface_culling"))
				.setSaveConsumer(b -> {terrainBackfaceCulling = b; reload = true;})
				.build());

		tweaks.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.terrain_setup_off_thread"), terrainSetupOffThread)
				.setDefaultValue(DEFAULTS.terrainSetupOffThread)
				.setTooltip(parse("config.canvas.help.terrain_setup_off_thread"))
				.setSaveConsumer(b -> {terrainSetupOffThread = b; reload = true;})
				.build());

		lighting.addEntry(ENTRY_BUILDER
				.startIntSlider(new TranslatableText("config.canvas.value.max_material_states"), maxMaterialStates, 0x10000, 0x100000)
				.setDefaultValue(DEFAULTS.maxMaterialStates)
				.setTooltip(parse("config.canvas.help.max_material_states"))
				.setSaveConsumer(b -> maxMaterialStates = b)
				.build());

		// DEBUG
		final ConfigCategory debug = builder.getOrCreateCategory(new TranslatableText("config.canvas.category.debug"));

		debug.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.shader_debug"), shaderDebug)
				.setDefaultValue(DEFAULTS.shaderDebug)
				.setTooltip(parse("config.canvas.help.shader_debug"))
				.setSaveConsumer(b -> shaderDebug = b)
				.build());

		debug.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.shader_debug_lightmap"), lightmapDebug)
				.setDefaultValue(DEFAULTS.lightmapDebug)
				.setTooltip(parse("config.canvas.help.shader_debug_lightmap"))
				.setSaveConsumer(b -> lightmapDebug = b)
				.build());

		debug.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.concise_errors"), conciseErrors)
				.setDefaultValue(DEFAULTS.conciseErrors)
				.setTooltip(parse("config.canvas.help.concise_errors"))
				.setSaveConsumer(b -> conciseErrors = b)
				.build());

		debug.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.log_machine_info"), logMachineInfo)
				.setDefaultValue(DEFAULTS.logMachineInfo)
				.setTooltip(parse("config.canvas.help.log_machine_info"))
				.setSaveConsumer(b -> logMachineInfo = b)
				.build());

		debug.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.log_gl_state_changes"), logGlStateChanges)
				.setDefaultValue(DEFAULTS.logGlStateChanges)
				.setTooltip(parse("config.canvas.help.log_gl_state_changes"))
				.setSaveConsumer(b -> logGlStateChanges = b)
				.build());

		debug.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.debug_native_allocation"), debugNativeMemoryAllocation)
				.setDefaultValue(DEFAULTS.debugNativeMemoryAllocation)
				.setTooltip(parse("config.canvas.help.debug_native_allocation"))
				.setSaveConsumer(b -> debugNativeMemoryAllocation = b)
				.build());

		debug.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.safe_native_allocation"), safeNativeMemoryAllocation)
				.setDefaultValue(DEFAULTS.safeNativeMemoryAllocation)
				.setTooltip(parse("config.canvas.help.safe_native_allocation"))
				.setSaveConsumer(b -> safeNativeMemoryAllocation = b)
				.build());

		debug.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.debug_occlusion_raster"), debugOcclusionRaster)
				.setDefaultValue(DEFAULTS.debugOcclusionRaster)
				.setTooltip(parse("config.canvas.help.debug_occlusion_raster"))
				.setSaveConsumer(b -> debugOcclusionRaster = b)
				.build());

		debug.addEntry(ENTRY_BUILDER
				.startBooleanToggle(new TranslatableText("config.canvas.value.debug_occlusion_boxes"), debugOcclusionBoxes)
				.setDefaultValue(DEFAULTS.debugOcclusionBoxes)
				.setTooltip(parse("config.canvas.help.debug_occlusion_boxes"))
				.setSaveConsumer(b -> debugOcclusionBoxes = b)
				.build());
		builder.setDoesConfirmSave(false);

		return builder.build();
	}

	@SuppressWarnings("resource")
	private static void saveUserInput() {
		saveConfig();

		if(reload) {
			Canvas.INSTANCE.reload();
			MinecraftClient.getInstance().worldRenderer.reload();
		}
	}

	// LEGACY STUFF

	//    @LangKey("config.acuity_enable_vao")
	//    @Comment({"Use Vertex Array Objects if available.",
	//        " VAOs generally improve performance when they are supported."})
	public static boolean enable_vao = true;

	//    @LangKey("config.acuity_fancy_fluids")
	//    @Comment({"Enable fancy water and lava rendering.",
	//        " This feature is currently work in progress and has no visible effect if enabled."})
	public static boolean fancyFluids = false;


	//    @LangKey("config.disable_yield")
	//    @RequiresMcRestart
	//    @Comment({"When enabled, disables the call to Thread.yield() in the main game loop ",
	//        " that normally occurs right after display update. The call is probably meant",
	//        " to give the OpenGL drivers time to process the command buffer, but in the multi-threaded game ",
	//        " Minecraft has become, and with modern drivers, this basically invites other tasks to step on your framerate.",
	//        " This patch is purely a performance optimization and is not required for Acuity to operate."})
	//    public static boolean disableYieldInGameLoop = true;

	//    public static void handleChange() // PostConfigChangedEvent event)
	//    {
	//        boolean oldFancyFluids = fancyFluids;
	//        boolean oldVAO = enable_vao;
	//        ConfigManager.sync(Acuity.MODID, Config.Type.INSTANCE);
	//        if (oldFancyFluids != fancyFluids || oldVAO != enable_vao) {
	//            RendererImpl.INSTANCE.reload();
	//
	//            // refresh appearances
	//            MinecraftClient.getInstance().worldRenderer.reload();
	//        }
	//    }

}
