package com.itmo.utils;

import com.itmo.client.User;
import com.itmo.collection.*;
import com.itmo.server.url.SshConnection;
import com.itmo.server.url.UrlGetterDirectly;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DatabaseManager implements MyCRUD  {

    private static final String DB_URL = new UrlGetterDirectly().getUrl() ;
    private static String USER;
    private static String PASS;
    private static final String COLLECTION_TABLE = "dragons";
    private static final String FILE_WITH_ACCOUNT = "account";
    private static final String USERS_TABLE = "users";
    private static final String pepper = "1@#$&^%$)3";


    //читаем данные аккаунта для входа подключения к бд, ищем драйвер
    static {
        try (FileReader fileReader = new FileReader(FILE_WITH_ACCOUNT);
             BufferedReader reader = new BufferedReader(fileReader)) {
            USER = "s288757";
            PASS = "pzw999";
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Connection to PostgreSQL JDBC");
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("PostgreSQL JDBC Driver successfully connected");
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found. Include it in your library path");
            e.printStackTrace();
        }
    }

    private Connection connection;
    private PassEncoder passEncoder;

    public DatabaseManager(String dbUrl, String user, String pass) {
        try {
            connection = DriverManager.getConnection(dbUrl, user, pass);
            passEncoder = new PassEncoder();
            System.out.println("БД инициализирована: " + dbUrl);
        } catch (SQLException e) {
            System.out.println("Connection to database failed");
            e.printStackTrace();
        }
    }

    public DatabaseManager(String address, int port, String dbName, String user, String pass) {
        this("jdbc:postgresql://" + address + ":" + port + "/" + dbName, user, pass);
    }

    public DatabaseManager() {
        this(DB_URL, USER, PASS);
    }

    public boolean insertDragon(Dragon d)  {
        try {
            String drtype = null;

            if (d.getType() != null) drtype = d.getType().name();
            String date =d.getCreationDateInFormat();
            String state = "INSERT INTO " + COLLECTION_TABLE +
                    "(dragon_name," +
                    " reg_date," +
                    " age," +
                    " wingspan," +
                    " dragon_type," +
                    " dragon_character, " +
                    "owner)\n" +
                    "VALUES (?, '" + date + "', ?, ?, ?::dragon_type, ?::dragon_character, ?)";
            PreparedStatement dragonItself = connection.prepareStatement(state);

            dragonItself.setString(1, d.getName());
            dragonItself.setInt(2, d.getAge());
            dragonItself.setFloat(3, d.getWingspan());
            dragonItself.setString(4, drtype);
            dragonItself.setString(5, d.getCharacter().name());
            dragonItself.setString(6, d.getOwnerName());
            dragonItself.executeUpdate();

            PreparedStatement dragonCoords = connection.prepareStatement(
                    "INSERT INTO dragon_coordinates(dragon_id,x,y)" +
                            "VALUES (currval('generate_id'), ?, ?)");
            dragonCoords.setInt(1, d.getCoordinates().getX());
            dragonCoords.setLong(2, d.getCoordinates().getY());
            dragonCoords.executeUpdate();

            Person killer = d.getKiller();
            if (killer != null) {
                String hair = killer.getHairColor() == null ?
                        "NULL" : killer.getHairColor().name();
                String nati = killer.getNationality() == null ?
                        "NULL" : killer.getNationality().name();
                PreparedStatement dragonKiller =
                        connection.prepareStatement(
                                "INSERT INTO dragon_killers(dragon_id, killer_name, birthday, color, country)" +
                                        "VALUES (currval('generate_id'), ?,'"
                                        +killer.getBirthdayInFormat() + "', ?::color, ?::country)"
                        );
                dragonKiller.setString(1, killer.getName());
                dragonKiller.setString(2, hair);
                dragonKiller.setString(3, nati);
                dragonKiller.executeUpdate();

                Location loc = killer.getLocation();
                PreparedStatement killerLoc =
                        connection.prepareStatement("INSERT INTO killers_locations( dragon_id, x,y,z,loc_name)\n" +
                                "VALUES (currval('generate_id'), ?, ?, ?, ?)");
                killerLoc.setInt(1, loc.getX());
                killerLoc.setLong(2, loc.getY());
                killerLoc.setFloat(3, loc.getZ());
                killerLoc.setString(4, loc.getName());
                killerLoc.executeUpdate();
            }
            return true;
        }catch (SQLException e){
            System.out.println("Ошибка при добавлении элемента в БД.");
            e.printStackTrace();
            return false;
        }
    }




    public Set<Dragon> getCollectionFromDatabase() throws SQLException {
        PreparedStatement statement =
                connection.prepareStatement(
                        "SELECT * FROM "+ COLLECTION_TABLE + " ds\n" +
                                "    inner join dragon_coordinates dc\n" +
                                "on ds.id = dc.dragon_id\n" +
                                "    left outer join dragon_killers dk\n" +
                                "    on dk.dragon_id = ds.id\n" +
                                "    left outer join killers_locations kl\n" +
                                "    on kl.dragon_id=ds.id"
                );
        ResultSet resultSet = statement.executeQuery();
        HashSet<Dragon> dragons = new HashSet<>();
        while (resultSet.next()) {
            long id = resultSet.getLong("id");
            String name = resultSet.getString("dragon_name");
            Date date = resultSet.getDate("reg_date");
            int age = resultSet.getInt("age");
            float wingspan = resultSet.getFloat("wingspan");
            String strType = resultSet.getString("dragon_type");
            DragonType type = strType == null ? null : Enum.valueOf(DragonType.class, strType);
            DragonCharacter character = Enum.valueOf(DragonCharacter.class, resultSet.
                    getString("dragon_character"));
            String ownerName = resultSet.getString("owner");
            Coordinates coordinates = new Coordinates(resultSet.getInt("x"), resultSet.getLong("y"));
            Person person = null;
            if(resultSet.getString("killer_name")!=null){
                person = new Person(
                        resultSet.getString("killer_name"),
                        DateTimeAdapter.convertToLocalDateViaInstant(resultSet.getDate("birthday")),
                        Enum.valueOf(com.itmo.collection.Color.class, resultSet.getString("color")),
                        Enum.valueOf(Country.class, resultSet.getString("country")),
                        new Location(
                                resultSet.getInt("x"),
                                resultSet.getLong("y"),
                                resultSet.getFloat("z"),
                                resultSet.getString("loc_name"))
                );
            }
            Dragon dragon = new Dragon(name, coordinates, date, age, wingspan, type, character, person);
            dragon.setOwnerName(ownerName);
            dragon.setId(id);
            User user = new User(ownerName);
            dragon.setUser(user);
            dragons.add(dragon);
        }
        return (Collections.synchronizedSet(dragons));
    }


    @Override
    public boolean deleteDragonById(long id) {
        String sqlRequest =
                "DELETE FROM " + COLLECTION_TABLE + " WHERE id=" + id;
        try {
            PreparedStatement statement = connection.prepareStatement(sqlRequest);
            statement.executeUpdate();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public long getIdOfDragon(Dragon d){
        String sqlRequest =
                "select id FROM " + COLLECTION_TABLE +
                        " WHERE owner=? " +
                        "and dragon_name=?" +
                        " and wingspan=?" +
                        " and dragon_character=?::dragon_character" +
                        " and age=?" +
                        " and reg_date='"; //+ d.getCreationDateInFormat() + "'";

        try {
            PreparedStatement statement = connection.prepareStatement(sqlRequest);
            statement.setString(1, d.getOwnerName());
            statement.setString(2, d.getName());
            statement.setFloat(3, d.getWingspan());
            statement.setString(4, d.getCharacter().name());
            statement.setInt(5, d.getAge());
            ResultSet set = statement.executeQuery();
            if (set.next()){
                return set.getInt("id");
            }
            return 0;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return 0;
        }
    }

    public boolean containsUser(User user) {
        try {
            PreparedStatement statement = connection.prepareStatement("select * from " + USERS_TABLE + " where login = ?");
            statement.setString(1, user.getName());
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) return false;
            String salt = resultSet.getString("salt");
            String hash = passEncoder.getHash(user.getHashPass() + salt, "1@#$&^%$)3");
            statement = connection.prepareStatement("select * from " + USERS_TABLE + " where login = ? " +
                    "and hashpass = ? and salt=?");
            statement.setString(1, user.getName());
            statement.setString(2, hash);
            statement.setString(3, salt);
            return statement.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void insertUser(String login, String passHash){
        try {
            String salt = new SimplePasswordGenerator(true, true, true, true)
                    .generate(10,10);
            String hash = passEncoder.getHash(passHash + salt, pepper);
            PreparedStatement statement = connection.prepareStatement("insert into " +
                    USERS_TABLE + " (login, hashpass, salt)" + " VALUES ( '"+login+"', '" + hash +"', '" + salt +"' )");
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void insertUser(User user){
        insertUser(user.getName(), user.getHashPass());
    }

    //ищем пользователя только по имени
    public boolean containsUserName(String login) {
        try {
            PreparedStatement statement = connection.prepareStatement("select * from " + USERS_TABLE + " where login = ?");
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}