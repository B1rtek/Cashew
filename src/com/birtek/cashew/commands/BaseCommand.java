package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.LeaderboardRecord;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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

    public Permission[] adminPermissions = {
            Permission.ADMINISTRATOR
    };

    public Permission[] moderateMembersPermission = {
            Permission.MODERATE_MEMBERS
    };

    public Permission[] manageServerPermission = {
            Permission.MANAGE_SERVER
    };

    protected ArrayList<String> timeUnits = new ArrayList<>() {
        {
            add("seconds");
            add("minutes");
            add("hours");
            add("days");
        }
    };

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

    public static boolean checkPermissions(MessageReceivedEvent event, Permission[] neededPermissions) {
        if (event.isWebhookMessage()) {
            return true;
        }
        if (event.isFromType(ChannelType.PRIVATE)) {
            return true;
        }
        EnumSet<Permission> permissionsSet = Objects.requireNonNull(event.getMember()).getPermissions();
        if (event.getAuthor().getId().equals(Cashew.BIRTEK_USER_ID)) {
            return true;
        }
        if (event.getAuthor().isBot() || event.getAuthor().getId().equals(Cashew.CASHEW_USER_ID)) {
            return false;
        }
        for (Permission neededPermission : neededPermissions) {
            if (!(permissionsSet.contains(neededPermission) || permissionsSet.contains(Permission.ADMINISTRATOR))) {
                return false;
            }
        }
        return true;
    }

    public boolean checkSlashCommandPermissions(SlashCommandInteractionEvent event, Permission[] neededPermissions) {
        if (event.getChannelType() == ChannelType.PRIVATE) {
            return true;
        }
        if (event.getUser().getId().equals(Cashew.BIRTEK_USER_ID)) {
            return true;
        }
        EnumSet<Permission> permissionsSet = Objects.requireNonNull(event.getMember()).getPermissions();
        return verifyCommonPermissionSubset(neededPermissions, permissionsSet, event.getUser());
    }

    public static boolean verifyCommonPermissionSubset(Permission[] neededPermissions, EnumSet<Permission> permissionsSet, User user) {
        if (user.isBot() || user.getId().equals(Cashew.CASHEW_USER_ID)) {
            return false;
        }
        for (Permission neededPermission : neededPermissions) {
            if (!(permissionsSet.contains(neededPermission) || permissionsSet.contains(Permission.ADMINISTRATOR))) {
                return false;
            }
        }
        return true;
    }

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

    public String readURL(URLConnection connection) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) response.append(inputLine);
        in.close();
        return response.toString();
    }

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

    protected boolean isInvalidTimestamp(String timestring) {
        return timestring.length() != 8 || Integer.parseInt(timestring.substring(0, 2)) > 23 || Integer.parseInt(timestring.substring(3, 5)) > 59 || Integer.parseInt(timestring.substring(6, 8)) > 59;
    }

    // https://stackoverflow.com/questions/5673430/java-jtable-change-cell-color
    private static class LeaderboardCellRenderer extends DefaultTableCellRenderer {

        final Color gradientColor;

        public LeaderboardCellRenderer(Color gradientColor) {
            this.gradientColor = gradientColor;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

            //Cells are by default rendered as a JLabel.
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            //Get the status for the current row.
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

    private static class LeaderboardHeaderRenderer extends DefaultTableCellRenderer {

        TableCellRenderer render;
        Border b;

        final Color gradientColor;

        public LeaderboardHeaderRenderer(TableCellRenderer r, Color gradientColor) {
            render = r;
            this.gradientColor = gradientColor;

            //It looks funky to have a different color on each side - but this is what you asked
            //You can comment out borders if you want too. (example try commenting out top and left borders)
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

    protected InputStream generateLeaderboard(ArrayList<LeaderboardRecord> leaderboardRecords, String pointsName, JDA jda, String serverID, Color themeColor, int pointsLength) {
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
        table.getColumnModel().getColumn(2).setMinWidth(pointsLength*15);
        table.setRowHeight(28);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(leaderboardFont);
        table.setFont(leaderboardFont);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(new LeaderboardCellRenderer(themeColor));
        }
        // https://stackoverflow.com/questions/11609900/how-to-make-the-background-of-a-jtable-transparent
        table.setOpaque(false);
        ((DefaultTableCellRenderer) table.getDefaultRenderer(Object.class)).setOpaque(false);
        table.setGridColor(new Color(255, 255, 255, 0));
        table.setShowGrid(false);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setDefaultRenderer(new LeaderboardHeaderRenderer(table.getTableHeader().getDefaultRenderer(), themeColor));
        // https://stackoverflow.com/questions/12477522/jframe-to-image-without-showing-the-jframe
        table.setSize(table.getPreferredSize());
        table.getTableHeader().setSize(table.getTableHeader().getPreferredSize());
        BufferedImage bi = new BufferedImage(table.getWidth(), table.getHeight() + table.getTableHeader().getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bi.createGraphics();
        table.getTableHeader().paint(graphics);
        graphics.translate(0, table.getTableHeader().getHeight());
        table.paint(graphics);
        graphics.dispose();
        // https://stackoverflow.com/questions/4251383/how-to-convert-bufferedimage-to-inputstream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, "png", outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            LOGGER.error("Failed to generate leaderboard " + pointsName);
            return null;
        }
    }

    public static boolean isPrivateChannel(SlashCommandInteractionEvent event) {
        return event.getGuild() == null;
    }
}