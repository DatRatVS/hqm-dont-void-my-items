package datrat.hqmdontvoidmyitems.mixin;

import datrat.hqmdontvoidmyitems.qds.QdsAcceptanceHooks;
import datrat.hqmdontvoidmyitems.qds.TileEntityBarrelAccess;
import hardcorequesting.tileentity.TileEntityBarrel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityBarrel.class, remap = false)
public abstract class MixinTileEntityBarrel implements TileEntityBarrelAccess {
    @Shadow
    private int modifiedSyncTimer;

    @Shadow
    private String playerName;

    @Override
    public String hqmdontvoidmyitems$getPlayerName() {
        return this.playerName;
    }

    @Override
    public int hqmdontvoidmyitems$getModifiedSyncTimer() {
        return this.modifiedSyncTimer;
    }

    @Override
    public void hqmdontvoidmyitems$setModifiedSyncTimer(int timer) {
        this.modifiedSyncTimer = timer;
    }

    @Inject(method = "func_94041_b(ILnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void hqmdontvoidmyitems$isItemValidForSlot(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(QdsAcceptanceHooks.canAcceptItem(this.hqmdontvoidmyitems$self(), stack));
    }

    @Inject(method = "func_70299_a(ILnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private void hqmdontvoidmyitems$setInventorySlotContents(int slot, ItemStack stack, CallbackInfo ci) {
        QdsAcceptanceHooks.acceptItem(this.hqmdontvoidmyitems$self(), stack);
        ci.cancel();
    }

    @Inject(method = "canFill(Lnet/minecraftforge/common/util/ForgeDirection;Lnet/minecraftforge/fluids/Fluid;)Z", at = @At("HEAD"), cancellable = true)
    private void hqmdontvoidmyitems$canFill(ForgeDirection from, Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(QdsAcceptanceHooks.canAcceptFluid(this.hqmdontvoidmyitems$self(), fluid));
    }

    @Inject(method = "fill(Lnet/minecraftforge/common/util/ForgeDirection;Lnet/minecraftforge/fluids/FluidStack;Z)I", at = @At("HEAD"), cancellable = true)
    private void hqmdontvoidmyitems$fill(ForgeDirection from, FluidStack resource, boolean doFill, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(QdsAcceptanceHooks.acceptFluid(this.hqmdontvoidmyitems$self(), resource, doFill));
    }

    private TileEntityBarrel hqmdontvoidmyitems$self() {
        return (TileEntityBarrel) (Object) this;
    }
}
