package folk.sisby.antique_atlas.gui.core;

import java.util.function.BiConsumer;

/**
 * A mechanism to encapsulate actions that need to be done every time a GUI
 * switches between distinct states of behavior.
 *
 * @author Hunternif
 */
public class ScreenState<T> {
    private final BiConsumer<IState<T>, IState<T>> onChangedState;

    /**
     * Meant to declare anonymous classes.
     */
    public interface IState<T> {
        void onEnterState(T screen);

        void onExitState(T screen);
    }

    /**
     * A simple state that does nothing upon enter or exit.
     */
    public static class SimpleState<T> implements IState<T> {
        @Override
        public void onEnterState(T screen) {
        }

        @Override
        public void onExitState(T screen) {
        }
    }

    public ScreenState(BiConsumer<IState<T>, IState<T>> onChangedState) {
        this.onChangedState = onChangedState;
    }

    public ScreenState() {
        this.onChangedState = null;
    }

    private volatile IState<T> currentState;

    public IState<T> current() {
        return currentState;
    }

    public boolean is(IState<T> state) {
        return current() == state;
    }

    public void switchTo(IState<T> state, T screen) {
        if (currentState != null) {
            currentState.onExitState(screen);
        }
        if (onChangedState != null) onChangedState.accept(currentState, state);
        currentState = state;
        if (state != null) {
            state.onEnterState(screen);
        }
    }
}
