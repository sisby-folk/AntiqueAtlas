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
import folk.sisby.antique_atlas.gui.core.IButtonListener;
import folk.sisby.antique_atlas.gui.core.ScreenState;
import folk.sisby.antique_atlas.gui.core.ScreenState.IState;
import folk.sisby.antique_atlas.gui.core.ScreenState.SimpleState;
import folk.sisby.antique_atlas.gui.core.ScrollBoxComponent;
import folk.sisby.antique_atlas.gui.tiles.SubTile;
import folk.sisby.antique_atlas.gui.tiles.SubTileQuartet;
import folk.sisby.antique_atlas.gui.tiles.TileRenderIterator;
import folk.sisby.antique_atlas.util.DrawUtil;
import folk.sisby.antique_atlas.util.MathUtil;
import folk.sisby.antique_atlas.util.Rect;
import folk.sisby.surveyor.landmark.Landmark;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
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

    public final int WIDTH;
    public final int HEIGHT;

    private static final int MAP_BORDER_WIDTH = 17;
    private static final int MAP_BORDER_HEIGHT = 11;
    private final int MAP_WIDTH;
    private final int MAP_HEIGHT;

    private static final float PLAYER_ROTATION_STEPS = 16;
    private static final int PLAYER_ICON_WIDTH = 7;
    private static final int PLAYER_ICON_HEIGHT = 8;

    public static final int MARKER_SIZE = 32;

    /**
     * If the map scale goes below this value, the tiles will not scale down
     * visually, but will instead span greater area.
     */
    private static final double MIN_SCALE_THRESHOLD = 0.5;

    // States ==================================================================

    private final ScreenState state = new ScreenState();

    /**
     * If on, navigate the map normally.
     */
    private final IState NORMAL = new SimpleState();

    /**
     * If on, all markers as well as the player icon are hidden.
     */
    private final IState HIDING_MARKERS = new IState() {
        @Override
        public void onEnterState() {
            // Set the button as not selected so that it can be clicked again:
            btnShowMarkers.setSelected(false);
            btnShowMarkers.setTitle(Text.translatable("gui.antique_atlas.showMarkers"));
            btnShowMarkers.setIconTexture(ICON_SHOW_MARKERS);
        }

        @Override
        public void onExitState() {
            btnShowMarkers.setSelected(false);
            btnShowMarkers.setTitle(Text.translatable("gui.antique_atlas.hideMarkers"));
            btnShowMarkers.setIconTexture(ICON_HIDE_MARKERS);
        }
    };

    /**
     * If on, a semi-transparent marker is attached to the cursor, and the
     * player's icon becomes semi-transparent as well.
     */
    private final IState PLACING_MARKER = new IState() {
        @Override
        public void onEnterState() {
            btnMarker.setSelected(true);
        }

        @Override
        public void onExitState() {
            btnMarker.setSelected(false);
        }
    };

    /**
     * If on, the closest marker will be deleted upon mouseclick.
     */
    private final IState DELETING_MARKER = new IState() {
        @Override
        public void onEnterState() {
            addChild(eraser);
            btnDelMarker.setSelected(true);
        }

        @Override
        public void onExitState() {
            removeChild(eraser);
            btnDelMarker.setSelected(false);
        }
    };
    private final CursorComponent eraser = new CursorComponent();

    // Buttons =================================================================

    /**
     * Button for placing a marker at current position, local to this Atlas instance.
     */
    private final BookmarkComponent btnMarker;

    /**
     * Button for deleting local markers.
     */
    private final BookmarkComponent btnDelMarker;

    /**
     * Button for showing/hiding all markers.
     */
    private final BookmarkComponent btnShowMarkers;

    /**
     * Button for restoring player's position at the center of the Atlas.
     */
    private final FollowButtonComponent btnPosition;


    // Navigation ==============================================================

    /**
     * How much the map view is offset, in blocks, per click (or per tick).
     */
    private static final int navigateStep = 24;

    /**
     * The button which is currently being pressed. Used for continuous
     * navigation using the arrow buttons. Also used to prevent immediate
     * canceling of placing marker.
     */
    private ButtonComponent selectedButton = null;

    /**
     * Set to true when dragging the map view.
     */
    private boolean isDragging = false;

    /**
     * Offset to the top left corner of the tile at (0, 0) from the center of
     * the map drawing area, in pixels.
     */
    private int mapOffsetX, mapOffsetY;

    /**
     * When dragging, this saves the partly updates of the mapOffset.
     * Turns out, mouse dragging events are too precise.
     */
    private double mapOffsetDeltaX, mapOffsetDeltaY;

    private Integer targetOffsetX, targetOffsetY;
    /**
     * If true, the player's icon will be in the center of the GUI, and the
     * offset of the tiles will be calculated accordingly. Otherwise it's the
     * position of the player that will be calculated with respect to the
     * offset.
     */
    private boolean followPlayer;

    private final BarScaleComponent scaleBar = new BarScaleComponent();

    private final ScrollBoxComponent markers = new ScrollBoxComponent();

    /**
     * Pixel-to-block ratio.
     */
    private double mapScale;
    /**
     * The visual size of a tile in pixels.
     */
    private int tileHalfSize;
    /**
     * The number of chunks a tile spans.
     */
    private int tile2ChunkScale;

    // Markers =================================================================

    private Landmark<?> hoveredLandmark = null;

    private final MarkerModalComponent markerFinalizer = new MarkerModalComponent();
    /**
     * Displayed where the marker is about to be placed when the Finalizer GUI is on.
     */
    private final BlinkingMarkerComponent blinkingIcon = new BlinkingMarkerComponent();

    // Misc stuff ==============================================================

    private PlayerEntity player;
    private WorldAtlasData worldAtlasData;

    /**
     * Coordinate scale factor relative to the actual screen size.
     */
    private double screenScale;

    private long lastUpdateMillis = System.currentTimeMillis();
    private int scaleAlpha = 255;
    private final int zoomLevelOne = 8;
    private int zoomLevel = zoomLevelOne;
    private final String[] zoomNames = new String[]{"256", "128", "64", "32", "16", "8", "4", "2", "1", "1/2", "1/4", "1/8", "1/16", "1/32", "1/64", "1/128", "1/256"};

    @SuppressWarnings("rawtypes")
    public AtlasScreen() {
        if (AntiqueAtlas.CONFIG.ui.fullscreen) {
            WIDTH = MinecraftClient.getInstance().getWindow().getScaledWidth() - 40;
            HEIGHT = MinecraftClient.getInstance().getWindow().getScaledHeight() - 40;
        } else {
            WIDTH = 310;
            HEIGHT = 218;
        }
        setSize(WIDTH, HEIGHT);
        MAP_WIDTH = WIDTH - MAP_BORDER_WIDTH * 2;
        MAP_HEIGHT = HEIGHT - MAP_BORDER_HEIGHT * 2;
        setMapScale(0.5);
        followPlayer = true;

        btnPosition = new FollowButtonComponent();
        btnPosition.setEnabled(!followPlayer);
        addChild(btnPosition).offsetGuiCoords(WIDTH - MAP_BORDER_WIDTH - FollowButtonComponent.WIDTH + (AntiqueAtlas.CONFIG.ui.fullscreen ? 0 : 1), HEIGHT - MAP_BORDER_HEIGHT - FollowButtonComponent.HEIGHT + (AntiqueAtlas.CONFIG.ui.fullscreen ? 0 : -2));
        IButtonListener positionListener = button -> {
            selectedButton = button;
            if (button.equals(btnPosition)) {
                followPlayer = true;
                targetOffsetX = null;
                targetOffsetY = null;
                btnPosition.setEnabled(false);
            }
        };
        btnPosition.addListener(positionListener);

        btnMarker = new BookmarkComponent(0, ICON_ADD_MARKER, Text.translatable("gui.antique_atlas.addMarker"));
        addChild(btnMarker).offsetGuiCoords(WIDTH - 10, 14);
        btnMarker.addListener(button -> {
            if (state.is(PLACING_MARKER)) {
                selectedButton = null;
                state.switchTo(NORMAL);
            } else {
                selectedButton = button;
                state.switchTo(PLACING_MARKER);

                // While holding shift, we create a marker on the player's position
                if (hasShiftDown()) {
                    markerFinalizer.setMarkerData(player.getEntityWorld(), player.getBlockX(), player.getBlockZ());
                    addChild(markerFinalizer);

                    blinkingIcon.setTexture(markerFinalizer.selectedTexture.id(), markerFinalizer.selectedTexture.textureWidth(), markerFinalizer.selectedTexture.textureHeight());
                    addChildBehind(markerFinalizer, blinkingIcon).setRelativeCoords(worldXToScreenX((int) player.getX()) - getGuiX() - MARKER_SIZE / 2, worldZToScreenY((int) player.getZ()) - getGuiY() - MARKER_SIZE / 2);

                    // Un-press all keys to prevent player from walking infinitely:
                    KeyBinding.unpressAll();

                    selectedButton = null;
                    state.switchTo(NORMAL);
                }
            }
        });
        btnDelMarker = new BookmarkComponent(2, ICON_DELETE_MARKER, Text.translatable("gui.antique_atlas.delMarker"));
        addChild(btnDelMarker).offsetGuiCoords(WIDTH - 10, 33);
        btnDelMarker.addListener(button -> {
            if (state.is(DELETING_MARKER)) {
                selectedButton = null;
                state.switchTo(NORMAL);
            } else {
                selectedButton = button;
                state.switchTo(DELETING_MARKER);
            }
        });
        btnShowMarkers = new BookmarkComponent(3, ICON_HIDE_MARKERS, Text.translatable("gui.antique_atlas.hideMarkers"));
        addChild(btnShowMarkers).offsetGuiCoords(WIDTH - 10, 52);
        btnShowMarkers.addListener(button -> {
            selectedButton = null;
            if (state.is(HIDING_MARKERS)) {
                state.switchTo(NORMAL);
            } else {
                selectedButton = null;
                state.switchTo(HIDING_MARKERS);
            }
        });

        addChild(scaleBar).offsetGuiCoords(MAP_BORDER_WIDTH - 1 + (AntiqueAtlas.CONFIG.ui.fullscreen ? 0 : 4), HEIGHT - MAP_BORDER_HEIGHT - BarScaleComponent.HEIGHT + 1 + (AntiqueAtlas.CONFIG.ui.fullscreen ? 0 : -2));
        scaleBar.setMapScale(1);

        addChild(markers).setRelativeCoords(-10, 14);
        markers.setViewportSize(21, 180);
        markers.setWheelScrollsVertically();

        markerFinalizer.addMarkerListener(blinkingIcon);

        eraser.setTexture(ERASER, 12, 14, 2, 11);

        state.switchTo(NORMAL);
    }

    public AtlasScreen prepareToOpen() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0F));

        this.player = MinecraftClient.getInstance().player;
        updateAtlasData();

        return this;
    }

    @Override
    public void init() {
        super.init();

        screenScale = MinecraftClient.getInstance().getWindow().getScaleFactor();
        setCentered();

        updateBookmarkerList();
    }

    public void updateBookmarkerList() {
        markers.removeAllContent();
        markers.scrollTo(0, 0);

        if (worldAtlasData == null) return;

        final int[] contentY = {0};
        worldAtlasData.getEditableLandmarks().forEach((landmark, texture) -> {
            MarkerBookmarkComponent bookmark = new MarkerBookmarkComponent(landmark, texture);

            bookmark.addListener(button -> {
                if (state.is(NORMAL)) {
                    setTargetPosition(new ColumnPos(landmark.pos().getX(), landmark.pos().getZ()));
                    followPlayer = false;
                    btnPosition.setEnabled(true);
                } else if (state.is(DELETING_MARKER)) {
                    if (!worldAtlasData.deleteLandmark(player.getEntityWorld(), landmark)) return;
                    updateBookmarkerList();
                    player.getEntityWorld().playSound(player, player.getBlockPos(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.AMBIENT, 1F, 0.5F);
                    state.switchTo(NORMAL);
                }
            });

            markers.addContent(bookmark).setRelativeY(contentY[0]);
            contentY[0] += 18 + 2;
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseState) {
        if (super.mouseClicked(mouseX, mouseY, mouseState)) return true;

        // If clicked on the map, start dragging
        int mapX = (width - MAP_WIDTH) / 2;
        int mapY = (height - MAP_HEIGHT) / 2;
        boolean isMouseOverMap = mouseX >= mapX && mouseX <= mapX + MAP_WIDTH && mouseY >= mapY && mouseY <= mapY + MAP_HEIGHT;
        if (!state.is(NORMAL) && !state.is(HIDING_MARKERS)) {
            if (state.is(PLACING_MARKER) && isMouseOverMap && mouseState == 0 /* left click */) {
                markerFinalizer.setMarkerData(player.getEntityWorld(), screenXToWorldX((int) mouseX), screenYToWorldZ((int) mouseY));
                addChild(markerFinalizer);

                blinkingIcon.setTexture(markerFinalizer.selectedTexture.id(), MARKER_SIZE, MARKER_SIZE);
                addChildBehind(markerFinalizer, blinkingIcon).setRelativeCoords((int) mouseX - getGuiX() - MARKER_SIZE / 2, (int) mouseY - getGuiY() - MARKER_SIZE / 2);

                // Un-press all keys to prevent player from walking infinitely:
                KeyBinding.unpressAll();

                state.switchTo(NORMAL);
                return true;
            } else if (state.is(DELETING_MARKER) && hoveredLandmark != null && isMouseOverMap && mouseState == 0) {
                if (worldAtlasData.deleteLandmark(player.getEntityWorld(), hoveredLandmark)) {
                    updateBookmarkerList();
                    player.getEntityWorld().playSound(player, player.getBlockPos(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.AMBIENT, 1F, 0.5F);
                }
            }
            state.switchTo(NORMAL);
        } else if (isMouseOverMap && selectedButton == null) {
            isDragging = true;
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_UP) {
            navigateMap(0, navigateStep);
        } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
            navigateMap(0, -navigateStep);
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            navigateMap(navigateStep, 0);
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            navigateMap(-navigateStep, 0);
        } else if (keyCode == GLFW.GLFW_KEY_EQUAL || keyCode == GLFW.GLFW_KEY_KP_ADD) {
            setMapScale(mapScale * 2);
        } else if (keyCode == GLFW.GLFW_KEY_MINUS || keyCode == GLFW.GLFW_KEY_KP_SUBTRACT) {
            setMapScale(mapScale / 2);
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE || (AntiqueAtlasKeybindings.ATLAS_KEYMAPPING.matchesKey(keyCode, scanCode) && this.markerFinalizer.getParent() == null)) {
            close();
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double wheelMove) {
        double origWheelMove = wheelMove;

        boolean handled = super.mouseScrolled(mx, my, origWheelMove);

        if (!handled && wheelMove != 0) {
            wheelMove = wheelMove > 0 ? 1 : -1;
            if (AntiqueAtlas.CONFIG.ui.reverseZoom) {
                wheelMove *= -1;
            }

            double mouseOffsetX = MinecraftClient.getInstance().getWindow().getFramebufferWidth() / screenScale / 2 - getMouseX();
            double mouseOffsetY = MinecraftClient.getInstance().getWindow().getFramebufferHeight() / screenScale / 2 - getMouseY();
            double newScale = mapScale * Math.pow(2, wheelMove);
            double addOffsetX = 0;
            double addOffsetY = 0;
            if (Math.abs(mouseOffsetX) < MAP_WIDTH / 2f && Math.abs(mouseOffsetY) < MAP_HEIGHT / 2f) {
                addOffsetX = mouseOffsetX * wheelMove;
                addOffsetY = mouseOffsetY * wheelMove;

                if (wheelMove > 0) {
                    addOffsetX *= mapScale / newScale;
                    addOffsetY *= mapScale / newScale;
                }
            }

            setMapScale(newScale, (int) addOffsetX, (int) addOffsetY);

            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0F));

            return true;
        }

        return handled;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseState) {
        boolean result = false;
        if (mouseState != -1) {
            result = selectedButton != null || isDragging;
            selectedButton = null;
            isDragging = false;
            mapOffsetDeltaX = 0;
            mapOffsetDeltaY = 0;
        }
        return super.mouseReleased(mouseX, mouseY, mouseState) || result;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int lastMouseButton, double deltaX, double deltaY) {
        boolean result = false;
        if (isDragging) {
            followPlayer = false;
            btnPosition.setEnabled(true);

            mapOffsetDeltaX += deltaX;
            mapOffsetDeltaY += deltaY;

            int offsetX = (int) (Math.signum(mapOffsetDeltaX) * Math.floor(Math.abs(mapOffsetDeltaX)));
            int offsetY = (int) (Math.signum(mapOffsetDeltaY) * Math.floor(Math.abs(mapOffsetDeltaY)));

            if (Math.abs(mapOffsetDeltaX) >= 1) {
                mapOffsetDeltaX = mapOffsetDeltaX - offsetX;
                mapOffsetX += offsetX;
            }

            if (Math.abs(mapOffsetDeltaY) >= 1) {
                mapOffsetDeltaY = mapOffsetDeltaY - offsetY;
                mapOffsetY += offsetY;
            }

            result = true;
        }
        return super.mouseDragged(mouseX, mouseY, lastMouseButton, deltaX, deltaY) || result;
    }

    @Override
    public void tick() {
        super.tick();
        if (player == null) return;

        if (followPlayer) {
            setMapPosition(player.getBlockX(), player.getBlockZ());
        }

        if (targetOffsetX != null) {
            if (Math.abs(getTargetPositionX() - mapOffsetX) > navigateStep) {
                navigateMap(getTargetPositionX() > mapOffsetX ? navigateStep : -navigateStep, 0);
            } else {
                mapOffsetX = getTargetPositionX();
                targetOffsetX = null;
            }
        }

        if (targetOffsetY != null) {
            if (Math.abs(getTargetPositionY() - mapOffsetY) > navigateStep) {
                navigateMap(0, getTargetPositionY() > mapOffsetY ? navigateStep : -navigateStep);
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

    /**
     * Offset the map view by given values, in blocks.
     */
    private void navigateMap(int dx, int dy) {
        mapOffsetX += dx;
        mapOffsetY += dy;
        followPlayer = false;
        btnPosition.setEnabled(true);
    }

    private void setMapPosition(int x, int z) {
        mapOffsetX = (int) (-x * mapScale);
        mapOffsetY = (int) (-z * mapScale);
    }

    private void setTargetPosition(ColumnPos pos) {
        targetOffsetX = pos.x();
        targetOffsetY = pos.z();
    }

    private int getTargetPositionX() {
        return (int) (-targetOffsetX * mapScale);
    }

    private int getTargetPositionY() {
        return (int) (-targetOffsetY * mapScale);
    }


    /**
     * Set the pixel-to-block ratio, maintaining the current center of the screen.
     */
    public void setMapScale(double scale) {
        setMapScale(scale, 0, 0);
    }

    /**
     * Set the pixel-to-block ratio, maintaining the current center of the screen with additional offset.
     */
    private void setMapScale(double scale, int addOffsetX, int addOffsetY) {
        hoveredLandmark = null;
        double oldScale = mapScale;
        mapScale = Math.min(Math.max(scale, AntiqueAtlas.CONFIG.ui.minScale), AntiqueAtlas.CONFIG.ui.maxScale);

        // Scaling not needed
        if (oldScale == mapScale) {
            return;
        }

        if (mapScale >= MIN_SCALE_THRESHOLD) {
            tileHalfSize = (int) Math.round(8 * mapScale);
            tile2ChunkScale = 1;
        } else {
            tileHalfSize = (int) Math.round(8 * MIN_SCALE_THRESHOLD);
            tile2ChunkScale = (int) Math.round(MIN_SCALE_THRESHOLD / mapScale);
        }

        // Times 2 because the contents of the Atlas are rendered at resolution 2 times smaller:
        scaleBar.setMapScale(mapScale * 2);
        mapOffsetX = (int) ((mapOffsetX + addOffsetX) * (mapScale / oldScale));
        mapOffsetY = (int) ((mapOffsetY + addOffsetY) * (mapScale / oldScale));
        int scaleClipIndex = MathHelper.floorLog2((int) (mapScale * 8192)) + 1 - 13;
        zoomLevel = -scaleClipIndex + zoomLevelOne;
        scaleAlpha = 255;

        if (followPlayer && (addOffsetX != 0 || addOffsetY != 0)) {
            followPlayer = false;
            btnPosition.setEnabled(true);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float par3) {
        long currentMillis = System.currentTimeMillis();
        long deltaMillis = currentMillis - lastUpdateMillis;
        lastUpdateMillis = currentMillis;

        super.renderBackground(context);

        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (AntiqueAtlas.CONFIG.ui.fullscreen) {
            context.fill(getGuiX(), getGuiY(), getGuiX() + WIDTH, getGuiY() + HEIGHT, 0xFFEAD2A5);
            context.drawBorder(getGuiX(), getGuiY(), WIDTH, HEIGHT, 0xFF4C1A0B);
            context.drawBorder(getGuiX() + MAP_BORDER_WIDTH - 1, getGuiY() + MAP_BORDER_HEIGHT - 1, MAP_WIDTH + 2, MAP_HEIGHT + 2, 0xFF4C1A0B);
        } else {
            context.drawTexture(BOOK, getGuiX(), getGuiY(), 0, 0, 310, 218, 310, 218);
        }

        if (worldAtlasData == null) return;

        if (state.is(DELETING_MARKER)) {
            RenderSystem.setShaderColor(1, 1, 1, 0.5f);
        }
        RenderSystem.enableScissor((int) ((getGuiX() + MAP_BORDER_WIDTH) * screenScale), (int) ((MinecraftClient.getInstance().getWindow().getFramebufferHeight() - (getGuiY() + MAP_BORDER_HEIGHT + MAP_HEIGHT) * screenScale)), (int) (MAP_WIDTH * screenScale), (int) (MAP_HEIGHT * screenScale));
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        // Find chunk coordinates of the top left corner of the map.
        // The 'roundToBase' is required so that when the map scales below the
        // threshold the tiles don't change when map position changes slightly.
        // The +-2 at the end provide margin so that tiles at the edges of
        // the page have their stitched texture correct.
        int mapStartX = MathUtil.roundToBase((int) Math.floor(-((double) MAP_WIDTH / 2d + mapOffsetX + 2 * tileHalfSize) / mapScale / 16d), tile2ChunkScale);
        int mapStartZ = MathUtil.roundToBase((int) Math.floor(-((double) MAP_HEIGHT / 2d + mapOffsetY + 2 * tileHalfSize) / mapScale / 16d), tile2ChunkScale);
        int mapEndX = MathUtil.roundToBase((int) Math.ceil(((double) MAP_WIDTH / 2d - mapOffsetX + 2 * tileHalfSize) / mapScale / 16d), tile2ChunkScale);
        int mapEndZ = MathUtil.roundToBase((int) Math.ceil(((double) MAP_HEIGHT / 2d - mapOffsetY + 2 * tileHalfSize) / mapScale / 16d), tile2ChunkScale);
        int mapStartScreenX = getGuiX() + WIDTH / 2 + (int) ((mapStartX << 4) * mapScale) + mapOffsetX;
        int mapStartScreenY = getGuiY() + HEIGHT / 2 + (int) ((mapStartZ << 4) * mapScale) + mapOffsetY;
        TileRenderIterator tiles = new TileRenderIterator(worldAtlasData);
        tiles.setScope(new Rect(mapStartX, mapStartZ, mapEndX, mapEndZ));
        tiles.setStep(tile2ChunkScale);

        context.getMatrices().push();
        context.getMatrices().translate(mapStartScreenX, mapStartScreenY, 0);

        for (SubTileQuartet subTiles : tiles) {
            for (SubTile subtile : subTiles) {
                if (subtile == null || subtile.texture == null) continue;
                context.drawTexture(subtile.texture.id(), subtile.x * tileHalfSize, subtile.y * tileHalfSize, tileHalfSize, tileHalfSize, subtile.getTextureU() * 8, subtile.getTextureV() * 8, 8, 8, 32, 48);
            }
        }

        context.getMatrices().pop();

        // Overlay the frame so that edges of the map are smooth:
        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (!AntiqueAtlas.CONFIG.ui.fullscreen) {
            context.drawTexture(BOOK_FRAME, getGuiX(), getGuiY(), 0, 0, 310, 218, 310, 218);
        }

        hoveredLandmark = null;
        if (!state.is(HIDING_MARKERS)) {
            double bestDistance = Double.MAX_VALUE;
            for (Map.Entry<Landmark<?>, MarkerTexture> entry : worldAtlasData.getAllMarkers().entrySet()) {
                Landmark<?> landmark = entry.getKey();
                MarkerTexture texture = entry.getValue();
                int markerX = worldXToScreenX(landmark.pos().getX());
                int markerY = worldZToScreenY(landmark.pos().getZ());

                double squaredDistance = Vector2i.distanceSquared(markerX + texture.offsetX() + texture.textureWidth() / 2, markerY + texture.offsetY() + texture.textureHeight() / 2, mouseX, mouseY);
                if (squaredDistance > 0 && squaredDistance < bestDistance && squaredDistance < (texture.textureWidth() * texture.textureHeight()) / 4.0) {
                    bestDistance = squaredDistance;
                    hoveredLandmark = landmark;
                }
            }
            worldAtlasData.getAllMarkers().forEach((landmark, texture) -> {
                renderMarker(context, landmark, texture, WorldAtlasData.landmarkIsEditable(landmark), hoveredLandmark == landmark);
            });
        }

        RenderSystem.disableScissor();

        if (!AntiqueAtlas.CONFIG.ui.fullscreen) {
            context.drawTexture(BOOK_FRAME_NARROW, getGuiX(), getGuiY(), 0, 0, 310, 218, 310, 218);
        }

        renderScaleOverlay(context, deltaMillis);

        if (!state.is(HIDING_MARKERS)) {
            renderPlayer(context, 1);
        }

        super.render(context, mouseX, mouseY, par3);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        if (state.is(PLACING_MARKER)) {
            RenderSystem.setShaderColor(1, 1, 1, 0.5f);
            context.drawTexture(markerFinalizer.selectedTexture.id(), mouseX + markerFinalizer.selectedTexture.offsetX(), mouseY + markerFinalizer.selectedTexture.offsetY(), 0, 0, markerFinalizer.selectedTexture.textureWidth(), markerFinalizer.selectedTexture.textureHeight(), markerFinalizer.selectedTexture.textureWidth(), markerFinalizer.selectedTexture.textureHeight());
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
        RenderSystem.disableBlend();

        if (AntiqueAtlas.CONFIG.debug.debugRender && !isDragging && isMouseOver) {
            int x = screenXToWorldX((int) getMouseX());
            int z = screenYToWorldZ((int) getMouseY());
            ChunkPos pos = new ChunkPos(new BlockPos(x, 0, z));
            context.drawText(textRenderer, Text.literal("%d,%d (%d,%d)".formatted(pos.x, pos.z, x, z)), getGuiX(), getGuiY() - 12, 0xFFFFFFFF, true);
            if (hoveredLandmark != null) {
                MarkerTexture texture = worldAtlasData.getMarkerTexture(hoveredLandmark);
                context.drawText(textRenderer, Text.literal(hoveredLandmark.type().id().toString()), getGuiX() + WIDTH - textRenderer.getWidth(Text.literal(hoveredLandmark.type().id().toString())), getGuiY() - 12, 0xFFFFFFFF, true);
                if (hoveredLandmark instanceof AtlasStructureLandmark sLandmark) context.drawText(textRenderer, Text.literal(sLandmark.displayId().toString()), getGuiX(), getGuiY() + HEIGHT, 0xFFFFFFFF, true);
                if (texture != null) context.drawText(textRenderer, Text.literal(texture.displayId()), getGuiX() + WIDTH - textRenderer.getWidth(Text.literal(texture.displayId())), getGuiY() + HEIGHT, 0xFFFFFFFF, true);
            } else {
                TileTexture texture = worldAtlasData.getTile(pos);
                Identifier providerId = worldAtlasData.getProvider(pos);
                String predicate = worldAtlasData.getTilePredicate(pos);
                if (texture != null) {
                    if (predicate != null) context.drawText(textRenderer, Text.literal(predicate), getGuiX() + WIDTH - textRenderer.getWidth(Text.literal(predicate)), getGuiY() - 12, 0xFFFFFFFF, true);
                    context.drawText(textRenderer, Text.literal(providerId.toString()), getGuiX(), getGuiY() + HEIGHT, 0xFFFFFFFF, true);
                    context.drawText(textRenderer, Text.literal(texture.displayId()), getGuiX() + WIDTH - textRenderer.getWidth(Text.literal(texture.displayId())), getGuiY() + HEIGHT, 0xFFFFFFFF, true);
                }
            }
        }
    }

    private void renderPlayer(DrawContext context, float iconScale) {
        int playerOffsetX = worldXToScreenX(player.getBlockX());
        int playerOffsetY = worldZToScreenY(player.getBlockZ());

        playerOffsetX = MathHelper.clamp(playerOffsetX, getGuiX() + MAP_BORDER_WIDTH, getGuiX() + MAP_WIDTH + MAP_BORDER_WIDTH);
        playerOffsetY = MathHelper.clamp(playerOffsetY, getGuiY() + MAP_BORDER_HEIGHT, getGuiY() + MAP_HEIGHT + MAP_BORDER_HEIGHT);

        // Draw the icon:
        RenderSystem.setShaderColor(1, 1, 1, state.is(PLACING_MARKER) ? 0.5f : 1);
        float playerRotation = (float) Math.round(player.getYaw() / 360f * PLAYER_ROTATION_STEPS) / PLAYER_ROTATION_STEPS * 360f;

        DrawUtil.drawCenteredWithRotation(context, PLAYER, playerOffsetX, playerOffsetY, iconScale, PLAYER_ICON_WIDTH, PLAYER_ICON_HEIGHT, playerRotation);

        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private void renderScaleOverlay(DrawContext context, long deltaMillis) {
        MatrixStack matrices = context.getMatrices();
        if (scaleAlpha > 3) {
            matrices.push();
            matrices.translate(getGuiX() + WIDTH - 13, getGuiY() + 12, 0);

            int color = scaleAlpha << 24;

            String text;
            int textWidth, xWidth;

            text = "x";
            xWidth = textWidth = this.textRenderer.getWidth(text);
            xWidth++;
            context.drawText(this.textRenderer, text, -textWidth, 0, color, false);

            text = zoomNames[zoomLevel];
            if (text.contains("/")) {
                String[] parts = text.split("/");

                int centerXtranslate = Math.max(this.textRenderer.getWidth(parts[0]), this.textRenderer.getWidth(parts[1])) / 2;
                matrices.translate(-xWidth - centerXtranslate, (float) -this.textRenderer.fontHeight / 2, 0);

                context.fill(-centerXtranslate - 1, this.textRenderer.fontHeight - 1, centerXtranslate, this.textRenderer.fontHeight, color);

                textWidth = this.textRenderer.getWidth(parts[0]);
                context.drawText(this.textRenderer, parts[0], -textWidth / 2, 0, color, false);

                textWidth = this.textRenderer.getWidth(parts[1]);
                context.drawText(this.textRenderer, parts[1], -textWidth / 2, 10, color, false);
            } else {
                textWidth = this.textRenderer.getWidth(text);
                context.drawText(this.textRenderer, text, -textWidth - xWidth + 1, 2, color, false);
            }

            matrices.pop();

            int deltaScaleAlpha = (int) (deltaMillis * 0.256);
            // because of some crazy high frame rate
            if (deltaScaleAlpha == 0) {
                deltaScaleAlpha = 1;
            }

            scaleAlpha -= deltaScaleAlpha;

            if (scaleAlpha < 0) scaleAlpha = 0;

        }
    }

    private void renderMarker(DrawContext context, Landmark<?> landmark, MarkerTexture texture, boolean editable, boolean hovering) {
        int markerX = worldXToScreenX(landmark.pos().getX());
        int markerY = worldZToScreenY(landmark.pos().getZ());

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

        if (isMouseOver && hovering && landmark.name() != null && !landmark.name().getString().isEmpty()) {
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
        markerFinalizer.closeChild();
        removeChild(blinkingIcon);
    }

    /**
     * Returns the Y coordinate that the cursor is pointing at.
     */
    private int screenXToWorldX(int mouseX) {
        return (int) Math.round((double) (mouseX - this.width / 2 - mapOffsetX) / mapScale);
    }

    /**
     * Returns the Y block coordinate that the cursor is pointing at.
     */
    private int screenYToWorldZ(int mouseY) {
        return (int) Math.round((double) (mouseY - this.height / 2 - mapOffsetY) / mapScale);
    }

    private int worldXToScreenX(int x) {
        return (int) Math.round((double) x * mapScale + this.width / 2f + mapOffsetX);
    }

    private int worldZToScreenY(int z) {
        return (int) Math.round((double) z * mapScale + this.height / 2f + mapOffsetY);
    }

    @Override
    protected void onChildClosed(Component child) {
        if (child.equals(markerFinalizer)) {
            removeChild(blinkingIcon);
        }
    }

    public WorldAtlasData getworldAtlasData() {
        return worldAtlasData;
    }
}
