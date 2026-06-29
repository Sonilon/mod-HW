package com.chatguard.mixin;

import com.chatguard.event.ChatBuffer;
import com.chatguard.util.ChatParser;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @Inject(method = "addMessage", at = @At("HEAD"))
    public void onMessage(Text message, CallbackInfo ci) {

        var parsed = ChatParser.parse(message.getString());

        if (parsed != null) {
            ChatBuffer.queue.add(parsed);
        }
    }
}
