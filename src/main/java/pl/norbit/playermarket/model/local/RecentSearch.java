package pl.norbit.playermarket.model.local;

import lombok.Getter;

import java.util.LinkedList;

public class RecentSearch {
    @Getter
    private final LinkedList<String> searches;
    private final int maxSize;

    public RecentSearch(int maxSize) {
        this.searches = new LinkedList<>();
        this.maxSize = maxSize;
    }

    public void addSearch(String search) {
        if (searches.size() >= maxSize) {
            searches.removeLast();
        }
        searches.addFirst(search);
    }
}
