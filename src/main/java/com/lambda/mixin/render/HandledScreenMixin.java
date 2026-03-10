/*
 * Copyright 2026 Lambda
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.lambda.mixin.render;

import com.lambda.module.modules.player.InventoryTweaks;
import com.lambda.module.modules.render.ContainerPreview;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    // TwinkNet start - Inventory drag to move
    @Shadow
    @Nullable
    protected abstract Slot getSlotAt(double mouseX, double mouseY);

    @Shadow
    protected abstract void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType);
    // TwinkNet end

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (ContainerPreview.INSTANCE.isEnabled() && ContainerPreview.isLocked()) {
            if (ContainerPreview.isMouseOverLockedTooltip((int) click.x(), (int) click.y())) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void onMouseReleased(Click click, CallbackInfoReturnable<Boolean> cir) {
        if (ContainerPreview.INSTANCE.isEnabled() && ContainerPreview.isLocked()) {
            if (ContainerPreview.isMouseOverLockedTooltip((int) click.x(), (int) click.y())) {
                cir.setReturnValue(true);
            }
        }
    }

    // TwinkNet start - Inventory drag to move
    @Inject(method = "mouseDragged", at = @At("TAIL"))
    private void onMouseDragged(Click click, double offsetX, double offsetY, CallbackInfoReturnable<Boolean> cir) {
        boolean flag = InventoryTweaks.INSTANCE.isEnabled() && InventoryTweaks.INSTANCE.doDragToMove();
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT || !flag) {
            return;
        }
        Slot slot = getSlotAt(click.x(), click.y());
        if (slot != null && slot.hasStack() && MinecraftClient.getInstance().isShiftPressed()) {
            onMouseClick(slot, slot.id, click.button(), SlotActionType.QUICK_MOVE);
        }
    }
    // TwinkNet end
}
