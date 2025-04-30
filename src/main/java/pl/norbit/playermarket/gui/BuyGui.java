package pl.norbit.playermarket.gui;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.cooldown.CooldownService;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.logs.DiscordLogs;
import pl.norbit.playermarket.logs.LogService;
import pl.norbit.playermarket.model.MarketItemData;
import pl.norbit.playermarket.economy.EconomyService;
import pl.norbit.playermarket.model.local.ConfigGui;
import pl.norbit.playermarket.model.local.ConfigIcon;
import pl.norbit.playermarket.service.CategoryService;
import pl.norbit.playermarket.service.SearchStorage;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.format.DoubleFormatter;
import pl.norbit.playermarket.utils.time.ExpireUtils;
import pl.norbit.playermarket.utils.player.PlayerUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static pl.norbit.playermarket.utils.TaskUtils.async;
import static pl.norbit.playermarket.utils.TaskUtils.sync;

public class BuyGui extends Gui {
    private final MarketItemData marketItemData;
    private final ItemStack is;
    private final ConfigGui configGui;

    public BuyGui(@NotNull Player player, MarketItemData marketItemData, ItemStack icon) {
        super(player, "market-buy-gui", ChatUtils.format(player, Settings.BUY_GUI.getTitle()), Settings.BUY_GUI.getSize());

        this.marketItemData = marketItemData;
        this.is = icon;
        this.configGui = Settings.BUY_GUI;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        Icon itemIcon = getIcon(is);

        addItem(13, itemIcon);

        if(this.configGui.isFill()){
            List<Integer> fillBlackList = this.configGui.getFillBlackList();

            fillBlackList.add(13);

            fillGui(this.configGui.getBorderIcon(), fillBlackList);
        }

        ConfigIcon acceptIcon = configGui.getIcon("accept-icon");
        ConfigIcon cancelIcon = configGui.getIcon("cancel-icon");

        addItem(acceptIcon.getSlot(), getAcceptIcon(acceptIcon.getIcon()));
        addItem(cancelIcon.getSlot(), getCanceIcon(cancelIcon.getIcon()));
    }

    private void backToShop(String message){
        player.sendMessage(ChatUtils.format(player, message));
        String search = SearchStorage.getSearch(player.getUniqueId());
        sync(() -> {
            if(search != null){
                new MarketSearchGui(player, search).open();
                return;
            }
            new MarketGui(player, CategoryService.getMain()).open();
        });
    }

    private static Icon getIcon(ItemStack is){
        return new Icon(is);
    }

    private Icon getCanceIcon(Icon icon){
        icon.onClick(e -> {
            e.setCancelled(true);

            String search = SearchStorage.getSearch(player.getUniqueId());

            if(search != null){
                new MarketSearchGui(player, search).open();
                return;
            }

            new MarketGui(player, CategoryService.getMain()).open();
        });
        return icon;
    }

    private Icon getAcceptIcon(Icon icon){
        ItemStack item = icon.getItem();

        icon.setName(formatLine(item.getItemMeta().getDisplayName()));
        icon.setLore(item.getItemMeta()
                .getLore()
                .stream()
                .map(this::formatLine)
                .collect(Collectors.toList()));

        icon.onClick(e -> {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();

            if(CooldownService.isOnCooldown(p.getUniqueId())){
                p.sendMessage(ChatUtils.format(Settings.getCooldownMessage()));
                return;
            }
            CooldownService.updateCooldown(p.getUniqueId());

            async(() -> {
                MarketItemData mItemData = DataService.getMarketItemData(marketItemData.getId());

                if(mItemData == null){
                    backToShop(configGui.getMessage("item-sold-message"));
                    return;
                }

                if(ExpireUtils.isExpired(mItemData.getOfferDate())){
                    backToShop(Settings.getExpireMessage());
                    return;
                }

                String ownerUUID = marketItemData.getOwnerUUID();

                if(ownerUUID.equals(p.getUniqueId().toString())){
                    backToShop(configGui.getMessage("player-is-owner-message"));
                    return;
                }

                if(PlayerUtils.isInventoryFull(p)){
                    backToShop(configGui.getMessage("inventory-full-message"));
                    return;
                }

                if(!EconomyService.withDrawIfPossible(p, mItemData.getPrice())){
                    backToShop(configGui.getMessage("not-enough-money-message"));
                    return;
                }

                double taxValue = DataService.buyItem(mItemData);
                ItemStack iStack = mItemData.getItemStackDeserialize();

                p.getInventory().addItem(iStack);

                LogService.log("Player " + p.getName() + " buy item " + iStack.getType() + " x" + iStack.getAmount()
                        + " from " + mItemData.getOwnerName() + " cost: " + mItemData.getPrice());

                //send message to discord
                DiscordLogs.buyItem(p.getName(), mItemData);

                if(Settings.isTaxEnabled() && Settings.isTaxCommandEnabled()){
                    String command = Settings.getTaxCommand()
                            .replace("{PLAYER}", p.getName())
                            .replace("{PRICE}", String.valueOf(taxValue));
                    Server server = p.getServer();
                    sync(() -> server.dispatchCommand(server.getConsoleSender(), command));
                }

                backToShop(configGui.getMessage("success-message")
                        .replace("{COST}", DoubleFormatter.format(mItemData.getPrice()))
                );

                //send message to seller
                PlayerUtils.getPlayer(UUID.fromString(ownerUUID)).ifPresent(seller -> {
                    String sellerMessages = Settings.SELL_ITEM_MESSAGE
                            .replace("{PLAYER}", p.getName())
                            .replace("{PRICE}",  DoubleFormatter.format(mItemData.getPrice()));
                    seller.sendMessage(ChatUtils.format(seller, sellerMessages));
                });
            });
        });
        return icon;
    }
    private String formatLine(String line){
        return ChatUtils.format(player, line.replace("{AMOUNT}", DoubleFormatter.format(marketItemData.getPrice())));
    }
}
