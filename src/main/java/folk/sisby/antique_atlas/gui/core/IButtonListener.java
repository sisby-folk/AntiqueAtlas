package folk.sisby.antique_atlas.gui.core;

/**
 * Listener for left click on a button.
 */
public interface IButtonListener<B extends ButtonComponent> {
	/**
	 * Called when the button was left-clicked on.
	 *
	 * @param button the button which was clicked on.
	 */
	void onClick(B button);
}
