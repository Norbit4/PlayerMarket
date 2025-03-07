package pl.norbit.playermarket.gui.anvil;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.gui.MarketSearchGui;
import pl.norbit.playermarket.utils.format.ChatUtils;

import java.util.Collections;
import java.util.List;

public class ItemTypeSearchGui {

    private ItemTypeSearchGui() {
        throw new IllegalStateException("Utility class");
    }

    public static void open(Player p){
        new AnvilGUI.Builder()
                .onClose(state -> {
                    String text = state.getText().replace(" ", "");

                    if(text.isEmpty() || text.equals(Settings.getAnvilEmpty())){
                        return;
                    }

                    new MarketSearchGui(p, text).open();
                })
                .onClick((slot, state) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList();
                    }

                    return List.of(AnvilGUI.ResponseAction.close());
                })
                .preventClose()
                .text(" ")
                .title(ChatUtils.format(Settings.getAnvilTitle()))
                .plugin(PlayerMarket.getInstance())
                .open(p);
    }
}
