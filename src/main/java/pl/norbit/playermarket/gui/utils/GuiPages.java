package pl.norbit.playermarket.gui.utils;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.pagination.PaginationManager;
import pl.norbit.playermarket.logs.LogService;
import pl.norbit.playermarket.utils.format.ChatUtils;

import java.util.*;

public class GuiPages<T> {

    private final Gui gui;
    private final String guiTitle;

    private final PaginationManager pagination;
    private final PaginationManager leftPagination;
    private final PaginationManager rightPagination;

    private final Icon leftIcon;
    private final Icon rightIcon;
    private final Icon fillIcon;

    private final List<Integer> visibleSlots;

    private final int itemsPerPage;

    private List<T> lastItems;
    private int lastHash;
    private int lastPage = -1;

    public GuiPages(
            Gui gui,
            String guiTitle,
            PaginationManager pagination,
            int leftSlot,
            Icon leftIcon,
            int rightSlot,
            Icon rightIcon,
            Icon fillIcon
    ) {

        this.gui = gui;
        this.guiTitle = guiTitle;

        this.pagination = pagination;
        this.fillIcon = fillIcon;

        this.leftPagination = new PaginationManager(gui);
        this.rightPagination = new PaginationManager(gui);

        this.leftPagination.registerPageSlots(leftSlot);
        this.rightPagination.registerPageSlots(rightSlot);

        this.leftIcon = leftIcon;
        this.rightIcon = rightIcon;

        this.visibleSlots = new ArrayList<>(pagination.getSlots());
        this.itemsPerPage = visibleSlots.size();

        initButtons();
    }

    public void initUpdateTitle(int size){
        updateTitle(false, size);
    }

    public void initUpdateTitle(){
        updateTitle(false);
    }

    private void initButtons() {
        leftIcon.onClick(e -> {
            pagination.goPreviousPage();
            update();
            updateTitle(true);
        });

        rightIcon.onClick(e -> {
            pagination.goNextPage();
            update();
            updateTitle(true);
        });
    }

    public void updateItems(
            List<T> newItems,
            IconProvider<T> iconProvider,
            HashProvider<T> hashProvider
    ) {
        int hash = computeHash(newItems, hashProvider);
        int currentPage = pagination.getCurrentPage();

        if (hash == lastHash && currentPage == lastPage) return;

        if (needsRebuild(newItems)) {
            rebuild(newItems, iconProvider);

            lastItems = newItems;
            lastHash = hash;
            lastPage = currentPage;
            return;
        }

        updateVisibleSlots(newItems, iconProvider, hashProvider);

        lastItems = newItems;
        lastHash = hash;
        lastPage = currentPage;
    }

    private boolean needsRebuild(List<T> items) {
        if (lastItems == null) return true;

        if (items.size() != lastItems.size()) return true;

        int oldPages = (int) Math.ceil((double) lastItems.size() / itemsPerPage);
        int newPages = (int) Math.ceil((double) items.size() / itemsPerPage);

        return oldPages != newPages;
    }

    private void updateVisibleSlots(
            List<T> items,
            IconProvider<T> iconProvider,
            HashProvider<T> hashProvider
    ) {

        int page = pagination.getCurrentPage();

        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, items.size());

        for (int i = start; i < end; i++) {
            if (!hashProvider.equals(lastItems.get(i), items.get(i))) {

                int localIndex = i - start;

                if (localIndex >= visibleSlots.size()) continue;

                gui.addItem(
                        visibleSlots.get(localIndex),
                        iconProvider.provide(items.get(i))
                );
                LogService.log("Updating visible slot " + localIndex);
            }
        }
    }

    private void rebuild(List<T> items, IconProvider<T> provider) {
        LogService.log("Rebuilding " + items.size() + " items");
        pagination.getItems().clear();

        for (T item : items) {
            pagination.addItem(provider.provide(item));
        }

        pagination.update();
        update();
    }

    private int computeHash(List<T> items, HashProvider<T> hashProvider) {
        int hash = 1;

        for (T item : items) {
            hash = 31 * hash + hashProvider.hash(item);
        }

        return hash;
    }

    public void update() {
        updateLeft();
        updateRight();
    }

    private void updateLeft() {
        leftPagination.getItems().clear();

        if (pagination.getCurrentPage() > 0) {
            leftPagination.addItem(leftIcon);
        } else if (fillIcon != null) {
            leftPagination.addItem(fillIcon);
        }

        leftPagination.update();
    }

    private void updateRight() {
        rightPagination.getItems().clear();

        if (pagination.getCurrentPage() < pagination.getLastPage()) {
            rightPagination.addItem(rightIcon);
        } else if (fillIcon != null) {
            rightPagination.addItem(fillIcon);
        }

        rightPagination.update();
    }
    private void updateTitle(boolean force) {
        updateTitle(force, -1);
    }

    private void updateTitle(boolean force, int size) {
        int current = pagination.getCurrentPage() + 1;

        int itemCount = pagination.getItems().size();

        if(size != -1){
            itemCount = size;
        }

        int totalPages = Math.max(
                (int) Math.ceil((double) itemCount / itemsPerPage),
                1
        );

        String title = ChatUtils.format(
                guiTitle
                        .replace("{CURRENT}", String.valueOf(current))
                        .replace("{TOTAL}", String.valueOf(totalPages))
        );

        if (force) {
            gui.sendTitleUpdate(title);
            pagination.update();
        }
        else gui.setTitle(title);
    }

    public interface IconProvider<T> {
        Icon provide(T item);
    }

    public interface HashProvider<T> {
        int hash(T item);
        boolean equals(T a, T b);
    }
}