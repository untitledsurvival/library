package com.untitledsurvival.lib.scoreboard;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerScoreboard {
    private final Scoreboard realScoreboard;
    private final Objective objective;
    @Getter private final UUID playerUUID;
    @Getter private final List<String> lines;
    @Getter private String displayName;

    private boolean isDisplayed;
    private final List<String> lastUpdateLines;

    PlayerScoreboard(Player player) {
        this.playerUUID = player.getUniqueId();

        this.realScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        // make the objective
        this.objective = realScoreboard.registerNewObjective("vapisb", "dummy", "");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        this.lines = new ArrayList<>();
        this.displayName = "&bV-API Default";

        this.isDisplayed = false;
        this.lastUpdateLines = new ArrayList<>();
    }

    public void show() {
        // set the player's scoreboard
        getPlayer().setScoreboard(realScoreboard);
        // update the local value as to whether or not it's being displayed
        isDisplayed = true;
    }

    public void hide() {
        if (!isDisplayed) {
            return;
        }
        // set the player's scoreboard to a new scoreboard (hides any scoreboard, no check is implemented)
        getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        // update and let it be known that this scoreboard is no longer being seen
        isDisplayed = false;
        // remove the last updated lines to allow initialization
        this.lastUpdateLines.clear();
    }

    public void updateLines() {
        if (!isDisplayed) {
            return; // don't do anything if the user cannot see the scoreboard
        }

        int newLineCount = this.lines.size() - this.lastUpdateLines.size();
        List<String> reversedLines = new ArrayList<>(lines);
        Collections.reverse(reversedLines);

        // set the new latest updated lines
        this.lastUpdateLines.clear();
        this.lastUpdateLines.addAll(lines);

        // reinit/init
        if (lastUpdateLines.size() <= 0 || newLineCount != 0) {
            // remove all existing teams and lines (it's easier this way)
            realScoreboard.getTeams().forEach(Team::unregister);
            realScoreboard.getEntries().forEach(realScoreboard::resetScores);

            // loop through each line and set it
            for (int i = 0; i < lines.size(); i++) {
                // start at the bottom and make your way up
                String lineString = reversedLines.get(i);
                // get the invisible "player name" and team that goes with it
                String colorCode = Objects.requireNonNull(ScoreboardAPI.getColorByLine(i)).toString();
                Team team = realScoreboard.getTeam(ScoreboardAPI.getTeamNameByLine(i));

                if (team == null) {
                    team = realScoreboard.registerNewTeam(ScoreboardAPI.getTeamNameByLine(i));
                    team.addEntry(colorCode);
                }

                Score score = objective.getScore(colorCode);
                score.setScore(i + 1);
                setTeamContent(team, lineString);
            }

            return;
        }

        // handle existing lines
        for (int i = 0; i < this.lines.size(); i++) {
            // start at the bottom and make your way up
            String lineString = reversedLines.get(i);
            // get the invisible "player name" and team that goes with it
            String colorCode = Objects.requireNonNull(ScoreboardAPI.getColorByLine(i)).toString();
            Team team = realScoreboard.getTeam(ScoreboardAPI.getTeamNameByLine(i));

            if (team == null || objective.getScore(colorCode).getScore() != i + 1) {
                continue; // skip this line since the team doesn't exist or the score does not match
            }

            setTeamContent(team, lineString);
        }
    }

    public void setLines(List<String> lines) {
        setLines(lines, isDisplayed);
    }

    public void setLines(List<String> lines, boolean update) {
        this.lines.clear(); // reset the lines
        // and add the new ones
        this.lines.addAll(lines);
        // call update if requested
        if (update) {
            updateLines();
        }
    }

    private void setTeamContent(Team team, String content) {
        if (content.length() > 32) {
            return; // this is illegal (having a string longer than 32 char)
        }

        String prefix = content.substring(0, Math.min(content.length(), 16));
        boolean splitColor = false;

        if ((prefix.endsWith("&") || prefix.endsWith("\u00A7")) && content.length() > 16) {
            prefix = content.substring(0, 15);
            splitColor = true;
        }

        // splits the content between both the prefix and suffix
        team.setPrefix(color(prefix));

        if (content.length() > 16) {
            // find the last known color in the prefix
            String lastColor;
            // right from ChatColor itself
            Pattern pattern = Pattern.compile("(?i)" + "[§&]" + "[0-9A-FK-OR]");
            Matcher matcher = pattern.matcher(prefix);
            int lastIndex = -1;
            while (matcher.find()) {
                lastIndex = matcher.start();
            }

            // static number 2 because that's the minimum and maximum detection of the regex string
            lastColor = prefix.substring(lastIndex, Math.min(lastIndex + 2, prefix.length()));
            team.setSuffix(color(lastColor + content.substring(splitColor ? 15 : 16, Math.min(content.length(), 30))));

            return;
        }

        // remove previous trailing
        team.setSuffix("");
    }

    public void setDisplayName(String displayName) {
        // set the objective's display name for the scoreboard (title)
        objective.setDisplayName(color(displayName));
        this.displayName = displayName;
    }

    protected Player getPlayer() {
        return Bukkit.getPlayer(playerUUID);
    }

    private static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
