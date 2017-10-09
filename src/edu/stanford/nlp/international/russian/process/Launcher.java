package edu.stanford.nlp.international.russian.process;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoNLLUOutputter;
import edu.stanford.nlp.pipeline.DependencyParseAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Launcher {

  private final static String DEFAULT_PATH_RESULTS = "results.conll";
  private final static String DEFAULT_PATH_PARSER_MODEL =
      "src//edu//stanford//nlp//models//russian//nndep.rus.modelWithTestWithoutAlpha.txt.gz";
  private final static String DEFAULT_PATH_TAGGER =
      "src//edu//stanford//nlp//models//russian//russian-ud-pos.tagger";
  private final static String DEFAULT_PATH_MF_TAGGER =
      "src//edu//stanford//nlp//models//russian//russian-ud-mf.tagger";
  private final static String DEFAULT_PATH_TEXT =
      "src//edu//stanford//nlp//models//russian//text.txt";

  public static void main(String[] args) throws FileNotFoundException, IOException {
    String tagger = DEFAULT_PATH_TAGGER;
    String taggerMF = DEFAULT_PATH_MF_TAGGER;
    String parser = DEFAULT_PATH_PARSER_MODEL;
    String pText = DEFAULT_PATH_TEXT;
    String pResults = DEFAULT_PATH_RESULTS;
    Properties pr = StringUtils.argsToProperties(args);
    if (pr.containsKey("tagger")) {
      tagger = pr.getProperty("tagger");
    }
    if (pr.containsKey("taggerMF")) {
      taggerMF = pr.getProperty("taggerMF");
    }
    if (pr.containsKey("parser")) {
      parser = pr.getProperty("parser");
    }
    if (pr.containsKey("pText")) {
      pText = pr.getProperty("pText");
    }
    if (pr.containsKey("pResults")) {
      pResults = pr.getProperty("pResults");
    }

    Properties props = new Properties();
    props.setProperty("annotators", "tokenize, ssplit");
    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
    pipeline.addAnnotator(new RussianMorphoAnnotator(new MaxentTagger(taggerMF)));

    Properties propsParser = new Properties();
    propsParser.setProperty("model", parser);
    propsParser.setProperty("tagger.model", tagger);
    pipeline.addAnnotator(new DependencyParseAnnotator(propsParser));

    pipeline.addAnnotator(new RussianLemmatizationAnnotator());



    List<String> text = getText(pText);
    for (String line : text) {
      Annotation annotation = pipeline.process(line);
      CoNLLUOutputter.conllUPrint(annotation, new FileOutputStream(pResults, false));
    }
  }

  private static List<String> getText(String file) throws IOException {
    List<String> res = new ArrayList<String>();
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(file));
      String line;
      while ((line = br.readLine()) != null) {
        res.add(line);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        br.close();
      }
    }
    return res;
  }

  private static String getTextStr(String file) throws IOException {
    List<String> res = new ArrayList<String>();
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(file));
      String line;
      while ((line = br.readLine()) != null) {
        res.add(line);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        br.close();
      }
    }
    return res.get(0);
  }

}
