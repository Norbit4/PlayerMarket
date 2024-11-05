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
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.format.DoubleFormatter;
import pl.norbit.playermarket.utils.gui.GuiIconUtil;
import pl.norbit.playermarket.utils.pagination.GuiPages;
import pl.norbit.playermarket.utils.player.PermUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static pl.norbit.playermarket.utils.TaskUtils.*;

public class PlayerItemsGui extends Gui {

    private final PaginationManager pagination;
    private LocalPlayerData localData;

    private final ConfigGui configGui;
    private boolean updateProgress;
    private final GuiPages guiPages;

    private static final Map<UUID, PlayerItemsGui> playersGui = new ConcurrentHashMap<>();

    static {
        asyncTimer(() -> playersGui.values().forEach(PlayerItemsGui::updateTask), 6L, 8L);
    }

    public PlayerItemsGui(@NotNull Player player, LocalPlayerData lPlayerData, int page) {
        super(player, "market-gui", "", 6);

        this.configGui = Settings.OFFERS_GUI;
        this.updateProgress = false;

        this.pagination = new PaginationManager(this);

        this.pagination.registerPageSlotsBetween(10, 16);
        this.pagination.registerPageSlotsBetween(19, 25);
        this.pagination.registerPageSlotsBetween(28, 34);

        this.localData = lPlayerData;

        Icon left = configGui.getIcon("previous-page-icon");
        Icon right = configGui.getIcon("next-page-icon");

        updateCategory(lPlayerData.getPlayerOffers(), page);

        this.guiPages = new GuiPages(this, Settings.OFFERS_GUI.getTitle(), pagination, 46, left, 52, right);
    }

    public void updateTask(){
        if(updateProgress) {
            return;
        }

        if(!isClosed()) {
            updateCategory();
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        playersGui.remove(player.getUniqueId());
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        this.pagination.update();
        this.guiPages.update();

        addItem(4, getProfileIcon());

        addItem(49, GuiIconUtil.getOpenGuItem(configGui.getIcon("back-to-market-icon"),
                new MarketGui(player, CategoryService.getMain())));

        setClosed(false);
        playersGui.compute(player.getUniqueId(), (k, v) -> this);
    }

    private Icon getProfileIcon(){
        PlayerData playerData = localData.getPlayerData();
        Icon icon = configGui.getIcon("statistics-icon");

        icon.setLore(icon.getItem()
                .getItemMeta()
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

            if(this.updateProgress) {
                return;
            }
            this.updateProgress = true;

            async(() -> {
                playerData.setEarnedMoney(0);
                playerData.setSoldItems(0);

                DataService.updatePlayerData(playerData);

                LocalPlayerData pLocalData = DataService.getPlayerLocalData(player);

                sync(() -> new PlayerItemsGui(player, pLocalData, this.pagination.getCurrentPage()).open());

                EconomyService.deposit(player, earnedMoney);
            });
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

        localData.getPlayerOffers()
                .stream()
                .filter(item -> !item.isRemoveProgress())
                .forEach(item -> this.pagination.addItem(item.getIcon()));
        this.pagination.update();
        this.guiPages.update();
    }
}
