package pl.norbit.playermarket.model.local;

import lombok.Data;
import mc.obliviate.inventory.Icon;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import pl.norbit.playermarket.config.layout.GuiLayout;
import pl.norbit.playermarket.exception.ConfigException;
import pl.norbit.playermarket.exception.MaterialException;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.item.CustomItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ConfigGui {
    private String title;
    private int size;
    private boolean fill;

    private GuiLayout layout;
    private Map<String, ConfigIcon> icons;
    private Map<String, String> messages;

    public ConfigGui(Configuration config, String secKey, String[] messagesKeys, String[] iconsKeys){
        this.icons = new HashMap<>();
        this.messages = new HashMap<>();

        ConfigurationSection section = config.getConfigurationSection(secKey);

        if(section == null) {
            throw new ConfigException("Section is null!");
        }

        title = section.getString("title");
        size = section.getInt("size");
        fill = section.getBoolean("fill");

        ConfigurationSection layoutSection = section.getConfigurationSection("layout");

        if(layoutSection != null) {
            GuiLayout guiLayout = new GuiLayout();
            guiLayout.setBorderLayout(layoutSection.getIntegerList("border"));
            guiLayout.setCategoryLayout(layoutSection.getIntegerList("categories"));
            guiLayout.setItemsLayout(layoutSection.getIntegerList("items"));
            this.layout = guiLayout;
        }

        for(String key : messagesKeys){
            messages.put(key, section.getString(key));
        }

        for(String key : iconsKeys){
            ConfigurationSection configurationSection = section.getConfigurationSection(key);

            String matId = configurationSection.getString("icon");

            if(matId == null){
                throw new MaterialException("Material is null!");
            }

            String name = configurationSection.getString("name");

            if(name == null){
                throw new ConfigException("Name is null!");
            }

            List<String> stringList = configurationSection.getStringList("lore");

            int slot = configurationSection.getInt("slot");

            if(!configurationSection.contains("slot")){
                slot = -1;
            }

            ConfigIcon configIcon = new ConfigIcon();

            configIcon.setCustomItem(new CustomItem(matId));
            configIcon.setName(ChatUtils.format(name));
            configIcon.setSlot(slot);
            configIcon.setLore(stringList.stream()
                    .map(ChatUtils::format)
                    .collect(Collectors.toList()));

            icons.put(key, configIcon);
        }
    }

    public List<Integer> getFillBlackList(){
        List<Integer> layoutBlackList = new ArrayList<>(getLayoutBlackList());

        for (ConfigIcon value : icons.values()) {
            if(value.getSlot() == -1){
                continue;
            }
            layoutBlackList.add(value.getSlot());
        }

        return layoutBlackList;
    }

    private List<Integer> getLayoutBlackList(){
        if(layout == null){
            return List.of();
        }

        List<Integer> blackList = layout.getCategoryLayout();
        blackList.addAll(layout.getItemsLayout());

        return blackList;
    }

    public int getSlot(String key){
        ConfigIcon configIcon = icons.get(key);

        if(configIcon == null){
            return 0;
        }

        return configIcon.getSlot();
    }

    public Icon getBorderIcon(){
        ConfigIcon borderIcon = icons.get("border-icon");

        if(borderIcon == null){
            return null;
        }

        return borderIcon.getIcon();
    }

    public ConfigIcon getIcon(String key){
        return icons.get(key);
    }

    public String getMessage(String key){
        return messages.get(key);
    }
}
