package pl.norbit.playermarket.model.local;

import lombok.Data;
import org.bukkit.Material;

import java.util.List;
import java.util.UUID;

@Data
public class Category {

    private UUID categoryUUID;

    private String name;
    private List<String> lore;
    private Material icon;

    private boolean enabled;
    private CategoryType type;

    private String file;
    private List<Material> materials;

    public Category(CategoryType type){
        this.categoryUUID = UUID.randomUUID();
        this.type = type;
    }

}
