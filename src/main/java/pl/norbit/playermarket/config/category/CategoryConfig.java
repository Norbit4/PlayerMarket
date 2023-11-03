package pl.norbit.playermarket.config.category;

import pl.norbit.playermarket.PlayerMarket;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class CategoryConfig {
    private static final String categoryPath = "categories/";
    private static final String resourcePath = "categories/%s.yml";
    private static final String[] categories = {"armor", "blocks", "combat", "food", "minerals", "nature", "redstone", "tools"};

    public static String getCategoryPath() {
        PlayerMarket inst = PlayerMarket.getInstance();

        File dataFolder = inst.getDataFolder();

        return dataFolder.getAbsolutePath() + "/" + categoryPath;
    }

    public static void generateDefaults() {
        PlayerMarket inst = PlayerMarket.getInstance();

        File dataFolder = inst.getDataFolder();

        String folderPath = dataFolder.getAbsolutePath() + "/" + categoryPath;

        if (Files.exists(Path.of(folderPath))) return;

        for (String category : categories) {
            String finalResourcePath = String.format(resourcePath, category);
            InputStream inputStream = inst.getResource(finalResourcePath);

            if (inputStream == null) continue;

            File configFile = new File(dataFolder, categoryPath + category + ".yml");

            if(configFile.exists()) continue;

            try {
                configFile.getParentFile().mkdirs();
                Files.copy(inputStream, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

