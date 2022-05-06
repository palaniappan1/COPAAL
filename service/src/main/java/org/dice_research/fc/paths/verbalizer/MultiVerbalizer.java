package org.dice_research.fc.paths.verbalizer;

import java.util.List;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.triple2nl.TripleConverter;
import org.aksw.triple2nl.converter.DefaultIRIConverter;
import org.aksw.triple2nl.converter.IRIConverter;
import org.aksw.triple2nl.converter.LiteralConverter;
import org.aksw.triple2nl.gender.DictionaryBasedGenderDetector;
import org.aksw.triple2nl.gender.GenderDetector;
import org.aksw.triple2nl.property.PropertyVerbalizer;
import org.apache.jena.graph.Triple;
import org.dllearner.reasoning.SPARQLReasoner;
import net.sf.extjwnl.dictionary.Dictionary;
import simplenlg.framework.CoordinatedPhraseElement;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.realiser.english.Realiser;

public class MultiVerbalizer extends TripleConverter {

  private NLGFactory nlgFactory;
  private Realiser realiser;

  private IRIConverter uriConverter;
  private LiteralConverter literalConverter;
  private PropertyVerbalizer pp;
  private SPARQLReasoner reasoner;
  
  private boolean determinePluralForm = false;
  //show language as adjective for literals
  private boolean considerLiteralLanguage = true;
  //encapsulate string literals in quotes ""
  private boolean encapsulateStringLiterals = true;
  //for multiple types use 'as well as' to coordinate the last type
  private boolean useAsWellAsCoordination = true;

  private boolean returnAsSentence = true;

  private boolean useGenderInformation = true;

  private GenderDetector genderDetector;

  public MultiVerbalizer(QueryExecutionFactory qef, PropertyVerbalizer propertyVerbalizer, IRIConverter uriConverter, String cacheDirectory, Dictionary wordnetDirectory, Lexicon lexicon) {
    if(uriConverter == null){
        uriConverter = new DefaultIRIConverter(qef, cacheDirectory);
    }
    this.uriConverter = uriConverter;
    
    if(propertyVerbalizer == null){
        propertyVerbalizer = new PropertyVerbalizer(uriConverter, wordnetDirectory);
    }
    pp = propertyVerbalizer;
    
    if(lexicon == null) {
        lexicon = Lexicon.getDefaultLexicon();
    }
    
    nlgFactory = new NLGFactory(lexicon);
    realiser = new Realiser(lexicon);
    
    literalConverter = new LiteralConverter(uriConverter);
    literalConverter.setEncapsulateStringLiterals(encapsulateStringLiterals);
    
    reasoner = new SPARQLReasoner(qef);

    genderDetector = new DictionaryBasedGenderDetector();
}

  @Override
  public String convert(List<Triple> triples) {

    // combine with conjunction
    CoordinatedPhraseElement typesConjunction = nlgFactory.createCoordinatedPhrase();
    

    return super.convert(triples);
  }
}
