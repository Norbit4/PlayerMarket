package pl.norbit.playermarket.data;

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

public class JDBCService {
    private static Connection connection;
    private static QueryRunner runner;
    private static final BeanListHandler<PlayerData> playerDataHandler = new BeanListHandler<>(PlayerData.class);

    private JDBCService() {
        throw new IllegalStateException("Utility class");
    }

    private static final ResultSetHandler<List<MarketItemData>> marketDataHandler = new BeanListHandler<>(MarketItemData.class) {
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

    protected static void init() {
        try {
            connection = getConnection();
            runner = new QueryRunner();
            createTable();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected static void close() {
        try {
            if (connection != null){
                connection.close();
            }
        } catch (SQLException ignored) {
        }
    }

    private static void createTable() {
        String createTablePlayer = "CREATE TABLE IF NOT EXISTS PlayerData (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "playerUUID VARCHAR(255)," +
                "playerName VARCHAR(255)," +
                "soldItems INT," +
                "totalSoldItems INT," +
                "earnedMoney DOUBLE," +
                "totalEarnedMoney DOUBLE" +
                ")";

        String createTableMarket = "CREATE TABLE IF NOT EXISTS MarketItemData (" +
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
            runner.update(connection, createTablePlayer );
            runner.update(connection, createTableMarket);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection getConnection() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        File dataFolder = PlayerMarket.getInstance().getDataFolder();

        String absolutePath = dataFolder.getAbsolutePath();

        boolean isLocal = !Settings.TYPE.equals("mysql");
        if (isLocal) {
            Class.forName("org.h2.Driver").newInstance();
            return DriverManager.getConnection("jdbc:h2:" + absolutePath + "/data/db", "", "");
        } else {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            return DriverManager.getConnection(Settings.HOST + "/" + Settings.DATABASE, Settings.USER, Settings.PASSWORD);
        }
    }

    public static void addMarketItemForPlayer(PlayerData player, MarketItemData marketItem) {
        String insertQuery = "INSERT INTO MarketItemData (ownerUUID,ownerName, itemStack, price, offerDate,  playerId) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            runner.update(connection, insertQuery,
                    player.getPlayerUUID(),
                    marketItem.getOwnerName(),
                    marketItem.getItemStack(),
                    marketItem.getPrice(),
                    marketItem.getOfferDate(),
                    player.getId());

        } catch (SQLException e) {
            throw new SQLQueryException("Error while adding market item for player with id: " + player.getId(), e);
        }
    }

    public static List<MarketItemData> getAllMarketItemsForPlayer(Long playerId) {
        String query = "SELECT * FROM MarketItemData WHERE playerId = ?";

        try {
            return runner.query(connection, query, marketDataHandler, playerId);
        } catch (SQLException e) {
            throw new SQLQueryException("Error while getting all market items for player with id: " + playerId, e);
        }
    }

    public static PlayerData getPlayerData(String playerUUID) {
        String query = "SELECT * FROM PlayerData WHERE playerUUID = ?";

        try {
            List<PlayerData> employees = runner.query(connection, query, playerDataHandler, playerUUID);

            if(employees.isEmpty()) return null;

            PlayerData playerData = employees.get(0);

            List<MarketItemData> marketItems = getAllMarketItemsForPlayer(playerData.getId());

            playerData.setPlayerOffers(marketItems);

            return playerData;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static List<MarketItemData> getAllMarketItems() {
        if(connection == null){
            return new ArrayList<>();
        }

        String query = "SELECT * FROM MarketItemData";

        try {
            return runner.query(connection, query, marketDataHandler);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public static void removeMarketItem(Long id) {
        String query = "DELETE FROM MarketItemData WHERE id = ?";

        try {
            runner.update(connection, query, id);
        } catch (SQLException e) {
            throw new SQLQueryException("Error while removing market item with id: " + id, e);
        }
    }

    public static MarketItemData getMarketItem(Long id) {
        String query = "SELECT * FROM MarketItemData WHERE id = ?";

        try {
            List<MarketItemData> employees = runner.query(connection, query, marketDataHandler, id);

            if(employees.isEmpty()){
                return null;
            }

            return employees.get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void createPlayerData(PlayerData playerData) {
        String query = "INSERT INTO PlayerData (playerUUID, playerName, soldItems, totalSoldItems, earnedMoney, totalEarnedMoney) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            runner.update(connection, query, playerData.getPlayerUUID(),
                    playerData.getPlayerName(), playerData.getSoldItems(),
                    playerData.getTotalSoldItems(), playerData.getEarnedMoney(),
                    playerData.getTotalEarnedMoney());
        } catch (SQLException e) {
            throw new SQLQueryException("Error while creating player data for player with id: " + playerData.getId(), e);
        }
    }

    public static void updatePlayerData(PlayerData playerData) {
        String query = "UPDATE PlayerData SET playerName = ?, soldItems = ?, " +
                "totalSoldItems = ?, earnedMoney = ?, totalEarnedMoney = ? WHERE playerUUID = ?";

        try {
            runner.update(connection, query, playerData.getPlayerName(),
                    playerData.getSoldItems(), playerData.getTotalSoldItems(),
                    playerData.getEarnedMoney(), playerData.getTotalEarnedMoney(),
                    playerData.getPlayerUUID());
        } catch (SQLException e) {
            throw new SQLQueryException("Error while updating player data for player with id: " + playerData.getId(), e);
        }
    }
}
