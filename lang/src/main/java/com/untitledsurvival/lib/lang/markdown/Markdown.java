package com.untitledsurvival.lib.lang.markdown;

import lombok.extern.java.Log;
import net.md_5.bungee.api.chat.BaseComponent;

import java.text.CharacterIterator;

@Log
public class Markdown {
    public static BaseComponent[] parse(String string) {
        return parse(string, true);
    }

    public static BaseComponent[] parse(String string, boolean allowEvents) {
        // the markdown component builder object
        MarkdownBuilder builder = new MarkdownBuilder(string);
        CharacterIterator iterator = builder.getIterator();

        builder.setAllowActions(allowEvents);

        // iterate over all the characters until hitting the DONE character
        char ch = iterator.current();

        while (ch != CharacterIterator.DONE) {
            switch (ch) {
                case '\\' -> { // a backslash means to ignore the next character, so we will do that and append it
                    iterator.next();
                    builder.addCurrent();
                }

                // handle color codes
                case '\u00A7', '&' -> builder.handleColors();
                case '(' -> builder.handleAction();

                default -> builder.addCurrent();
            }

            ch = iterator.next();
        }

        // flush the rest of the buffer
        builder.complete();
        return builder.getBuilder().create();
    }
}
