package com.untitledsurvival.lib.scoreboard;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

public class PlayerScoreboard {
    private final ScoreboardManager manager = Bukkit.getScoreboardManager();

    @Getter private final Player player;
    @Getter private final Scoreboard scoreboard;
    @Getter private final Objective objective;

    @Getter private final Map<String, Team> teamLines = new HashMap<>();

    @Getter private boolean isDisplayed = true;
    @Getter private String displayName = this.color("Scoreboard");

    public PlayerScoreboard(Player player) {
        this.player = player;

        this.scoreboard = manager.getNewScoreboard();

        // create the objective that the scoreboard will be based off of
        this.objective  = scoreboard.registerNewObjective("feels", "dummy", this.displayName);
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void setDisplayName(String displayName) {
        this.displayName = this.color(displayName);
        this.objective.setDisplayName(this.displayName);
    }

    public void setDisplayed(boolean displayed) {
        this.isDisplayed = displayed;

        if (!displayed) {
            // set the player's scoreboard back to the main scoreboard
            player.setScoreboard(manager.getMainScoreboard());
            return;
        }

        player.setScoreboard(this.scoreboard);
    }

    public void update(List<String> lines) {
        this.inheritMainScoreboard();

        List<Team> teams = this.teamLines.values().stream().toList();

        // if the line length changes then create a whole new set of line to teams
        if (lines.size() != this.teamLines.size() || this.teamLines.isEmpty()) {
            // unregister all teams
            teams.forEach(Team::unregister);

            // clear the team lines
            this.teamLines.clear();
            this.scoreboard.getEntries().forEach(this.scoreboard::resetScores);

            setTeamLines(lines);
            return;
        }

        this.teamLines.clear();

        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);
            String line = lines.get(i);

            // set the text for every team
            this.applyTextToTeam(line, team);
            this.teamLines.put(line, team);
        }
    }

    private void setTeamLines(List<String> lines) {
        // create new teams for each line
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Team team = this.scoreboard.registerNewTeam("line" + i);

            // get the new color from the line
            char code = ChatColor.ALL_CODES.charAt(i);
            String color = String.valueOf(ChatColor.getByChar(code));

            // add a person that is a color code to the team (color code, so they do not appear)
            this.objective.getScore(color).setScore(lines.size() - i);
            team.addEntry(color);

            this.applyTextToTeam(line, team);
            this.teamLines.put(line, team);
        }
    }

    public void applyTextToTeam(String text, Team team) {
        if (text.length() > 32) {
            return;
        }

        // split the text into two separate parts, suffix and prefix
        String prefix = text.substring(0, Math.min(text.length(), 16));
        String suffix = text.length() > 16 ? text.substring(16) : "";

        // apply the prefix to the team
        team.setPrefix(this.color(prefix));

        if (suffix.isEmpty()) {
            // clear previous trailing
            team.setSuffix("");
            return;
        }

        // if there is an ampersand at the end of the prefix, then remove it
        // and add it to the beginning of the suffix
        if (prefix.endsWith("&") || prefix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
            suffix = "&" + suffix;
            prefix = prefix.substring(0, prefix.length() - 1);
        }

        // find if there are any color codes left behind in the prefix
        Matcher colorCode = ChatColor.STRIP_COLOR_PATTERN.matcher(prefix);

        // find the last instance of a color code in the prefix
        int lastIndex = -1;
        while (colorCode.find()) {
            lastIndex = colorCode.start();
        }

        // if there is a last index, then add the color code to the suffix
        if (lastIndex != -1) {
            suffix = prefix.substring(lastIndex, lastIndex + 2) + suffix;
        }

        // truncate suffix to be no longer than 16 characters
        if (suffix.length() > 16) {
            suffix = suffix.substring(0, 16);
        }

        // apply the suffix to the team
        team.setSuffix(this.color(suffix));
    }

    public void inheritMainScoreboard() {
        Scoreboard mainScoreboard = manager.getMainScoreboard();
        Set<Objective> objectives = mainScoreboard.getObjectives();

        for (Objective objective : objectives) {
            String name = objective.getName();

            // keep all objective scores up to date
            if (this.scoreboard.getObjective(name) != null) {
                Set<Score> scores = this.scoreboard.getScores(name);

                for (Score score : scores) {
                    score.setScore(objective.getScore(score.getEntry()).getScore());
                }

                continue;
            }

            Objective newObjective = player.getScoreboard().registerNewObjective(name, objective.getCriteria(), objective.getDisplayName(), objective.getRenderType());

            // set the display slot of the objective unless it is a sidebar
            if (objective.getDisplaySlot() != DisplaySlot.SIDEBAR) {
                newObjective.setDisplaySlot(objective.getDisplaySlot());
            }
        }

        mainScoreboard.getTeams().forEach(team -> {
            if (this.scoreboard.getTeam(team.getName()) != null) {
                return;
            }

            Team newTeam = this.scoreboard.registerNewTeam(team.getName());
            newTeam.setPrefix(team.getPrefix());
            newTeam.setSuffix(team.getSuffix());
            newTeam.setAllowFriendlyFire(team.allowFriendlyFire());

            newTeam.setCanSeeFriendlyInvisibles(team.canSeeFriendlyInvisibles());

            // for every Option in the team, add it to the new team
            for (Team.Option option : Team.Option.values()) {
                newTeam.setOption(option, team.getOption(option));
            }
        });
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
