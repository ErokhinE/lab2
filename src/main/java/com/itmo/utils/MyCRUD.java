package com.itmo.utils;

import com.itmo.collection.Dragon;

import java.sql.SQLException;
import java.util.Set;

public interface MyCRUD {
    boolean insertDragon(Dragon d);
    void insertUser(String login, String passHash);
    Set<Dragon> getCollectionFromDatabase() throws SQLException;
    boolean deleteDragonById(long id);
}
