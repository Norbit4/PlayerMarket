package pl.norbit.playermarket.utils.pagination;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.pagination.PaginationManager;
import pl.norbit.playermarket.utils.ChatUtils;

public class GuiPages {
    private final PaginationManager paginationManager;
    private final PaginationManager leftIconPagination;
    private final PaginationManager rightIconPagination;
    private final Icon rightIcon;
    private final Icon leftIcon;
    private final Gui gui;
    private final String guiTitle;

    public GuiPages(Gui gui, String guiTitle, PaginationManager paginationManager, int leftSlot, Icon leftIcon, int rightSlot, Icon rightIcon) {
        this.paginationManager = paginationManager;
        this.leftIconPagination = new PaginationManager(gui);
        this.rightIconPagination = new PaginationManager(gui);

        this.leftIconPagination.registerPageSlots(leftSlot);
        this.rightIconPagination.registerPageSlots(rightSlot);

        this.gui = gui;
        this.guiTitle = guiTitle;

        this.leftIcon = leftIcon;
        this.leftIcon.onClick(event -> {
            paginationManager.goPreviousPage();
            paginationManager.update();
            update();
            updateTitle(true);
        });

        this.rightIcon = rightIcon;

        this.rightIcon.onClick(event -> {
            paginationManager.goNextPage();
            paginationManager.update();
            update();
            updateTitle(true);
        });

        updateTitle(false);
    }

    private void updateTitle(boolean force) {
        int currentPage = paginationManager.getCurrentPage() + 1;
        int lastPage = paginationManager.getLastPage() + 1;

        if(lastPage == 0){
            lastPage = 1;
        }

        String title = ChatUtils.format(guiTitle
                .replace("{CURRENT}", String.valueOf(currentPage))
                .replace("{TOTAL}", String.valueOf(lastPage)));

        if(force){
            this.gui.sendTitleUpdate(title);
        }else {
            this.gui.setTitle(title);
        }
    }

    public void update(){
        updateLeftIcon();
        updateRightIcon();
    }

    private void updateLeftIcon() {
        leftIconPagination.getItems().clear();

        if(existLeft(paginationManager)){
            leftIconPagination.addItem(leftIcon);
        }

        leftIconPagination.update();
    }

    private void updateRightIcon() {
        rightIconPagination.getItems().clear();

        if(existRight(paginationManager)){
            rightIconPagination.addItem(rightIcon);
        }
        rightIconPagination.update();
    }

    private static boolean existLeft(PaginationManager pagination) {
        int actualPage = pagination.getCurrentPage();
        return actualPage > 0;
    }

    private static boolean existRight(PaginationManager pagination) {
        int currentPage = pagination.getCurrentPage();
        int maxPage = pagination.getLastPage();
        return currentPage < maxPage;
    }
}
