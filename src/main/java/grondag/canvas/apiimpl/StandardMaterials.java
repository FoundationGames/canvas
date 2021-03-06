package grondag.canvas.apiimpl;

import java.util.IdentityHashMap;

import net.minecraft.client.render.RenderLayer;

import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;

import grondag.canvas.apiimpl.RenderMaterialImpl.CompositeMaterial;

// TODO: expose in API as alternate for render layer
public class StandardMaterials {
	public static final CompositeMaterial BLOCK_TRANSLUCENT = Canvas.INSTANCE.materialFinder().blendMode(0, BlendMode.TRANSLUCENT).find();
	public static final CompositeMaterial BLOCK_SOLID = Canvas.INSTANCE.materialFinder().blendMode(0, BlendMode.SOLID).find();
	public static final CompositeMaterial BLOCK_CUTOUT = Canvas.INSTANCE.materialFinder().blendMode(0, BlendMode.CUTOUT).find();
	public static final CompositeMaterial BLOCK_CUTOUT_MIPPED = Canvas.INSTANCE.materialFinder().blendMode(0, BlendMode.CUTOUT_MIPPED).find();

	private  static final IdentityHashMap<RenderLayer, RenderMaterialImpl.CompositeMaterial> LAYER_MAP = new IdentityHashMap<>();

	static {
		LAYER_MAP.put(RenderLayer.getSolid(), BLOCK_SOLID);
		LAYER_MAP.put(RenderLayer.getCutout(), BLOCK_CUTOUT);
		LAYER_MAP.put(RenderLayer.getCutoutMipped(), BLOCK_CUTOUT_MIPPED);
		LAYER_MAP.put(RenderLayer.getTranslucent(), BLOCK_TRANSLUCENT);
	}

	public static CompositeMaterial get(RenderLayer layer) {
		return LAYER_MAP.get(layer);
	}
}
