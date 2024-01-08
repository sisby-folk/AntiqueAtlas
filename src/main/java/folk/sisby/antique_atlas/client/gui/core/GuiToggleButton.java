package folk.sisby.antique_atlas.client.gui.core;

/**
 * A button that can be toggled on, and only toggled off by selecting
 * a different ToggleButton.
 */
public class GuiToggleButton extends GuiComponentButton {
    private boolean selected;

    /**
     * Sets the button selected state. If the button is part of a RadioGroup,
     * use the RadioGroup's setSelected method instead!
     */
    public void setSelected(boolean value) {
        this.selected = value;
    }

    protected boolean isSelected() {
        return selected;
    }

    @Override
    protected void onClick() {
        if (isEnabled()) {
            setSelected(!isSelected());
        }
        super.onClick();
    }
}
