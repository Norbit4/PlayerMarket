package pl.norbit.playermarket.gui;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.pagination.PaginationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.jetbrains.annotations.NotNull;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.model.PlayerData;
import pl.norbit.playermarket.economy.EconomyService;
import pl.norbit.playermarket.model.local.ConfigGui;
import pl.norbit.playermarket.model.local.LocalPlayerData;
import pl.norbit.playermarket.model.local.LocalPlayerItem;
import pl.norbit.playermarket.service.CategoryService;
import pl.norbit.playermarket.utils.*;
import pl.norbit.playermarket.utils.gui.GuiIconUtil;
import pl.norbit.playermarket.utils.gui.IconType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerItemsGui extends Gui {

    private final PaginationManager pagination;
    private LocalPlayerData localData;

    private final ConfigGui configGui;

    private static final List<PlayerItemsGui> itemsGui = new ArrayList<>();

    static {
        TaskUtils.runTaskTimerAsynchronously(() -> itemsGui.forEach(PlayerItemsGui::updateTask), 6L, 8L);
    }

    public PlayerItemsGui(@NotNull Player player, LocalPlayerData lPlayerData, int page) {
        super(player, "market-gui", ChatUtils.format(player, Settings.OFFERS_GUI.getTitle()), 6);

        this.configGui = Settings.OFFERS_GUI;

        this.pagination = new PaginationManager(this);

        this.pagination.registerPageSlotsBetween(10, 16);
        this.pagination.registerPageSlotsBetween(19, 25);
        this.pagination.registerPageSlotsBetween(28, 34);

        this.localData = lPlayerData;

        updateCategory(lPlayerData.getPlayerOffers(), page);
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

        addItem(46,GuiIconUtil.getPaginationItem(pagination,
                IconType.LEFT,
                configGui.getIcon("previous-page-icon")));

        addItem(49, GuiIconUtil.getOpenGuItem(configGui.getIcon("back-to-market-icon"),
                new MarketGui(player, CategoryService.getMain())));

        addItem(52,GuiIconUtil.getPaginationItem(pagination,
                IconType.RIGHT,
                configGui.getIcon("next-page-icon")));

        itemsGui.add(this);
    }

    private Icon getProfileIcon(){
        PlayerData playerData = localData.getPlayerData();
        Icon icon = configGui.getIcon("statistics-icon");

        icon.setLore(icon.getItem()
                .getLore()
                .stream()
                .map(l -> formatLine(l, playerData))
                .collect(Collectors.toList()));
        icon.hideFlags();


        icon.onClick(e -> {
            e.setCancelled(true);

            double earnedMoney = playerData.getEarnedMoney();

            if(earnedMoney == 0) {
                player.sendMessage(ChatUtils.format(player, configGui.getMessage("nothing-to-get-message")));
                return;
            }

            player.sendMessage(ChatUtils.format(
                    player, configGui.getMessage("success-message")
                            .replace("{MONEY}", DoubleFormatter.format(earnedMoney))));

            playerData.setEarnedMoney(0);
            playerData.setSoldItems(0);

            DataService.updatePlayerData(playerData);

            LocalPlayerData pLocalData = DataService.getPlayerLocalData(player);

            new PlayerItemsGui(player, pLocalData, this.pagination.getCurrentPage()).open();

            EconomyService.deposit(player, earnedMoney);
        });
        return icon;
    }

    private String formatLine(String line, PlayerData playerData){
        int amount = PermUtils.getAmount(player, Settings.OFFER_COMMAND_LIMIT_PERMISSION, Settings.OFFER_COMMAND_DEFAULT_LIMIT);

        return ChatUtils.format(player,
                line.replace("{OFFERS}", String.valueOf(playerData.getPlayerOffers().size()))
                        .replace("{OFFERS_LIMIT}", String.valueOf(amount))
                        .replace("{SOLD}", String.valueOf(playerData.getSoldItems()))
                        .replace("{MONEY_EARNED}", DoubleFormatter.format(playerData.getEarnedMoney()))
                        .replace("{ALL_SOLD}", String.valueOf(playerData.getTotalSoldItems()))
                        .replace("{ALL_MONEY_EARNED}", DoubleFormatter.format(playerData.getTotalEarnedMoney()))
        );
    }

    private void updateCategory(List<LocalPlayerItem> items, int page) {
        this.pagination.getItems().clear();
        this.pagination.goFirstPage();

        items.forEach(item -> this.pagination.addItem(item.getIcon()));

        if(page < this.pagination.getLastPage()) {
            this.pagination.setPage(page);
        } else {
            this.pagination.goLastPage();
        }
    }
    private void updateCategory() {
        LocalPlayerData localData = DataService.getPlayerLocalData(player);
        this.localData = localData;

        this.pagination.getItems().clear();

        localData.getPlayerOffers().forEach(item -> this.pagination.addItem(item.getIcon()));
        this.pagination.update();
    }
}
