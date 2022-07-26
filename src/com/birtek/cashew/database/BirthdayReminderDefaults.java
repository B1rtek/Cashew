package com.birtek.cashew.database;

/**
 * Class describing default behaviour settings for BirthdayReminders on a server
 *
 * @param serverID  ID of the server to which these settings belong
 * @param channelID ID of the channel set as the default one for reminders
 * @param override  if set to true will redirect all reminders to the channel with the ID above
 */
public record BirthdayReminderDefaults(String serverID, String channelID, boolean override) {
}
