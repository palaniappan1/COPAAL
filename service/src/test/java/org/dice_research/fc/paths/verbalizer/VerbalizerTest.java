package org.dice_research.fc.paths.verbalizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.dice_research.fc.data.QRestrictedPath;
import org.dice_research.fc.sparql.query.QueryExecutionFactoryCustomHttp;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests the verbalization feature.
 *
 */
@RunWith(Parameterized.class)
public class VerbalizerTest {
  /**
   * The path
   */
  private QRestrictedPath path;
  /**
   * The fact's subject
   */
  private Resource subject;
  /**
   * The fact's object
   */
  private Resource object;
  /**
   * True if we expect an empty result
   */
  private boolean isEmptyExpected;

  private final QueryExecutionFactory qef =
      new QueryExecutionFactoryCustomHttp("https://dbpedia.org/sparql");


  public VerbalizerTest(Resource subject, Resource object, QRestrictedPath path,
      boolean isEmptyExpected) {
    this.path = path;
    this.subject = subject;
    this.object = object;
    this.isEmptyExpected = isEmptyExpected;
  }

  @Test
  public void testVerbalization() {
    IPathVerbalizer verbalizer = new MultiplePathVerbalizer(qef);
    String output = verbalizer.verbalizePaths(subject, object, path);
    System.out.println(output);
    Assert.assertEquals(isEmptyExpected, output.isEmpty());
    System.out.println();
  }

  @Parameters
  public static Collection<Object[]> data() {
    List<Object[]> testConfigs = new ArrayList<Object[]>();
    Resource subject = ResourceFactory.createResource("http://dbpedia.org/resource/Bill_Gates");
    Resource object = ResourceFactory.createResource("http://dbpedia.org/resource/United_States");
    

    Resource subject2 = ResourceFactory.createResource("http://dbpedia.org/resource/Nia_Gill");
    Resource object2 = ResourceFactory.createResource("http://dbpedia.org/resource/Bachelor_of_Arts");
    
    Resource subject3 = ResourceFactory.createResource("http://dbpedia.org/resource/Tay_Zonday");
    Resource object3 = ResourceFactory.createResource("http://dbpedia.org/resource/Minneapolis");
    
    Resource subject4 = ResourceFactory.createResource("http://dbpedia.org/resource/Barack_Obama");
    Resource object4 = ResourceFactory.createResource("http://dbpedia.org/resource/United_States");

    Resource randomRes = ResourceFactory.createResource("http://dbpedia.org/resource/randomRes");

    String pathStr = "<http://dbpedia.org/ontology/birthPlace>/<http://dbpedia.org/ontology/country>";
    String pathStr2 = "<http://dbpedia.org/ontology/almaMater>";
    String pathStr3 = "<http://dbpedia.org/ontology/birthPlace>/^<http://dbpedia.org/ontology/deathPlace>/<http://dbpedia.org/ontology/residence>";
    
    

    QRestrictedPath path = QRestrictedPath.create(pathStr, 0);
    QRestrictedPath path2 = QRestrictedPath.create(pathStr2, 0);
    QRestrictedPath path3 = QRestrictedPath.create(pathStr3, 0);

    testConfigs.add(new Object[] {subject, object, path, false});
    testConfigs.add(new Object[] {subject2, object2, path2, false});
    testConfigs.add(new Object[] {subject3, object3, path3, false});
    testConfigs.add(new Object[] {subject4, object4, path, false});
    testConfigs.add(new Object[] {randomRes, randomRes, path, false});
    
    // // Variants of length 2
    // private static final String L2_1 = "?s ?p1 ?x1 . ?x1 ?p2 ?o .";
    // private static final String L2_2 = "?s ?p1 ?x1 . ?o ?p2 ?x1 .";
    // private static final String L2_3 = "?x1 ?p1 ?s . ?x1 ?p2 ?o .";
    // private static final String L2_4 = "?x1 ?p1 ?s . ?o ?p2 ?x1 .";
    //
    // // Variants of length 3
    // private static final String L3_1 = "?s ?p1 ?x1 . ?x1 ?p2 ?x2 . ?x2 ?p3 ?o .";
    // private static final String L3_2 = "?s ?p1 ?x1 . ?x2 ?p2 ?x1 . ?x2 ?p3 ?o .";
    // private static final String L3_3 = "?x1 ?p1 ?s . ?x1 ?p2 ?x2 . ?x2 ?p3 ?o .";
    // private static final String L3_4 = "?x1 ?p1 ?s . ?x2 ?p2 ?x1 . ?x2 ?p3 ?o .";
    // private static final String L3_5 = "?s ?p1 ?x1 . ?x1 ?p2 ?x2 . ?o ?p3 ?x2 .";
    // private static final String L3_6 = "?s ?p1 ?x1 . ?x2 ?p2 ?x1 . ?o ?p3 ?x2 .";
    // private static final String L3_7 = "?x1 ?p1 ?s . ?x1 ?p2 ?x2 . ?o ?p3 ?x2 .";
    // private static final String L3_8 = "?x1 ?p1 ?s . ?x2 ?p2 ?x1 . ?o ?p3 ?x2 .";
    return testConfigs;
  }

}
