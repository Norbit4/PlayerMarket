package pl.norbit.playermarket.model.local;

import lombok.Data;
import org.bukkit.Material;

import java.util.List;

@Data
public class ConfigIcon {

    private String name;
    private Material material;
    private List<String> lore;
}
