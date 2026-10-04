package pl.norbit.playermarket.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.utils.format.ChatUtils;

import java.util.ArrayList;
import java.util.List;

public final class DialogBuilder {

    private final List<DialogBody> body = new ArrayList<>();
    private final List<DialogInput> inputs = new ArrayList<>();
    private final List<ActionButton> buttons = new ArrayList<>();

    private Component title = ChatUtils.format(Settings.getDialogSearchTitle());

    private int columns = 2;

    private DialogBuilder() {}

    public static DialogBuilder create() {
        return new DialogBuilder();
    }

    public DialogBuilder title(String title) {
        this.title = ChatUtils.format(title);
        return this;
    }

    public DialogBuilder title(Component title) {
        this.title = title;
        return this;
    }

    public DialogBuilder text(String text) {
        text = text.replace("\\n", "\n");

        body.add(DialogBody.plainMessage(
                ChatUtils.format(text)
        ));

        return this;
    }

    public DialogBuilder text(Component text) {
        body.add(DialogBody.plainMessage(text));

        return this;
    }

    public DialogBuilder item(ItemStack item) {
        body.add(DialogBody.item(item)
                .showDecorations(true)
                .showTooltip(true)
                .build()
        );

        return this;
    }

    public DialogBuilder input(
            String key,
            String label
    ) {
        inputs.add(DialogInput.text(key, ChatUtils.format(label))
                        .width(300)
                        .initial("")
                        .maxLength(100)
                        .labelVisible(true)
                        .build()
        );

        return this;
    }

    public DialogBuilder button(
            String text,
            Key action
    ) {
        return button(text, action, null);
    }

    public DialogBuilder button(
            String text,
            Key action,
            String nbt
    ) {
        DialogAction dialogAction = null;

        if (action != null) {
            dialogAction = DialogAction.customClick(
                    action,
                    nbt == null ? null : BinaryTagHolder.binaryTagHolder(nbt)
            );
        }

        buttons.add(ActionButton.create(
                ChatUtils.format(text),
                null,
                100,
                dialogAction
        ));

        return this;
    }

    public DialogBuilder button(
            String text,
            DialogActionCallback callback,
            ClickCallback.Options options
    ) {
        buttons.add(ActionButton.create(
                ChatUtils.format(text),
                null,
                100,
                DialogAction.customClick(callback, options)
        ));

        return this;
    }

    public DialogBuilder columns(int columns) {
        this.columns = columns;
        return this;
    }

    public Dialog build() {
        return Dialog.create(builder -> builder
                .empty()
                .base(DialogBase.builder(title)
                        .body(body)
                        .inputs(inputs)
                        .build()
                )
                .type(DialogType.multiAction(
                        buttons,
                        null,
                        columns
                ))
        );
    }

    public void open(Player player) {
        player.showDialog(build());
    }
}