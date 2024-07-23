package pl.norbit.playermarket.gui;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.pagination.PaginationManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import pl.norbit.playermarket.gui.template.GuiTemplate;
import pl.norbit.playermarket.gui.template.TemplateService;
import pl.norbit.playermarket.model.local.Category;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.model.local.ConfigGui;
import pl.norbit.playermarket.model.local.LocalMarketItem;
import pl.norbit.playermarket.model.local.LocalPlayerData;
import pl.norbit.playermarket.service.MarketService;
import pl.norbit.playermarket.utils.ChatUtils;
import pl.norbit.playermarket.utils.gui.GuiIconUtil;
import pl.norbit.playermarket.utils.gui.IconType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static pl.norbit.playermarket.utils.TaskUtils.asyncTimer;

public class MarketGui extends Gui {
    private final PaginationManager marketItemsPagination;
    private final PaginationManager categoriesPagination;
    private final PaginationManager borderPagination;
    private final Category category;

    private final ConfigGui configGui;

    private static final List<MarketGui> marketGuis = new ArrayList<>();

    static {
        asyncTimer(() -> marketGuis.forEach(MarketGui::updateTask), 6L, 4L);
    }

    public MarketGui(Player player, Category category) {
        super(player, "market-gui",
                ChatUtils.format(player, Settings.MARKET_GUI.getTitle().replace("{CATEGORY}", category.getName())), 6);

        this.category = category;
        this.configGui = Settings.MARKET_GUI;

        GuiTemplate template = TemplateService.getTemplate(this);

        this.marketItemsPagination = template.getMarketItemsPagination();
        this.categoriesPagination = template.getCategoriesPagination();
        this.borderPagination = template.getBorderPagination();

        updateCategory(category);

        for (int i = 0; i < this.borderPagination.getSlots().size(); i++) {
            this.borderPagination.addItem(getBorderIcon());
        }

        //add categories
        this.categoriesPagination.addItem(createCategory(Settings.ALL_CATEGORY));

        Settings.CATEGORIES.stream().map(this::createCategory).forEach(this.categoriesPagination::addItem);

        this.categoriesPagination.addItem(createCategory(Settings.OTHER_CATEGORY));
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

        List<LocalMarketItem> icons = MarketService.getIcons(category);
        if(icons != null) icons.forEach(item -> this.marketItemsPagination.addItem(item.getMarketItem()));

        this.marketItemsPagination.update();
    }

    private void updateCategory(Category category) {
        this.marketItemsPagination.getItems().clear();
        this.marketItemsPagination.goFirstPage();

        List<LocalMarketItem> icons = MarketService.getIcons(category);
        if(icons != null) icons.forEach(item -> this.marketItemsPagination.addItem(item.getMarketItem()));
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

        //update pagination
        this.marketItemsPagination.update();
        this.categoriesPagination.update();
        this.borderPagination.update();

        addItem(47,GuiIconUtil.getPaginationItem(marketItemsPagination,
                IconType.LEFT,
                configGui.getIcon("previous-page-icon")));
        addItem(50, getProfileIcon());
        addItem(53,GuiIconUtil.getPaginationItem(marketItemsPagination,
                IconType.RIGHT,
                configGui.getIcon("next-page-icon")));

        marketGuis.add(this);
    }

    private Icon getProfileIcon(){
        Icon icon = configGui.getIcon("your-offers-icon");

        icon.hideFlags();
        icon.onClick(e -> {
            e.setCancelled(true);

            LocalPlayerData playerLocalData = DataService.getPlayerLocalData(player);

            new PlayerItemsGui(player, playerLocalData ,0).open();
        });
        return icon;
    }

    private Icon getBorderIcon(){
        return new Icon(Material.GRAY_STAINED_GLASS_PANE).setName(" ");
    }

    private Icon createCategory(Category category) {
        Icon icon = new Icon(category.getIcon());

        boolean isSel = category.getCategoryUUID().equals(this.category.getCategoryUUID());

        icon.setName(ChatUtils.format(player, Settings.CATEGORY_NAME_FORMAT.
                replace("{CATEGORY}", category.getName())));

        icon.hideFlags();

        if(isSel){
            icon.setLore(Settings.CATEGORY_SELECTED_LORE.stream()
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
}
