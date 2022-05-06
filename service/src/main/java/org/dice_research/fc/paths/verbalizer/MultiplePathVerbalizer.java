package org.dice_research.fc.paths.verbalizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.triple2nl.TripleConverter;
import org.apache.commons.math3.util.Pair;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.dice_research.fc.data.IPieceOfEvidence;
import org.dice_research.fc.data.QRestrictedPath;
import org.dice_research.fc.util.RDFUtil;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import simplenlg.framework.NLGElement;
import simplenlg.framework.WordElement;

public class MultiplePathVerbalizer extends DefaultPathVerbalizer {

  private final String PERSON_PLACEHOLDER = "Other person";
  private final String THINGS_PLACEHOLDER = "Other";
  private final String SAME = "with the same";
  private final String PLURAL_IS = " are ";
  private final String SING_IS = " is ";
  private final String PLURAL = "s";

  private static final String NOT_SUPPORTED = "Verbalization not supported for this path length.";
  private static final String NOT_FOUND = "Verbalization not found, no intermediate nodes found.";

  private static final SparqlEndpoint ENDPOINT_DBPEDIA = SparqlEndpoint.getEndpointDBpedia();
  private static final SparqlEndpointKS KS = new SparqlEndpointKS(ENDPOINT_DBPEDIA);
  private static TripleConverter converter;


  // FIXME
  // Variants of length 2
  private static final String L2_1 = "?s ?p1 ?x1 . ?x1 ?p2 ?o .";
  private static final String L2_2 = "?s ?p1 ?x1 . ?o ?p2 ?x1 .";
  private static final String L2_3 = "?x1 ?p1 ?s . ?x1 ?p2 ?o .";
  private static final String L2_4 = "?x1 ?p1 ?s . ?o ?p2 ?x1 .";

  // Variants of length 3
  private static final String L3_1 = "?s ?p1 ?x1 . ?x1 ?p2 ?x2 . ?x2 ?p3 ?o .";
  private static final String L3_2 = "?s ?p1 ?x1 . ?x2 ?p2 ?x1 . ?x2 ?p3 ?o .";
  private static final String L3_3 = "?x1 ?p1 ?s . ?x1 ?p2 ?x2 . ?x2 ?p3 ?o .";
  private static final String L3_4 = "?x1 ?p1 ?s . ?x2 ?p2 ?x1 . ?x2 ?p3 ?o .";
  private static final String L3_5 = "?s ?p1 ?x1 . ?x1 ?p2 ?x2 . ?o ?p3 ?x2 .";
  private static final String L3_6 = "?s ?p1 ?x1 . ?x2 ?p2 ?x1 . ?o ?p3 ?x2 .";
  private static final String L3_7 = "?x1 ?p1 ?s . ?x1 ?p2 ?x2 . ?o ?p3 ?x2 .";
  private static final String L3_8 = "?x1 ?p1 ?s . ?x2 ?p2 ?x1 . ?o ?p3 ?x2 .";

  public MultiplePathVerbalizer(QueryExecutionFactory qef) {
    super(qef);
    try {
      KS.init();
    } catch (ComponentInitException e) {
      e.printStackTrace();
    }
    converter = new TripleConverter(ENDPOINT_DBPEDIA);
  }

