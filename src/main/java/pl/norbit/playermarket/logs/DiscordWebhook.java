package pl.norbit.playermarket.logs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import pl.norbit.playermarket.config.discord.DiscordConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private DiscordWebhook() {
        throw new IllegalStateException("Utility class");
    }

    public static void send(DiscordConfig discordConfig, String message, int color) throws Exception {
        JsonObject embed = new JsonObject();
        embed.addProperty("description", message);
        embed.addProperty("color", color);

        JsonObject json = new JsonObject();
        JsonArray embeds = new JsonArray();
        embeds.add(embed);
        json.add("embeds", embeds);

        String requestBody = json.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(discordConfig.getWebhookURL()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        response.statusCode();
    }
}
