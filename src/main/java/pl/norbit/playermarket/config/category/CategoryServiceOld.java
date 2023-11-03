package pl.norbit.playermarket.config.category;

import pl.norbit.playermarket.PlayerMarket;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class CategoryServiceOld {
    private final static String categoryPath = "categories/";
    private final static String resourcePath = "/categories/{CATEGORY}";
    private final static String[] categories = {"armor", "blocks", "combat", "food", "minerals", "nature", "redstone", "tools"};


    public static void generateDefaults(){
        String stringPath = PlayerMarket.getInstance().getDataFolder().getAbsolutePath() + "/" + categoryPath;

        Path path = Path.of(stringPath);

        if(Files.exists(path)) return;

        try {
            Files.createDirectories(path);

            for (String category : categories) loadDefault(category + ".yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadDefault(String category) throws IOException {

        String finalPath = resourcePath.replace("{CATEGORY}", category);

        InputStream inputStream = PlayerMarket.class.getResourceAsStream(finalPath);

        if (inputStream == null) return;

        Path targetPath = Path.of(categoryPath + category);
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }
}
