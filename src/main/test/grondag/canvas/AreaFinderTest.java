package grondag.canvas;

import java.util.Random;

import io.netty.util.internal.ThreadLocalRandom;
import org.junit.jupiter.api.Test;

import grondag.canvas.chunk.occlusion.region.Area;
import grondag.canvas.chunk.occlusion.region.AreaFinder;
import grondag.canvas.chunk.occlusion.region.AreaSample;
import grondag.canvas.chunk.occlusion.region.AreaUtil;
import grondag.canvas.chunk.occlusion.region.OcclusionBitPrinter;

class AreaFinderTest {

	@Test
	void test() {
		Area rect = new Area(AreaUtil.areaKey(0, 0, 15, 15), 0);
		assert(rect.areaHash == -1L);

		rect = new Area(AreaUtil.areaKey(0, 0, 0, 0), 0);
		assert(rect.areaHash == 1);

		rect = new Area(AreaUtil.areaKey(4, 4, 5, 5), 0);
		assert rect.areaHash == AreaUtil.areaWordHashMask(2, 2);

		rect = new Area(AreaUtil.areaKey(11, 11, 13, 13), 0);
		assert rect.areaHash == (AreaUtil.areaWordHashMask(5, 5) | AreaUtil.areaWordHashMask(5, 6) | AreaUtil.areaWordHashMask(6, 5) | AreaUtil.areaWordHashMask(6, 6));


		final AreaFinder finder = new AreaFinder();
		final AreaSample sample = new AreaSample();

		final Random rand = ThreadLocalRandom.current();

		for(int i = 0; i < 512; i++) {
			final int x = rand.nextInt(16);
			final int y = rand.nextInt(16);
			sample.fill(x, y, x, y);
		}

		System.out.println("INPUT");
		OcclusionBitPrinter.printShape(sample.bits, 0);

		System.out.println();
		System.out.println("OUTPUT");
		finder.find(sample.bits, 0);

		for (final Area a : finder.areas) {
			a.printShape();
		}
	}
}
