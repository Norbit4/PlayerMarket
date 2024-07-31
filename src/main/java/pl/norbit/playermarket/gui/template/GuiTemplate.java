package pl.norbit.playermarket.gui.template;

import lombok.Builder;
import lombok.Data;
import mc.obliviate.inventory.pagination.PaginationManager;

@Data
@Builder
public class GuiTemplate {
    private PaginationManager marketItemsPagination;
    private PaginationManager categoriesPagination;
    private PaginationManager borderPagination;
}
