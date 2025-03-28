package pl.norbit.playermarket.config;

import lombok.Data;
import lombok.Setter;

@Data
public class DiscordConfig {
    private boolean enabled;
    private String webhookURL;

    public static DiscordConfig createDefault(){
        return new DiscordConfig(false, "");
    }

    @Setter
    private DiscordEmbed buyEmbed;
    @Setter
    private DiscordEmbed offerEmbed;

    public DiscordConfig(boolean enabled, String webhookUrl) {
        this.enabled = enabled;
        this.webhookURL = webhookUrl;
    }
}
