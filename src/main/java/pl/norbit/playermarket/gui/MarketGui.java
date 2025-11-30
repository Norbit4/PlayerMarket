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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static pl.norbit.playermarket.utils.TaskUtils.*;

public class MarketGui extends Gui {
    private final PaginationManager marketItems;
    private final PaginationManager categoriesPagination;
    private final PaginationManager borderPagination;
    private final Category category;
    private List<LocalMarketItem> lastItems = null;
    private final List<Integer> visibleSlots = new ArrayList<>();
    private int itemsPerPage = 0;

    private final ConfigGui configGui;
    private final GuiPages guiPages;

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

        if(this.configGui.isFill()){
            this.borderPagination = new PaginationManager(this);
        }else {
            this.borderPagination = template.getBorderPagination();
        }

        updateCategory(category);

        GuiUtils.loadBorder(this.configGui, this.borderPagination, this.configGui.getFillBlackList(), this.getSize());

        //add categories
        this.categoriesPagination.addItem(createCategory(Settings.ALL_CATEGORY));

        Settings.CATEGORIES.stream()
                .map(this::createCategory)
                .forEach(this.categoriesPagination::addItem);

        this.categoriesPagination.addItem(createCategory(Settings.OTHER_CATEGORY));

        ConfigIcon left = configGui.getIcon("previous-page-icon");
        ConfigIcon right = configGui.getIcon("next-page-icon");
        ConfigIcon fill = configGui.getIcon("border-icon");

        Icon fillIcon = null;

        if(this.configGui.isFill()){
            fillIcon = fill.getIcon();
        }

        String title = ChatUtils.format(player, Settings.MARKET_GUI.getTitle()
                .replace("{CATEGORY}", category.getName()));

        this.guiPages = new GuiPages(this, title, marketItems, left.getSlot(), left.getIcon(),
                right.getSlot(), right.getIcon(), fillIcon);
    }

    public void updateTask(){
        if(!isClosed()) {
            updatePage(category);
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        playersGui.remove(player.getUniqueId());
    }

//    private void updatePage(Category category){
//        this.marketItems.getItems().clear();
//
//        List<LocalMarketItem> icons = MarketService.getIcons(category);
//        if(icons != null){
//            icons.forEach(item -> this.marketItems.addItem(item.getMarketItem()));
//        }
//
//        this.marketItems.update();
//        this.guiPages.update();
//    }

    private void updateCategory(Category category) {
        this.marketItems.getItems().clear();
        this.marketItems.goFirstPage();

        List<LocalMarketItem> icons = MarketService.getIcons(category);
        if(icons != null){
            icons.forEach(item -> this.marketItems.addItem(item.getMarketItem()));
        }
    }

    private void rebuildPagination(List<LocalMarketItem> list) {
        marketItems.getItems().clear();
        list.forEach(i -> marketItems.addItem(i.getMarketItem()));

        marketItems.update();
        guiPages.update();

        // detect visible slots ONCE
        if (visibleSlots.isEmpty()) {
            visibleSlots.addAll(marketItems.getSlots());
            itemsPerPage = visibleSlots.size();
        }
    }

    private void updatePage(Category category) {
        List<LocalMarketItem> newItems = MarketService.getIcons(category);

        // first load
        if (lastItems == null) {
            lastItems = new ArrayList<>(newItems);
            rebuildPagination(newItems);
            return;
        }

        // item count changed -> rebuild
        if (newItems.size() != lastItems.size()) {
            lastItems = new ArrayList<>(newItems);
            rebuildPagination(newItems);
            return;
        }

        for (int i = 0; i < newItems.size(); i++) {
            LocalMarketItem oldItem = lastItems.get(i);
            LocalMarketItem newItem = newItems.get(i);

            if (!equalsItem(oldItem, newItem)) {
                updateSingleSlot(i, newItem);
            }
        }

        lastItems = new ArrayList<>(newItems);
    }


    private boolean equalsItem(LocalMarketItem a, LocalMarketItem b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.getId().equals(b.getId())
                && a.getOfferDate() == b.getOfferDate();
    }


    private void updateSingleSlot(int index, LocalMarketItem newItem) {
        if (visibleSlots.isEmpty()) return;
        if (index < 0 || index >= itemsPerPage) return;

        int slot = visibleSlots.get(index);
        Icon newIcon = newItem.getMarketItem();

        addItem(slot, newIcon);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        //update pagination
        this.marketItems.update();
        this.categoriesPagination.update();
        this.borderPagination.update();

        if (visibleSlots.isEmpty()) {
            visibleSlots.addAll(marketItems.getSlots());
            itemsPerPage = visibleSlots.size();
        }

        ConfigIcon profileIcon = configGui.getIcon("your-offers-icon");
        ConfigIcon searchIcon = configGui.getIcon("search-icon");

        addItem(profileIcon.getSlot(), getProfileIcon(profileIcon.getIcon()));
        addItem(searchIcon.getSlot(), getSearchIcon(searchIcon.getIcon()));

        this.guiPages.update();

        setClosed(false);
        playersGui.compute(player.getUniqueId(), (k, v) -> this);

        SearchStorage.clear(player.getUniqueId());
    }

    private Icon getProfileIcon(Icon icon){
        icon.hideFlags();
        icon.onClick(e -> {
            e.setCancelled(true);

            async(() -> {
                Player player = (Player) e.getWhoClicked();
                LocalPlayerData playerLocalData = DataService.getPlayerLocalData(player);

                sync(() -> new PlayerItemsGui(player, playerLocalData, 0).open());
            });
        });
        return icon;
    }

    private Icon createCategory(Category category) {
        Icon icon = new Icon(category.getIcon());

        boolean isSel = category.getCategoryUUID().equals(this.category.getCategoryUUID());

        icon.setName(ChatUtils.format(player, Settings.CATEGORY_NAME_FORMAT.
                replace("{CATEGORY}", category.getName())));

        icon.hideFlags();

        if(isSel){
            icon.setLore(Settings.CATEGORY_SELECTED_LORE
                    .stream()
                    .map(ChatUtils::format)
                    .collect(Collectors.toList()));

            icon.enchant(Enchantment.DURABILITY);

            return icon;
        }

        icon.setLore(category.getLore()
                .stream()
                .map(line -> ChatUtils.format(player, line))
                .collect(Collectors.toList()));

        icon.onClick(e -> {
            e.setCancelled(true);

            new MarketGui(player, category).open();
        });
        return icon;
    }

    private Icon getSearchIcon(Icon icon){
        icon.hideFlags();
        icon.onClick(e -> ItemTypeSearchGui.open(player));
        return icon;
    }
}