package pl.norbit.playermarket.utils.gui;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.norbit.playermarket.utils.format.ChatUtils;

import java.util.*;

public class LoreBuilder {

    private final ItemStack item;
    private final Map<String, String> placeholders = new LinkedHashMap<>();

    public LoreBuilder(ItemStack item) {
        this.item = item.clone();
    }

    public LoreBuilder replace(String placeholder, Object value) {
        placeholders.put(placeholder, String.valueOf(value));
        return this;
    }

    public ItemStack append(List<String> template) {

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        List<String> lore = meta.hasLore()
                ? new ArrayList<>(meta.getLore())
                : new ArrayList<>();

        for (String line : template) {

            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                line = line.replace(entry.getKey(), entry.getValue());
            }

            lore.add(ChatUtils.format(line));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }
}