package folk.sisby.antique_atlas.client.gui.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.List;

/**
 * A GuiComponent that can act like a button.
 */
@SuppressWarnings("rawtypes")
public class GuiComponentButton extends GuiComponent {
    private final List<IButtonListener> listeners = new ArrayList<>();

    private boolean enabled = true;
    private final SoundEvent clickSound = SoundEvents.UI_BUTTON_CLICK.value();

    public void setEnabled(boolean value) {
        enabled = value;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean mouseClicked(double x, double y, int mouseButton) {
        if (mouseButton == 0 /*left-click*/ && enabled && isMouseOver) {
            onClick();
            return true;
        }

        return super.mouseClicked(x, y, mouseButton);
    }

    /**
     * Called when the user left-clicks on this component.
     */
    @SuppressWarnings("unchecked")
    void onClick() {
        if (clickSound != null) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(clickSound, 1.0F));
        }

        for (IButtonListener listener : listeners) {
            listener.onClick(this);
        }
    }

    public void addListener(IButtonListener listener) {
        listeners.add(listener);
    }
}
