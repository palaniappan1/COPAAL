# COPAAL
# Paper
This repository contains code (service and demo) for the paper and demo "Unsupervised Discovery of Corroborative Paths for FactValidation", "COPAAL – An Interface for Explaining Facts usingCorroborative Paths" respectively, accepted at International Semantic Web Conference (ISWC-2019).

# Description
COPAAL is an unsupervised fact validation approach for RDF knowledge graphs which identifies paths that support a given fact (s,p,o). This approach is based on the insight that the predicate p(e.g.,nationality) carries mutual information with a set of other paths (e.g., paths pertaining to birthPlace and country) in the background knowledge graph G. Hence,the presence of certain sets of paths in G that begin in s and end in o can be regarded as evidence which corroborates the veracity of (s,p,o). For example, we would have good reasons to believe that BarackObama is a citizen of the USA given that BarackObama was born in Hawaii and Hawaii is located in the USA.

![A subgraph of DBpedia version 10-2016.](https://github.com/dice-group/COPAAL/blob/master/service/src/main/resources/Running_Example_DBpedia.png)
