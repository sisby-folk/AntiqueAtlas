package folk.sisby.antique_atlas.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.AntiqueAtlasKeybindings;
import folk.sisby.antique_atlas.AtlasStructureLandmark;
import folk.sisby.antique_atlas.MarkerTexture;
import folk.sisby.antique_atlas.TileTexture;
import folk.sisby.antique_atlas.WorldAtlasData;
import folk.sisby.antique_atlas.gui.core.ButtonComponent;
import folk.sisby.antique_atlas.gui.core.Component;
import folk.sisby.antique_atlas.gui.core.CursorComponent;
import folk.sisby.antique_atlas.gui.core.ScreenState;
import folk.sisby.antique_atlas.gui.core.ScreenState.State;
import folk.sisby.antique_atlas.gui.core.ScreenState.ToggleState;
import folk.sisby.antique_atlas.gui.core.ScrollBoxComponent;
import folk.sisby.antique_atlas.gui.tiles.SubTile;
import folk.sisby.antique_atlas.gui.tiles.SubTileQuartet;
import folk.sisby.antique_atlas.gui.tiles.TileRenderIterator;
import folk.sisby.antique_atlas.util.DrawBatcher;
import folk.sisby.antique_atlas.util.DrawUtil;
import folk.sisby.antique_atlas.util.MathUtil;
import folk.sisby.antique_atlas.util.Rect;
import folk.sisby.surveyor.landmark.Landmark;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AtlasScreen extends Component {
    public static final Identifier BOOK = AntiqueAtlas.id("textures/gui/book.png");
    public static final Identifier BOOK_FRAME = AntiqueAtlas.id("textures/gui/book_frame.png");
    public static final Identifier BOOK_FRAME_NARROW = AntiqueAtlas.id("textures/gui/book_frame_narrow.png");
    public static final Identifier PLAYER = AntiqueAtlas.id("textures/gui/player.png");
    public static final Identifier ERASER = AntiqueAtlas.id("textures/gui/eraser.png");
    public static final Identifier ICON_ADD_MARKER = AntiqueAtlas.id("textures/gui/icons/add_marker.png");
    public static final Identifier ICON_DELETE_MARKER = AntiqueAtlas.id("textures/gui/icons/del_marker.png");
    public static final Identifier ICON_SHOW_MARKERS = AntiqueAtlas.id("textures/gui/icons/show_markers.png");
    public static final Identifier ICON_HIDE_MARKERS = AntiqueAtlas.id("textures/gui/icons/hide_markers.png");
    private static final Text TEXT_ADD_MARKER = Text.translatable("gui.antique_atlas.addMarker");
    private static final Text TEXT_ADD_MARKER_HERE = Text.translatable("gui.antique_atlas.addMarkerHere");

    private static final int MAP_BORDER_WIDTH = 17;
    private static final int MAP_BORDER_HEIGHT = 11;
    private static final float PLAYER_ROTATION_STEPS = 16;
    private static final int PLAYER_ICON_WIDTH = 7;
    private static final int PLAYER_ICON_HEIGHT = 8;
    private static final int BOOKMARK_SPACING = 2;
    public static final int MARKER_SIZE = 32;
    /**
     * If the map scale goes below this value, the tiles will not scale down
     * visually, but will instead span greater area.
     */
    private static final double MIN_SCALE_THRESHOLD = 1;
    /**
     * How much the map view is offset, in blocks, per click (or per tick).
     */
    private static final int NAVIGATE_STEP = 24;

    public static final State<AtlasScreen> NORMAL = new ToggleState<>();
    public static final State<AtlasScreen> PLACING_MARKER = new ToggleState<>(s -> s.addMarkerBookmark);
    public static final State<AtlasScreen> DELETING_MARKER = new ToggleState<>(s -> s.deleteMarkerBookmark, s -> s.addChild(s.eraser), s -> s.removeChild(s.eraser));
    public static final State<AtlasScreen> HIDING_MARKERS = new ToggleState<>(s -> s.markerVisibilityBookmark, s -> {
        s.markerVisibilityBookmark.setTitle(Text.translatable("gui.antique_atlas.showMarkers"));
        s.markerVisibilityBookmark.setIconTexture(ICON_SHOW_MARKERS);
    }, s -> {
        s.clearTargetBookmarks(s.playerBookmark);
        s.markerVisibilityBookmark.setTitle(Text.translatable("gui.antique_atlas.hideMarkers"));
        s.markerVisibilityBookmark.setIconTexture(ICON_HIDE_MARKERS);
    });

    public final int BOOK_WIDTH;
    public final int BOOK_HEIGHT;
    private final int MAP_WIDTH;
    private final int MAP_HEIGHT;

    /**
     * Button for placing a marker at current position, local to this Atlas instance.
     */
    private final BookmarkButton addMarkerBookmark;
    /**
     * Button for deleting local markers.
     */
    private final BookmarkButton deleteMarkerBookmark;
    /**
     * Button for showing/hiding all markers.
     */
    private final BookmarkButton markerVisibilityBookmark;
    /**
     * Button for displaying the scale, and setting the scale to 1 chunk / 1 tile / 16px.
     */
    private final TextBookmarkButton resetScaleBookmark;
    /**
     * Button for restoring player's position at the center of the Atlas.
     */
    private final BookmarkButton playerBookmark;
    private final ScrollBoxComponent markerScrollBox = new ScrollBoxComponent(true, BookmarkButton.HEIGHT + BOOKMARK_SPACING);
    private final MarkerModal markerModal = new MarkerModal();
    private final BlinkingMarkerComponent markerCursor = new BlinkingMarkerComponent();
    private final CursorComponent eraser = new CursorComponent();

    private final List<BookmarkButton> markerBookmarks = new ArrayList<>();
    private final ScreenState<AtlasScreen> state = new ScreenState<>((oldState, newState) -> AntiqueAtlas.lastState.switchTo(newState, this));
    private Landmark<?> hoveredLandmark = null;
    /**
     * The button which is currently being pressed. Used for continuous
     * navigation using the arrow buttons. Also used to prevent immediate
     * canceling of placing marker.
     */
    private ButtonComponent selectedButton = null;
    private PlayerEntity player;
    private WorldAtlasData worldAtlasData;
    private Integer targetOffsetX, targetOffsetY;
    private boolean isMouseOverMap = false;

    private boolean isDragging = false;

    private double mapOffsetX;
    private double mapOffsetY;

    private int subTilePixels = 8;
    private int tileChunks = 1;

    public AtlasScreen() {
        if (AntiqueAtlas.CONFIG.fullscreen) {
            BOOK_WIDTH = MinecraftClient.getInstance().getWindow().getScaledWidth() - 40;
            BOOK_HEIGHT = MinecraftClient.getInstance().getWindow().getScaledHeight() - 40;
        } else {
            BOOK_WIDTH = 310;
            BOOK_HEIGHT = 218;
        }
        setSize(BOOK_WIDTH, BOOK_HEIGHT);
        MAP_WIDTH = BOOK_WIDTH - MAP_BORDER_WIDTH * 2;
        MAP_HEIGHT = BOOK_HEIGHT - MAP_BORDER_HEIGHT * 2;

        playerBookmark = new BookmarkButton(Text.translatable("gui.antique_atlas.followPlayer"), AntiqueAtlas.id("textures/gui/player.png"), DyeColor.GRAY, null, 7, 8, false);
        playerBookmark.setSelected(true);
        addChild(playerBookmark).offsetGuiCoords(BOOK_WIDTH - 10, BOOK_HEIGHT - MAP_BORDER_HEIGHT - BookmarkButton.HEIGHT - 10);
        playerBookmark.addListener(b -> {
            selectedButton = playerBookmark;
            clearTargetBookmarks(playerBookmark);
            playerBookmark.setSelected(true);
        });

        addMarkerBookmark = new BookmarkButton(TEXT_ADD_MARKER, ICON_ADD_MARKER, DyeColor.RED, null, 16, 16, false);
        addChild(addMarkerBookmark).offsetGuiCoords(BOOK_WIDTH - 10, 14);
        addMarkerBookmark.addListener(button -> {
            if (state.is(PLACING_MARKER)) {
                selectedButton = null;
                state.switchTo(NORMAL, this);
            } else {
                selectedButton = button;
                state.switchTo(PLACING_MARKER, this);

                // While holding shift, we create a marker on the player's position
                if (hasShiftDown()) {
                    markerModal.setMarkerData(player.getEntityWorld(), player.getBlockX(), player.getBlockZ());
                    addChild(markerModal);

                    markerCursor.setTexture(markerModal.selectedTexture.id(), markerModal.selectedTexture.textureWidth(), markerModal.selectedTexture.textureHeight());
                    addChildBehind(markerModal, markerCursor).setGuiCoords((int) worldXToScreenX(player.getBlockX() - MARKER_SIZE / 2), (int) worldZToScreenY(player.getBlockZ() - MARKER_SIZE / 2));

                    // Un-press all keys to prevent player from walking infinitely:
                    KeyBinding.unpressAll();

                    selectedButton = null;
                    state.switchTo(NORMAL, this);
                }
            }
        });
        deleteMarkerBookmark = new BookmarkButton(Text.translatable("gui.antique_atlas.delMarker"), ICON_DELETE_MARKER, DyeColor.YELLOW, null, 16, 16, false);
        addChild(deleteMarkerBookmark).offsetGuiCoords(BOOK_WIDTH - 10, 33);
        deleteMarkerBookmark.addListener(button -> {
            if (state.is(DELETING_MARKER)) {
                selectedButton = null;
                state.switchTo(NORMAL, this);
            } else {
                selectedButton = button;
                state.switchTo(DELETING_MARKER, this);
            }
        });
        markerVisibilityBookmark = new BookmarkButton(Text.translatable("gui.antique_atlas.hideMarkers"), ICON_HIDE_MARKERS, DyeColor.GREEN, null, 16, 16, false);
        addChild(markerVisibilityBookmark).offsetGuiCoords(BOOK_WIDTH - 10, 52);
        markerVisibilityBookmark.addListener(button -> {
            selectedButton = null;
            if (state.is(HIDING_MARKERS)) {
                state.switchTo(NORMAL, this);
            } else {
                selectedButton = null;
                state.switchTo(HIDING_MARKERS, this);
            }
        });
        resetScaleBookmark = new TextBookmarkButton(Text.translatable("gui.antique_atlas.resetScale"), Text.of("1c"));
        addChild(resetScaleBookmark).offsetGuiCoords(BOOK_WIDTH - 10, 71);
        resetScaleBookmark.addListener(button -> {
            resetZoom();
            resetScaleBookmark.setSelected(false);
        });

        addChild(markerScrollBox).setRelativeCoords(-14, MAP_BORDER_HEIGHT + 8);
        int markersOnScreen = (MAP_HEIGHT - 20) / ((BookmarkButton.HEIGHT + BOOKMARK_SPACING) - BOOKMARK_SPACING);
        markerScrollBox.getViewport().setSize(BookmarkButton.WIDTH, markersOnScreen * (BookmarkButton.HEIGHT + BOOKMARK_SPACING) - BOOKMARK_SPACING);

        markerModal.addMarkerListener(markerCursor);

        eraser.setTexture(ERASER, 12, 14, 2, 11);

        state.switchTo(AntiqueAtlas.lastState.is(HIDING_MARKERS) ? HIDING_MARKERS : NORMAL, this);
    }

    public AtlasScreen prepareToOpen() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0F));

        this.player = MinecraftClient.getInstance().player;
        updateAtlasData();
        setMapPosition(player.getBlockX(), player.getBlockZ());

        return this;
    }

    @Override
    public void init() {
        super.init();

        setGuiCoords((this.width - BOOK_WIDTH) / 2, (this.height - BOOK_HEIGHT) / 2);

        updateBookmarkerList();
    }

    public void updateBookmarkerList() {
        markerScrollBox.getViewport().removeAllContent();
        markerScrollBox.setScrollPos(0);
        markerBookmarks.clear();

        if (worldAtlasData == null) return;

        worldAtlasData.getEditableLandmarks().forEach((landmark, texture) -> {
            BookmarkButton bookmark = new BookmarkButton(landmark.name(), texture.id(), landmark.color(), (texture == MarkerTexture.DEFAULT && landmark.color() != null) ? landmark.color() : null, 32, 32, true);

            bookmark.addListener(button -> {
                if (state.is(NORMAL)) {
                    clearTargetBookmarks(bookmark);
                    setTargetPosition(new ColumnPos(landmark.pos().getX(), landmark.pos().getZ()));
                } else if (state.is(DELETING_MARKER)) {
                    if (!worldAtlasData.deleteLandmark(player.getEntityWorld(), landmark)) return;
                    updateBookmarkerList();
                    player.getEntityWorld().playSound(player, player.getBlockPos(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.AMBIENT, 1F, 0.5F);
                    state.switchTo(NORMAL, this);
                }
            });

            markerBookmarks.add(bookmark);
        });

        final int[] contentY = {0};
        for (BookmarkButton bookmark : markerBookmarks) {
            markerScrollBox.getViewport().addContent(bookmark).setRelativeY(contentY[0]);
            contentY[0] += BookmarkButton.HEIGHT + BOOKMARK_SPACING;
        }
    }

    public void clearTargetBookmarks(BookmarkButton except) {
        if (playerBookmark != except) playerBookmark.setSelected(false);
        for (BookmarkButton bookmark : markerBookmarks) {
            if (bookmark != except) bookmark.setSelected(false);
        }
    }

    public void updateMouse(double mouseX, double mouseY) {
        double relativeMouseX = mouseX - getGuiX();
        double relativeMouseY = mouseY - getGuiY();
        isMouseOverMap = relativeMouseX >= MAP_BORDER_WIDTH && relativeMouseX <= MAP_BORDER_WIDTH + MAP_WIDTH && relativeMouseY >= MAP_BORDER_HEIGHT && relativeMouseY <= MAP_BORDER_HEIGHT + MAP_HEIGHT;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        updateMouse(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseState) {
        updateMouse(mouseX, mouseY);
        if (super.mouseClicked(mouseX, mouseY, mouseState)) return true;
        if (markerModal.getParent() != null) return false;

        // If clicked on the map, start dragging
        if (!state.is(NORMAL) && !state.is(HIDING_MARKERS)) {
            if (state.is(PLACING_MARKER) && isMouseOverMap && mouseState == 0 /* left click */) {
                markerModal.setMarkerData(player.getEntityWorld(), screenXToWorldX(mouseX), screenYToWorldZ(mouseY));
                addChild(markerModal);

                markerCursor.setTexture(markerModal.selectedTexture.id(), MARKER_SIZE, MARKER_SIZE);
                addChildBehind(markerModal, markerCursor).setGuiCoords((int) mouseX - MARKER_SIZE / 2, (int) mouseY - MARKER_SIZE / 2);

                // Un-press all keys to prevent player from walking infinitely:
                KeyBinding.unpressAll();

                state.switchTo(NORMAL, this);
                return true;
            } else if (state.is(DELETING_MARKER) && hoveredLandmark != null && isMouseOverMap && mouseState == 0) {
                if (worldAtlasData.deleteLandmark(player.getEntityWorld(), hoveredLandmark)) {
                    updateBookmarkerList();
                    player.getEntityWorld().playSound(player, player.getBlockPos(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.AMBIENT, 1F, 0.5F);
                }
            }
            state.switchTo(NORMAL, this);
        } else if (isMouseOverMap && selectedButton == null) {
            isDragging = true;
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_UP) {
            navigateMap(0, NAVIGATE_STEP);
        } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
            navigateMap(0, -NAVIGATE_STEP);
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            navigateMap(NAVIGATE_STEP, 0);
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            navigateMap(-NAVIGATE_STEP, 0);
        } else if (keyCode == GLFW.GLFW_KEY_EQUAL || keyCode == GLFW.GLFW_KEY_KP_ADD) {
            zoomIn(true, (8 << AntiqueAtlas.CONFIG.maxTilePixels));
        } else if (keyCode == GLFW.GLFW_KEY_MINUS || keyCode == GLFW.GLFW_KEY_KP_SUBTRACT) {
            zoomOut(true, (1 << AntiqueAtlas.CONFIG.maxTileChunks));
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE || (AntiqueAtlasKeybindings.ATLAS_KEYMAPPING.matchesKey(keyCode, scanCode) && this.markerModal.getParent() == null)) {
            close();
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        return true;
    }

    private double getPixelsPerBlock() {
        return ((double) subTilePixels * 2.0) / ((double) tileChunks * 16.0);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double wheelMove) {
        updateMouse(mouseX, mouseY);
        if (super.mouseScrolled(mouseX, mouseY, wheelMove)) return true;
        if (markerModal.getParent() == null && wheelMove != 0) {
            int direction = wheelMove > 0 ? 1 : -1;
            if ((wheelMove > 0 ? zoomIn(true, (8 << AntiqueAtlas.CONFIG.maxTilePixels)) : zoomOut(true, 1 << AntiqueAtlas.CONFIG.maxTileChunks)) && (isMouseOverMap || isDragging)) { // Keep mouse over the same block.
                double xOffset = (getGuiX() + MAP_BORDER_WIDTH + (double) MAP_WIDTH / 2 - mouseX) * direction;
                double yOffset = (getGuiY() + MAP_BORDER_HEIGHT + (double) MAP_HEIGHT / 2 - mouseY) * direction;
                if (Math.abs(xOffset) > 10 || Math.abs(yOffset) > 10) {
                    mapOffsetX += xOffset / (direction < 0 ? 2.0 : 1.0);
                    mapOffsetY += yOffset / (direction < 0 ? 2.0 : 1.0);
                    clearTargetBookmarks(null);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseState) {
        boolean result = false;
        if (mouseState != -1) {
            result = selectedButton != null || isDragging;
            selectedButton = null;
            isDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, mouseState) || result;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int lastMouseButton, double deltaX, double deltaY) {
        boolean result = false;
        if (isDragging) {
            clearTargetBookmarks(null);
            mapOffsetX += deltaX;
            mapOffsetY += deltaY;
            result = true;
        }
        return super.mouseDragged(mouseX, mouseY, lastMouseButton, deltaX, deltaY) || result;
    }

    @Override
    public void tick() {
        super.tick();
        if (player == null) return;

        if (playerBookmark.isSelected()) {
            setTargetPosition(new ColumnPos(player.getBlockX(), player.getBlockZ()));
        }

        if (targetOffsetX != null) {
            if (Math.abs(getTargetPositionX() - mapOffsetX) > NAVIGATE_STEP) {
                softNavigateMap(getTargetPositionX() > mapOffsetX ? NAVIGATE_STEP : -NAVIGATE_STEP, 0);
            } else {
                mapOffsetX = getTargetPositionX();
                targetOffsetX = null;
            }
        }

        if (targetOffsetY != null) {
            if (Math.abs(getTargetPositionY() - mapOffsetY) > NAVIGATE_STEP) {
                softNavigateMap(0, getTargetPositionY() > mapOffsetY ? NAVIGATE_STEP : -NAVIGATE_STEP);
            } else {
                mapOffsetY = getTargetPositionY();
                targetOffsetY = null;
            }
        }
    }

    private void updateAtlasData() {
        if (MinecraftClient.getInstance().world != null) {
            worldAtlasData = WorldAtlasData.getOrCreate(MinecraftClient.getInstance().world);
        }
    }

    private void navigateMap(int dx, int dy) {
        mapOffsetX += dx;
        mapOffsetY += dy;
        clearTargetBookmarks(null);
    }

    private void softNavigateMap(int dx, int dy) {
        mapOffsetX += dx;
        mapOffsetY += dy;
    }

    private void setMapPosition(int x, int z) {
        mapOffsetX = (int) (-x * getPixelsPerBlock());
        mapOffsetY = (int) (-z * getPixelsPerBlock());
    }

    private void setTargetPosition(ColumnPos pos) {
        targetOffsetX = pos.x();
        targetOffsetY = pos.z();
    }

    private int getTargetPositionX() {
        return (int) (-targetOffsetX * getPixelsPerBlock());
    }

    private int getTargetPositionY() {
        return (int) (-targetOffsetY * getPixelsPerBlock());
    }

    private boolean zoomIn(boolean playSound, int maxTilePixels) {
        if (tileChunks == 1) {
            if (subTilePixels >= maxTilePixels) return false;
            subTilePixels <<= 1;
            resetScaleBookmark.setLabel(Text.literal("%db".formatted(128 / subTilePixels)).formatted(Formatting.DARK_RED));
            if (playSound) MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_SPYGLASS_USE, 1.0F));
        } else {
            tileChunks >>= 1;
            resetScaleBookmark.setLabel(Text.literal("%dc".formatted(tileChunks)).formatted(tileChunks == 1 ? Formatting.BLACK : Formatting.DARK_BLUE));
            if (playSound) MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0F));
        }
        mapOffsetX *= 2;
        mapOffsetY *= 2;
        return true;
    }

    private boolean zoomOut(boolean playSound, int maxTileChunks) {
        if (subTilePixels == 8) {
            if (tileChunks >= maxTileChunks) return false;
            tileChunks <<= 1;
            resetScaleBookmark.setLabel(Text.literal("%dc".formatted(tileChunks)).formatted(Formatting.DARK_BLUE));
            if (playSound) MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0F));
        } else {
            subTilePixels >>= 1;
            resetScaleBookmark.setLabel(subTilePixels == 8 ? Text.literal("%dc".formatted(tileChunks)) : Text.literal("%db".formatted(128 / subTilePixels)).formatted(Formatting.DARK_RED));
            if (playSound) MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_SPYGLASS_USE, 1.0F));
        }
        mapOffsetX /= 2;
        mapOffsetY /= 2;
        return true;
    }

    private void resetZoom() {
        if (zoomIn(true, 8)) {
            while (zoomIn(false, 8));
        } else if (zoomOut(true, 1)) {
            while (zoomOut(false, 1));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float par3) {
        super.renderBackground(context);

        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (AntiqueAtlas.CONFIG.fullscreen) {
            context.fill(getGuiX(), getGuiY(), getGuiX() + BOOK_WIDTH, getGuiY() + BOOK_HEIGHT, 0xFFEAD2A5);
            context.drawBorder(getGuiX(), getGuiY(), BOOK_WIDTH, BOOK_HEIGHT, 0xFF4C1A0B);
            context.drawBorder(getGuiX() + MAP_BORDER_WIDTH - 1, getGuiY() + MAP_BORDER_HEIGHT - 1, MAP_WIDTH + 2, MAP_HEIGHT + 2, 0xFF4C1A0B);
        } else {
            context.drawTexture(BOOK, getGuiX(), getGuiY(), 0, 0, 310, 218, 310, 218);
        }

        if (worldAtlasData == null) return;

        if (state.is(DELETING_MARKER)) {
            RenderSystem.setShaderColor(1, 1, 1, 0.5f);
        }
        double guiScale = client.getWindow().getScaleFactor();
        RenderSystem.enableScissor(
            (int) (guiScale * (getGuiX() + MAP_BORDER_WIDTH)),
            (int) (guiScale * (getGuiY() + MAP_BORDER_HEIGHT)),
            (int) (guiScale * MAP_WIDTH),
            (int) (guiScale * MAP_HEIGHT)
        );
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        // Find chunk coordinates of the top left corner of the map.
        // The 'roundToBase' is required so that when the map scales below the
        // threshold the tiles don't change when map position changes slightly.
        // The +-2 at the end provide margin so that tiles at the edges of
        // the page have their stitched texture correct.
        int mapStartX = MathUtil.roundToBase((int) Math.floor(-((double) MAP_WIDTH / 2d + mapOffsetX + 2 * subTilePixels) / getPixelsPerBlock() / 16d), tileChunks);
        int mapStartZ = MathUtil.roundToBase((int) Math.floor(-((double) MAP_HEIGHT / 2d + mapOffsetY + 2 * subTilePixels) / getPixelsPerBlock() / 16d), tileChunks);
        int mapEndX = MathUtil.roundToBase((int) Math.ceil(((double) MAP_WIDTH / 2d - mapOffsetX + 2 * subTilePixels) / getPixelsPerBlock() / 16d), tileChunks);
        int mapEndZ = MathUtil.roundToBase((int) Math.ceil(((double) MAP_HEIGHT / 2d - mapOffsetY + 2 * subTilePixels) / getPixelsPerBlock() / 16d), tileChunks);
        double mapStartScreenX = getGuiX() + MAP_BORDER_WIDTH + (double) MAP_WIDTH / 2 + ((mapStartX << 4) * getPixelsPerBlock()) + mapOffsetX;
        double mapStartScreenY = getGuiY() + MAP_BORDER_HEIGHT + (double) MAP_HEIGHT / 2 + ((mapStartZ << 4) * getPixelsPerBlock()) + mapOffsetY;
        TileRenderIterator tiles = new TileRenderIterator(worldAtlasData);
        tiles.setScope(new Rect(mapStartX, mapStartZ, mapEndX, mapEndZ));
        tiles.setStep(tileChunks);

        context.getMatrices().push();
        context.getMatrices().translate(mapStartScreenX, mapStartScreenY, 0);

        Map<TileTexture, Collection<SubTile>> tileTextures = new Reference2ObjectArrayMap<>();
        for (SubTileQuartet subTiles : tiles) {
            for (SubTile subtile : subTiles) {
                if (subtile == null || subtile.texture == null) continue;
                tileTextures.computeIfAbsent(subtile.texture, k -> new ArrayList<>()).add(subtile.copy());
            }
        }
        tileTextures.forEach((texture, subtiles) -> {
            try (DrawBatcher batcher = new DrawBatcher(context, texture.id(), 32, 48)) {
                for (SubTile subtile : subtiles) {
                    batcher.add(subtile.x * subTilePixels, subtile.y * subTilePixels, subTilePixels, subTilePixels, subtile.getTextureU() * 8, subtile.getTextureV() * 8, 8, 8);
                }
            }
        });

        context.getMatrices().pop();

        // Overlay the frame so that edges of the map are smooth:
        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (!AntiqueAtlas.CONFIG.fullscreen) {
            context.drawTexture(BOOK_FRAME, getGuiX(), getGuiY(), 0, 0, 310, 218, 310, 218);
        }

        hoveredLandmark = null;
        if (!state.is(HIDING_MARKERS)) {
            if (isMouseOverMap) {
                double bestDistance = Double.MAX_VALUE;
                for (Map.Entry<Landmark<?>, MarkerTexture> entry : worldAtlasData.getAllMarkers().entrySet()) {
                    Landmark<?> landmark = entry.getKey();
                    MarkerTexture texture = entry.getValue();
                    double markerX = worldXToScreenX(landmark.pos().getX());
                    double markerY = worldZToScreenY(landmark.pos().getZ());
                    double squaredDistance = Vector2d.distanceSquared(markerX + texture.offsetX() + (double) texture.textureWidth() / 2, markerY + texture.offsetY() + (double) texture.textureHeight() / 2, mouseX, mouseY);
                    if (squaredDistance > 0 && squaredDistance < bestDistance && squaredDistance < (texture.textureWidth() * texture.textureHeight()) / 4.0) {
                        bestDistance = squaredDistance;
                        hoveredLandmark = landmark;
                    }
                }
            }
            context.getMatrices().push();
            worldAtlasData.getAllMarkers().forEach((landmark, texture) -> {
                renderMarker(context, landmark, texture, WorldAtlasData.landmarkIsEditable(landmark), hoveredLandmark == landmark && markerModal.getParent() == null);
            });
            context.getMatrices().pop();
        }

        RenderSystem.disableScissor();

        if (!AntiqueAtlas.CONFIG.fullscreen) {
            context.drawTexture(BOOK_FRAME_NARROW, getGuiX(), getGuiY(), 0, 0, 310, 218, 310, 218);
        }

        markerScrollBox.getViewport().setHidden(state.is(HIDING_MARKERS));
        if (!state.is(HIDING_MARKERS) || playerBookmark.isSelected()) {
            renderPlayer(context, 1);
        }

        super.render(context, mouseX, mouseY, par3);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        if (state.is(PLACING_MARKER)) {
            RenderSystem.setShaderColor(1, 1, 1, 0.5f);
            context.drawTexture(markerModal.selectedTexture.id(), mouseX + markerModal.selectedTexture.offsetX(), mouseY + markerModal.selectedTexture.offsetY(), 0, 0, markerModal.selectedTexture.textureWidth(), markerModal.selectedTexture.textureHeight(), markerModal.selectedTexture.textureWidth(), markerModal.selectedTexture.textureHeight());
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
        RenderSystem.disableBlend();

        addMarkerBookmark.setTitle(hasShiftDown() ? TEXT_ADD_MARKER_HERE : TEXT_ADD_MARKER);

        if (AntiqueAtlas.CONFIG.debugRender && !isDragging && isMouseOverMap && markerModal.getParent() == null) {
            int x = screenXToWorldX((int) getMouseX());
            int z = screenYToWorldZ((int) getMouseY());
            ChunkPos pos = new ChunkPos(new BlockPos(x, 0, z));
            context.drawText(textRenderer, Text.literal("%d,%d (%d,%d)".formatted(pos.x, pos.z, x, z)), getGuiX(), getGuiY() - 12, 0xFFFFFFFF, true);
            if (hoveredLandmark != null) {
                MarkerTexture texture = worldAtlasData.getMarkerTexture(hoveredLandmark);
                context.drawText(textRenderer, Text.literal(hoveredLandmark.type().id().toString()), getGuiX() + BOOK_WIDTH - textRenderer.getWidth(Text.literal(hoveredLandmark.type().id().toString())), getGuiY() - 12, 0xFFFFFFFF, true);
                if (hoveredLandmark instanceof AtlasStructureLandmark sLandmark) context.drawText(textRenderer, Text.literal(sLandmark.displayId().toString()), getGuiX(), getGuiY() + BOOK_HEIGHT, 0xFFFFFFFF, true);
                if (texture != null) context.drawText(textRenderer, Text.literal(texture.displayId()), getGuiX() + BOOK_WIDTH - textRenderer.getWidth(Text.literal(texture.displayId())), getGuiY() + BOOK_HEIGHT, 0xFFFFFFFF, true);
            } else {
                TileTexture texture = worldAtlasData.getTile(pos);
                Identifier providerId = worldAtlasData.getProvider(pos);
                String predicate = worldAtlasData.getTilePredicate(pos);
                if (texture != null) {
                    if (predicate != null) context.drawText(textRenderer, Text.literal(predicate), getGuiX() + BOOK_WIDTH - textRenderer.getWidth(Text.literal(predicate)), getGuiY() - 12, 0xFFFFFFFF, true);
                    context.drawText(textRenderer, Text.literal(providerId.toString()), getGuiX(), getGuiY() + BOOK_HEIGHT, 0xFFFFFFFF, true);
                    context.drawText(textRenderer, Text.literal(texture.displayId()), getGuiX() + BOOK_WIDTH - textRenderer.getWidth(Text.literal(texture.displayId())), getGuiY() + BOOK_HEIGHT, 0xFFFFFFFF, true);
                }
            }
        }
    }

    private void renderPlayer(DrawContext context, float iconScale) {
        double playerOffsetX = worldXToScreenX(player.getBlockX());
        double playerOffsetY = worldZToScreenY(player.getBlockZ());

        playerOffsetX = MathHelper.clamp(playerOffsetX, getGuiX() + MAP_BORDER_WIDTH, getGuiX() + MAP_WIDTH + MAP_BORDER_WIDTH);
        playerOffsetY = MathHelper.clamp(playerOffsetY, getGuiY() + MAP_BORDER_HEIGHT, getGuiY() + MAP_HEIGHT + MAP_BORDER_HEIGHT);

        // Draw the icon:
        RenderSystem.setShaderColor(1, 1, 1, state.is(PLACING_MARKER) ? 0.5f : 1);
        float playerRotation = (float) Math.round(player.getYaw() / 360f * PLAYER_ROTATION_STEPS) / PLAYER_ROTATION_STEPS * 360f;

        DrawUtil.drawCenteredWithRotation(context, PLAYER, playerOffsetX, playerOffsetY, iconScale, PLAYER_ICON_WIDTH, PLAYER_ICON_HEIGHT, playerRotation);

        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private void renderMarker(DrawContext context, Landmark<?> landmark, MarkerTexture texture, boolean editable, boolean hovering) {
        double markerX = worldXToScreenX(landmark.pos().getX());
        double markerY = worldZToScreenY(landmark.pos().getZ());

        float tint = hovering ? 0.8f : 1.0f;
        float alpha = state.is(PLACING_MARKER) || (state.is(DELETING_MARKER) && !editable) ? 0.5f : 1.0f;
        if (texture == MarkerTexture.DEFAULT && landmark.color() != null) {
            float[] rgb = landmark.color().getColorComponents();
            RenderSystem.setShaderColor(tint * rgb[0], tint * rgb[1], tint * rgb[2], alpha);
        } else {
            RenderSystem.setShaderColor(tint, tint, tint, alpha);
        }

        if (editable) {
            if (markerX <= getGuiX() + MAP_BORDER_WIDTH || markerX >= getGuiX() + MAP_WIDTH + MAP_BORDER_WIDTH || markerY <= getGuiY() + MAP_BORDER_HEIGHT || markerY >= getGuiY() + MAP_HEIGHT + MAP_BORDER_HEIGHT) {
                RenderSystem.setShaderColor(1, 1, 1, 0.5f);
            }
            markerX = MathHelper.clamp(markerX, getGuiX() + MAP_BORDER_WIDTH, getGuiX() + MAP_WIDTH + MAP_BORDER_WIDTH);
            markerY = MathHelper.clamp(markerY, getGuiY() + MAP_BORDER_HEIGHT, getGuiY() + MAP_HEIGHT + MAP_BORDER_HEIGHT);
        }

        texture.draw(context, markerX, markerY);

        RenderSystem.setShaderColor(1, 1, 1, 1);

        if (hovering && landmark.name() != null && !landmark.name().getString().isEmpty()) {
            drawTooltip(Collections.singletonList(landmark.name()), textRenderer);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        super.close();
        markerModal.closeChild();
        removeChild(markerCursor);
    }

    private int screenXToWorldX(double mouseX) {
        double mapX = (int) Math.round(mouseX - getGuiX() - MAP_BORDER_WIDTH);
        return (int) Math.round((mapX - (MAP_WIDTH / 2f) - mapOffsetX) / getPixelsPerBlock());
    }

    private int screenYToWorldZ(double mouseY) {
        double mapY = (int) Math.round(mouseY - getGuiY() - MAP_BORDER_HEIGHT);
        return (int) Math.round((mapY - (MAP_HEIGHT / 2f) - mapOffsetY) / getPixelsPerBlock());
    }

    private double worldXToScreenX(double x) {
        double mapX = x * getPixelsPerBlock() + mapOffsetX + (MAP_WIDTH / 2f);
        return mapX + getGuiX() + MAP_BORDER_WIDTH;
    }

    private double worldZToScreenY(double z) {
        double mapY = z * getPixelsPerBlock() + mapOffsetY + (MAP_HEIGHT / 2f);
        return mapY + getGuiY() + MAP_BORDER_HEIGHT;
    }

    @Override
    protected void onChildClosed(Component child) {
        if (child.equals(markerModal)) {
            removeChild(markerCursor);
        }
    }

    public WorldAtlasData getworldAtlasData() {
        return worldAtlasData;
    }
}
