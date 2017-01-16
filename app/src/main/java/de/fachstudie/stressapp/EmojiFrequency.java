package de.fachstudie.stressapp;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sanjeev on 24.11.2016.
 */

public class EmojiFrequency extends EmojiParser {
    private static final String DELIMITER = ",";
    private static Map<Emoji, Integer> frequencies;

    public EmojiFrequency(){
        frequencies = new HashMap<>();
    }

    public static Map<Emoji, Integer> getEmoticons(String content){
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
