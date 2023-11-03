package pl.norbit.playermarket.data;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.model.MarketItemData;
import pl.norbit.playermarket.model.PlayerData;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HibernateService {
    private static SessionFactory sessionFactory;

    public static void init(){
        initHibernate();
    }

    public static void close(){
        try {
            closeHibernate();
        } catch (Exception ignored){} {
        }
    }
    private static void initHibernate() {
        Configuration configuration = getConfiguration();
        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();

        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    }

    private static Configuration getConfiguration(){
        Configuration configuration = new Configuration();

        boolean isLocal = Settings.TYPE.equals("mysql");

        Properties hibernateProp = getConnectionSettings(!isLocal);

        hibernateProp.setProperty("hibernate.hbm2ddl.auto", "update");
        hibernateProp.setProperty("hibernate.show_sql", "false");
        hibernateProp.setProperty("hibernate.format_sql", "false");
        hibernateProp.setProperty("hibernate.use_sql_comments", "false");
        hibernateProp.setProperty("hibernate.generate_statistics", "false");
        hibernateProp.setProperty("hibernate.c3p0.provider_class", "org.hibernate.c3p0.internal.C3P0ConnectionProvider");
        hibernateProp.setProperty("hibernate.c3p0.min_size", "5");
        hibernateProp.setProperty("hibernate.c3p0.max_size", "35");
        hibernateProp.setProperty("hibernate.c3p0.timeout", "1200");
        hibernateProp.setProperty("hibernate.c3p0.max_statements", "20");
        hibernateProp.setProperty("hibernate.c3p0.acquire_increment", "3");

        Logger logger = Logger.getLogger("org.hibernate");
        logger.setLevel(Level.OFF);

        Logger c3p0Logger= Logger.getLogger("com.mchange.v2.c3p0.impl.AbstractPoolBackedDataSource");
        c3p0Logger.setLevel(Level.OFF);

        configuration.setProperties(hibernateProp);

        configuration.addAnnotatedClass(PlayerData.class);
        configuration.addAnnotatedClass(MarketItemData.class);

        return configuration;
    }

    private static Properties getConnectionSettings(boolean isLocal) {
        Properties hibernateProp = new Properties();

        File dataFolder = PlayerMarket.getInstance().getDataFolder();

        String absolutePath = dataFolder.getAbsolutePath();

        if(isLocal) {
            hibernateProp.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
            hibernateProp.setProperty("hibernate.connection.url", "jdbc:h2:" + absolutePath + "/data/db");
            hibernateProp.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            return hibernateProp;
        }
        hibernateProp.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        hibernateProp.setProperty("hibernate.connection.url", Settings.HOST + "/" + Settings.DATABASE);
        hibernateProp.setProperty("hibernate.connection.username", Settings.USER);
        hibernateProp.setProperty("hibernate.connection.password", Settings.PASSWORD);
        hibernateProp.setProperty("hibernate.dialect", Settings.DIALECT);

        return hibernateProp;
    }

    private static void closeHibernate() {
        if (sessionFactory != null) sessionFactory.close();
    }

    public static List<MarketItemData> getAllMarketItems() {
        Session session = sessionFactory.openSession();
        List<MarketItemData> users = session.createQuery("FROM MarketItemData", MarketItemData.class).list();

        session.close();
        return users;
    }

    public static MarketItemData getMarketItem(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM MarketItemData WHERE id = :id", MarketItemData.class)
                    .setParameter("id", id)
                    .getSingleResult();
        }catch (Exception e){
            return null;
        }
    }

    public static PlayerData getPlayerData(String playerUUID){
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM PlayerData WHERE playerUUID = :playerUUID", PlayerData.class)
                    .setParameter("playerUUID", playerUUID)
                    .getSingleResult();
        }catch (Exception e){
            return null;
        }
    }

    public static void deleteMarketItem(MarketItemData marketItemData) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            session.remove(marketItemData);

            transaction.commit();
        }
    }
    public static void deleteMarketItem(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            session.remove(id);

            transaction.commit();
        }
    }

    public static void updatePlayerData(PlayerData playerData){
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            session.merge(playerData);

            transaction.commit();
        }
    }

    public static List<PlayerData> getAllPlayers(){
        Session session = sessionFactory.openSession();
        List<PlayerData> users = session.createQuery("FROM PlayerData", PlayerData.class).list();

        session.close();
        return users;
    }
}
