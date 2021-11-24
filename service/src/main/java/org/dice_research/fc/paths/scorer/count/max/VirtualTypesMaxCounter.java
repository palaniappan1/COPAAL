package org.dice_research.fc.paths.scorer.count.max;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dice_research.fc.data.Predicate;
import org.dice_research.fc.paths.VirtualTypePredicateFactory;

/**
 * Maximum count retriever class for the virtual types configuration. This is meant to be used with
 * a predicate generated by {@link VirtualTypePredicateFactory}.
 * 
 * @author Alexandra Silva
 *
 */
public class VirtualTypesMaxCounter extends MaxCounter {

  /**
   * Constructor.
   */
  public VirtualTypesMaxCounter(QueryExecutionFactory qef) {
    super(qef);
  }

  /**
   * Retrieves the count of the unique subjects or objects a triple with a given {@link Predicate}
   * has.
   * 
   * @param predicate the {@link Predicate} object count
   * @return
   */
  @Override
  public long deriveMaxCount(Predicate predicate) {
    String predicateURI = predicate.getProperty().getURI();
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("SELECT ((?c1*?c2) AS ?").append(COUNT_VARIABLE_NAME).append(") WHERE {");
    queryBuilder.append("{SELECT (count(DISTINCT ?s) AS ?c1) WHERE {");
    queryBuilder.append("?s <").append(predicateURI).append("> [] . }}");
    queryBuilder.append("{SELECT (count(DISTINCT ?o) AS ?c2) WHERE {");
    queryBuilder.append(" [] <").append(predicateURI).append("> ?o . }}}");
    return executeCountQuery(queryBuilder);
  }
}
