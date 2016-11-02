/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.omegat.plugins.fuzzymatchrepair;

import es.ua.dlsi.patch.patches.PatchedSegment;
import es.ua.dlsi.patch.suggestions.RankingSuggestions;
import es.ua.dlsi.patch.suggestions.ScoredSuggestion;
import es.ua.dlsi.patch.tokenisation.SegmentToken;
import es.ua.dlsi.patch.suggestions.SuggestionWithEditingInfo;
import es.ua.dlsi.patch.tokenisation.TokenizedSegment;
import es.ua.dlsi.patch.patches.PatchOperatorsCollection;
import es.ua.dlsi.patch.translation.GenericTranslator;
import es.ua.dlsi.segmentation.Word;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.matching.NearString;
import org.omegat.gui.common.EntryInfoSearchThread;
import org.omegat.gui.common.EntryInfoThreadPane;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Token;

/**
 *
 * @author mespla
 */
public class FindAndRepairFuzzyMatchesThread  extends EntryInfoSearchThread<List<SuggestionWithEditingInfo>>{
        
    final private FuzzyMatchRepairMarker marker;
    final private FuzzyMatchRepairTextArea text_area;
    final private NearString match;
    private PatchOperatorsCollection poc;
    private boolean interrput;

    public FindAndRepairFuzzyMatchesThread(EntryInfoThreadPane<List<SuggestionWithEditingInfo>> pane,
            SourceTextEntry entry, FuzzyMatchRepairTextArea fmra, NearString match, FuzzyMatchRepairMarker marker) {
        super(pane, entry);
        this.text_area = fmra;
        this.match = match;
        this.marker = marker;
        this.interrput = false;
    }
    
    public void Interrupt(){
        this.interrput = true;
        this.poc.Interrupt();
    }

    @Override
    protected synchronized List<SuggestionWithEditingInfo> search() throws EntryChangedException, Exception {
        //Checking if there is any match
        
        List<SuggestionWithEditingInfo> suggestions = new LinkedList<SuggestionWithEditingInfo>();
        if(match==null || interrput){
            return new LinkedList<>();
        }
        else{
            //Tokenising the source segment from the translation unit
            ITokenizer tokenizer = Core.getProject().getSourceTokenizer();
            if (tokenizer == null) {
                return suggestions;
            }
            Token[] tokens = tokenizer.tokenizeVerbatim(match.source);
            //Token[] tokens = tokenizer.tokenizeWords(match.source, StemmingMode.MATCHING);

            List<Word> words=new LinkedList<Word>();
            List<SegmentToken> tokenlist=new LinkedList<SegmentToken>();
            for(int i=0;i<tokens.length;i++){
                String word=match.source.substring(tokens[i].getOffset(),
                        tokens[i].getOffset()+tokens[i].getLength());
                if(!word.matches("\\s")){
                    words.add(new Word(word));
                    tokenlist.add(new SegmentToken(tokens[i].getOffset(), tokens[i].getLength()));
                }
            }
            TokenizedSegment sourceseg=new TokenizedSegment(match.source, tokenlist, words);
            //Segment sourceseg=new Segment(match.source);
            words.clear();


            tokens = tokenizer.tokenizeVerbatim((Core.getEditor().getCurrentEntry().getSrcText()));
            //tokens = tokenizer.tokenizeWords((Core.getEditor().getCurrentEntry().getSrcText()), StemmingMode.NONE);
            words=new LinkedList<Word>();
            tokenlist=new LinkedList<SegmentToken>();
            for(int i=0;i<tokens.length;i++){
                String word=Core.getEditor().getCurrentEntry().getSrcText().substring(tokens[i].getOffset(),
                        tokens[i].getOffset()+tokens[i].getLength());
                if(!word.matches("\\s")){
                    words.add(new Word(word));
                    tokenlist.add(new SegmentToken(tokens[i].getOffset(), tokens[i].getLength()));
                }
            }
            TokenizedSegment sprime=new TokenizedSegment(Core.getEditor().getCurrentEntry().getSrcText(), tokenlist, words);
            //Segment sprime=new Segment(Core.getEditor().getCurrentEntry().getSrcText());
            words.clear();

            tokenizer = Core.getProject().getTargetTokenizer();
            if (tokenizer == null) {
                return suggestions;
            }

            //tokens = tokenizer.tokenizeWords(match.translation, StemmingMode.NONE);
            tokens = tokenizer.tokenizeVerbatim(match.translation);
            //tokens = tokenizer.tokenizeWords(match.translation, StemmingMode.NONE);
            tokenlist=new LinkedList<SegmentToken>();
            for(int i=0;i<tokens.length;i++){
                String word=match.translation.substring(tokens[i].getOffset(),
                        tokens[i].getOffset()+tokens[i].getLength());
                if(!word.matches("\\s")){
                    words.add(new Word(word));
                    tokenlist.add(new SegmentToken(tokens[i].getOffset(), tokens[i].getLength()));
                }
            }
            TokenizedSegment targetseg=new TokenizedSegment(match.translation, tokenlist, words);
            words.clear();

            //Obtaining the evidence and the recommendations
            //TranslationUnit tu=new TranslationUnit(sourceseg, targetseg);

            if(!interrput){
                GenericTranslator translator = new OmegaTTranslator(Core.getProject().getProjectProperties().getSourceLanguage(), Core.getProject().getProjectProperties().getTargetLanguage());
                poc=new PatchOperatorsCollection(sprime, sourceseg,
                        targetseg, marker.getMenu().GetSuggestionSize(),
                        marker.getMenu().GetBothSideGrounded(), new OmegaTTokenizer(tokenizer), translator);
                Set<PatchedSegment> notsuggestions = poc.BuildAllPatchedTranslations();

                if(!interrput){
                    SortedSet<ScoredSuggestion> tmp_suggestions;
                    if(marker.getMenu().GetRankingByMeanLengthSource().isSelected())
                        tmp_suggestions = RankingSuggestions.SourceContext(sprime, sourceseg, targetseg, notsuggestions);
                    else if (marker.getMenu().GetRankingByFMSProportion().isSelected())
                        tmp_suggestions = RankingSuggestions.EditedWordsRanker(sprime, sourceseg, targetseg, notsuggestions);
                    else
                        tmp_suggestions = RankingSuggestions.TargetContext(sprime, sourceseg, targetseg, notsuggestions);
                    //SortedSet<ScoredSuggestion> tmp_suggestions = RankingSuggestions.OverlappingRanker(sprime, sourceseg, targetseg, notsuggestions);
                    for(ScoredSuggestion s: tmp_suggestions){
                        suggestions.add(new SuggestionWithEditingInfo(s.getScore(), targetseg, s));
                    }
                }
            }
            return suggestions;
        }
    }
}
