package com.syuto.bytes.module;

import com.syuto.bytes.Byte;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.api.SettingHolder;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public abstract class Module extends SettingHolder {

    protected final Minecraft mc = Minecraft.getInstance();
    protected float delta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
    public final String name, description;
    public final Category category;
    public boolean enabled;
    public int key;

    // New field for suffix
    private Supplier<String> suffix = () -> "";

    protected void onEnable() {}
    protected void onDisable() {}

    public final void toggle() {
        setEnabled(!enabled);
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (this.enabled) {
                onEnable();
                Byte.INSTANCE.eventBus.register(this);
                //ChatUtils.print("Enabled " + this.name);
            } else {
                Byte.INSTANCE.eventBus.unregister(this);
                //ChatUtils.print("Disabled " + this.name);
                onDisable();
            }

        }
    }

    @NotNull
    public String getSuffix() {
        return suffix.get();
    }
}
