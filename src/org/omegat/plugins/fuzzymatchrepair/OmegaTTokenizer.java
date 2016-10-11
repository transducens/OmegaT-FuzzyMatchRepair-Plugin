/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.omegat.plugins.fuzzymatchrepair;

import es.ua.dlsi.patch.tokenisation.SegmentToken;
import es.ua.dlsi.patch.tokenisation.FMRTokenizer;
import es.ua.dlsi.segmentation.Word;
import java.util.LinkedList;
import java.util.List;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Token;

/**
 *
 * @author mespla
 */
public class OmegaTTokenizer implements FMRTokenizer{
    
    private ITokenizer tokenizer;
    
    public OmegaTTokenizer(ITokenizer tokenizer){
        this.tokenizer = tokenizer;
    }

    @Override
    public List<SegmentToken> Tokenize(String s, List<Word> w) {
        Token[] tokens = tokenizer.tokenizeVerbatim(s);
        //Token[] tokens = tokenizer.tokenizeWords(match.source, StemmingMode.MATCHING);

        List<SegmentToken> tokenlist=new LinkedList<SegmentToken>();
        for(int i=0;i<tokens.length;i++){
            String word=s.substring(tokens[i].getOffset(),
                    tokens[i].getOffset()+tokens[i].getLength());
            if(!word.matches("\\s+")){
                if(w!=null)
                    w.add(new Word(word));
                tokenlist.add(new SegmentToken(tokens[i].getOffset(), tokens[i].getLength()));
            }
        }
        return tokenlist;
    }
    
}
