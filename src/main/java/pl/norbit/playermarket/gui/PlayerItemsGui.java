package pl.norbit.playermarket.gui;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.pagination.PaginationManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.jetbrains.annotations.NotNull;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.model.PlayerData;
import pl.norbit.playermarket.economy.EconomyService;
import pl.norbit.playermarket.model.local.LocalPlayerData;
import pl.norbit.playermarket.model.local.LocalPlayerItem;
import pl.norbit.playermarket.utils.*;
import pl.norbit.playermarket.utils.gui.GuiIconUtil;
import pl.norbit.playermarket.utils.gui.IconType;

import java.util.ArrayList;
import java.util.List;

public class PlayerItemsGui extends Gui {

    private final PaginationManager pagination;
    private LocalPlayerData localData;

    private static final List<PlayerItemsGui> itemsGui = new ArrayList<>();

    static {
        TaskUtils.runTaskTimerAsynchronously(() -> itemsGui.forEach(PlayerItemsGui::updateTask), 6L, 8L);
    }

    public PlayerItemsGui(@NotNull Player player, LocalPlayerData lPlayerData) {
        super(player, "market-gui", ChatUtils.format("&8&lTwoje oferty"), 6);

        this.pagination = new PaginationManager(this);

        this.pagination.registerPageSlotsBetween(10, 16);
        this.pagination.registerPageSlotsBetween(19, 25);
        this.pagination.registerPageSlotsBetween(28, 34);

        this.localData = lPlayerData;

        updateCategory(lPlayerData.getPlayerOffers());
    }

    public void updateTask(){
        if(!isClosed()) updateCategory();
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        itemsGui.remove(this);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        this.pagination.update();

        addItem(4, getProfileIcon());

        addItem(46, GuiIconUtil.getPaginationItem(pagination, IconType.LEFT));

        addItem(52, GuiIconUtil.getPaginationItem(pagination, IconType.RIGHT));

        addItem(49, GuiIconUtil.getOpenGuItem(Material.BARRIER, "&b&lPowrót do sklepu", new MarketGui(player, Settings.CATEGORIES.get(0))));

        itemsGui.add(this);
    }

    private Icon getProfileIcon(){
        Icon icon = new Icon(Material.CHEST);
        PlayerData playerData = localData.getPlayerData();

        icon.setName(ChatUtils.format("&b&lTwoje konto"));
        icon.hideFlags();
        icon.appendLore("",
                ChatUtils.format("&6◆ &fWystawione przedmioty: &6" + playerData.getPlayerOffers().size()),
                ChatUtils.format("&d◆ &fSprzedane przedmioty: &d" + playerData.getSoldItems()),
                ChatUtils.format("&a$ &fDo odebrania: &a" + DoubleFormatter.format(playerData.getEarnedMoney())),
                "",
                ChatUtils.format("&d◆ &fSprzedane przedmioty ogólnie: &d" + DoubleFormatter.format(playerData.getTotalSoldItems())),
                ChatUtils.format("&a$ &fZarobione ogólnie: &a" + DoubleFormatter.format(playerData.getTotalEarnedMoney())),
                "",
                ChatUtils.format("&eKliknij aby odebrać!"));

        icon.onClick(e -> {
            e.setCancelled(true);

            double earnedMoney = playerData.getEarnedMoney();

            if(earnedMoney == 0) {
                player.sendMessage(ChatUtils.format("&cNie masz nic do odebrania!"));
                return;
            }

            player.sendMessage(ChatUtils.format("&fOdebrano &e" + DoubleFormatter.format(earnedMoney)));

            playerData.setEarnedMoney(0);
            playerData.setSoldItems(0);

            DataService.updatePlayerData(playerData);

            LocalPlayerData pLocalData = DataService.getPlayerLocalData(player);

            new PlayerItemsGui(player, pLocalData).open();

            EconomyService.deposit(player, earnedMoney);
        });
        return icon;
    }

    private void updateCategory(List<LocalPlayerItem> items) {
        this.pagination.getItems().clear();
        this.pagination.goFirstPage();

        items.forEach(item -> this.pagination.addItem(item.getIcon()));
    }
    private void updateCategory() {
        LocalPlayerData localData = DataService.getPlayerLocalData(player);
        this.localData = localData;

        this.pagination.getItems().clear();
        this.pagination.goFirstPage();

        localData.getPlayerOffers().forEach(item -> this.pagination.addItem(item.getIcon()));
        this.pagination.update();
    }
}
