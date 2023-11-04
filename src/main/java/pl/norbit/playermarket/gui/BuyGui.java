package pl.norbit.playermarket.gui;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.model.MarketItemData;
import pl.norbit.playermarket.economy.EconomyService;
import pl.norbit.playermarket.service.CategoryService;
import pl.norbit.playermarket.utils.ChatUtils;
import pl.norbit.playermarket.utils.DoubleFormatter;
import pl.norbit.playermarket.utils.TaskUtils;

public class BuyGui extends Gui {
    private final MarketItemData marketItemData;
    private final ItemStack is;

    public BuyGui(@NotNull Player player, MarketItemData marketItemData, ItemStack icon) {
        super(player, "BUY-1", ChatUtils.format("&8&lKup przedmiot!"), 4);
        this.marketItemData = marketItemData;
        this.is = icon;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

        Icon itemIcon = getIcon(is);

        addItem(13, itemIcon);

        Icon buyIcon = getIcon(Material.GREEN_WOOL, "&aKup za &b{AMOUNT}$!".replace("{AMOUNT}",
                DoubleFormatter.format(marketItemData.getPrice())),
                "&eKliknij aby kupić!");

        Icon cancelIcon = getIcon(Material.RED_WOOL, "&c&lCofnij", "&eKliknij aby wrócić!");

        buyIcon.onClick(e -> {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();

            TaskUtils.runTaskLaterAsynchronously(() -> {
                MarketItemData mItemData = DataService.getMarketItemData(marketItemData.getId());

                if(mItemData == null){
                    backToShop("&cPrzedmiot został już sprzedany!");
                    return;
                }

                if(!EconomyService.withDrawIfPossible(p, mItemData.getPrice())){
                    backToShop("&cNie masz wystarczająco środków!");
                    return;
                }

                DataService.buyItem(mItemData);
                p.getInventory().addItem(mItemData.getItemStack());

                backToShop("&aKupiłeś przedmiot!");

            },0L);
        });

        cancelIcon.onClick(e -> {
            e.setCancelled(true);
            new MarketGui((Player)e.getWhoClicked(), CategoryService.getMain()).open();
        });

        addItem(20, buyIcon);
        addItem(24, cancelIcon);
    }

    private void backToShop(String message){
        player.sendMessage(ChatUtils.format(message));
        TaskUtils.runTaskLater(() -> new MarketGui(player, CategoryService.getMain()), 0L);
    }

    private static Icon getIcon(ItemStack is){
        return new Icon(is);
    }

    private static Icon getIcon(Material material, String name, String lore){
        Icon icon = new Icon(material);

        icon.setName(ChatUtils.format("&b&l" + name));
        icon.setLore("", ChatUtils.format(lore));
        icon.hideFlags();

        return icon;
    }
}
