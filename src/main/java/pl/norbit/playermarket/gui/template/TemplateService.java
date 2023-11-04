package pl.norbit.playermarket.gui.template;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.pagination.PaginationManager;

public class TemplateService {

    public static GuiTemplate getTemplate(Gui gui){
            var marketItemsPagination = new PaginationManager(gui);

            marketItemsPagination.registerPageSlotsBetween(3, 8);
            marketItemsPagination.registerPageSlotsBetween(12, 17);
            marketItemsPagination.registerPageSlotsBetween(21, 26);
            marketItemsPagination.registerPageSlotsBetween(30, 35);

            var categoriesPagination = new PaginationManager(gui);

            categoriesPagination.registerPageSlotsBetween(0, 1);
            categoriesPagination.registerPageSlotsBetween(9, 10);
            categoriesPagination.registerPageSlotsBetween(18, 19);
            categoriesPagination.registerPageSlotsBetween(27, 28);
            categoriesPagination.registerPageSlotsBetween(36, 37);

            var borderPagination = new PaginationManager(gui);

            borderPagination.registerPageSlots(2, 11, 20, 29);
            borderPagination.registerPageSlotsBetween(38, 44);

            return GuiTemplate.builder()
                    .marketItemsPagination(marketItemsPagination)
                    .categoriesPagination(categoriesPagination)
                    .borderPagination(borderPagination)
                    .build();
    }
}
