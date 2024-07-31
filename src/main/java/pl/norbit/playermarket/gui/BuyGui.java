package pl.norbit.playermarket.gui;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.model.MarketItemData;
import pl.norbit.playermarket.economy.EconomyService;
import pl.norbit.playermarket.model.local.ConfigGui;
import pl.norbit.playermarket.service.CategoryService;
import pl.norbit.playermarket.service.SearchStorage;
import pl.norbit.playermarket.utils.ChatUtils;
import pl.norbit.playermarket.utils.DoubleFormatter;
import pl.norbit.playermarket.utils.TaskUtils;

import java.util.stream.Collectors;

import static pl.norbit.playermarket.utils.TaskUtils.async;
import static pl.norbit.playermarket.utils.TaskUtils.sync;

public class BuyGui extends Gui {
    private final MarketItemData marketItemData;
    private final ItemStack is;
    private final ConfigGui configGui;

    public BuyGui(@NotNull Player player, MarketItemData marketItemData, ItemStack icon) {
        super(player, "BUY-1", ChatUtils.format(player, Settings.BUY_GUI.getTitle()), 4);

        this.marketItemData = marketItemData;
        this.is = icon;
        this.configGui = Settings.BUY_GUI;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

        Icon itemIcon = getIcon(is);

        addItem(13, itemIcon);

        addItem(20, getAcceptIcon());
        addItem(24, getCanceIcon());
    }

    private void backToShop(String message){
        player.sendMessage(ChatUtils.format(player, message));
        String search = SearchStorage.getSearch(player.getUniqueId());
        sync(() -> {
            if(search != null){
                new MarketSearchGui(player, search).open();
                return;
            }
            new MarketGui(player, CategoryService.getMain()).open();
        });
    }

    private static Icon getIcon(ItemStack is){
        return new Icon(is);
    }

    private Icon getCanceIcon(){
        Icon icon = configGui.getIcon("cancel-icon");

        icon.onClick(e -> {
            e.setCancelled(true);

            String search = SearchStorage.getSearch(player.getUniqueId());

            if(search != null){
                new MarketSearchGui(player, search).open();
                return;
            }

            new MarketGui(player, CategoryService.getMain()).open();
        });
        return icon;
    }

    private Icon getAcceptIcon(){
        Icon icon = configGui.getIcon("accept-icon");

        ItemStack item = icon.getItem();

        icon.setName(formatLine(item.getItemMeta().getDisplayName()));
        icon.setLore(item.getItemMeta()
                .getLore()
                .stream()
                .map(this::formatLine)
                .collect(Collectors.toList()));

        icon.onClick(e -> {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();

            async(() -> {
                MarketItemData mItemData = DataService.getMarketItemData(marketItemData.getId());

                if(mItemData == null){
                    backToShop(configGui.getMessage("item-sold-message"));
                    return;
                }

                if(!EconomyService.withDrawIfPossible(p, mItemData.getPrice())){
                    backToShop(configGui.getMessage("not-enough-money-message"));
                    return;
                }

                String ownerUUID = marketItemData.getOwnerUUID();

                if(ownerUUID.equals(p.getUniqueId().toString())){
                    backToShop(configGui.getMessage("player-is-owner-message"));
                    return;
                }

                DataService.buyItem(mItemData);
                p.getInventory().addItem(mItemData.getItemStackDeserialize());

                backToShop(configGui.getMessage("success-message")
                        .replace("{COST}", DoubleFormatter.format(mItemData.getPrice()))
                );

            });
        });
        return icon;
    }
    private String formatLine(String line){
        return ChatUtils.format(player, line.replace("{AMOUNT}", DoubleFormatter.format(marketItemData.getPrice())));
    }
}
