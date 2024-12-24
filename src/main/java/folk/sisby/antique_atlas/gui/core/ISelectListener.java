package folk.sisby.antique_atlas.gui.core;

/**
 * Listener for button select in a RadioGroup.
 */
public interface ISelectListener<B extends ToggleButtonComponent> {
	/**
	 * Called when a button in the group was selected.
	 *
	 * @param button the button which was selected.
	 */
	void onSelect(B button);
}
