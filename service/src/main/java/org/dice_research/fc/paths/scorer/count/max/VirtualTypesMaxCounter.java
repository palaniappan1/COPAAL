package org.dice_research.fc.paths.scorer.count.max;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dice_research.fc.data.Predicate;
import org.dice_research.fc.paths.EmptyPredicateFactory;

/**
 * Maximum count retriever class for the virtual types configuration. This is meant to be used with
 * a predicate generated by {@link EmptyPredicateFactory}
 * 
 * @author Alexandra Silva
 *
 */
public class VirtualTypesMaxCounter extends MaxCounter {

  /**
   * The subject variable name
   */
  private final String SUBJECT_VARIABLE_NAME = "s";

  /**
   * The object variable name
   */
  private final String OBJECT_VARIABLE_NAME = "o";


  /**
   * Constructor.
   */
  public VirtualTypesMaxCounter(QueryExecutionFactory qef) {
    super(qef);
  }

  @Override
  public long deriveMaxCount(Predicate predicate) {
    return countInstances(predicate, SUBJECT_VARIABLE_NAME)
        * countInstances(predicate, OBJECT_VARIABLE_NAME);
  }

  /**
   * Retrieves the count of the unique subjects or objects a triple with a given {@link Predicate}
   * has.
   * 
   * @param predicate the {@link Predicate} object
   * @param var variable name in the SPARQL query, to retrieve either the subject or the object
   *        count
   * @return
   */
  protected long countInstances(Predicate predicate, String var) {
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("SELECT (count(DISTINCT ?").append(var).append(") AS ?");
    queryBuilder.append(COUNT_VARIABLE_NAME);
    queryBuilder.append(") WHERE { ");
    queryBuilder.append("?").append(SUBJECT_VARIABLE_NAME);
    queryBuilder.append(" <").append(predicate.getProperty().getURI()).append("> ?");
    queryBuilder.append(OBJECT_VARIABLE_NAME).append(" . }");
    return executeCountQuery(queryBuilder);
  }
}
