package us.markforte.voidtotem.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import us.markforte.voidtotem.FallableEntity;


@Mixin(Entity.class)
public abstract class MixinEntity implements Nameable, CommandOutput, FallableEntity {

    @Shadow protected boolean onGround;

    @Shadow public abstract BlockPos getBlockPos();

    @Shadow public abstract void move(MovementType type, Vec3d movement);

    BlockPos lastSafeBlock;
    boolean setLastBlock = false;

    @Inject(method = "move", at = @At("RETURN"))
    public void injectMove(MovementType type, Vec3d movement, CallbackInfo ci) {
        if (lastSafeBlock == null) {
            lastSafeBlock = getBlockPos();
        }
        if (!onGround) {
            if (!setLastBlock) {
                lastSafeBlock = lastSafeBlock.mutableCopy();
            }
            setLastBlock = true;
            return;
        }
        setLastBlock = false;
        if (movement.y == 0) {
            lastSafeBlock = getBlockPos();
        }
    }

    public BlockPos getLastNonFallingPosition() {
        return lastSafeBlock;
    }


}
