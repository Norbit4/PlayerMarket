package pl.norbit.playermarket.service;

import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.gui.MarketGui;
import pl.norbit.playermarket.gui.MarketSearchGui;
import pl.norbit.playermarket.gui.PlayerItemsGui;
import pl.norbit.playermarket.model.local.Category;
import pl.norbit.playermarket.model.local.CategoryType;
import pl.norbit.playermarket.model.local.LocalMarketItem;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.utils.time.ExpireUtils;

import java.util.*;
import java.util.stream.Collectors;

import static pl.norbit.playermarket.utils.TaskUtils.asyncTimer;

public class MarketService {

    private static HashMap<UUID, List<LocalMarketItem>> marketItems = new HashMap<>();
    private static List<LocalMarketItem> cachedAllItems = Collections.emptyList();

    private MarketService() {}

    public static List<LocalMarketItem> getIcons(Category category){
        if (category.getType() == CategoryType.ALL) {
            return getAllIcons();
        }
        List<LocalMarketItem> localMarketItems = marketItems.get(category.getCategoryUUID());

        if(localMarketItems == null){
            return Collections.emptyList();
        }

        List<LocalMarketItem> reverse = new ArrayList<>(localMarketItems);

        Collections.reverse(reverse);
        return reverse;
    }

    public static void notifyCategoryChanged(UUID categoryUUID) {
        Map<UUID, Set<MarketGui>> viewers = MarketGui.getViewers();

        Set<MarketGui> categoryGuis = viewers.get(categoryUUID);
        if (categoryGuis != null) {
            for (MarketGui gui : categoryGuis) {
                gui.onItemAdded();
            }
        }

        // update ALL-category
        Set<MarketGui> allGuis = viewers.get(Settings.ALL_CATEGORY.getCategoryUUID());

        if (allGuis != null) {
            for (MarketGui gui : allGuis) {
                gui.onItemAdded();
            }
        }

        PlayerItemsGui.updateAll();
        MarketSearchGui.updateAll();
    }

    private static List<LocalMarketItem> getAllIcons(){
        return cachedAllItems;
    }

    public static List<LocalMarketItem> searchItemsByMaterial(String itemMatName){
        return marketItems.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(item -> item.getItemStack().getType().name().contains(itemMatName.toUpperCase()))
                // Sort by date
                .sorted(Comparator.comparingLong(LocalMarketItem::getOfferDate).reversed())
                .collect(Collectors.toList());
    }

    public static void start() {
        asyncTimer(() -> {
            DataService.getAll().thenAccept(items -> {
                HashMap<UUID, List<LocalMarketItem>> newMarketItems = new HashMap<>();

                items.stream()
                        .map(LocalMarketItem::new)
                        .filter(item -> !ExpireUtils.isExpired(item.getOfferDate()))
                        .forEach(item ->
                                addToMarketItems(
                                        CategoryService.getCategoryUUID(item),
                                        item,
                                        newMarketItems
                                )
                        );

                Set<UUID> changedCategories =
                        getChangedCategories(marketItems, newMarketItems);

                marketItems = newMarketItems;

                cachedAllItems = newMarketItems.values()
                        .stream()
                        .flatMap(Collection::stream)
                        .sorted(Comparator.comparingLong(LocalMarketItem::getOfferDate).reversed())
                        .collect(Collectors.toList());

                for (UUID categoryUUID : changedCategories) {
                    notifyCategoryChanged(categoryUUID);
                }
            });

        }, 40L, 30L);
    }

    private static Set<UUID> getChangedCategories(
            Map<UUID, List<LocalMarketItem>> oldMap,
            Map<UUID, List<LocalMarketItem>> newMap
    ) {

        Set<UUID> changed = new HashSet<>();

        Set<UUID> allKeys = new HashSet<>();
        allKeys.addAll(oldMap.keySet());
        allKeys.addAll(newMap.keySet());

        for (UUID key : allKeys) {
            List<LocalMarketItem> oldList = oldMap.getOrDefault(key, Collections.emptyList());
            List<LocalMarketItem> newList = newMap.getOrDefault(key, Collections.emptyList());

            if (oldList.size() != newList.size()) {
                changed.add(key);
                continue;
            }

            for (int i = 0; i < oldList.size(); i++) {
                if (!equalsItem(oldList.get(i), newList.get(i))) {
                    changed.add(key);
                    break;
                }
            }
        }

        return changed;
    }

    private static boolean equalsItem(LocalMarketItem a, LocalMarketItem b) {
        if (a == b) return true;
        if (a == null || b == null) return false;

        return a.getId().equals(b.getId()) &&
                a.getOfferDate() == b.getOfferDate();
    }

    private static void addToMarketItems(UUID categoryUUID, LocalMarketItem item,
                                         HashMap<UUID, List<LocalMarketItem>> marketItems) {

        marketItems
                .computeIfAbsent(categoryUUID, k -> new ArrayList<>())
                .add(item);
    }
}
