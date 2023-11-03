package pl.norbit.playermarket.utils;

import org.bukkit.Material;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaterialUtils {

    public static Set<Material> convertToMaterialSet(List<String> list){
        Set<Material> materialSet = new HashSet<>();

        list.forEach(mat -> {
            Material material = Material.getMaterial(mat.toUpperCase());

            if(material != null) materialSet.add(material);
        });

        return materialSet;
    }
}
