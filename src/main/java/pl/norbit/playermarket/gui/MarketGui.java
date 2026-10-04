package pl.norbit.playermarket.gui;

import lombok.Getter;
import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.pagination.PaginationManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.cooldown.CooldownService;
import pl.norbit.playermarket.gui.template.GuiTemplate;
import pl.norbit.playermarket.gui.template.TemplateUtils;
import pl.norbit.playermarket.gui.utils.GuiPages;
import pl.norbit.playermarket.model.local.*;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.service.DialogService;
import pl.norbit.playermarket.service.MarketService;
import pl.norbit.playermarket.data.SearchStorage;
import pl.norbit.playermarket.utils.custom.CustomItemsUtils;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.gui.GuiUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static pl.norbit.playermarket.utils.TaskUtils.*;

public class MarketGui extends Gui {

    private final PaginationManager marketItems;
    private final PaginationManager borderPagination;

    private final Category category;
    private final ConfigGui configGui;

    private final GuiPages<LocalMarketItem> guiPages;

    @Getter
    private static final Map<UUID, Set<MarketGui>> viewers = new ConcurrentHashMap<>();

    public MarketGui(Player player, Category category) {
        super(player, "market-main-gui", "", Settings.getMarketGui().getSize());

        this.category = category;
        this.configGui = Settings.getMarketGui();

        GuiTemplate template = TemplateUtils.getTemplate(this, this.configGui.getLayout());

        this.marketItems = template.getMarketItemsPagination();

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

        String title = Settings.getMarketGui().getTitle().replace("{category}", category.getName());
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
                item -> item.getMarketItem(MarketItemType.MAIN),
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
        borderPagination.update();

        ConfigIcon profileIcon = configGui.getIcon("your-offers-icon");
        ConfigIcon searchIcon = configGui.getIcon("search-icon");
        ConfigIcon categoryIcon = configGui.getIcon("categories-icon");

        addItem(profileIcon.getSlot(), getProfileIcon(profileIcon.getIcon()));

        if(categoryIcon.isEnabled()){
            addItem(categoryIcon.getSlot(), getCategoryIcon(categoryIcon, category));
        }

        if(searchIcon.isEnabled()){
            addItem(searchIcon.getSlot(), getSearchIcon(searchIcon.getIcon()));
        }

        guiPages.update();

        setClosed(false);

        SearchStorage.clear(player.getUniqueId());
        update();
        viewers.computeIfAbsent(category.getCategoryUUID(), k -> ConcurrentHashMap.newKeySet()).add(this);
    }

    private Icon getProfileIcon(Icon icon) {
        icon.hideFlags();

        icon.onClick(e -> {
            if (!CooldownService.tryClick(player.getUniqueId())) {
                player.closeInventory();
                player.sendMessage(ChatUtils.format(Settings.getCooldownMessage()));
                return;
            }

            DataService.getPlayerLocalData(player).thenAccept(data -> {
                sync(() -> new PlayerItemsGui(player, data, 0).open());
            });
        });

        return icon;
    }

    private Icon getSearchIcon(Icon icon) {
        icon.hideFlags();
        icon.onClick(e -> {
            if (!CooldownService.tryClick(player.getUniqueId())) {
                player.closeInventory();
                player.sendMessage(ChatUtils.format(Settings.getCooldownMessage()));
                return;
            }
            DialogService.openSearch(player);
        });

        return icon;
    }

    private Icon getCategoryIcon(ConfigIcon categoryIcon, Category selectedCategory) {
        ItemStack itemStack = CustomItemsUtils.getItemStack(categoryIcon.getConfigId());

        if (itemStack == null) {
            Icon icon = new Icon(Material.BARRIER);
            icon.setName(ChatUtils.formatLegacy("&cInvalid item"));
            return icon;
        }
        List<Category> categories = Settings.getFilterCategories();
        List<Component> lore = getCategoryLore(categoryIcon, selectedCategory, categories);
        itemStack.lore(lore);

        Icon icon = new Icon(itemStack);

        icon.setName(categoryIcon.getName());
        icon.onClick(e -> {
            if (!CooldownService.tryClick(player.getUniqueId())) {
                player.closeInventory();
                player.sendMessage(ChatUtils.format(Settings.getCooldownMessage()));
                return;
            }

            if (categories.isEmpty()) {
                return;
            }

            Category nextCategory = getNextCategory(selectedCategory, categories);
            new MarketGui(player, nextCategory).open();
        });

        return icon;
    }

    private Category getNextCategory(Category selectedCategory, List<Category> categories) {
        int currentIndex = 0;

        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getCategoryUUID().equals(selectedCategory.getCategoryUUID())) {
                currentIndex = i;
                break;
            }
        }

        int nextIndex = (currentIndex + 1) % categories.size();
        return categories.get(nextIndex);
    }

    private List<Component> getCategoryLore(ConfigIcon categoryIcon, Category selectedCategory, List<Category> categories) {
        List<Component> lore = new ArrayList<>();

        for (String line : categoryIcon.getLore()) {

            if (line.equals("{categories}")) {
                for (Category cat : categories) {
                    boolean selected = selectedCategory != null
                            && cat.getCategoryUUID().equals(selectedCategory.getCategoryUUID());

                    String categoryLine = configGui.getMessage(selected ? "category-active" : "category-inactive")
                            .replace("{category}", cat.getName());

                    lore.add(ChatUtils.format(categoryLine));
                }

            } else {
                lore.add(ChatUtils.format(line));
            }
        }

        return lore;
    }
}