package pl.norbit.playermarket.gui;

import lombok.Getter;
import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.pagination.PaginationManager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.gui.anvil.ItemTypeSearchGui;
import pl.norbit.playermarket.gui.template.GuiTemplate;
import pl.norbit.playermarket.gui.template.TemplateUtils;
import pl.norbit.playermarket.gui.utils.GuiPages;
import pl.norbit.playermarket.model.local.*;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.service.MarketService;
import pl.norbit.playermarket.service.SearchStorage;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.gui.GuiUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static pl.norbit.playermarket.utils.TaskUtils.*;

public class MarketGui extends Gui {

    private final PaginationManager marketItems;
    private final PaginationManager categoriesPagination;
    private final PaginationManager borderPagination;

    private final Category category;
    private final ConfigGui configGui;

    private final GuiPages<LocalMarketItem> guiPages;

    @Getter
    private static final Map<UUID, Set<MarketGui>> viewers = new ConcurrentHashMap<>();

    public MarketGui(Player player, Category category) {
        super(player, "market-main-gui", "", Settings.MARKET_GUI.getSize());

        this.category = category;
        this.configGui = Settings.MARKET_GUI;

        GuiTemplate template = TemplateUtils.getTemplate(this, this.configGui.getLayout());

        this.marketItems = template.getMarketItemsPagination();
        this.categoriesPagination = template.getCategoriesPagination();

        this.borderPagination = this.configGui.isFill()
                ? new PaginationManager(this)
                : template.getBorderPagination();

        GuiUtils.loadBorder(
                this.configGui,
                this.borderPagination,
                this.configGui.getFillBlackList(),
                this.getSize()
        );

        categoriesPagination.addItem(createCategory(Settings.ALL_CATEGORY));
        Settings.CATEGORIES.stream()
                .map(this::createCategory)
                .forEach(categoriesPagination::addItem);
        categoriesPagination.addItem(createCategory(Settings.OTHER_CATEGORY));

        ConfigIcon left = configGui.getIcon("previous-page-icon");
        ConfigIcon right = configGui.getIcon("next-page-icon");
        ConfigIcon fill = configGui.getIcon("border-icon");

        Icon fillIcon = configGui.isFill() ? fill.getIcon() : null;

        String title = ChatUtils.format(player,
                Settings.MARKET_GUI.getTitle().replace("{CATEGORY}", category.getName())
        );
        int size = MarketService.getIcons(category).size();

        this.guiPages = new GuiPages<>(
                this,
                title,
                marketItems,
                left.getSlot(),
                left.getIcon(),
                right.getSlot(),
                right.getIcon(),
                fillIcon
        );
        this.guiPages.initUpdateTitle(size);
    }

    public void update() {
        if (isClosed()) return;

        List<LocalMarketItem> items = MarketService.getIcons(category);

        if (items == null) return;

        guiPages.updateItems(
                items,
                LocalMarketItem::getMarketItem,
                new GuiPages.HashProvider<>() {
                    public int hash(LocalMarketItem item) {
                        return Objects.hash(item.getId(), item.getOfferDate());
                    }

                    public boolean equals(LocalMarketItem a, LocalMarketItem b) {
                        return a.getId().equals(b.getId())
                                && a.getOfferDate() == b.getOfferDate();
                    }
                }
        );
    }

    public void onItemAdded() {
        update();
    }

    @Override
    public void onClose(InventoryCloseEvent e) {
        Set<MarketGui> set = viewers.get(category.getCategoryUUID());

        if (set != null) {

            set.remove(this);

            if (set.isEmpty()) {
                viewers.remove(category.getCategoryUUID());
            }
        }
    }

    @Override
    public void onOpen(InventoryOpenEvent e) {
        marketItems.update();
        categoriesPagination.update();
        borderPagination.update();

        ConfigIcon profileIcon = configGui.getIcon("your-offers-icon");
        ConfigIcon searchIcon = configGui.getIcon("search-icon");

        addItem(profileIcon.getSlot(), getProfileIcon(profileIcon.getIcon()));
        addItem(searchIcon.getSlot(), getSearchIcon(searchIcon.getIcon()));

        guiPages.update();

        setClosed(false);

        SearchStorage.clear(player.getUniqueId());
        update();
        viewers
                .computeIfAbsent(category.getCategoryUUID(), k -> ConcurrentHashMap.newKeySet())
                .add(this);
    }

    private Icon getProfileIcon(Icon icon) {
        icon.hideFlags();

        icon.onClick(e -> {

            Player p = (Player) e.getWhoClicked();

            DataService.getPlayerLocalData(p).thenAccept(data -> {
                sync(() -> new PlayerItemsGui(p, data, 0).open());
            });
        });

        return icon;
    }

    private Icon getSearchIcon(Icon icon) {

        icon.hideFlags();
        icon.onClick(e -> ItemTypeSearchGui.open(player));

        return icon;
    }

    private Icon createCategory(Category category) {

        Icon icon = new Icon(category.getIcon());

        boolean selected = category.getCategoryUUID().equals(this.category.getCategoryUUID());

        icon.setName(ChatUtils.format(
                player,
                Settings.CATEGORY_NAME_FORMAT.replace("{CATEGORY}", category.getName())
        ));

        icon.hideFlags();

        if (selected) {

            icon.setLore(Settings.CATEGORY_SELECTED_LORE.stream()
                    .map(ChatUtils::format)
                    .collect(Collectors.toList()));

            icon.enchant(Enchantment.DURABILITY);

            return icon;
        }

        icon.setLore(category.getLore().stream()
                .map(line -> ChatUtils.format(player, line))
                .collect(Collectors.toList()));

        icon.onClick(e -> {

            e.setCancelled(true);

            new MarketGui(player, category).open();
        });

        return icon;
    }
}