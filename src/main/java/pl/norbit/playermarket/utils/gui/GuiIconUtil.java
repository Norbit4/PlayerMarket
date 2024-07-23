package pl.norbit.playermarket.utils.gui;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.pagination.PaginationManager;

public class GuiIconUtil {

    private GuiIconUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static Icon getPaginationItem(PaginationManager pagination, IconType type, Icon icon){
        icon.hideFlags();
        icon.onClick(e -> {
            e.setCancelled(true);
            if(type == IconType.RIGHT) pagination.goNextPage();
            else if(type == IconType.LEFT) pagination.goPreviousPage();
            pagination.update();
        });
        return icon;
    }
    public static Icon getOpenGuItem(Icon icon, Gui gui){
        icon.hideFlags();
        icon.onClick(e -> {
            e.setCancelled(true);
            gui.open();
        });
        return icon;
    }
}
