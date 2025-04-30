package pl.norbit.playermarket.config.layout;

import lombok.Data;

import java.util.List;

@Data
public class GuiLayout {
    private List<Integer> itemsLayout;
    private List<Integer> categoryLayout;
    private List<Integer> borderLayout;
}
