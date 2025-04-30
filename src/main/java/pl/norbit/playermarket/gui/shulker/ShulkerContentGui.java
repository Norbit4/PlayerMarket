package pl.norbit.playermarket.gui.shulker;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.pagination.PaginationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.gui.*;
import pl.norbit.playermarket.model.MarketItemData;
import pl.norbit.playermarket.model.local.ConfigGui;
import pl.norbit.playermarket.model.local.ConfigIcon;
import pl.norbit.playermarket.model.local.LocalPlayerData;
import pl.norbit.playermarket.service.CategoryService;
import pl.norbit.playermarket.service.SearchStorage;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.player.ItemsUtils;

import java.util.ArrayList;
import java.util.List;

import static pl.norbit.playermarket.utils.TaskUtils.async;
import static pl.norbit.playermarket.utils.TaskUtils.sync;

public class ShulkerContentGui extends Gui {
    private final PaginationManager items;
    private final ConfigGui configGui;
    private final GuiType guiType;
    private final ItemStack itemIcon;
    private MarketItemData marketItemData;

    public ShulkerContentGui(@NotNull Player player, MarketItemData marketItemData, ItemStack icon) {
        super(player, "shulker-gui", ChatUtils.format(Settings.SHULKER_GUI.getTitle()), 5);

        this.items = new PaginationManager(this);
        this.items.registerPageSlotsBetween(0, 26);

        this.marketItemData = marketItemData;
        this.itemIcon = icon;

        configGui = Settings.SHULKER_GUI;
        this.guiType = GuiType.MAIN;

        List<ItemStack> shulkerBoxInv = ItemsUtils.getShulkerBoxInv(marketItemData.getItemStackDeserialize());

        shulkerBoxInv.forEach(is -> items.addItem(new Icon(is)));
    }
    public ShulkerContentGui(@NotNull Player player, ItemStack icon) {
        super(player, "shulker-gui", ChatUtils.format(Settings.SHULKER_GUI.getTitle()), 5);

        this.items = new PaginationManager(this);
        this.items.registerPageSlotsBetween(0, 26);

        this.itemIcon = icon;

        configGui = Settings.SHULKER_GUI;
        this.guiType = GuiType.PLAYER_ITEMS;

        List<ItemStack> shulkerBoxInv = ItemsUtils.getShulkerBoxInv(icon);

        shulkerBoxInv.forEach(is -> items.addItem(new Icon(is)));
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        items.update();

        ConfigIcon backIcon = configGui.getIcon("back-icon");
        addItem(backIcon.getSlot(), getBackIcon(backIcon.getIcon()));

        ConfigIcon buyIcon = configGui.getIcon("buy-icon");


        if(this.configGui.isFill()){
            List<Integer> fillBlackList = new ArrayList<>(this.items.getSlots());

            if(guiType == GuiType.MAIN){
                fillBlackList.add(buyIcon.getSlot());
            }
            fillBlackList.add(backIcon.getSlot());

            fillGui(this.configGui.getBorderIcon(), fillBlackList);
        }

        if(guiType == GuiType.MAIN){
            addItem(buyIcon.getSlot(), getBuyIcon(buyIcon.getIcon()));
        }
    }

    public Icon getBuyIcon(Icon icon) {
        icon.onClick(e -> {
            e.setCancelled(true);

            if(marketItemData == null){
                return;
            }

            async(() -> {
                Player player = (Player) e.getWhoClicked();
                MarketItemData mItemData = DataService.getMarketItemData(this.marketItemData.getId());

                if(mItemData == null){
                    String message = configGui.getMessage("item-sold-message");
                    player.sendMessage(ChatUtils.format(player, message));

                    if(guiType == GuiType.MAIN){
                        sync(() -> new MarketGui(player, CategoryService.getMain()).open());
                    }else {
                        LocalPlayerData playerLocalData = DataService.getPlayerLocalData(player);
                        sync(() -> new PlayerItemsGui(player, playerLocalData, 0).open());
                    }
                    return;
                }
                sync(() -> new BuyGui(player, mItemData, itemIcon).open());
            });
        });

        return icon;
    }

    public Icon getBackIcon(Icon icon) {
        icon.onClick(e -> {
            e.setCancelled(true);

            String search = SearchStorage.getSearch(player.getUniqueId());

            if(search != null){
                new MarketSearchGui(player, search).open();
                return;
            }

            if(guiType == GuiType.MAIN){
                new MarketGui(player, CategoryService.getMain()).open();
            }else {
                async(() -> {
                    Player player = (Player) e.getWhoClicked();
                    LocalPlayerData playerLocalData = DataService.getPlayerLocalData(player);

                    sync(() -> new PlayerItemsGui(player, playerLocalData, 0).open());
                });
            }
        });

        return icon;
    }
}
