package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.gui.Drawable;
import folk.sisby.antique_atlas.gui.Texture;
import net.minecraft.util.Identifier;

public class AntiqueAtlasTextures {
    private static final String MOD_PREFIX = AntiqueAtlas.ID + ":";
    private static final String GUI = MOD_PREFIX + "textures/gui/";
    private static final String GUI_ICONS = GUI + "icons/";
    private static final String GUI_SCALEBAR = GUI + "scalebar/";

    public static final Drawable
        BOOK = gui("book.png", 310, 218),
        BOOK_FRAME = gui("book_frame.png", 310, 218),
        BOOK_FRAME_NARROW = gui("book_frame_narrow.png", 310, 218),
        BTN_POSITION = gui("position.png", 11, 11),
        BOOKMARKS = gui("bookmarks.png", 84, 36),
        BOOKMARKS_LEFT = gui("bookmarks_l.png", 84, 36),
        PLAYER = gui("player.png", 7, 8),
        SCROLLBAR_HOR = gui("scrollbar_hor.png", 8, 7),
        SCROLLBAR_VER = gui("scrollbar_ver.png", 7, 8),
        MARKER_FRAME_ON = gui("marker_frame_on.png", 34, 34),
        MARKER_FRAME_OFF = gui("marker_frame_off.png", 34, 34),
        ERASER = gui("eraser.png", 24, 24),
        SCALEBAR_4 = scaleBar("scalebar_4.png"),
        SCALEBAR_8 = scaleBar("scalebar_8.png"),
        SCALEBAR_16 = scaleBar("scalebar_16.png"),
        SCALEBAR_32 = scaleBar("scalebar_32.png"),
        SCALEBAR_64 = scaleBar("scalebar_64.png"),
        SCALEBAR_128 = scaleBar("scalebar_128.png"),
        SCALEBAR_256 = scaleBar("scalebar_256.png"),
        SCALEBAR_512 = scaleBar("scalebar_512.png"),
        ICON_ADD_MARKER = icon("add_marker.png"),
        ICON_DELETE_MARKER = icon("del_marker.png"),
        ICON_SHOW_MARKERS = icon("show_markers.png"),
        ICON_HIDE_MARKERS = icon("hide_markers.png");

    // Constructor helpers:
    private static Drawable gui(String fileName, int width, int height) {
        return new Texture(new Identifier(GUI + fileName), width, height);
    }

    private static Drawable scaleBar(String fileName) {
        return new Texture(new Identifier(GUI_SCALEBAR + fileName), 20, 8);
    }

    private static Drawable icon(String fileName) {
        return new Texture(new Identifier(GUI_ICONS + fileName), 16, 16);
    }
}
