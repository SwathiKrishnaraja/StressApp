package org.hcilab.projects.stressblocks;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles extracting the emoticons of a text.
 */

public class EmojiFrequency extends EmojiParser {
    private static final String DELIMITER = ",";

    public static Map<Emoji, Integer> getEmoticons(String content){
       Map<Emoji, Integer> frequencies = new HashMap<>();
        for(UnicodeCandidate uc: getUnicodeCandidates(content)){
            Emoji emoji = uc.getEmoji();
            Integer previous = frequencies.get(emoji);
            frequencies.put(emoji, previous != null ? previous + 1 : 1);
        }
        return frequencies;
    }

    public static String getCommaSeparatedEmoticons(String content){
        StringBuilder builder = new StringBuilder();
        for(UnicodeCandidate uc: getUnicodeCandidates(content)){
            Emoji emoji = uc.getEmoji();
            builder.append(emoji.getUnicode());
            builder.append(DELIMITER);
        }
        return builder.toString();
    }
}
