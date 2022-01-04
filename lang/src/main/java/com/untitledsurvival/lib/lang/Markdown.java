package com.untitledsurvival.lib.lang;

import lombok.extern.java.Log;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Log
public class Markdown {
    private static final Map<String, BaseComponent[]> cache = new HashMap<>();

    public static BaseComponent[] parse(String string) {
        return parse(string, true);
    }

    public static BaseComponent[] parse(String string, boolean allowEvents) {
        String key = "%s:%s".formatted(allowEvents, string);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        // the iterator of the characters of the input string
        CharacterIterator iterator = new StringCharacterIterator(string);

        AtomicReference<ComponentBuilder> builder = new AtomicReference<>(new ComponentBuilder());

        AtomicReference<TextComponent> component = new AtomicReference<>(new TextComponent());
        AtomicReference<StringBuilder> text = new AtomicReference<>(new StringBuilder());

        Runnable reset = () -> {
            boolean isDefault = ComponentSerializer.toString(component.get()).equals("{\"text\":\"\"}");
            if (text.get().isEmpty() && isDefault) {
                return; // do not append and create a new component if text is empty or the component is default
            }

            // compile the component and text together and append it to the builder
            component.get().setText(text.get().toString());
            builder.get().append(component.get(), ComponentBuilder.FormatRetention.FORMATTING);

            // reset both the component and associated text
            text.set(new StringBuilder());
            component.set(new TextComponent());
        };

        // iterate over all the characters until hitting the DONE character
        char ch = iterator.current();

        while (ch != CharacterIterator.DONE) {
            switch (ch) {
                case '\\' -> // a backslash means to ignore the next character, so we will do that and append it
                        text.get().append(iterator.next());

                // handle color codes
                case '\u00A7', '&' -> {
                    char next = iterator.next();

                    // if the next character after the ampersand or section sign
                    // is a hashtag we assume that this is a hex color code
                    // and append the next 6 digits
                    if (next == '#') {
                        StringBuilder hexBuilder = new StringBuilder("#");
                        String hex;

                        for (int i = 0; i < 6; i++) hexBuilder.append(iterator.next());
                        hex = hexBuilder.toString();

                        // make an attempt at parsing the hex digits that follow as an integer
                        // if it is a valid hex format, it will pass
                        try {
                            // reset all the components
                            reset.run();

                            component.set(new TextComponent());
                            component.get().setColor(ChatColor.of(hex));
                        } catch (IllegalArgumentException e) {
                            log.warning("Invalid hex &%s".formatted(hex));
                            break;
                        }

                        break;
                    }

                    // if it's not a special hex color then just use plain colors
                    ChatColor color = ChatColor.getByChar(next);
                    if (color == null) {
                        continue; // this will only append an ampersand (&)
                    }
                    reset.run();

                    // if it's not a formatting code, or it's a RESET
                    if (color.getColor() != null) {
                        component.get().setColor(color);
                        break;
                    }

                    // set the appropriate property depending on the type of format
                    // no switch statement unfortunately due to lack of ChatColor being an enum
                    // (btw if there's a better way of doing this PLEASE do)
                    if (ChatColor.ITALIC.equals(color)) {
                        component.get().setItalic(true);
                    } else if (ChatColor.BOLD.equals(color)) {
                        component.get().setBold(true);
                    } else if (ChatColor.STRIKETHROUGH.equals(color)) {
                        component.get().setStrikethrough(true);
                    } else if (ChatColor.UNDERLINE.equals(color)) {
                        component.get().setUnderlined(true);
                    } else if (ChatColor.MAGIC.equals(color)) {
                        component.get().setObfuscated(true);
                    } else if (ChatColor.RESET.equals(color)) {
                        component.get().setItalic(false);
                        component.get().setStrikethrough(false);
                        component.get().setObfuscated(false);
                        component.get().setUnderlined(false);
                        component.get().setBold(false);
                    }
                }

                case '(' -> {
                    if (!allowEvents) { // if we are not parsing to allow events then ignore opening parenthesis
                        text.get().append(ch);
                        break;
                    }

                    // run a reset immediately
                    reset.run();

                    // call the next to skip the opening parenthesis
                    iterator.next();
                    String displayText = collectUntil(iterator, ')');

                    if (displayText == null) {
                        log.severe("Expected a closing parenthesis");
                        return builder.get().create();
                    }

                    // collect all components of displayText to have the action applied to
                    BaseComponent[] components = parse(displayText, false);

                    char[] actions = new char[2];
                    String[] actionValues = new String[2];

                    // the action to add to the display components
                    actions[0] = iterator.next();

                    // expect an opening square bracket next
                    if (iterator.next() != '[') {
                        log.severe("Expected an opening square bracket at index %s".formatted(iterator.getIndex()));
                        return builder.get().create();
                    }

                    iterator.next();
                    actionValues[0] = collectUntil(iterator, ']');

                    // check for a secondary action if character proceeding collected
                    // next action is a square bracket
                    char secondAction = iterator.next();
                    if (iterator.next() == '[') {
                        actions[1] = secondAction;

                        iterator.next();
                        actionValues[1] = collectUntil(iterator, ']');
                    } else {
                        // if there isn't a secondary action then ignore
                        iterator.previous();

                        if (iterator.current() != ']') iterator.previous();
                    }

                    // for each available action apply it
                    for (int i = 0; i < 2; i++) {
                        char action  = actions[i];
                        String value = actionValues[i];

                        if (action == '\0') {
                            continue;
                        }

                        if (value == null) {
                            log.severe("Expected a closing square bracket");
                            return builder.get().create();
                        }

                        BaseComponent[] actionMarkdown = parse(value, false);

                        // create the action hover value's markdown
                        for (BaseComponent displayComponent : components) {
                            boolean valid = applyAction(actionMarkdown, value, action, displayComponent);

                            if (!valid) {
                                return builder.get().create();
                            }
                        }
                    }

                    // append all components after having each action applied
                    for (BaseComponent comp : components) builder.get().append(comp, ComponentBuilder.FormatRetention.FORMATTING);
                }

                default -> text.get().append(ch);
            }

            ch = iterator.next();
        }

        // flush the rest of the buffer
        reset.run();

        BaseComponent[] components = builder.get().create();

        // caching
        cache.put(key, components);

        return components;
    }

    private static boolean applyAction(BaseComponent[] markdown, String value, char action, BaseComponent component) {
        switch (action) {
            case '@' -> // link
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, value));

            case '!' -> // run command
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, value));

            case '?' -> // suggest a command
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, value));

            case '"' -> // hover text
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(markdown)));

            default -> {
                return false;
            }
        }

        return true;
    }

    private static String collectUntil(CharacterIterator iterator, char until) {
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
