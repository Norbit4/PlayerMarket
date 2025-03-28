package pl.norbit.playermarket.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiscordEmbed {
    private boolean enabled;
    private String message;
    private int color;

    public static DiscordEmbed createDefault(){
        return new DiscordEmbed(false, "", 0);
    }
}
