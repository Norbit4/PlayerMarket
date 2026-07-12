package pl.norbit.playermarket.plugins;

import lombok.Getter;

@Getter
public enum PluginHook {

    ITEMS_ADDER("ItemsAdder"),
    VAULT("Vault"),
    NEXO("Nexo"),
    MMO_ITEMS("MMOItems"),
    ORAXEN("Oraxen"),
    CRAFT_ENGINE("CraftEngine"),
    PLACEHOLDER_API("PlaceholderAPI"),
    PLAYER_POINTS("PlayerPoints"),
    MYTHIC_MOBS("MythicMobs");

    private final String pluginName;

    PluginHook(String pluginName) {
        this.pluginName = pluginName;
    }

}