  @Override
  public String parseResults(String queryStr, int size) {

    Map<String, Set<String>> map = getIntermediateNodes(queryStr, size);
    StringBuilder verbalizedOps = new StringBuilder();

    // 1. verbalize with query string
    List<Triple> stmts = new ArrayList<>();
    String[] items = queryStr.toString().trim().split("\\s+[.]\\s+");
    StringBuilder op = new StringBuilder();
    for (String curStmt : items) {
      if (curStmt.isEmpty()) {
        continue;
      }
      
      Triple stmt = RDFUtil.getTripleFromString(curStmt);
      op.append(converter.convert(stmt)).append(" ");
      stmts.add(stmt);
    }
    
    op.append("\n").append(converter.convert(stmts));
    String output = op.toString();
       

    // 2. Substitute variables with first instance for verbalized output
    String result = output.trim();
    for (String key : map.keySet()) {
      Set<String> values = map.get(key);
      if (values != null && !values.isEmpty()) {
        NLGElement t = converter.processNode(NodeFactory.createURI(values.iterator().next()));
        String word = ((WordElement) t.getFeature("head")).getBaseForm();
        result = result.replace("?" + key, word);
      } else {
        return NOT_FOUND;
      }
    }
    verbalizedOps.append(result).append("\n");


    // 3. Process the rest of the variables as alternatives
    // paths of length 1 should correspond to single-instances of meta-paths
    if (size > 1) {
      // concatenate the base forms
      for (String key : map.keySet()) {
        Set<String> values = map.get(key);
        boolean isPlural = false;

        // single-instances
        if (values.size() == 1) {
          continue;
        }

        // many
        if (values.size() > 1) {
          isPlural = true;
        }

        // TODO check for type and decide
        String ph = THINGS_PLACEHOLDER;
        if(isResourceAPerson(values.iterator().next())) {
          ph = PERSON_PLACEHOLDER;
        }

        verbalizedOps.append(ph);
        if (isPlural) {
          verbalizedOps.append(PLURAL);
        }
        verbalizedOps.append(" ").append(SAME).append(" ?").append(key);
        // TODO

        if (isPlural) {
          verbalizedOps.append(PLURAL_IS);
        } else {
          verbalizedOps.append(SING_IS);
        }
        
        
        int count = 0;
        for (String curVal : values) {
          NLGElement t = converter.processNode(NodeFactory.createURI(curVal));
          String word = ((WordElement) t.getFeature("head")).getBaseForm();
          verbalizedOps.append(" ").append(word);
          if (++count < values.size() - 1) {
            verbalizedOps.append(",");
          } else if (count < values.size()) {
            verbalizedOps.append(" and");
          } else {
            verbalizedOps.append(". ");
          }
        }

      }
    } else {
      return NOT_SUPPORTED;
    }



    return verbalizedOps.toString();
  }

  // FIXME not needed
  public boolean isResourceAPerson(String subject) {
    
    boolean isPerson = false;
    AskBuilder askBuilder = new AskBuilder();
    askBuilder.addWhere(NodeFactory.createURI(subject), RDF.type, NodeFactory.createURI("http://dbpedia.org/ontology/Person"));
    Query query = askBuilder.build();
    try (QueryExecution queryExecution = qef.createQueryExecution(query)) {
      isPerson = queryExecution.execAsk() ;
    }
    return isPerson;
  }



  public String verbalizeMetaPath(Resource subject, Resource object, IPieceOfEvidence path) {
    StringBuilder builder = new StringBuilder();

    // initialize as if there was a previous path stretch
    String stretchStart = null;
    String stretchEnd = RDFUtil.format(subject);

    List<Pair<Property, Boolean>> pathElements =
        QRestrictedPath.create(path.getEvidence(), path.getScore()).getPathElements();

    for (int i = 0; i < pathElements.size(); i++) {
      Pair<Property, Boolean> pathStretch = pathElements.get(i);

      // new start is the old end, new end is updated
      stretchStart = stretchEnd;
      if (i == pathElements.size() - 1) {
        stretchEnd = RDFUtil.format(object);
      } else {
        stretchEnd = INTERMEDIATE_VAR + i;
      }

      // build string
      if (pathStretch.getSecond()) {
        builder.append(stretchStart);
        builder.append(RDFUtil.format(pathStretch.getFirst()));
        builder.append(stretchEnd);
      } else {
        builder.append(stretchEnd);
        builder.append(RDFUtil.format(pathStretch.getFirst()));
        builder.append(stretchStart);
      }
      builder.append(" . ");

    }
    return parseResults(builder.toString(), pathElements.size());
  }

  /**
   * 
   * @param queryStr
   * @param size
   * @return
   */
  protected Map<String, Set<String>> getIntermediateNodes(String queryStr, int size) {
    StringBuilder builder = new StringBuilder();
    builder.append("SELECT * WHERE {");
    builder.append(queryStr);
    builder.append("}");

    Query query = QueryFactory.create(builder.toString());
    Map<String, Set<String>> map = new HashMap<>();
    try (QueryExecution queryExecution = qef.createQueryExecution(query)) {
      ResultSet resultSet = queryExecution.execSelect();
      while (resultSet.hasNext()) {
        QuerySolution curSol = resultSet.next();
        Iterator<String> varNames = curSol.varNames();
        while (varNames.hasNext()) {
          String curVarName = varNames.next();
          if (curVarName.equals("_star_fake")) {
            continue;
          }
          map.computeIfAbsent(curVarName, k -> new LinkedHashSet<String>())
              .add(curSol.get(curVarName).toString());
        }
      }
    }
    return map;
  }


}
