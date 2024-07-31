package pl.norbit.playermarket.service;

import pl.norbit.playermarket.model.local.Category;
import pl.norbit.playermarket.model.local.CategoryType;
import pl.norbit.playermarket.model.local.LocalMarketItem;
import pl.norbit.playermarket.data.DataService;

import java.util.*;
import java.util.stream.Collectors;

import static pl.norbit.playermarket.utils.TaskUtils.asyncTimer;

public class MarketService {

    private static HashMap<UUID, List<LocalMarketItem>> marketItems = new HashMap<>();

    private MarketService() {
        throw new IllegalStateException("Utility class");
    }

    public static List<LocalMarketItem> getIcons(Category category){
        if (Objects.requireNonNull(category.getType()) == CategoryType.ALL) {
            return getAllIcons();
        }
        List<LocalMarketItem> localMarketItems = marketItems.get(category.getCategoryUUID());

        if(localMarketItems == null){
            return new ArrayList<>();
        }

        List<LocalMarketItem> reverse = new ArrayList<>(localMarketItems);

        Collections.reverse(reverse);
        return reverse;
    }

    private static List<LocalMarketItem> getAllIcons(){
        List<LocalMarketItem> all = marketItems.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        Collections.reverse(all);
        return all;
    }

    public static List<LocalMarketItem> searchItemsByMaterial(String itemMatName){
        List<LocalMarketItem> items = marketItems.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(item -> item.getItemStack().getType().name().contains(itemMatName.toUpperCase()))
                .collect(Collectors.toList());

        Collections.reverse(items);
        return items;
    }

    public static void start() {
        asyncTimer(() -> {
            HashMap<UUID, List<LocalMarketItem>> newMarketItems = new HashMap<>();

            DataService.getAll()
                    .stream()
                    .map(LocalMarketItem::new)
                    .forEach(item -> addToMarketItems(CategoryService.getCategoryUUID(item), item, newMarketItems));

            marketItems = newMarketItems;
        }, 30L, 8L);
    }

    private static void addToMarketItems(UUID categoryUUID, LocalMarketItem item, HashMap<UUID, List<LocalMarketItem>> marketItems){
        if(marketItems.containsKey(categoryUUID)) {
            marketItems.get(categoryUUID).add(item);
        }
        else{
            List<LocalMarketItem> itemsInCategory = new ArrayList<>();
            itemsInCategory.add(item);
            marketItems.put(categoryUUID, itemsInCategory);
        }
    }
}
