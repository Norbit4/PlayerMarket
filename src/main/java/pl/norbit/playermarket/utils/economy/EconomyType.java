package pl.norbit.playermarket.utils.economy;

import lombok.Getter;

@Getter
public enum EconomyType {
    PLAYER_POINTS("PLAYERPOINTS"),
    VAULT("VAULT"),
    COINS_ENGINE("COINSENGINE");

    EconomyType(String name) {
        this.name = name;
    }

    final String name;
}