package pl.norbit.playermarket.gui;

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
import pl.norbit.playermarket.model.local.*;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.service.MarketService;
import pl.norbit.playermarket.service.SearchStorage;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.gui.GuiUtils;
import pl.norbit.playermarket.utils.pagination.GuiPages;

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
    private final GuiPages guiPages;

    private final List<Integer> visibleSlots = new ArrayList<>();
    private int itemsPerPage = 0;

    private List<LocalMarketItem> lastItems = null;
    private int lastViewedPage = -1;

    private static final Map<UUID, MarketGui> playersGui = new ConcurrentHashMap<>();

    static {
        asyncTimer(() -> playersGui.values().forEach(MarketGui::updateTask), 10L, 10L);
    }

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

        updateCategory(category);

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

        this.guiPages = new GuiPages(
                this,
                title,
                marketItems,
                left.getSlot(),
                left.getIcon(),
                right.getSlot(),
                right.getIcon(),
                fillIcon
        );
    }

    public void updateTask() {
        if (!isClosed()) {
            updatePage(category);
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        playersGui.remove(player.getUniqueId());
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

        marketItems.update();
        categoriesPagination.update();
        borderPagination.update();

        if (visibleSlots.isEmpty()) {
            visibleSlots.addAll(marketItems.getSlots());
            itemsPerPage = visibleSlots.size();
        }

        ConfigIcon profileIcon = configGui.getIcon("your-offers-icon");
        ConfigIcon searchIcon = configGui.getIcon("search-icon");

        addItem(profileIcon.getSlot(), getProfileIcon(profileIcon.getIcon()));
        addItem(searchIcon.getSlot(), getSearchIcon(searchIcon.getIcon()));

        guiPages.update();

        setClosed(false);
        playersGui.put(player.getUniqueId(), this);

        SearchStorage.clear(player.getUniqueId());
    }

    private void updateCategory(Category category) {
        lastItems = null;
        lastViewedPage = -1;

        marketItems.getItems().clear();
        marketItems.goFirstPage();

        List<LocalMarketItem> icons = MarketService.getIcons(category);
        if (icons != null) {
            icons.forEach(i -> marketItems.addItem(i.getMarketItem()));
        }
    }

    private void rebuildPagination(List<LocalMarketItem> list) {
        marketItems.getItems().clear();
        list.forEach(i -> marketItems.addItem(i.getMarketItem()));

        marketItems.update();
        guiPages.update();
    }

    private void updatePage(Category category) {
        List<LocalMarketItem> newItems = MarketService.getIcons(category);
        if (newItems == null) return;

        int currentPage = marketItems.getCurrentPage();
        int lastPage = marketItems.getLastPage();

        if (newItems.isEmpty()) {
            if (lastItems != null && lastItems.isEmpty()) {
                return;
            }
            marketItems.getItems().clear();
            marketItems.goFirstPage();
            marketItems.update();
            guiPages.update();
            lastItems = Collections.emptyList();
            lastViewedPage = 0;
            return;
        }

        if (currentPage != lastViewedPage) {
            lastViewedPage = currentPage;
            lastItems = new ArrayList<>(newItems);
            rebuildPagination(newItems);
            return;
        }

        if (lastItems == null || lastItems.size() != newItems.size()) {
            lastItems = new ArrayList<>(newItems);
            rebuildPagination(newItems);
            return;
        }

        if (currentPage > lastPage) {
            marketItems.goLastPage();
            marketItems.update();
            guiPages.update();
            lastViewedPage = marketItems.getCurrentPage();
            return;
        }

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, newItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            if (!equalsItem(lastItems.get(i), newItems.get(i))) {
                updateSingleSlot(i, newItems.get(i));
            }
        }

        lastItems = new ArrayList<>(newItems);
    }

    private void updateSingleSlot(int globalIndex, LocalMarketItem newItem) {
        int currentPage = marketItems.getCurrentPage();
        int localIndex = globalIndex - (currentPage * itemsPerPage);

        if (localIndex < 0 || localIndex >= visibleSlots.size()) return;

        int slot = visibleSlots.get(localIndex);
        addItem(slot, newItem.getMarketItem());
    }

    private boolean equalsItem(LocalMarketItem a, LocalMarketItem b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.getId().equals(b.getId()) && a.getOfferDate() == b.getOfferDate();
    }

    private Icon getProfileIcon(Icon icon) {
        icon.hideFlags();
        icon.onClick(e -> {
            e.setCancelled(true);
            async(() -> {
                Player p = (Player) e.getWhoClicked();
                LocalPlayerData data = DataService.getPlayerLocalData(p);
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
