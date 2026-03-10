package pl.norbit.playermarket.utils.gui;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.pagination.PaginationManager;
import org.bukkit.entity.Player;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.cooldown.CooldownService;
import pl.norbit.playermarket.model.local.ConfigGui;
import pl.norbit.playermarket.utils.format.ChatUtils;

import java.util.List;

public class GuiUtils {

    private GuiUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void loadBorder(ConfigGui configGui, PaginationManager borderPagination, List<Integer> blackList, int size) {
        if(!configGui.isFill()){
            for (int i = 0; i < borderPagination.getSlots().size(); i++) {
                borderPagination.addItem(configGui.getBorderIcon());
            }
        }else {
            for (int i = 0; i < size; i++) {
                if(blackList.contains(i)){
                    continue;
                }
                borderPagination.registerPageSlots(i);
                borderPagination.addItem(configGui.getBorderIcon());
            }
        }
    }

    public static Icon getOpenGuItem(Icon icon, Gui gui){
        icon.hideFlags();
        Player p = gui.player;

        icon.onClick(e -> {
            if (!CooldownService.tryClick(p.getUniqueId())) {
                p.closeInventory();
                p.sendMessage(ChatUtils.format(Settings.getCooldownMessage()));
                return;
            }
            gui.open();
        });
        return icon;
    }
}
