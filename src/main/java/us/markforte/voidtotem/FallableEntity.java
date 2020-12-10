package us.markforte.voidtotem;

import net.minecraft.util.math.BlockPos;

public interface FallableEntity {

    BlockPos getLastNonFallingPosition();

}
