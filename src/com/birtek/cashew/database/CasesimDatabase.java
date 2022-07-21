package com.birtek.cashew.database;

import com.birtek.cashew.commands.CaseSim;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CasesimDatabase {

    /**
     * Creates a list of Strings from a ResultSet with a single column of text
     *
     * @param results ResultSet with a single column of text
     * @return ArrayList of Strings extracted from the ResultSet
     * @throws SQLException when an error occurs while reading from the ResultSet
     */
    protected static ArrayList<String> createArrayListFromResultSet(ResultSet results) throws SQLException {
        ArrayList<String> list = new ArrayList<>();
        while (results.next()) {
            list.add(results.getString(1));
        }
        return list;
    }

    /**
     * Creates a list of {@link SkinInfo SkinInfos} from a ResultSet full of columns from Casesim databases
     *
     * @param results ResultSet with columns containing all data needed to create {@link SkinInfo SkinInfo} objects
     * @return ArrayList of {@link SkinInfo SkinInfos} extracted from the ResultSet
     * @throws SQLException when an error occurs while reading from the ResultSet
     */
    protected static ArrayList<SkinInfo> getSkinsFromResultSet(ResultSet results) throws SQLException {
        ArrayList<SkinInfo> skins = new ArrayList<>();
        while (results.next()) {
            skins.add(new SkinInfo(results.getInt(1), results.getInt(2), results.getString(3), CaseSim.SkinRarity.values()[results.getInt(4)], results.getFloat(5), results.getFloat(6), results.getString(7), results.getString(8), results.getString(9), results.getString(10), results.getString(11), results.getString(12), results.getString(13), results.getString(14), results.getString(15), results.getString(16), results.getString(17), results.getString(18)));
        }
        return skins;
    }
}
