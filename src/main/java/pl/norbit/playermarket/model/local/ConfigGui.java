package pl.norbit.playermarket.model.local;

import lombok.Data;
import mc.obliviate.inventory.Icon;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import pl.norbit.playermarket.utils.ChatUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ConfigGui {

    private String title;
    private Map<String, Icon> icons;
    private Map<String, String> messages;

    public ConfigGui(Configuration config, String secKey, String[] messagesKeys, String[] iconsKeys){

        this.icons = new HashMap<>();
        this.messages = new HashMap<>();

        ConfigurationSection section = config.getConfigurationSection(secKey);

        if(section == null) throw new NullPointerException("Section is null!");

        title = section.getString("title");

        for(String key : messagesKeys){
            messages.put(key, section.getString(key));
        }

        for(String key : iconsKeys){
            ConfigurationSection configurationSection = section.getConfigurationSection(key);

            String material = configurationSection.getString("icon");

            if(material == null) throw new NullPointerException("Material is null!");

            Material mat = Material.getMaterial(material.toUpperCase());

            if(mat == null) throw new RuntimeException("This material is not exist!");

            String name = configurationSection.getString("name");

            if(name == null) throw new NullPointerException("Name is null!");

            List<String> stringList = configurationSection.getStringList("lore");

            Icon icon = new Icon(mat);

            icon.setName(ChatUtils.format(name));
            icon.setLore(stringList.stream().map(ChatUtils::format).collect(Collectors.toList()));

            icons.put(key, icon);
        }
    }

    public Icon getIcon(String key){
        return icons.get(key);
    }

    public String getMessage(String key){
        return messages.get(key);
    }
}
