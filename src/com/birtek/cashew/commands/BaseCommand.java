package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.LeaderboardRecord;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLConnection;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BaseCommand extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseCommand.class);
    private static Font leaderboardFont;

    static {
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        try {
            leaderboardFont = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/NotoSansDisplay-Regular.ttf")).deriveFont(Font.PLAIN, 24);
        } catch (FontFormatException | IOException e) {
            LOGGER.error("Failed to load the custom leaderboards font!");
        }
    }

    protected ArrayList<String> timeUnits = new ArrayList<>() {
        {
            add("seconds");
            add("minutes");
            add("hours");
            add("days");
        }
    };

    /**
     * Checks whether the command can be executed by a user
     *
     * @param event        {@link SlashCommandInteractionEvent event} that triggered one of the slash command event listeners
     * @param isModCommand if set to true, user will be checked against moderator permissions
     * @return true if the command can be executed, false if the command is turned off, or if the user has no permissions to
     * execute the command
     */
    public static boolean cantBeExecuted(SlashCommandInteractionEvent event, boolean isModCommand) {
        if (!event.isFromGuild()) {
            return false;
        }
        if (isModCommand) {
            return !Objects.requireNonNull(event.getMember()).hasPermission(Cashew.moderatorPermission);
        } else {
            return !Cashew.commandsSettingsManager.getCommandSettings(Objects.requireNonNull(event.getGuild()).getId(), event.getChannel().getId(), event.getName());
        }
    }

    /**
     * Checks whether the command can be executed by a user, used only by prefix commands which will later be removed at some point
     *
     * @param event        {@link MessageReceivedEvent event} that triggered one of the slash command event listeners
     * @param command      name of the executed command
     * @param isModCommand if set to true, user will be checked against moderator permissions
     * @return true if the command can be executed, false if the command is turned off, or if the user has no permissions to
     * execute the command
     */
    public static boolean cantBeExecutedPrefix(MessageReceivedEvent event, String command, boolean isModCommand) {
        if (!event.isFromGuild()) {
            return false;
        }
        if (isModCommand) {
            return !Objects.requireNonNull(event.getMember()).hasPermission(Cashew.moderatorPermission);
        } else {
            return !Cashew.commandsSettingsManager.getCommandSettings(Objects.requireNonNull(event.getGuild()).getId(), event.getChannel().getId(), command);
        }
    }

    /**
     * Generates a list of autocompleted options that match the user input
     *
     * @param options ArrayList of Strings representing available options
     * @param typed   a String representing user input
     * @return an ArrayList of matching options, or an ArrayList with a single entry containing the phrase
     * "There's more than 25 matching options" if there were more options that can be displayed with
     * {@link net.dv8tion.jda.api.interactions.callbacks.IAutoCompleteCallback#replyChoices(Collection) replyChoices}
     */
    public static ArrayList<String> autocompleteFromList(ArrayList<String> options, String typed) {
        ArrayList<String> matching = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().contains(typed.toLowerCase(Locale.ROOT))) {
                matching.add(option);
            }
        }
        if (matching.size() > 25) {
            matching = new ArrayList<>() {
                {
                    add("There's more than 25 matching options");
                }
            };
        }
        return matching;
    }

    /**
     * Calculates target LocalDateTime for a specified delay
     *
     * @param time time in a unit specified in the second argument
     * @param unit unit of the amount of time from the first argument
     * @return String with a formatted calulcated LocalDateTime
     */
    protected String calculateTargetTime(int time, String unit) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Warsaw"));
        switch (unit) {
            case "seconds" -> now = now.plusSeconds(time);
            case "minutes" -> now = now.plusMinutes(time);
            case "hours" -> now = now.plusHours(time);
            case "days" -> now = now.plusDays(time);
        }
        return now.format(dateTimeFormatter);
    }

    /**
     * Calculates target Instant for a specified delay
     *
     * @param time time in a unit specified in the second argument
     * @param unit unit of the amount of time from the first argument
     * @return Instant with an added delay
     */
    protected Instant calculateInstantTargetTime(int time, String unit) {
        Instant now = Instant.now();
        switch (unit) {
            case "seconds" -> now = now.plusSeconds(time);
            case "minutes" -> now = now.plusSeconds(60L * time);
            case "hours" -> now = now.plusSeconds(3600L * time);
            case "days" -> now = now.plusSeconds(24L * 3600 * time);
        }
        return now;
    }

    /**
     * Method used to check whether a string contains an integer, although the integer doesn't have a length limit
     *
     * @param strNum String with potentially a number in it
     * @return true if the given String has an integer in it, false if it doesn't
     */
    public boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        if (strNum.isEmpty()) {
            return true;
        }
        int start = 0;
        if (strNum.charAt(0) == '-') {
            start = 1;
        }
        for (int i = start; i < strNum.length(); i++) {
            if (strNum.charAt(i) > '9' || strNum.charAt(i) < '0') {
                return false;
            }
        }
        return true;
    }

    /**
     * Inspired by a similar Kotlin method, connects with an URL and reads text from it
     *
     * @param connection {@link URLConnection URLConnection} for a request
     * @return read response in a String
     */
    public static String readURL(URLConnection connection) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) response.append(inputLine);
        in.close();
        return response.toString();
    }

    /**
     * Gets the current page number from an embed with the text "page x out of y" in the footer
     *
     * @param embed {@link MessageEmbed MessageEmbed} with the text "page x out of y" in the footer
     * @return integer with the page number (counting from 1)
     */
    protected int getPageNumber(MessageEmbed embed) {
        return Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(embed.getFooter()).getText()).split("\\s+")[1]);
    }

    /**
     * Returns the index of the selected item from a list embed
     *
     * @param listEmbed {@link MessageEmbed MessageEmbed} with a list made of fields
     * @return index of the selected (underlined) item, counting from zero, or -1 if no items were selected
     */
    protected int getSelectedItemIndex(MessageEmbed listEmbed) {
        int selectedItemIndex = -1, index = 0;
        for (MessageEmbed.Field field : listEmbed.getFields()) {
            if (Objects.requireNonNull(field.getName()).startsWith("__")) {
                selectedItemIndex = index;
                break;
            }
            index++;
        }
        return selectedItemIndex;
    }

    /**
     * Checks whether the given timestamp is in HH:MM:SS format
     *
     * @param timestring String with potentially a timestamp in it
     * @return true if the input contains an invalid timestamp, false if it's valid
     */
    protected boolean isInvalidTimestamp(String timestring) {
        return timestring.length() != 8 || Integer.parseInt(timestring.substring(0, 2)) > 23 || Integer.parseInt(timestring.substring(3, 5)) > 59 || Integer.parseInt(timestring.substring(6, 8)) > 59;
    }

    /**
     * Generates an InputStream containing an image of a leaderboard
     *
     * @param leaderboardRecords an ArrayList of {@link LeaderboardRecord LeaderboardRecords} with the place of the
     *                           record on the leaderboard, the name of the record (usually userID) and points that they
     *                           gathered
     * @param pointsName         name of the points that is used as a header for the points column
     * @param jda                JDA instance used to obtain usernames from userIDs
     * @param serverID           ID of the server used to obtain usernames from userIDs
     * @param themeColor         color to apply to table cells gradient
     * @return an InputStream with an image of the leaderboard or null if an error occurred
     */
    protected InputStream generateLeaderboard(ArrayList<LeaderboardRecord> leaderboardRecords, String pointsName, JDA jda, String serverID, Color themeColor) {
        String[][] tableData = new String[leaderboardRecords.size()][3];
        for (int i = 0; i < leaderboardRecords.size(); i++) {
            tableData[i][0] = String.valueOf(leaderboardRecords.get(i).place());
            String userName = leaderboardRecords.get(i).userID();
            try {
                Guild server = jda.getGuildById(serverID);
                assert server != null;
                Member member = server.retrieveMemberById(leaderboardRecords.get(i).userID()).complete();
                userName = member.getEffectiveName();
            } catch (Exception ignored) {
            }
            tableData[i][1] = userName;
            tableData[i][2] = String.valueOf(leaderboardRecords.get(i).count());
        }
        JTable table = new JTable(tableData, new String[]{"#", "User", pointsName});
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setMinWidth(250);
        table.getColumnModel().getColumn(2).setMinWidth(calculatePointsWidth(leaderboardRecords));
        table.setRowHeight(28);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(leaderboardFont);
        table.setFont(leaderboardFont);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(new LeaderboardCellRenderer(themeColor));
        }
        table.setOpaque(false);
        ((DefaultTableCellRenderer) table.getDefaultRenderer(Object.class)).setOpaque(false);
        table.setGridColor(new Color(255, 255, 255, 0));
        table.setShowGrid(false);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setDefaultRenderer(new LeaderboardHeaderRenderer(table.getTableHeader().getDefaultRenderer(), themeColor));
        table.setSize(table.getPreferredSize());
        table.getTableHeader().setSize(table.getTableHeader().getPreferredSize());
        BufferedImage bi = new BufferedImage(table.getWidth(), table.getHeight() + table.getTableHeader().getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bi.createGraphics();
        table.getTableHeader().paint(graphics);
        graphics.translate(0, table.getTableHeader().getHeight());
        table.paint(graphics);
        graphics.dispose();
        InputStream result = convertToInputStream(bi);
        if (result == null) {
            LOGGER.error("Failed to generate leaderboard " + pointsName);
        }
        return result;
    }

    /**
     * Converts a {@link BufferedImage BufferedImage} to an {@link InputStream InputStream
     */
    private static InputStream convertToInputStream(BufferedImage bi) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, "png", outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Calculates points column width for the leaderboard table
     *
     * @param leaderboardRecords ArrayList with {@link LeaderboardRecord LeaderboardRecords} for the leaderboard
     * @return width in pixels of the points column
     */
    private int calculatePointsWidth(ArrayList<LeaderboardRecord> leaderboardRecords) {
        int width = 0;
        for (LeaderboardRecord record : leaderboardRecords) {
            width = Math.max(width, String.valueOf(record.count()).length());
        }
        return width * 15;
    }

    /**
     * Generates an InputStream with an image of a piechart
     *
     * @param distribution an ArrayList of Pairs of Strings and Integers, which represent pairs of labels and their
     *                     points
     * @param colorMap     a HashMap with Strings corresponding to series' colors
     * @param title        title of the piechart, only for debugging purposes
     * @return an InputStream with an image of a piechart or null if an error occurred
     */
    public static InputStream generatePiechart(ArrayList<Pair<String, Integer>> distribution, HashMap<String, Color> colorMap, String title) {
        BufferedImage bi = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bi.createGraphics();
        double total = 0.0;
        for (Pair<String, Integer> record : distribution) {
            total += record.getRight();
        }
        ArrayList<Integer> angles = new ArrayList<>();
        angles.add(0);
        double curTotal = 0.0;
        for (Pair<String, Integer> record : distribution) {
            curTotal += record.getRight();
            angles.add((int) Math.round(curTotal * 360.0 / total));
        }
        if (angles.get(angles.size() - 1) != 360) angles.set(angles.size() - 1, 360);
        graphics.setFont(leaderboardFont);
        int degreeIndex = 1, startAngle, arcAngle;
        for (Pair<String, Integer> slice : distribution) {
            startAngle = angles.get(degreeIndex - 1);
            arcAngle = angles.get(degreeIndex) - startAngle;
            graphics.setColor(colorMap.get(slice.getLeft()));
            graphics.fillArc(0, 0, 500, 500, startAngle, arcAngle);
            degreeIndex++;
        }
        graphics.setColor(Color.BLACK);
        degreeIndex = 1;
        for (Pair<String, Integer> slice : distribution) {
            startAngle = angles.get(degreeIndex - 1);
            arcAngle = angles.get(degreeIndex) - startAngle;
            Pair<Integer, Integer> position = calculateTextPositionOnCircle(Pair.of(250, 250), 250, startAngle + arcAngle / 2 + 90, 0.75, slice.getLeft(), 12, 24);
            graphics.drawString(slice.getLeft(), position.getLeft(), position.getRight());
            String percentage = Math.round((double) slice.getRight() * 100.0 / total * 100.0) / 100.0 + " %";
            position = calculatePercentagePosition(position, slice.getLeft(), percentage, 12, 24);
            graphics.drawString(percentage, position.getLeft(), position.getRight());
            degreeIndex++;
        }
        graphics.dispose();
        InputStream result = convertToInputStream(bi);
        if (result == null) {
            LOGGER.error("Failed to generate piechart " + title);
        }
        return result;
    }

    /**
     * Calculates the position of a text label for the piechart
     *
     * @param center          a Pair of Integers with coordinates of the piechart's center
     * @param radius          radius of the piechart circle
     * @param angle           angle specifying the direction in which the text should be offset from the center
     * @param awayFromCenter  distance from the center of the circle where the text should be placed as a fraction of the radius
     * @param text            text that will be placed
     * @param characterWidth  width of the characters
     * @param characterHeight height of the characters
     * @return coordinates for the drawString() method of Graphics2D which will place the text centered on the X axis, above the point on the radius
     */
    private static Pair<Integer, Integer> calculateTextPositionOnCircle(Pair<Integer, Integer> center, int radius, int angle, double awayFromCenter, String text, int characterWidth, int characterHeight) {
        int x = (int) (Math.round(Math.sin((double) angle * Math.PI / 180.0) * radius * awayFromCenter) + center.getLeft());
        x -= text.length() * characterWidth / 2;
        int y = (int) Math.round(Math.cos((double) angle * Math.PI / 180.0) * radius * awayFromCenter) + center.getRight();
        return Pair.of(x, y);
    }

    /**
     * Calculates the position of a percentage label for the piechart
     *
     * @param labelOrigin     coordinates of the text label corresponding to this percentage label
     * @param label           text label under which this percentage label will be placed
     * @param percentageLabel this percentage label
     * @param characterWidth  width of the characters
     * @param characterHeight height of the characters
     * @return coordinates for the drawString() method of Graphics2D which will place the percentage label centered under the text label
     */
    private static Pair<Integer, Integer> calculatePercentagePosition(Pair<Integer, Integer> labelOrigin, String label, String percentageLabel, int characterWidth, int characterHeight) {
        int x = labelOrigin.getLeft() + (label.length() - percentageLabel.length()) * characterWidth / 2;
        int y = labelOrigin.getRight() + characterHeight;
        return Pair.of(x, y);
    }

    /**
     * Renderer for rows in the leaderboards, applies a gradient from white to the given color according to the row
     * drawn
     * Source used: <a href="https://stackoverflow.com/questions/5673430/java-jtable-change-cell-color">Java JTable change cell color</a>
     * Thank you  <a href="https://stackoverflow.com/users/355232/sbrattla">sbrattla</a>!
     */
    private static class LeaderboardCellRenderer extends DefaultTableCellRenderer {

        final Color gradientColor;

        public LeaderboardCellRenderer(Color gradientColor) {
            this.gradientColor = gradientColor;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

            //Cells are by default rendered as a JLabel.
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            if (row >= 0) {
                int targetR = gradientColor.getRed() + (255 - gradientColor.getRed()) / 10 * (10 - row);
                int targetG = gradientColor.getGreen() + (255 - gradientColor.getGreen()) / 10 * (10 - row);
                int targetB = gradientColor.getBlue() + (255 - gradientColor.getBlue()) / 10 * (10 - row);
                l.setBackground(new Color(targetR, targetG, targetB));
            } else l.setBackground(Color.WHITE);


            //Return the JLabel which renders the cell.
            return l;
        }
    }

    /**
     * Renderer for headers in the leaderboards, paints a nice border between the header and rows
     * Source used: <a href="https://stackoverflow.com/questions/12837574/how-is-it-possible-to-give-jtable-cell-borderleft-right-top-bottom-of-differen">How is it possible to give JTable cell border(Left,right,Top,Bottom) of different color?</a>
     * Thank you  <a href="https://stackoverflow.com/users/1515592/nick-rippe">Nick Rippe</a>!
     */
    private static class LeaderboardHeaderRenderer extends DefaultTableCellRenderer {

        TableCellRenderer render;
        Border b;

        final Color gradientColor;

        public LeaderboardHeaderRenderer(TableCellRenderer r, Color gradientColor) {
            render = r;
            this.gradientColor = gradientColor;

            b = BorderFactory.createCompoundBorder();
            b = BorderFactory.createCompoundBorder(b, BorderFactory.createMatteBorder(0, 0, 2, 0, gradientColor));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            JComponent result = (JComponent) render.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            result.setBorder(b);
            return result;
        }
    }
}