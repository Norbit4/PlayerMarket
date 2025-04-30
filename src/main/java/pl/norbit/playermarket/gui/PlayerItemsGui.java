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
import pl.norbit.playermarket.gui.template.GuiTemplate;
import pl.norbit.playermarket.gui.template.TemplateUtils;
import pl.norbit.playermarket.model.PlayerData;
import pl.norbit.playermarket.economy.EconomyService;
import pl.norbit.playermarket.model.local.ConfigGui;
import pl.norbit.playermarket.model.local.ConfigIcon;
import pl.norbit.playermarket.model.local.LocalPlayerData;
import pl.norbit.playermarket.model.local.LocalPlayerItem;
import pl.norbit.playermarket.service.CategoryService;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.format.DoubleFormatter;
import pl.norbit.playermarket.utils.gui.GuiUtils;
import pl.norbit.playermarket.utils.pagination.GuiPages;
import pl.norbit.playermarket.utils.player.PermUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static pl.norbit.playermarket.utils.TaskUtils.*;

public class PlayerItemsGui extends Gui {

    private final PaginationManager itemsPagination;
    private final PaginationManager borderPagination;
    private LocalPlayerData localData;

    private final ConfigGui configGui;
    private boolean updateProgress;
    private final GuiPages guiPages;

    private static final Map<UUID, PlayerItemsGui> playersGui = new ConcurrentHashMap<>();

    static {
        asyncTimer(() -> playersGui.values().forEach(PlayerItemsGui::updateTask), 6L, 8L);
    }

    public PlayerItemsGui(@NotNull Player player, LocalPlayerData lPlayerData, int page) {
        super(player, "player-items-gui", "", Settings.OFFERS_GUI.getSize());

        this.configGui = Settings.OFFERS_GUI;
        this.updateProgress = false;

        GuiTemplate template = TemplateUtils.getTemplate(this, this.configGui.getLayout());

        this.itemsPagination = template.getMarketItemsPagination();
        this.localData = lPlayerData;

        if(this.configGui.isFill()){
            this.borderPagination = new PaginationManager(this);
        }else {
            this.borderPagination = template.getBorderPagination();
        }

        GuiUtils.loadBorder(this.configGui, this.borderPagination, this.configGui.getFillBlackList(), this.getSize());

        ConfigIcon left = configGui.getIcon("previous-page-icon");
        ConfigIcon right = configGui.getIcon("next-page-icon");

        ConfigIcon fill = configGui.getIcon("border-icon");

        Icon fillIcon = null;

        if(this.configGui.isFill()){
            fillIcon = fill.getIcon();
        }

        updateCategory(lPlayerData.getPlayerOffers(), page);

        this.guiPages = new GuiPages(this, Settings.OFFERS_GUI.getTitle(), itemsPagination, left.getSlot(),
                left.getIcon(), right.getSlot(), right.getIcon(), fillIcon);
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
        this.itemsPagination.update();
        this.guiPages.update();
        this.borderPagination.update();

        ConfigIcon profileIcon = configGui.getIcon("statistics-icon");
        ConfigIcon backIcon = configGui.getIcon("back-to-market-icon");

        addItem(profileIcon.getSlot(), getProfileIcon(profileIcon.getIcon()));
        addItem(backIcon.getSlot(), GuiUtils.getOpenGuItem(backIcon.getIcon(),
                new MarketGui(player, CategoryService.getMain())));

        setClosed(false);
        playersGui.compute(player.getUniqueId(), (k, v) -> this);
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
            e.setCancelled(true);

            Player p = (Player) e.getWhoClicked();

            if(CooldownService.isOnCooldown(p.getUniqueId())){
                p.sendMessage(ChatUtils.format(Settings.getCooldownMessage()));
                return;
            }
            CooldownService.updateCooldown(p.getUniqueId());

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

                sync(() -> new PlayerItemsGui(player, pLocalData, this.itemsPagination.getCurrentPage()).open());

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
        this.itemsPagination.getItems().clear();
        this.itemsPagination.goFirstPage();

        items.forEach(item -> this.itemsPagination.addItem(item.getIcon()));

        if(page < this.itemsPagination.getLastPage()) {
            this.itemsPagination.setPage(page);
        } else {
            this.itemsPagination.goLastPage();
        }
    }
    private void updateCategory() {
        LocalPlayerData localPlayerData = DataService.getPlayerLocalData(player);
        this.localData = localPlayerData;

        this.itemsPagination.getItems().clear();

        localPlayerData.getPlayerOffers()
                .stream()
                .filter(item -> !item.isRemoveProgress())
                .forEach(item -> this.itemsPagination.addItem(item.getIcon()));
        this.itemsPagination.update();
        this.guiPages.update();
    }
}
