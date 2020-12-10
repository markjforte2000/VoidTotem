package us.markforte.voidtotem.mixin;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import us.markforte.voidtotem.FallableEntity;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow public abstract ItemStack getStackInHand(Hand hand);
    @Shadow public abstract void setHealth(float health);
    @Shadow public abstract boolean clearStatusEffects();
    @Shadow public abstract boolean addStatusEffect(StatusEffectInstance effect);


    @Shadow public abstract boolean teleport(double x, double y, double z, boolean particleEffects);

    /**
     * @author Mark Forte
     * @reason need to include death by falling into the void
     */
    @Overwrite
    private boolean tryUseTotem(DamageSource source) {

        // If the player is killed by void damage not caused by falling out of the world. eg: /kill
        // wont work for players falling into the void that were /killed
        if (source.isOutOfWorld() && getBlockPos().getY() > 0) {
            return false;
        }

        ItemStack totem = null;
        Hand[] heldItems = Hand.values();

        for (Hand heldItem : heldItems) {
            ItemStack stack = this.getStackInHand(heldItem);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                totem = stack.copy();
                stack.decrement(1);
                break;
            }
        }

        if (totem == null) {
            return false;
        }

        // teleport player to their last position
        if (source.isOutOfWorld()) {
            fallDistance = 0;
            BlockPos lastGround = ((FallableEntity)this).getLastNonFallingPosition();
            teleport(lastGround.getX(), lastGround.getY(), lastGround.getZ());
        }

        if (EntityType.PLAYER.equals(getType())) {
            ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
            player.incrementStat(Stats.USED.getOrCreateStat(Items.TOTEM_OF_UNDYING));
            Criteria.USED_TOTEM.trigger(player, totem);
        }

        this.setHealth(1.0F);
        this.clearStatusEffects();
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
        this.world.sendEntityStatus(this, (byte)35);

        return true;
    }


}
