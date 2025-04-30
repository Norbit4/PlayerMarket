package pl.norbit.playermarket.gui;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.pagination.PaginationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.gui.template.GuiTemplate;
import pl.norbit.playermarket.gui.template.TemplateUtils;
import pl.norbit.playermarket.model.local.ConfigGui;
import pl.norbit.playermarket.model.local.ConfigIcon;
import pl.norbit.playermarket.model.local.LocalMarketItem;
import pl.norbit.playermarket.service.CategoryService;
import pl.norbit.playermarket.service.MarketService;
import pl.norbit.playermarket.service.SearchStorage;
import pl.norbit.playermarket.utils.gui.GuiUtils;
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
        super(player, "market-search-gui", "", Settings.SEARCH_GUI.getSize());

        this.configGui = Settings.SEARCH_GUI;
        this.search = search;

        GuiTemplate template = TemplateUtils.getTemplate(this, this.configGui.getLayout());

        this.marketItemsPagination = template.getMarketItemsPagination();
        if(this.configGui.isFill()){
            this.borderPagination = new PaginationManager(this);
        }else {
            this.borderPagination = template.getBorderPagination();
        }

        GuiUtils.loadBorder(this.configGui, this.borderPagination, this.configGui.getFillBlackList(), this.getSize());

        ConfigIcon left = configGui.getIcon("previous-page-icon");
        ConfigIcon right = configGui.getIcon("next-page-icon");

        String title = configGui.getTitle()
                .replace("{SEARCH}", search.toUpperCase());

        ConfigIcon fill = configGui.getIcon("border-icon");

        Icon fillIcon = null;

        if(this.configGui.isFill()){
            fillIcon = fill.getIcon();
        }

        this.guiPages = new GuiPages(this, title, marketItemsPagination, left.getSlot(), left.getIcon(),
                right.getSlot(), right.getIcon(), fillIcon);
    }

    public void updateTask(){
        if(!isClosed()){
            updatePage(search);
        }
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

//        if(!this.configGui.isFill()){
//            this.borderPagination.update();
//        }

        ConfigIcon backIcon = configGui.getIcon("back-to-market-icon");

        addItem(backIcon.getSlot(), GuiUtils.getOpenGuItem(backIcon.getIcon(),
                new MarketGui(player, CategoryService.getMain())));

//        if(this.configGui.isFill()){
//            fillGui(this.configGui.getBorderIcon(), this.configGui.getFillBlackList());
//        }

        async(() -> {
            updatePage(search);
            setClosed(false);
        });

        playersGui.compute(player.getUniqueId(), (k, v) -> this);

        SearchStorage.updateSearch(player.getUniqueId(), search);
    }
}
