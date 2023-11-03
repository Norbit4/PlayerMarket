package pl.norbit.playermarket.gui;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.pagination.PaginationManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitRunnable;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.model.local.Category;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.model.local.LocalMarketItem;
import pl.norbit.playermarket.model.local.LocalPlayerData;
import pl.norbit.playermarket.service.MarketService;
import pl.norbit.playermarket.utils.ChatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MarketGui extends Gui {
    private final PaginationManager marketItemsPagination;
    private final PaginationManager categoriesPagination;
    private final Category category;

    private static final List<MarketGui> marketGuis = new ArrayList<>();

    static {
        new BukkitRunnable() {
            @Override
            public void run() {
                marketGuis.forEach(MarketGui::updateTask);
            }
        }.runTaskTimerAsynchronously(PlayerMarket.getInstance(), 6, 6);
    }

    public MarketGui(Player player, Category category) {
        super(player, "market-gui", ChatUtils.format("&8&lMarket - " + category.getName()), 6);

        this.category = category;

        //item slots
        this.marketItemsPagination = new PaginationManager(this);

        this.marketItemsPagination.registerPageSlotsBetween(3, 8);
        this.marketItemsPagination.registerPageSlotsBetween(12, 17);
        this.marketItemsPagination.registerPageSlotsBetween(21, 26);

        //category slots
        this.categoriesPagination = new PaginationManager(this);

        this.categoriesPagination.registerPageSlotsBetween(0, 1);
        this.categoriesPagination.registerPageSlotsBetween(9, 10);
        this.categoriesPagination.registerPageSlotsBetween(18, 19);
        this.categoriesPagination.registerPageSlotsBetween(27, 28);
        this.categoriesPagination.registerPageSlotsBetween(36, 37);

        updateCategory(category);

        Settings.CATEGORIES.stream().map(this::createCategory).forEach(this.categoriesPagination::addItem);
    }


    public void updateTask(){
        if(!isClosed()) updatePage(category);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        marketGuis.remove(this);
    }

    private void updatePage(Category category){
        this.marketItemsPagination.getItems().clear();

        Set<LocalMarketItem> icons = MarketService.getIcons(category.getCategoryUUID());
        if(icons != null) icons.forEach(item -> this.marketItemsPagination.addItem(item.getMarketItem()));

        this.marketItemsPagination.update();
    }

    private void updateCategory(Category category) {
        this.marketItemsPagination.getItems().clear();
        this.marketItemsPagination.goFirstPage();

        Set<LocalMarketItem> icons = MarketService.getIcons(category.getCategoryUUID());
        if(icons != null) icons.forEach(item -> this.marketItemsPagination.addItem(item.getMarketItem()));
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

        this.marketItemsPagination.update();
        this.categoriesPagination.update();

        marketGuis.add(this);

        buildGui();
    }

    public void buildGui(){
        //black border
        addItem(2, new Icon(Material.BLACK_STAINED_GLASS_PANE).setName(" "));
        addItem(11, new Icon(Material.BLACK_STAINED_GLASS_PANE).setName(" "));
        addItem(20, new Icon(Material.BLACK_STAINED_GLASS_PANE).setName(" "));
        addItem(29, new Icon(Material.BLACK_STAINED_GLASS_PANE).setName(" "));
        addItem(38, new Icon(Material.BLACK_STAINED_GLASS_PANE).setName(" "));
        addItem(39, new Icon(Material.BLACK_STAINED_GLASS_PANE).setName(" "));
        addItem(40, new Icon(Material.BLACK_STAINED_GLASS_PANE).setName(" "));
        addItem(41, new Icon(Material.BLACK_STAINED_GLASS_PANE).setName(" "));
        addItem(42, new Icon(Material.BLACK_STAINED_GLASS_PANE).setName(" "));
        addItem(43, new Icon(Material.BLACK_STAINED_GLASS_PANE).setName(" "));
        addItem(44, new Icon(Material.BLACK_STAINED_GLASS_PANE).setName(" "));

        Icon aRight = new Icon(Material.ARROW);

        aRight.setName(ChatUtils.format("&e&lNastępna strona"));
        aRight.hideFlags();
        aRight.onClick(e -> {
            e.setCancelled(true);
            this.marketItemsPagination.goNextPage();
            this.marketItemsPagination.update();
        });

        Icon aLeft = new Icon(Material.ARROW);

        aLeft.setName(ChatUtils.format("&e&lPoprzednia strona"));
        aLeft.hideFlags();
        aLeft.onClick(e -> {
            e.setCancelled(true);
            this.marketItemsPagination.goPreviousPage();
            this.marketItemsPagination.update();
        });

        Icon profile = new Icon(Material.CHEST);

        profile.setName(ChatUtils.format("&b&lTwoje oferty"));
        profile.hideFlags();
        profile.onClick(e -> {
            e.setCancelled(true);

            LocalPlayerData playerLocalData = DataService.getPlayerLocalData(player);

            new PlayerItemsGui(player, playerLocalData).open();
        });

        addItem(47, aLeft);
        addItem(53, aRight);
        addItem(50, profile);
    }

    private Icon createCategory(Category category) {
        Icon icon = new Icon(category.getIcon());

        boolean isSel = category.getCategoryUUID().equals(this.category.getCategoryUUID());

        String sel = isSel ? "&aWybrano!" : "&eKliknij aby zobaczyc oferty!";

        icon.setName(ChatUtils.format("&a&l" + category.getName()));
        icon.setLore("", ChatUtils.format(sel));
        icon.hideFlags();

        if(isSel) icon.enchant(Enchantment.DURABILITY);

        if(!isSel) icon.onClick(e -> {
            e.setCancelled(true);

            new MarketGui(player, category).open();
        });
        return icon;
    }
}
