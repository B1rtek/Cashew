package com.birtek.cashew.database;

/**
 * Stores a record from the reactions database
 *
 * @param id            ID of the reaction
 * @param name          name of the reaction
 * @param description   description of the reaction
 * @param pattern       pattern that when spotted in a message will trigger the reaction
 * @param actionID      ID of the action to execute when the pattern is spotted and reactions are turned on
 * @param actionContent content of the action to execute
 */
public record Reaction(int id, String name, String description, String pattern, int actionID, String actionContent) {
}
