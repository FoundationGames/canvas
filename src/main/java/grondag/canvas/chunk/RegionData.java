package grondag.canvas.chunk;

import java.util.List;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.block.entity.BlockEntity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import grondag.canvas.buffer.packing.VertexCollectorImpl;
import grondag.canvas.buffer.packing.VertexCollectorList;
import grondag.canvas.draw.DrawHandlers;
import grondag.canvas.material.MaterialContext;
import grondag.canvas.material.MaterialState;

@Environment(EnvType.CLIENT)
public class RegionData {
	public static final RegionData EMPTY = new RegionData();

	final ObjectArrayList<BlockEntity> blockEntities = new ObjectArrayList<>();
	int[] occlusionData = null;
	int backfaceCullFlags;

	@Nullable int[] translucentState;

	public List<BlockEntity> getBlockEntities() {
		return blockEntities;
	}

	public void endBuffering(float x, float y, float z, VertexCollectorList buffers) {
		final VertexCollectorImpl buffer = buffers.getIfExists(MaterialState.get(MaterialContext.TERRAIN, DrawHandlers.TRANSLUCENT));

		if (buffer != null) {
			buffer.sortQuads(x, y, z);
			translucentState = buffer.saveState(translucentState);
		}
	}

	public int[] getOcclusionData() {
		return occlusionData;
	}

	public int backfaceCullFlags() {
		return backfaceCullFlags;
	}

	public void complete(int[] occlusionData, int backfaceCullFlags) {
		this.occlusionData = occlusionData;
		this.backfaceCullFlags = backfaceCullFlags;
	}
}