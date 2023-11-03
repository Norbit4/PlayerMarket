package pl.norbit.playermarket.model.local;

import lombok.AllArgsConstructor;
import lombok.Data;
import pl.norbit.playermarket.model.PlayerData;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class LocalPlayerData {

    private PlayerData playerData;
    private List<LocalPlayerItem> playerOffers;

    public LocalPlayerData(PlayerData playerData) {
        this.playerData = playerData;

        playerOffers = playerData.getPlayerOffers()
                .stream()
                .map(iData -> new LocalPlayerItem(iData.getId(), iData.getItemStack(), iData.getPrice()))
                .collect(Collectors.toList());

    }
}
