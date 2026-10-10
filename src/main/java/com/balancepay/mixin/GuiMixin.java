package com.balancepay.mixin;

import com.balancepay.BalancePay;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Blocks the on-screen popups: action bar text, titles and subtitles. */
@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "setOverlayMessage", at = @At("HEAD"), cancellable = true, require = 0)
    private void balancepay$overlay(Component message, boolean animate, CallbackInfo ci) {
        if (message != null && BalancePay.shouldHide(message.getString())) ci.cancel();
    }

    @Inject(method = "setTitle", at = @At("HEAD"), cancellable = true, require = 0)
    private void balancepay$title(Component message, CallbackInfo ci) {
        if (message != null && BalancePay.shouldHide(message.getString())) ci.cancel();
    }

    @Inject(method = "setSubtitle", at = @At("HEAD"), cancellable = true, require = 0)
    private void balancepay$subtitle(Component message, CallbackInfo ci) {
        if (message != null && BalancePay.shouldHide(message.getString())) ci.cancel();
    }
}
