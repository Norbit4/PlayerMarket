package pl.norbit.playermarket.gui;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.pagination.PaginationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.jetbrains.annotations.NotNull;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.cooldown.CooldownService;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.economy.EconomyService;
import pl.norbit.playermarket.gui.template.GuiTemplate;
import pl.norbit.playermarket.gui.template.TemplateUtils;
import pl.norbit.playermarket.gui.utils.GuiPages;
import pl.norbit.playermarket.model.PlayerData;
import pl.norbit.playermarket.model.local.*;
import pl.norbit.playermarket.service.CategoryService;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.format.DoubleFormatter;
import pl.norbit.playermarket.utils.gui.GuiUtils;
import pl.norbit.playermarket.utils.player.PermUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static pl.norbit.playermarket.utils.TaskUtils.*;

public class PlayerItemsGui extends Gui {

    private final PaginationManager itemsPagination;
    private final PaginationManager borderPagination;

    private final ConfigGui configGui;
    private final GuiPages<LocalPlayerItem> guiPages;

    private LocalPlayerData localData;
    private boolean updateProgress;

    private static final Map<UUID, PlayerItemsGui> playersGui = new ConcurrentHashMap<>();

    public static void updateAll(){
        for (PlayerItemsGui value : playersGui.values()) {
            value.updateTask();
        }
    }

    public PlayerItemsGui(@NotNull Player player, LocalPlayerData lPlayerData, int page) {
        super(player, "player-items-gui", "", Settings.OFFERS_GUI.getSize());

        this.configGui = Settings.OFFERS_GUI;
        this.updateProgress = false;
        this.localData = lPlayerData;

        GuiTemplate template = TemplateUtils.getTemplate(this, this.configGui.getLayout());

        this.itemsPagination = template.getMarketItemsPagination();

        this.borderPagination = this.configGui.isFill()
                ? new PaginationManager(this)
                : template.getBorderPagination();

        GuiUtils.loadBorder(
                this.configGui,
                this.borderPagination,
                this.configGui.getFillBlackList(),
                this.getSize()
        );

        ConfigIcon left = configGui.getIcon("previous-page-icon");
        ConfigIcon right = configGui.getIcon("next-page-icon");
        ConfigIcon fill = configGui.getIcon("border-icon");

        Icon fillIcon = configGui.isFill() ? fill.getIcon() : null;

        this.guiPages = new GuiPages<>(
                this,
                Settings.OFFERS_GUI.getTitle(),
                itemsPagination,
                left.getSlot(),
                left.getIcon(),
                right.getSlot(),
                right.getIcon(),
                fillIcon
        );

        updateCategory(lPlayerData.getPlayerOffers(), page);

        this.guiPages.initUpdateTitle();
    }

    private void updateTask(){
        if(updateProgress) return;
        if(isClosed()) return;

        DataService.getPlayerLocalData(player).thenAccept(localPlayerData -> {

            List<LocalPlayerItem> items = localPlayerData.getPlayerOffers()
                    .stream()
                    .filter(item -> !item.isRemoveProgress())
                    .collect(Collectors.toList());

            guiPages.updateItems(
                    items,
                    LocalPlayerItem::getIcon,
                    new GuiPages.HashProvider<>() {

                        public int hash(LocalPlayerItem item) {
                            return Objects.hash(item.getId(), item.isRemoveProgress());
                        }

                        public boolean equals(LocalPlayerItem a, LocalPlayerItem b) {
                            if (a == null || b == null) return false;

                            return a.getId().equals(b.getId())
                                    && a.isRemoveProgress() == b.isRemoveProgress();
                        }
                    }
            );
        });
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        playersGui.remove(player.getUniqueId());
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

        itemsPagination.update();
        guiPages.update();
        borderPagination.update();

        ConfigIcon profileIcon = configGui.getIcon("statistics-icon");
        ConfigIcon backIcon = configGui.getIcon("back-to-market-icon");

        addItem(profileIcon.getSlot(), getProfileIcon(profileIcon.getIcon()));

        addItem(
                backIcon.getSlot(),
                GuiUtils.getOpenGuItem(
                        backIcon.getIcon(),
                        new MarketGui(player, CategoryService.getMain())
                )
        );

        setClosed(false);
        playersGui.put(player.getUniqueId(), this);
    }

    private Icon getProfileIcon(Icon icon){
        PlayerData playerData = localData.getPlayerData();

        icon.setLore(icon.getItem()
                .getItemMeta()
                .getLore()
                .stream()
                .map(l -> formatLine(l, playerData))
                .collect(Collectors.toList()));

        icon.hideFlags();

        icon.onClick(e -> {
            Player p = (Player) e.getWhoClicked();

            if(CooldownService.isOnCooldown(p.getUniqueId())){
                p.sendMessage(ChatUtils.format(Settings.getCooldownMessage()));
                return;
            }

            CooldownService.updateCooldown(p.getUniqueId());

            double earnedMoney = playerData.getEarnedMoney();

            if(earnedMoney == 0) {
                player.sendMessage(ChatUtils.format(
                        player,
                        configGui.getMessage("nothing-to-get-message")
                ));
                return;
            }

            player.sendMessage(ChatUtils.format(
                    player,
                    configGui.getMessage("success-message")
                            .replace("{MONEY}", DoubleFormatter.format(earnedMoney))
            ));

            if(updateProgress) return;

            updateProgress = true;

            playerData.setEarnedMoney(0);
            playerData.setSoldItems(0);

            DataService.updatePlayerData(playerData);

            DataService.getPlayerLocalData(player).thenAccept(pLocalData -> {
                sync(() -> {

                    new PlayerItemsGui(
                            player,
                            pLocalData,
                            itemsPagination.getCurrentPage()
                    ).open();

                    EconomyService.deposit(player, earnedMoney);
                });
            });
        });

        return icon;
    }

    private String formatLine(String line, PlayerData playerData){
        int amount = PermUtils.getAmount(
                player,
                Settings.OFFER_COMMAND_LIMIT_PERMISSION,
                Settings.OFFER_COMMAND_DEFAULT_LIMIT
        );

        return ChatUtils.format(
                player,
                line.replace("{OFFERS}", String.valueOf(playerData.getPlayerOffers().size()))
                        .replace("{OFFERS_LIMIT}", String.valueOf(amount))
                        .replace("{SOLD}", String.valueOf(playerData.getSoldItems()))
                        .replace("{MONEY_EARNED}", DoubleFormatter.format(playerData.getEarnedMoney()))
                        .replace("{ALL_SOLD}", String.valueOf(playerData.getTotalSoldItems()))
                        .replace("{ALL_MONEY_EARNED}", DoubleFormatter.format(playerData.getTotalEarnedMoney()))
        );
    }

    private void updateCategory(List<LocalPlayerItem> items, int page) {
        itemsPagination.getItems().clear();
        itemsPagination.goFirstPage();

        items.forEach(item -> itemsPagination.addItem(item.getIcon()));

        if(page < itemsPagination.getLastPage()) {
            itemsPagination.setPage(page);
        } else {
            itemsPagination.goLastPage();
        }
    }
}