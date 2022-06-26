package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.*;

public class BaseCommand extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseCommand.class);

    public Permission[] adminPermissions = {
            Permission.ADMINISTRATOR
    };

    public Permission[] moderateMembersPermission = {
            Permission.MODERATE_MEMBERS
    };

    public Permission[] manageServerPermission = {
            Permission.MANAGE_SERVER
    };

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

        Color blue = new Color(81, 195, 237);
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

            //Cells are by default rendered as a JLabel.
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            //Get the status for the current row.
            if(row >= 0) {
                int targetR = blue.getRed() + (255 - blue.getRed())/10*(10-row);
                int targetG = blue.getGreen() + (255 - blue.getGreen())/10*(10-row);
                int targetB = blue.getBlue() + (255 - blue.getBlue())/10*(10-row);
                l.setBackground(new Color(targetR, targetG, targetB));
            } else l.setBackground(Color.WHITE);


            //Return the JLabel which renders the cell.
            return l;
        }
    }

    private static class LeaderboardHeaderRenderer extends DefaultTableCellRenderer {

        TableCellRenderer render;
        Border b;
        public LeaderboardHeaderRenderer(TableCellRenderer r){
            render = r;

            //It looks funky to have a different color on each side - but this is what you asked
            //You can comment out borders if you want too. (example try commenting out top and left borders)
            b = BorderFactory.createCompoundBorder();
            b = BorderFactory.createCompoundBorder(b, BorderFactory.createMatteBorder(0,0,2,0, new Color(81, 195, 237)));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            JComponent result = (JComponent)render.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            result.setBorder(b);
            return result;
        }
    }

    protected String generateLeaderboard(ArrayList<LeaderboardRecord> leaderboardRecords, String pointsName, JDA jda, String serverID) {
        String[][] tableData = new String[leaderboardRecords.size()][3];
        for(int i=0; i<leaderboardRecords.size(); i++) {
            tableData[i][0] = String.valueOf(leaderboardRecords.get(i).place());
            String userName = leaderboardRecords.get(i).userID();
            try {
                Guild server = jda.getGuildById(serverID);
                assert server != null;
                Member member = server.retrieveMemberById(leaderboardRecords.get(i).userID()).complete();
                userName = member.getEffectiveName();
            } catch (Exception ignored) {}
            tableData[i][1] = userName;
            tableData[i][2] = String.valueOf(leaderboardRecords.get(i).count());
        }
        JTable table = new JTable(tableData, new String[]{"#", "User", pointsName});
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setMinWidth(250);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 24));
        for(int i=0; i<table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(new LeaderboardCellRenderer());
        }
        // https://stackoverflow.com/questions/11609900/how-to-make-the-background-of-a-jtable-transparent
        table.setOpaque(false);
        ((DefaultTableCellRenderer)table.getDefaultRenderer(Object.class)).setOpaque(false);
        table.setGridColor(new Color(255, 255, 255, 0));
        table.setShowGrid(false);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setIntercellSpacing(new Dimension(0,0));
        table.getTableHeader().setDefaultRenderer(new LeaderboardHeaderRenderer(table.getTableHeader().getDefaultRenderer()));
        // https://stackoverflow.com/questions/12477522/jframe-to-image-without-showing-the-jframe
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setBackground(Color.CYAN);
        frame.getContentPane().add(table);
        frame.pack();
        JFrame frame2 = new JFrame();
        frame2.setBackground(Color.WHITE);
        frame2.setUndecorated(true);
        frame2.getContentPane().add(table.getTableHeader());
        frame2.pack();
        BufferedImage bi = new BufferedImage(table.getWidth(), table.getHeight() + table.getTableHeader().getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bi.createGraphics();
        table.getTableHeader().paint(graphics);
        graphics.translate(0, table.getTableHeader().getHeight());
        table.paint(graphics);
        graphics.dispose();
        frame.dispose();
        frame2.dispose();
        // https://coderanch.com/t/338608/java/save-jtable-image
        Random random = new Random();
        String fileName = "generated/leaderboardTable" + random.nextInt(10000) + ".png";
        try {
            ImageIO.write(bi, "png", new File(fileName));
            return fileName;
        } catch (IOException e) {
            LOGGER.error("Failed to generate leaderboard " + pointsName);
            return null;
        }
    }
}