package pl.norbit.playermarket.data;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.exception.SQLQueryException;
import pl.norbit.playermarket.model.MarketItemData;
import pl.norbit.playermarket.model.PlayerData;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class JDBCService {

    private static Connection connection;
    private static QueryRunner runner;

    private static final ExecutorService executor =
            Executors.newSingleThreadExecutor(r -> new Thread(r, "PlayerMarket-DB"));

    @Getter(AccessLevel.PROTECTED)
    private static boolean ready = false;

    private static final BeanListHandler<PlayerData> playerDataHandler =
            new BeanListHandler<>(PlayerData.class);

    private JDBCService() {
        throw new IllegalStateException("Utility class");
    }

    private static final ResultSetHandler<List<MarketItemData>> marketDataHandler =
            new BeanListHandler<>(MarketItemData.class) {
                @Override
                public List<MarketItemData> handle(ResultSet rs) throws SQLException {
                    List<MarketItemData> result = new ArrayList<>();

                    while (rs.next()) {
                        MarketItemData item = new MarketItemData();
                        item.setId(rs.getLong("id"));
                        item.setOwnerName(rs.getString("ownerName"));
                        item.setOwnerUUID(rs.getString("ownerUUID"));
                        item.setPrice(rs.getDouble("price"));
                        item.setItemStack(rs.getBytes("itemStack"));
                        item.setPlayerId(rs.getLong("playerId"));
                        item.setOfferDate(rs.getLong("offerDate"));
                        result.add(item);
                    }

                    return result;
                }
            };

    private static synchronized void ensureConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                connection = getConnection();
            }
        } catch (Exception e) {
            try {
                connection = getConnection();
            } catch (Exception ex) {
                throw new SQLQueryException("Cannot reconnect to database", ex);
            }
        }
    }

    private static void runUpdate(String query, Object... params) throws SQLException {
        try {
            ensureConnection();
            runner.update(connection, query, params);
        } catch (SQLException e) {
            ensureConnection();
            runner.update(connection, query, params);
        }
    }

    private static <T> T runQuery(String query, ResultSetHandler<T> handler, Object... params) throws SQLException {
        try {
            ensureConnection();
            return runner.query(connection, query, handler, params);
        } catch (SQLException e) {
            ensureConnection();
            return runner.query(connection, query, handler, params);
        }
    }

    protected static void init() {
        try {
            connection = getConnection();
            runner = new QueryRunner();
            createTable();
            ready = true;
        } catch (Exception e) {
            throw new SQLQueryException("Error while initializing JDBCService", e);
        }
    }

    protected static void close() {
        executor.shutdown();
        try {
            if (connection != null) connection.close();
        } catch (SQLException ignored) {}
    }

    private static void createTable() {

        String createTablePlayer =
                "CREATE TABLE IF NOT EXISTS PlayerData (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "playerUUID VARCHAR(255)," +
                        "playerName VARCHAR(255)," +
                        "soldItems INT," +
                        "totalSoldItems INT," +
                        "earnedMoney DOUBLE," +
                        "totalEarnedMoney DOUBLE" +
                        ")";

        String createTableMarket =
                "CREATE TABLE IF NOT EXISTS MarketItemData (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "ownerUUID VARCHAR(255)," +
                        "ownerName VARCHAR(255)," +
                        "itemStack BLOB," +
                        "price DOUBLE," +
                        "offerDate BIGINT," +
                        "playerId BIGINT," +
                        "FOREIGN KEY (playerId) REFERENCES PlayerData(id)" +
                        ")";

        try {
            runUpdate(createTablePlayer);
            runUpdate(createTableMarket);
        } catch (SQLException e) {
            throw new SQLQueryException("Error while creating tables", e);
        }
    }

    private static Connection getConnection() throws Exception {

        File dataFolder = PlayerMarket.getInstance().getDataFolder();
        String absolutePath = dataFolder.getAbsolutePath();

        boolean isLocal = !Settings.TYPE.equals("mysql");

        if (isLocal) {

            Class.forName("org.h2.Driver");

            return DriverManager.getConnection(
                    "jdbc:h2:" + absolutePath + "/data/db"
            );

        } else {

            Class.forName("com.mysql.cj.jdbc.Driver");

            return DriverManager.getConnection(
                    Settings.HOST + "/" + Settings.DATABASE + "?autoReconnect=true&useSSL=false",
                    Settings.USER,
                    Settings.PASSWORD
            );
        }
    }

    public static void addMarketItemForPlayer(PlayerData player, MarketItemData marketItem) {

        executor.execute(() -> {
            try {

                runUpdate(
                        "INSERT INTO MarketItemData (ownerUUID,ownerName,itemStack,price,offerDate,playerId) VALUES (?, ?, ?, ?, ?, ?)",
                        player.getPlayerUUID(),
                        marketItem.getOwnerName(),
                        marketItem.getItemStack(),
                        marketItem.getPrice(),
                        marketItem.getOfferDate(),
                        player.getId()
                );

            } catch (SQLException e) {
                throw new SQLQueryException("Error while adding market item for player with id: " + player.getId(), e);
            }
        });
    }

    public static CompletableFuture<List<MarketItemData>> getAllMarketItemsForPlayer(Long playerId) {

        return CompletableFuture.supplyAsync(() -> {

            try {
                return runQuery(
                        "SELECT * FROM MarketItemData WHERE playerId = ?",
                        marketDataHandler,
                        playerId
                );
            } catch (SQLException e) {
                throw new SQLQueryException("Error while getting market items for player: " + playerId, e);
            }

        }, executor);
    }

    public static CompletableFuture<PlayerData> getPlayerData(String playerUUID) {

        return CompletableFuture.supplyAsync(() -> {

            try {

                List<PlayerData> list =
                        runQuery("SELECT * FROM PlayerData WHERE playerUUID = ?", playerDataHandler, playerUUID);

                if (list.isEmpty()) return null;

                PlayerData playerData = list.get(0);

                List<MarketItemData> items =
                        runQuery("SELECT * FROM MarketItemData WHERE playerId = ?", marketDataHandler, playerData.getId());

                playerData.setPlayerOffers(items);

                return playerData;

            } catch (SQLException e) {
                throw new SQLQueryException("Error while loading player data", e);
            }

        }, executor);
    }

    public static CompletableFuture<List<MarketItemData>> getAllMarketItems() {

        return CompletableFuture.supplyAsync(() -> {

            try {
                return runQuery("SELECT * FROM MarketItemData", marketDataHandler);
            } catch (SQLException e) {
                throw new SQLQueryException("Error while loading market items", e);
            }

        }, executor);
    }

    public static void removeMarketItem(Long id) {

        executor.execute(() -> {
            try {
                runUpdate("DELETE FROM MarketItemData WHERE id = ?", id);
            } catch (SQLException e) {
                throw new SQLQueryException("Error removing market item: " + id, e);
            }
        });
    }

    public static CompletableFuture<MarketItemData> getMarketItem(Long id) {

        return CompletableFuture.supplyAsync(() -> {

            try {

                List<MarketItemData> list =
                        runQuery("SELECT * FROM MarketItemData WHERE id = ?", marketDataHandler, id);

                if (list.isEmpty()) return null;

                return list.get(0);

            } catch (SQLException e) {
                throw new SQLQueryException("Error loading market item", e);
            }

        }, executor);
    }

    public static void createPlayerData(PlayerData playerData) {
        executor.execute(() -> {

            try {

                runUpdate(
                        "INSERT INTO PlayerData (playerUUID, playerName, soldItems, totalSoldItems, earnedMoney, totalEarnedMoney) VALUES (?, ?, ?, ?, ?, ?)",
                        playerData.getPlayerUUID(),
                        playerData.getPlayerName(),
                        playerData.getSoldItems(),
                        playerData.getTotalSoldItems(),
                        playerData.getEarnedMoney(),
                        playerData.getTotalEarnedMoney()
                );

            } catch (SQLException e) {
                throw new SQLQueryException("Error creating player data: " + playerData.getPlayerUUID(), e);
            }

        });
    }

    public static void updatePlayerData(PlayerData playerData) {
        executor.execute(() -> {

            try {

                runUpdate(
                        "UPDATE PlayerData SET playerName=?, soldItems=?, totalSoldItems=?, earnedMoney=?, totalEarnedMoney=? WHERE playerUUID=?",
                        playerData.getPlayerName(),
                        playerData.getSoldItems(),
                        playerData.getTotalSoldItems(),
                        playerData.getEarnedMoney(),
                        playerData.getTotalEarnedMoney(),
                        playerData.getPlayerUUID()
                );

            } catch (SQLException e) {
                throw new SQLQueryException("Error updating player data: " + playerData.getPlayerUUID(), e);
            }

        });
    }
}