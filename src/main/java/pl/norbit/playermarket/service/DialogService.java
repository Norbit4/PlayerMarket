package pl.norbit.playermarket.service;

import io.papermc.paper.dialog.DialogResponseView;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.config.ConfigDialog;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.data.SearchStorage;
import pl.norbit.playermarket.dialog.DialogBuilder;
import pl.norbit.playermarket.gui.MarketGui;
import pl.norbit.playermarket.gui.MarketSearchGui;
import pl.norbit.playermarket.logs.DiscordLogs;
import pl.norbit.playermarket.logs.LogService;
import pl.norbit.playermarket.model.MarketItemData;
import pl.norbit.playermarket.model.local.ConfigGui;
import pl.norbit.playermarket.utils.economy.EconomyUtils;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.format.DoubleFormatter;
import pl.norbit.playermarket.utils.player.PlayerUtils;
import pl.norbit.playermarket.utils.time.ExpireUtils;
import pl.norbit.playermarket.utils.time.TimeUtils;

import java.util.UUID;

import static pl.norbit.playermarket.utils.TaskUtils.sync;

public class DialogService {

    private DialogService(){}

    public static void openSearch(Player p){
        DialogBuilder.create()
                .text(Settings.getDialogSearchBreak())
                .input("search", Settings.getDialogSearchTextLabel())
                .button(Settings.getDialogSearchButton(),
                        DialogService::handleSearch,
                        ClickCallback.Options.builder()
                                .uses(1)
                                .build()
                )
                .button(Settings.getDialogSearchBackButton(),
                        (response, audience)
                                -> new MarketGui(p, CategoryService.getMain()).open(),
                        ClickCallback.Options.builder()
                                .uses(1)
                                .build())
                .open(p);
    }

