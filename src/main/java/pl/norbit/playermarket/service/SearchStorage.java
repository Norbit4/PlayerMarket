package pl.norbit.playermarket.service;

import pl.norbit.playermarket.model.local.RecentSearch;

import java.util.*;

public class SearchStorage {
    private static final Map<UUID, String> search = new HashMap<>();
    private static final RecentSearch recentSearch = new RecentSearch(5);

    private SearchStorage() {
        throw new IllegalStateException("Utility class");
    }

    public static void updateSearch(UUID playerUUID, String search) {
        SearchStorage.search.put(playerUUID, search);
        recentSearch.addSearch(search);
    }

    public static void clear(UUID playerUUID) {
        SearchStorage.search.remove(playerUUID);
    }

    public static String getSearch(UUID playerUUID) {
        return search.get(playerUUID);
    }

    public static String getRecentSearch(int index) {
        LinkedList<String> searches = recentSearch.getSearches();

        if (index < 0 || index >= searches.size()) {
            return null;
        }
        return searches.get(index);
    }
}
