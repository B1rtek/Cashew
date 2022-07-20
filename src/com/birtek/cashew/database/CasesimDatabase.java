package com.birtek.cashew.database;

import com.birtek.cashew.commands.CaseSim;
import com.birtek.cashew.commands.SkinInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CasesimDatabase {

    protected static ArrayList<String> createArrayListFromResultSet(ResultSet results) throws SQLException {
        ArrayList<String> list = new ArrayList<>();
        if (results == null) {
            return list;
        }
        while (results.next()) {
            list.add(results.getString(1));
        }
        return list;
    }

    protected static ArrayList<SkinInfo> getSkinsFromResultSet(ResultSet results) throws SQLException {
        ArrayList<SkinInfo> skins = new ArrayList<>();
        while (results.next()) {
            skins.add(new SkinInfo(results.getInt(1), results.getInt(2), results.getString(3), CaseSim.SkinRarity.values()[results.getInt(4)], results.getFloat(5), results.getFloat(6), results.getString(7), results.getString(8), results.getString(9), results.getString(10), results.getString(11), results.getString(12), results.getString(13), results.getString(14), results.getString(15), results.getString(16), results.getString(17), results.getString(18)));
        }
        if (skins.isEmpty()) {
            return null;
        }
        return skins;
    }
}
