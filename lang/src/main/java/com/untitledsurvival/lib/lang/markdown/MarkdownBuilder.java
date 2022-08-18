package com.untitledsurvival.lib.lang.markdown;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Log @Data
public class MarkdownBuilder {
    private final String text;
    private final CharacterIterator iterator;

    public MarkdownBuilder(String text) {
        this.text = text;
        this.iterator = new StringCharacterIterator(text);
    }

    private final ComponentBuilder builder = new ComponentBuilder();

    private boolean allowActions = true;

    private TextComponent currentComp = new TextComponent();
    private FormatRetention retention = FormatRetention.FORMATTING;

    private StringBuilder currentText = new StringBuilder();

    public void complete() {
        if (isDefault()) {
            return; // do not reset the variables if they do not need to be
        }

        currentComp.setText(currentText.toString());
        builder.append(currentComp, retention);

        // clear the builder and component
        currentText = new StringBuilder();
        currentComp = new TextComponent();
        retention = FormatRetention.FORMATTING;
    }

    public void handleColors() {
        char ch = iterator.current();
        if (ch != '&' && ch != '\u00A7') {
            return; // do not handle on non-color characters
        }

        char next = iterator.next();

        // if the next character after the ampersand or section sign
        // is a hashtag we assume that this is a hex color code
        // and append the next 6 digits
        if (next == '#') {
            handleHex();
            return;
        }

        // if it's not a special hex color then just use plain colors
        ChatColor color = ChatColor.getByChar(next);
        if (color == null) {
            addCurrent(); // this will only append an ampersand (&)
            return;
        }

        complete();

        // if it's not a formatting code then we reset all previous
        if (color.getColor() != null || color.equals(ChatColor.RESET)) {
            currentComp.setColor(color);
            retention = FormatRetention.NONE;

            return;
        }

        retention = FormatRetention.FORMATTING;
        handleFormatting(color);
    }

    private void handleFormatting(@NonNull ChatColor color) {
        assert color.getColor() == null;

        switch (color.getName()) {
            case "italic"        -> currentComp.setItalic(true);
            case "bold"          -> currentComp.setBold(true);
            case "strikethrough" -> currentComp.setStrikethrough(true);
            case "underline"     -> currentComp.setUnderlined(true);
            case "obfuscated"    -> currentComp.setObfuscated(true);
        }
    }

    private void handleHex() {
        StringBuilder hexBuilder = new StringBuilder("#");
        String hex;

        for (int i = 0; i < 6; i++) hexBuilder.append(iterator.next());
        hex = hexBuilder.toString();

        // make an attempt at parsing the hex digits that follow as an integer
        // if it is a valid hex format, it will pass
        try {
            // reset all the components
            complete();
            currentComp.setColor(ChatColor.of(hex));
        } catch (IllegalArgumentException e) {
            log.warning("Invalid hex &%s".formatted(hex));
        }
    }

    public void handleAction() {
        if (iterator.current() != '(') {
            return;
        }

        if (!allowActions) { // if we are not parsing to allow events then ignore opening parenthesis
            addCurrent();
            return;
        }

        // run a reset immediately
        complete();

        // call the next to skip the opening parenthesis
        iterator.next();
        String displayText = collectUntil(')');

        if (displayText == null) {
            log.severe("Expected a closing parenthesis");
            return;
        }

        // collect all components of displayText to have the action applied to
        BaseComponent[] components = Markdown.parse(displayText, false);

        /*
         add all the components as extras to the current component
         so that they still appear, but we avoid applying hover and click
         to every single component
        */
        Arrays.stream(components).forEach(currentComp::addExtra);

        // the action to add to the display components
        Map<Character, String> actions = parseActions();

        // apply each of the actions provided to the current component
        actions.forEach(this::addAction);
        complete();
    }

    private Map<Character, String> parseActions() {
        Map<Character, String> actions = new HashMap<>();

        // collect up to 2 chained actions
        for (int i = 0; i < 2; i++) {
            Map.Entry<Character, String> action = nextAction();
            if (action == null) { // no action was found matching x[SOME VALUE]
                break;
            }

            actions.put(action.getKey(), action.getValue());
        }

        return actions;
    }

    private Map.Entry<Character, String> nextAction() {
        char action = iterator.next();

        // if there is no opening point for the value to be collected
        if (iterator.next() != '[') {
            iterator.previous();
            if (iterator.current() != ']') iterator.previous();
            return null;
        }

        // call next to skip the opening bracket
        iterator.next();
        String value = collectUntil(']');

        // reached end of buffer without a closing square bracket
        if (value == null) {
            log.severe("Expected a closing square bracket at index %s".formatted(iterator.getIndex()));
            return null;
        }

        return new AbstractMap.SimpleEntry<>(action, value);
    }

    public void addAction(char actionCode, @NonNull String value) {
        addAction(currentComp, actionCode, value);
    }

    public void addAction(@NonNull BaseComponent component, char actionCode, @NonNull String value) {
        switch (actionCode) {
            case '@' -> // link
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, value));

            case '!' -> // run command
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, value));

            case '?' -> // suggest a command
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, value));

            case '"' -> // hover text
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(Markdown.parse(value, false))));
        }
    }

    public void addCurrent() {
        currentText.append(iterator.current());
    }

    public boolean isDefault() {
        return currentText.isEmpty() && ComponentSerializer.toString(currentComp).equals("{\"text\":\"\"}");
    }

    private String collectUntil(char until) {
        StringBuilder builder = new StringBuilder();
        char ch = iterator.current();

        do {
            if (ch == CharacterIterator.DONE) {
                return null;
            }

            if (ch == '\\') {
                ch = iterator.next();
            }

            builder.append(ch);
        } while ((ch = iterator.next()) != until);

        return builder.toString();
    }
}
