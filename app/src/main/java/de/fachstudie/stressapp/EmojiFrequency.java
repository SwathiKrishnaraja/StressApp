package de.fachstudie.stressapp;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sanjeev on 24.11.2016.
 */

public class EmojiFrequency extends EmojiParser {
    private Map<Emoji, Integer> frequencies;

    public EmojiFrequency(){
        frequencies = new HashMap<>();
    }

    public Map<Emoji, Integer> getEmojiFrequenciesFromText(String text){
        for(UnicodeCandidate uc: getUnicodeCandidates(text)){
            Emoji emoji = uc.getEmoji();
            Integer previous = frequencies.get(emoji);
            frequencies.put(emoji, previous != null ? previous + 1 : 1);
        }
        return frequencies;
    }
}
