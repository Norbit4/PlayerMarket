package pl.norbit.playermarket.gui.template;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.pagination.PaginationManager;
import pl.norbit.playermarket.config.layout.GuiLayout;

import java.util.List;

public class TemplateUtils {

    private TemplateUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static GuiTemplate getTemplate(Gui gui, GuiLayout layout){
        return GuiTemplate.builder()
                .marketItemsPagination(getPagination(gui, layout.getItemsLayout()))
                .categoriesPagination(getPagination(gui, layout.getCategoryLayout()))
                .borderPagination(getPagination(gui, layout.getBorderLayout()))
                .build();
    }

    private static PaginationManager getPagination(Gui gui, List<Integer> slots){
        if(slots == null || slots.isEmpty()){
            return new PaginationManager(gui);
        }
        var marketItemsPagination = new PaginationManager(gui);

        for (Integer i : slots) {
            marketItemsPagination.registerPageSlots(i);
        }
        return marketItemsPagination;
    }
}
