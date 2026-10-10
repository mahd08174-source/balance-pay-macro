package com.balancepay.mixin;

import com.balancepay.BalancePay;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatListener.class)
public class ChatListenerMixin {
    // Covers both normal system chat and overlay (action bar) system messages.
    @Inject(method = "handleSystemMessage", at = @At("HEAD"), cancellable = true, require = 0)
    private void balancepay$hide(Component message, boolean overlay, CallbackInfo ci) {
        if (BalancePay.shouldHide(message.getString())) {
            ci.cancel();
        }
    }
}
