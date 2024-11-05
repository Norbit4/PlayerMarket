package pl.norbit.playermarket.gui;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.pagination.PaginationManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.gui.template.GuiTemplate;
import pl.norbit.playermarket.gui.template.TemplateService;
import pl.norbit.playermarket.model.local.ConfigGui;
import pl.norbit.playermarket.model.local.LocalMarketItem;
import pl.norbit.playermarket.service.CategoryService;
import pl.norbit.playermarket.service.MarketService;
import pl.norbit.playermarket.service.SearchStorage;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.gui.GuiIconUtil;
import pl.norbit.playermarket.utils.pagination.GuiPages;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static pl.norbit.playermarket.utils.TaskUtils.*;

public class MarketSearchGui extends Gui {
    private final PaginationManager marketItemsPagination;
    private final PaginationManager borderPagination;
    private final String search;

    private final ConfigGui configGui;
    private final GuiPages guiPages;

    private static final Map<UUID, MarketSearchGui> playersGui = new ConcurrentHashMap<>();

    static {
        asyncTimer(() -> playersGui.values().forEach(MarketSearchGui::updateTask), 6L, 4L);
    }

    public MarketSearchGui(Player player, String search) {
        super(player, "market-search-gui", "", 6);

        this.configGui = Settings.SEARCH_GUI;
        this.search = search;

        GuiTemplate template = TemplateService.getSearchTemplate(this);

        this.marketItemsPagination = template.getMarketItemsPagination();
        this.borderPagination = template.getBorderPagination();

        for (int i = 0; i < this.borderPagination.getSlots().size(); i++) {
            this.borderPagination.addItem(getBorderIcon());
        }

        Icon left = configGui.getIcon("previous-page-icon");
        Icon right = configGui.getIcon("next-page-icon");

        String title = configGui.getTitle()
                .replace("{SEARCH}", search.toUpperCase());

        this.guiPages = new GuiPages(this, title, marketItemsPagination, 45, left, 53, right);
    }

    public void updateTask(){
        if(!isClosed()){
            updatePage(search);
        }
    }

    public void updateTitle(){
        String title = configGui.getTitle()
                .replace("{SEARCH}", search.toUpperCase());

        this.setTitle(ChatUtils.format(title));
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        playersGui.remove(player.getUniqueId());
    }

    private void updatePage(String search){
        this.marketItemsPagination.getItems().clear();

        List<LocalMarketItem> icons = MarketService.searchItemsByMaterial(search);
        if(icons != null){
            icons.forEach(item -> this.marketItemsPagination.addItem(item.getMarketItem()));
        }

        this.marketItemsPagination.update();
        this.guiPages.update();
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        //update pagination
        this.marketItemsPagination.update();
        this.borderPagination.update();

        addItem(49, GuiIconUtil.getOpenGuItem(configGui.getIcon("back-to-market-icon"),
                new MarketGui(player, CategoryService.getMain())));

        async(() -> {
            updatePage(search);
            setClosed(false);
        });

        playersGui.compute(player.getUniqueId(), (k, v) -> this);

        SearchStorage.updateSearch(player.getUniqueId(), search);
    }

    private Icon getBorderIcon(){
        return new Icon(Material.GRAY_STAINED_GLASS_PANE).setName(" ");
    }
}
