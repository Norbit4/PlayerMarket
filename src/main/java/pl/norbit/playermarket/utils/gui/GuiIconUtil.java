package pl.norbit.playermarket.utils.gui;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.pagination.PaginationManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.norbit.playermarket.utils.ChatUtils;

public class GuiIconUtil {

    public static Icon getPaginationItem(PaginationManager pagination, IconType type){
        Icon icon = new Icon(Material.ARROW);

        if(type == IconType.LEFT){
            icon.setName(ChatUtils.format("&e&lPoprzednia strona"));
        }
        else if(type == IconType.RIGHT){
            icon.setName(ChatUtils.format("&e&lNastępna strona"));
        }
        icon.hideFlags();
        icon.onClick(e -> {
            e.setCancelled(true);
            if(type == IconType.RIGHT) pagination.goNextPage();
            else if(type == IconType.LEFT) pagination.goPreviousPage();
            pagination.update();
        });
        return icon;
    }

    public static Icon getOpenGuItem(Material material, String name, Gui gui){

        Icon icon = new Icon(material);
        icon.setName(ChatUtils.format("&a&l" + name));

        icon.setLore("", ChatUtils.format("&eKliknij aby otworzyć!"));

        icon.hideFlags();
        icon.onClick(e -> {
            e.setCancelled(true);
            gui.open();
        });
        return icon;
    }
    public static Icon getCloseGuiItem(Player p){

        Icon icon = new Icon(Material.BARRIER);
        icon.setName(ChatUtils.format("&c&lZamknij"));

        icon.setLore("", ChatUtils.format("&eKliknij aby zamknąć!"));

        icon.hideFlags();
        icon.onClick(e -> {
            e.setCancelled(true);
            p.closeInventory();
        });
        return icon;
    }
}
