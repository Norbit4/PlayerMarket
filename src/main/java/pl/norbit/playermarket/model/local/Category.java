package pl.norbit.playermarket.model.local;

import lombok.Data;
import org.bukkit.Material;

import java.util.List;
import java.util.UUID;

@Data
public class Category {

    private UUID categoryUUID;
    private String name;
    private Material icon;
    private boolean enabled;

    private String file;

    private List<String> lore;
    private List<Material> materials;

    public Category(){
        this.categoryUUID = UUID.randomUUID();
    }
}
