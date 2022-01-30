package frozenblock.wild.mod.mixins;

import frozenblock.wild.mod.WildMod;
import frozenblock.wild.mod.fromAccurateSculk.ActivatorGrower;
import frozenblock.wild.mod.fromAccurateSculk.CatalystThreader;
import frozenblock.wild.mod.fromAccurateSculk.SculkTags;
import frozenblock.wild.mod.liukrastapi.Sphere;
import frozenblock.wild.mod.registry.RegisterAccurateSculk;
import frozenblock.wild.mod.registry.RegisterBlocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

	@Inject(method = "setHealth", at = @At("HEAD"))
	private void setHealth(float f, CallbackInfo info) {
		LivingEntity entity = LivingEntity.class.cast(this);
		if (entity.getType()==EntityType.ENDER_DRAGON && f==0.0F) {
			entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, entity.getBlockPos().down(20));
			entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, entity.getBlockPos().down(14));
			entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, entity.getBlockPos().down(7));
			entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, entity.getBlockPos());
			entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, entity.getBlockPos().up(7));
			entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, entity.getBlockPos().up(14));
			entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, entity.getBlockPos().up(20));
			}
		}


	@Inject(method = "updatePostDeath", at = @At("HEAD"))
	private void updatePostDeath(CallbackInfo info) throws InterruptedException {
		LivingEntity entity = LivingEntity.class.cast(this);
		++entity.deathTime;
		if (entity.deathTime == 19 && !entity.world.isClient()) {
			BlockPos pos = new BlockPos(entity.getBlockPos().getX(), entity.getBlockPos().getY(), entity.getBlockPos().getZ());
			if (SculkTags.DROPSXP.contains(entity.getType()) && entity.world.getGameRules().getBoolean(WildMod.DO_CATALYST_POLLUTION)) {

			if (Sphere.sphereBlock(RegisterBlocks.SCULK_CATALYST, entity.world, pos, 8)) {

				if (!entity.world.getGameRules().getBoolean(WildMod.SCULK_THREADING)) {
					new ActivatorGrower().startActivator(entity, pos);
				} else if (entity.world.getGameRules().getBoolean(WildMod.SCULK_THREADING)) {
						int numCatalysts = Sphere.generateSphere(pos, 8, false, entity.world);
						if (SculkTags.THREE.contains(entity.getType())) {
							entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, pos);
							CatalystThreader.main(entity.world, pos, 3, 4, numCatalysts, 7);
						} else if (SculkTags.FIVE.contains(entity.getType())) {
							entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, pos);
							CatalystThreader.main(entity.world, pos, 4, 5, numCatalysts, 7);
						} else if (SculkTags.TEN.contains(entity.getType())) {
							entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, pos);
							CatalystThreader.main(entity.world, pos, 9, 10, numCatalysts, 6);
						} else if (SculkTags.TWENTY.contains(entity.getType())) {
							entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, pos);
							CatalystThreader.main(entity.world, pos, 19, 20, numCatalysts, 9);
						} else if (SculkTags.FIFTY.contains(entity.getType())) {
							entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, pos);
							CatalystThreader.main(entity.world, pos, 59, 50, numCatalysts, 14);
						} else if (SculkTags.ONEHUNDRED.contains(entity.getType())) {
							entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, pos);
							CatalystThreader.main(entity.world, pos, 1599, 33, numCatalysts, 20);
						} else if (entity.world.getGameRules().getBoolean(WildMod.CATALYST_DETECTS_ALL)) {
							entity.emitGameEvent(RegisterAccurateSculk.DEATH, entity, pos);
							CatalystThreader.main(entity.world, pos, (UniformIntProvider.create(1, 7).get(entity.world.getRandom())) * numCatalysts, (UniformIntProvider.create(1, 7).get(entity.world.getRandom())), numCatalysts, 5);
						}
					}
				}
			}
		}
		}
	}
