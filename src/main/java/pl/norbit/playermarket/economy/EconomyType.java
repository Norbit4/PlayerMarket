package pl.norbit.playermarket.economy;

import lombok.Getter;

@Getter
public enum EconomyType {
    VAULT("Vault"),
    PLAYERPOINTS("PlayerPoints");

    EconomyType(String name) {
        this.name = name;
    }

    final String name;
}
