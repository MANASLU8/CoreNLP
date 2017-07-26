package edu.stanford.nlp.international.russian.process;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoNLLOutputter;
import edu.stanford.nlp.pipeline.CoNLLUOutputter;
import edu.stanford.nlp.pipeline.DependencyParseAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Launcher {

  private final static String DEFAULT_PATH = "results.conll";

  public static void main(String[] args) throws FileNotFoundException, IOException {
    Properties props = new Properties();
    props.setProperty("annotators", "tokenize, ssplit");
    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
    pipeline.addAnnotator(new RussianMorphoAnnotator(
        new MaxentTagger("C://Users//Ivan//Desktop//russian-ud-mf.tagger")));


    Properties propsParser = new Properties();
    propsParser.setProperty("model", "C://Users//Ivan//Desktop//nndep.rus.modelWithTest.txt.gz");
    // propsParser.setProperty("model",
    // "C://Users//Ivan//workspace//stanford//CoreNLP//nndep.rus.model.txt.gz");
    propsParser.setProperty("tagger.model", "C://Users//Ivan//Desktop//russian-ud-pos.tagger");
    pipeline.addAnnotator(new DependencyParseAnnotator(propsParser));

    pipeline.addAnnotator(new RussianLemmatizationAnnotator());



    List<String> text = getText("C://Users//Ivan//Desktop//0000_co");
    for (String line : text) {
      Annotation annotation = pipeline.process(line);

      if (args.length == 1) {
        CoNLLUOutputter.conllUPrint(annotation, new FileOutputStream(args[0], true));
      } else {
        CoNLLUOutputter.conllUPrint(annotation, new FileOutputStream(DEFAULT_PATH, true));
      }
    } 
    
    /*
    String text = getTextStr("C://Users//Ivan//Desktop//0000_corenlpStr");
      Annotation annotation = pipeline.process(text);

      if (args.length == 1) {
        CoNLLUOutputter.conllUPrint(annotation, new FileOutputStream(args[0], true));
      } else {
        CoNLLUOutputter.conllUPrint(annotation, new FileOutputStream(DEFAULT_PATH, true));
      }
    */
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
