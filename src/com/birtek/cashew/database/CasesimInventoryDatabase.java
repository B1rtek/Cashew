package com.birtek.cashew.database;

import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

public class CasesimInventoryDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasesimInventoryDatabase.class);

    private static volatile CasesimInventoryDatabase instance;

    private Connection casesimInventoryConnection;

    /**
     * Initializes the connection to the Postgres database, specifically to the
     * casesiminventory table
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     * <p>
     * Inventory JSON scheme:
     * {
     * "casesop": 12,
     * "colsop": 1,
     * "capsop": 3,
     * "public": true,
     * "items": [
     * {
     * "org": 123123123123123123,
     * "type": 1,
     * "id": 10,
     * "float": 0.08013371337,
     * "st": true
     * },
     * {
     * "org": 123123123123123124,
     * "type": 1,
     * "id": 2,
     * "float": 0.000101231323,
     * "st": false
     * },
     * {
     * "origin": 123123123123123125,
     * "type": 3,
     * "id": 120,
     * "float": 0.000102137001,
     * },
     * {
     * "origin": 123123123123123126,
     * "type": 4,
     * "id": 952,
     * }
     * ]
     * }
     * <p>
     * casesop, colsop and capsop are counts of opened cases, collections and capsules. public tells whether the
     * inventory is public, by default it's private for every new inventory. Items are stored in the items list, each
     * of them has an org value, which is the ID of the message from which the item was obtained, type which specifies
     * what type of item it is - a case item (1), a knife (2), a collection item (3) or a capsule item (4). ID is the
     * ID from the table corresponding to the item type for that item. All items apart from ones from capsules have the
     * float parameter, and all case and knife items also have a stattrak parameter.
     */
    private CasesimInventoryDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Missing Postgres JDBC driver");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            casesimInventoryConnection = DriverManager.getConnection(System.getenv("JDBC_DATABASE_URL"));
        } catch (SQLException e) {
            LOGGER.error("Couldn't connect to the Postgres database - database could be offline or the url might be wrong or being currently refreshed");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            PreparedStatement preparedStatement = casesimInventoryConnection.prepareStatement("CREATE TABLE IF NOT EXISTS casesiminventory(userid text, inventory text)");
            preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.error("Failed to create the casesiminventory table");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static CasesimInventoryDatabase getInstance() {
        CasesimInventoryDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (CasesimInventoryDatabase.class) {
            if (instance == null) {
                instance = new CasesimInventoryDatabase();
            }
            return instance;
        }
    }

    /**
     * Checks whether a user has a record in the database
     *
     * @param userID ID of the user to check for
     * @return true if the user has a record in the inventory database, false if it's not or if an error occurred
     */
    private boolean isInDatabase(String userID) {
        try {
            PreparedStatement preparedStatement = casesimInventoryConnection.prepareStatement("SELECT COUNT(*) FROM casesiminventory WHERE userid = ?");
            preparedStatement.setString(1, userID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1) == 1;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimInventoryDatabase.isInDatabase()");
            return false;
        }
    }

    /**
     * Obtains the JSON representing a user's inventory
     *
     * @param userID ID of the user whose inventory will be obtained
     * @return a JSONObject with user's inventory in it or null if an error occurred or if it doesn't exist. To check
     * for existence, use {@link #isInDatabase(String) isInDatabase()}
     */
    private JSONObject getInventoryJSON(String userID) {
        try {
            PreparedStatement preparedStatement = casesimInventoryConnection.prepareStatement("SELECT inventory FROM casesiminventory WHERE userid = ?");
            preparedStatement.setString(1, userID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new JSONObject(results.getString(1));
            }
            return createEmptyInventory();
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimInventoryDatabase.getInventoryJSON()");
            return null;
        }
    }

    /**
     * Inserts a new inventory JSON into the database
     *
     * @param userID    ID of the user to whom the inventory belongs to
     * @param inventory JSONObject representing the inventory
     * @return true if the insertion was successful, false otherwise
     */
    private boolean insertInventoryJSON(String userID, JSONObject inventory) {
        String jsonString = inventory.toString();
        if (jsonString == null) return false;
        try {
            PreparedStatement preparedStatement = casesimInventoryConnection.prepareStatement("INSERT INTO casesiminventory(userid, inventory) VALUES(?, ?)");
            preparedStatement.setString(1, userID);
            preparedStatement.setString(2, jsonString);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimInventoryDatabase.insertInventoryJSON()");
            return false;
        }
    }

    /**
     * Updates the inventory JSON in the database
     *
     * @param userID    ID of the user to whom the inventory belongs to
     * @param inventory JSONObject representing the inventory
     * @return true if the update was successful, false otherwise
     */
    private boolean updateInventoryJSON(String userID, JSONObject inventory) {
        String jsonString = inventory.toString();
        if (jsonString == null) return false;
        try {
            PreparedStatement preparedStatement = casesimInventoryConnection.prepareStatement("UPDATE casesiminventory SET inventory = ? WHERE userid = ?");
            preparedStatement.setString(1, jsonString);
            preparedStatement.setString(2, userID);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimInventoryDatabase.updateInventoryJSON()");
            return false;
        }
    }

    /**
     * Creates an empty inventory JSON
     *
     * @return a JSONObject with all fields initialized
     */
    private JSONObject createEmptyInventory() {
        JSONObject inventory = new JSONObject();
        inventory.put("casesop", 0);
        inventory.put("colsop", 0);
        inventory.put("capsop", 0);
        inventory.put("public", false);
        inventory.put("items", new JSONArray());
        return inventory;
    }

    /**
     * Checks whether the user has their inventory set as public
     *
     * @param userID ID of the user to check
     * @return true if the inventory is public, false if it's not, if it doesn't yet exist or if an error occurred
     */
    public boolean isPrivateInventory(String userID) {
        JSONObject inventory = getInventoryJSON(userID);
        if (inventory == null) return true;
        try {
            return !inventory.getBoolean("public");
        } catch (JSONException e) {
            LOGGER.warn(e + " thrown at CasesimInventoryDatabase.isPublicInventory()");
            return true;
        }
    }

    /**
     * Updates the inventory visibility setting - either private (false) or public (true) - a public inventory can be
     * viewed by everyone
     *
     * @param userID   ID of the user who's changing their settings
     * @param isPublic if set to true, the inventory will be made public, otherwise it'll be made private
     * @return true if changing the setting was successful, false otherwise
     */
    public boolean setInventoryVisibility(String userID, boolean isPublic) {
        JSONObject inventory = getInventoryJSON(userID);
        if (inventory == null) return false;
        inventory.put("public", isPublic);
        if (!isInDatabase(userID)) {
            return insertInventoryJSON(userID, inventory);
        } else {
            return updateInventoryJSON(userID, inventory);
        }
    }

    /**
     * Gets user's inventory stats, containing information about opened cases, collections and capsules and the amount of items in their inventory
     *
     * @param requestingUserID ID of the user who requested the data
     * @param requestedUserID  ID of the user to get the stats of
     * @return a {@link CasesimInvStats CasesimInvStats} record containing all the information above, with all values
     * set to -1 if the inventory is private, and to null if an error occurred
     */
    public CasesimInvStats getInventoryStats(String requestingUserID, String requestedUserID) {
        if (!requestedUserID.equals(requestingUserID) && isPrivateInventory(requestedUserID))
            return new CasesimInvStats(-1, -1, -1, -1, false);
        try {
            JSONObject inventory = getInventoryJSON(requestedUserID);
            if (inventory == null) return null;
            return new CasesimInvStats(inventory.getInt("casesop"), inventory.getInt("colsop"), inventory.getInt("capsop"), inventory.getJSONArray("items").length(), inventory.getBoolean("public"));
        } catch (JSONException e) {
            LOGGER.warn(e + " thrown at CasesimInventoryDatabase.getInventoryStats()");
            return null;
        }
    }

    /**
     * Gets a desired page of a user's inventory from the
     *
     * @param requestingUserID ID of the user who requested the data
     * @param requestedUserID  ID of the user to get the stats of
     * @param page             number of the page to obtain
     * @return an ArrayList of pairs of {@link SkinData SkinData} and {@link SkinInfo SkinInfo} objects representing items, null if an error occurred, an empty
     * ArrayList if the page doesn't exist, or an ArrayList with a single pair of nulls if the inventory is private
     */
    public ArrayList<Pair<SkinData, SkinInfo>> getInventoryPage(String requestingUserID, String requestedUserID, int page) {
        if (!requestedUserID.equals(requestingUserID) && isPrivateInventory(requestedUserID)) {
            ArrayList<Pair<SkinData, SkinInfo>> privateInv = new ArrayList<>();
            privateInv.add(Pair.of(null, null));
            return privateInv;
        }
        try {
            // get the SkinData objects from the database
            JSONObject inventory = getInventoryJSON(requestedUserID);
            if (inventory == null) return null;
            ArrayList<SkinData> skinDatas = new ArrayList<>();
            JSONArray itemsArray = inventory.getJSONArray("items");
            for (int i = (page - 1) * 10; i < itemsArray.length() && i < page * 10; i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                int itemType = item.getInt("type");
                boolean statTrak = false;
                float floatValue = 0;
                if (itemType < 4) {
                    floatValue = item.getFloat("float");
                    if (itemType < 3) {
                        statTrak = item.getBoolean("st");
                    }
                }
                skinDatas.add(new SkinData(item.getString("org"), itemType, item.getInt("id"), floatValue, statTrak));
            }
            // and then their corresponding SkinInfos
            ArrayList<Pair<SkinData, SkinInfo>> items = new ArrayList<>();
            CasesimCasesDatabase casesDatabase = CasesimCasesDatabase.getInstance();
            CasesimCollectionsDatabase collectionsDatabase = CasesimCollectionsDatabase.getInstance();
            CasesimCapsulesDatabase capsulesDatabase = CasesimCapsulesDatabase.getInstance();
            for (SkinData item : skinDatas) {
                SkinInfo skin = switch (item.containterType()) {
                    case 1 -> casesDatabase.getSkinById(item.id());
                    case 2 -> casesDatabase.getKnifeById(item.id());
                    case 3 -> collectionsDatabase.getSkinById(item.id());
                    case 4 -> capsulesDatabase.getItemById(item.id());
                    default -> null;
                };
                if (skin == null) return null;
                items.add(Pair.of(item, skin));
            }
            return items;
        } catch (JSONException e) {
            LOGGER.warn(e + " thrown at CasesimInventoryDatabase.getInventoryPage()");
            return null;
        }
    }

    /**
     * Gets an item from the inventory by the index of the item
     *
     * @param requestingUserID ID of the user who requested the item
     * @param requestedUserID  ID of the user's from whose inventory the item will be returned
     * @param index            index of the item in the inventory (counting from zero)
     * @return a Pair of {@link SkinData SkinData} and {@link SkinInfo SkinInfo} objects representing the item, null if
     * an error occurred, a pair of nulls if the inventory is private and a non-null SkinData with a null SkinInfo if
     * the item doesn't exist (index out of range of the inventory)
     */
    public Pair<SkinData, SkinInfo> getItemByIndex(String requestingUserID, String requestedUserID, int index) {
        if (!requestedUserID.equals(requestingUserID) && isPrivateInventory(requestedUserID)) {
            return Pair.of(null, null);
        }
        try {
            // get the SkinData object
            JSONObject inventory = getInventoryJSON(requestedUserID);
            if (inventory == null) return null;
            ArrayList<SkinData> skinDatas = new ArrayList<>();
            JSONArray itemsArray = inventory.getJSONArray("items");
            if (itemsArray.length() <= index) {
                return Pair.of(new SkinData("", -1, -1, 0, false), null);
            }
            JSONObject item = itemsArray.getJSONObject(index);
            int itemType = item.getInt("type");
            boolean statTrak = false;
            float floatValue = 0;
            if (itemType < 4) {
                floatValue = item.getFloat("float");
                if (itemType < 3) {
                    statTrak = item.getBoolean("st");
                }
            }
            SkinData skinData = new SkinData(item.getString("org"), itemType, item.getInt("id"), floatValue, statTrak);
            // and then its corresponding SkinInfo
            CasesimCasesDatabase casesDatabase = CasesimCasesDatabase.getInstance();
            CasesimCollectionsDatabase collectionsDatabase = CasesimCollectionsDatabase.getInstance();
            CasesimCapsulesDatabase capsulesDatabase = CasesimCapsulesDatabase.getInstance();
            SkinInfo skinInfo = switch (skinData.containterType()) {
                case 1 -> casesDatabase.getSkinById(skinData.id());
                case 2 -> casesDatabase.getKnifeById(skinData.id());
                case 3 -> collectionsDatabase.getSkinById(skinData.id());
                case 4 -> capsulesDatabase.getItemById(skinData.id());
                default -> null;
            };
            if (skinInfo == null) return null;
            return Pair.of(skinData, skinInfo);
        } catch (JSONException e) {
            LOGGER.warn(e + " thrown at CasesimInventoryDatabase.getItemByIndex()");
            return null;
        }
    }

    /**
     * Removes an item from user's inventory
     *
     * @param userID ID of the user removing the item from their inventory
     * @param index  index of the item in their inventory
     * @return true if the removal was successful, false otherwise
     */
    public boolean removeItemByIndex(String userID, int index) {
        JSONObject inventory = getInventoryJSON(userID);
        if (inventory == null) return false;
        try {
            JSONArray items = inventory.getJSONArray("items");
            if (items.length() >= index) return false;
            items.remove(index);
            inventory.put("items", items);
        } catch (JSONException e) {
            LOGGER.warn(e + " thrown at CasesimInventoryDatabase.removeItemByIndex()");
            return false;
        }
        if (!isInDatabase(userID)) {
            return insertInventoryJSON(userID, inventory);
        } else {
            return updateInventoryJSON(userID, inventory);
        }
    }

    /**
     * Adds count to the opened containers statistics
     *
     * @param userID        ID of the user who opened the container
     * @param containerType type of the container, 1 and 2 are case, 3 is collection and 4 is capsule
     * @return true if the modification was successful, false otherwise
     */
    public boolean addOpenedContainer(String userID, int containerType) {
        JSONObject inventory = getInventoryJSON(userID);
        if (inventory == null) return false;
        try {
            switch (containerType) {
                case 1, 2 -> {
                    int casesOpened = inventory.getInt("casesop");
                    inventory.put("casesop", casesOpened + 1);
                }
                case 3 -> {
                    int casesOpened = inventory.getInt("colsop");
                    inventory.put("colsop", casesOpened + 1);
                }
                case 4 -> {
                    int casesOpened = inventory.getInt("capsop");
                    inventory.put("capsop", casesOpened + 1);
                }
            }
        } catch (JSONException e) {
            LOGGER.warn(e + " thrown at CasesimInventoryDatabase.addOpenedContainer()");
            return false;
        }
        if (!isInDatabase(userID)) {
            return insertInventoryJSON(userID, inventory);
        } else {
            return updateInventoryJSON(userID, inventory);
        }
    }

    /**
     * Adds a new item to the inventory
     *
     * @param userID  ID of the user who is adding the item into their inventory
     * @param newItem a {@link SkinData SkinData} object of the item being added
     * @return 1 if the addition was successful, 0 if the inventory is full, -1 if an error occurred, and -2 if the item
     * was already obtained (items can be readded after deletion though)
     */
    public int addToInventory(String userID, SkinData newItem) {
        JSONObject inventory = getInventoryJSON(userID);
        if (inventory == null) return -1;
        try {
            JSONArray items = inventory.getJSONArray("items");
            if (items.length() == 100) return 0;
            // check whether the item was already added
            for (int i = 0; i < items.length(); i++) {
                String origin = items.getJSONObject(i).getString("org");
                if (Objects.equals(origin, newItem.originMessageID())) return -2;
            }
            // if the check went through, add the item
            JSONObject item = new JSONObject();
            item.put("org", newItem.originMessageID());
            item.put("type", newItem.containterType());
            item.put("id", newItem.id());
            if (newItem.containterType() < 4) {
                item.put("float", newItem.floatValue());
                if (newItem.containterType() < 3) {
                    item.put("st", newItem.statTrak());
                }
            }
            items.put(item);
            inventory.put("items", items);
        } catch (JSONException e) {
            LOGGER.warn(e + " thrown at CasesimInventoryDatabase.addToInventory()");
            return -1;
        }
        if (!isInDatabase(userID)) {
            return insertInventoryJSON(userID, inventory) ? 1 : -1;
        } else {
            return updateInventoryJSON(userID, inventory) ? 1 : -1;
        }
    }
}
