package pl.norbit.playermarket.gui;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.pagination.PaginationManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.model.PlayerData;
import pl.norbit.playermarket.economy.EconomyService;
import pl.norbit.playermarket.model.local.LocalPlayerData;
import pl.norbit.playermarket.model.local.LocalPlayerItem;
import pl.norbit.playermarket.utils.ChatUtils;
import pl.norbit.playermarket.utils.DoubleFormatter;

import java.util.ArrayList;
import java.util.List;

public class PlayerItemsGui extends Gui {

    private final PaginationManager pagination;
    private final Player p;
    private LocalPlayerData localData;

    private static final List<PlayerItemsGui> itemsGui = new ArrayList<>();

    static {
        new BukkitRunnable() {
            @Override
            public void run() {
                itemsGui.forEach(PlayerItemsGui::updateTask);
            }
        }.runTaskTimerAsynchronously(PlayerMarket.getInstance(), 8, 8);
    }

    public PlayerItemsGui(@NotNull Player player, LocalPlayerData lPlayerData) {
        super(player, "market-gui", ChatUtils.format("&l&8Twoje oferty"), 6);
        this.pagination = new PaginationManager(this);
        this.pagination.registerPageSlotsBetween(10, 16);
        this.pagination.registerPageSlotsBetween(19, 25);
        this.pagination.registerPageSlotsBetween(28, 34);
        this.p = player;
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
        Player p = (Player)event.getPlayer();

        this.pagination.update();
        itemsGui.add(this);

        Icon aRight = new Icon(Material.ARROW);

        aRight.setName(ChatUtils.format("&e&lNastępna strona"));
        aRight.hideFlags();
        aRight.onClick(e -> {
            e.setCancelled(true);
            this.pagination.goNextPage();
            this.pagination.update();
        });

        Icon aLeft = new Icon(Material.ARROW);

        aLeft.setName(ChatUtils.format("&e&lPoprzednia strona"));
        aLeft.hideFlags();
        aLeft.onClick(e -> {
            e.setCancelled(true);
            this.pagination.goPreviousPage();
            this.pagination.update();
        });

        Icon profile = new Icon(Material.BARRIER);

        profile.setName(ChatUtils.format("&b&lPowrót do sklepu"));
        profile.hideFlags();
        profile.onClick(e -> {
            e.setCancelled(true);
            new MarketGui(p, Settings.CATEGORIES.get(0)).open();
        });

        Icon currency = new Icon(Material.CHEST);
        PlayerData playerData = localData.getPlayerData();

        currency.setName(ChatUtils.format("&b&lTwoje konto"));
        currency.hideFlags();
        currency.appendLore("",
                ChatUtils.format("&6◆ &fWystawione przedmioty: &6" + playerData.getPlayerOffers().size()),
                ChatUtils.format("&d◆ &fSprzedane przedmioty: &d" + playerData.getSoldItems()),
                ChatUtils.format("&a$ &fDo odebrania: &a" + DoubleFormatter.format(playerData.getEarnedMoney())),
                "",
                ChatUtils.format("&d◆ &fSprzedane przedmioty ogólnie: &d" + DoubleFormatter.format(playerData.getTotalSoldItems())),
                ChatUtils.format("&a$ &fZarobione ogólnie: &a" + DoubleFormatter.format(playerData.getTotalEarnedMoney())),
                "",
                ChatUtils.format("&eKliknij aby odebrać!"));

        currency.onClick(e -> {
            e.setCancelled(true);

            double earnedMoney = playerData.getEarnedMoney();

            if(earnedMoney == 0) {
                p.sendMessage(ChatUtils.format("&cNie masz nic do odebrania!"));
                return;
            }

            p.sendMessage(ChatUtils.format("&fOdebrano &e" + DoubleFormatter.format(earnedMoney)));

            playerData.setEarnedMoney(0);
            playerData.setSoldItems(0);

            DataService.updatePlayerData(playerData);

            LocalPlayerData pLocalData = DataService.getPlayerLocalData(p);

            new PlayerItemsGui(p, pLocalData).open();

            EconomyService.deposit(p, earnedMoney);
        });

        addItem(4, currency);

        addItem(46, aLeft);
        addItem(52, aRight);

        addItem(49, profile);
    }

    private void updateCategory(List<LocalPlayerItem> items) {
        this.pagination.getItems().clear();
        this.pagination.goFirstPage();

        items.forEach(item -> this.pagination.addItem(item.getIcon()));
    }
    private void updateCategory() {
        LocalPlayerData localData = DataService.getPlayerLocalData(p);
        this.localData = localData;

        this.pagination.getItems().clear();
        this.pagination.goFirstPage();

        localData.getPlayerOffers().forEach(item -> this.pagination.addItem(item.getIcon()));
        this.pagination.update();
    }
}