    private static void handleSearch(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player p)) {
            return;
        }
        String text = response.getText("search");

        if (text == null || text.isBlank()) {
            return;
        }

        new MarketSearchGui(p, text).open();
    }

    public static void openAdminDelete(Player p, MarketItemData marketItemData) {
        ConfigDialog configDialog = Settings.getAdminDeleteDialog();

        double price = marketItemData.getPrice();
        String ownerName = marketItemData.getOwnerName();
        long offerDate = marketItemData.getOfferDate();

        String textTitle = configDialog.getTextTitle()
                .replace("{cost}", DoubleFormatter.format(price))
                .replace("{seller}", ownerName)
                .replace("{expire}", ExpireUtils.getRemainingTime(offerDate))
                .replace("{date}", TimeUtils.getFormattedDate(offerDate));

        String textInfo = configDialog.getTextInfo()
                .replace("{cost}", DoubleFormatter.format(price))
                .replace("{seller}", ownerName)
                .replace("{expire}", ExpireUtils.getRemainingTime(offerDate))
                .replace("{date}", TimeUtils.getFormattedDate(offerDate));

        DialogBuilder.create()
                .title(configDialog.getTitle())
                .text(configDialog.getTextBreak())
                .text(textTitle)
                .item(marketItemData.getItemStackDeserialize())
                .text(textInfo)
                .button(configDialog.getAcceptButton(),
                        (response, audience) -> handleAdminDelete(marketItemData.getId()),
                        ClickCallback.Options.builder()
                                .uses(1)
                                .build()
                )
                .button(configDialog.getBackButton(),
                (response, audience)
                        -> sync(() -> new MarketGui(p, CategoryService.getMain()).open()),
                ClickCallback.Options.builder()
                        .uses(1)
                        .build())
                .open(p);
    }


    private static void handleAdminDelete(Long id) {
        DataService.removeMarketItem(id);
    }

    public static void openBuy(Player p, MarketItemData marketItemData) {
        ConfigDialog configDialog = Settings.getBuyDialog();

        double price = marketItemData.getPrice();
        String ownerName = marketItemData.getOwnerName();
        long offerDate = marketItemData.getOfferDate();

        String textTitle = configDialog.getTextTitle()
                .replace("{cost}", DoubleFormatter.format(price))
                .replace("{seller}", ownerName)
                .replace("{expire}", ExpireUtils.getRemainingTime(offerDate))
                .replace("{date}", TimeUtils.getFormattedDate(offerDate));

        String textInfo = configDialog.getTextInfo()
                .replace("{cost}", DoubleFormatter.format(price))
                .replace("{seller}", ownerName)
                .replace("{expire}", ExpireUtils.getRemainingTime(offerDate))
                .replace("{date}", TimeUtils.getFormattedDate(offerDate));

        DialogBuilder.create()
                .title(configDialog.getTitle())
                .text(configDialog.getTextBreak())
                .text(textTitle)
                .item(marketItemData.getItemStackDeserialize())
                .text(textInfo)
                .button(configDialog.getAcceptButton(),
                        (response, audience) -> handleBuy(audience, marketItemData),
                        ClickCallback.Options.builder()
                                .uses(1)
                                .build()
                )
                .button(configDialog.getBackButton(),
                        (response, audience)
                                ->  new MarketGui(p, CategoryService.getMain()).open(),
                        ClickCallback.Options.builder()
                                .uses(1)
                                .build())
                .open(p);
    }

    private static void handleBuy(Audience audience, MarketItemData localMarketItem) {
        if (!(audience instanceof Player p)) {
            return;
        }

        ConfigGui configGui = Settings.getBuyGui();

        DataService.getMarketItemData(localMarketItem.getId()).thenAccept(mItemData -> {

            if (mItemData == null) {
                sync(() -> backToShop(p, configGui.getMessage("item-sold-message")));
                return;
            }

            if (ExpireUtils.isExpired(mItemData.getOfferDate())) {
                sync(() -> backToShop(p, Settings.getExpireMessage()));
                return;
            }

            if (mItemData.getOwnerUUID().equals(p.getUniqueId().toString())) {
                sync(() -> backToShop(p, configGui.getMessage("player-is-owner-message")));
                return;
            }

            sync(() -> {
                if (PlayerUtils.isInventoryFull(p)) {
                    backToShop(p, configGui.getMessage("inventory-full-message"));
                    return;
                }

                if (!EconomyUtils.withDrawIfPossible(p, mItemData.getPrice())) {
                    backToShop(p, configGui.getMessage("not-enough-money-message"));
                    return;
                }

                DataService.buyItem(mItemData).thenAccept(taxValue -> {
                    sync(() -> {
                        ItemStack iStack = mItemData.getItemStackDeserialize();
                        p.getInventory().addItem(iStack);

                        LogService.log("Player " + p.getName() + " buy item " + iStack.getType());

                        DiscordLogs.buyItem(p.getName(), mItemData);

                        if (Settings.isTaxEnabled() && Settings.isTaxCommandEnabled()) {
                            String command = Settings.getTaxCommand()
                                    .replace("{player}", p.getName())
                                    .replace("{cost}", String.valueOf(taxValue));
                            Server server = p.getServer();

                            server.dispatchCommand(
                                    server.getConsoleSender(),
                                    command
                            );
                        }

                        Player owner = PlayerUtils.getPlayer(UUID.fromString(mItemData.getOwnerUUID()));

                        if(owner != null){
                            String message = configGui.getMessage("sell-item-to-owner")
                                    .replace("{player}", p.getName())
                                    .replace("{price}", DoubleFormatter.format(localMarketItem.getPrice()));

                            owner.sendMessage(ChatUtils.format(message));
                        }

                        backToShop(p, configGui.getMessage("success-message")
                                .replace("{cost}", DoubleFormatter.format(localMarketItem.getPrice()))
                        );

                    });

                });
            });
        });
    }

    private static void backToShop(Player p, String message){
        p.sendMessage(ChatUtils.format(p, message));
        back(p);
    }

    private static void back(Player p) {
        String search = SearchStorage.getSearch(p.getUniqueId());

        if (search != null) {
            new MarketSearchGui(p, search).open();
        } else {
            new MarketGui(p, CategoryService.getMain()).open();
        }
    }
}
