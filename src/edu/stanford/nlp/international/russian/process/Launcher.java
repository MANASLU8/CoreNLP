package edu.stanford.nlp.international.russian.process;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoNLLUOutputter;
import edu.stanford.nlp.pipeline.DependencyParseAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;
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
    String pConll = null;
    boolean mf = false;
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
    if (pr.containsKey("pConll")) {
      pConll = pr.getProperty("pConll");
    } 
    if (pr.containsKey("mf")) {
      mf = true;
    } 

    Properties props = new Properties();
    props.setProperty("annotators", "tokenize, ssplit");
    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

    if (mf) {
      pipeline.addAnnotator(new RussianMorphoAnnotator(new MaxentTagger(taggerMF)));
    } else {
      pipeline.addAnnotator(new POSTaggerAnnotator(new MaxentTagger(tagger)));
    }

    Properties propsParser = new Properties();
    propsParser.setProperty("model", parser);
    propsParser.setProperty("tagger.model", tagger);
    pipeline.addAnnotator(new DependencyParseAnnotator(propsParser));

    pipeline.addAnnotator(new RussianLemmatizationAnnotator());

    FileOutputStream fos = new FileOutputStream(pResults, false);
    if (pConll != null) {
      List<CoreMap> sents = new ArrayList<>();
      loadConllFileWithoutAnnotation(pConll, sents);
      Annotation annotation = new Annotation(sents);
      pipeline.annotate(annotation);
      CoNLLUOutputter.conllUPrint(annotation, fos);
    } else {
      List<String> text = getText(pText);
      for (String line : text) {
        Annotation annotation = pipeline.process(line);
        CoNLLUOutputter.conllUPrint(annotation, fos);
        // pipeline.conllPrint(annotation, w);
      }
    }
    fos.flush();
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

  public static void loadConllFile(String inFile, List<CoreMap> sents) {
    CoreLabelTokenFactory tf = new CoreLabelTokenFactory(false);

    BufferedReader reader = null;
    try {
      reader = IOUtils.readerFromString(inFile);

      List<CoreLabel> sentenceTokens = new ArrayList<>();

      for (String line : IOUtils.getLineIterable(reader, false)) {
        String[] splits = line.split("\t");
        if (splits.length < 10) {
          if (sentenceTokens.size() > 0) {
            CoreMap sentence = new CoreLabel();
            sentence.set(CoreAnnotations.TokensAnnotation.class, sentenceTokens);
            sents.add(sentence);
            sentenceTokens = new ArrayList<>();
          }
        } else {
          String word = splits[1], pos = splits[3], depType = splits[7];

          int head = -1;
          try {
            head = Integer.parseInt(splits[6]);
          } catch (NumberFormatException e) {
            continue;
          }

          CoreLabel token = tf.makeToken(word, 0, 0);
          token.setTag(pos);
          token.set(CoreAnnotations.CoNLLDepParentIndexAnnotation.class, head);
          token.set(CoreAnnotations.CoNLLDepTypeAnnotation.class, depType);
          sentenceTokens.add(token);
        }
      }
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    } finally {
      IOUtils.closeIgnoringExceptions(reader);
    }
  }

  public static void loadConllFileWithoutAnnotation(String inFile, List<CoreMap> sents) {
    CoreLabelTokenFactory tf = new CoreLabelTokenFactory(false);

    BufferedReader reader = null;
    try {
      reader = IOUtils.readerFromString(inFile);

      List<CoreLabel> sentenceTokens = new ArrayList<>();

      for (String line : IOUtils.getLineIterable(reader, false)) {
        String[] splits = line.split("\t");
        if (splits.length < 10) {
          if (sentenceTokens.size() > 0) {
            CoreMap sentence = new CoreLabel();
            sentence.set(CoreAnnotations.TokensAnnotation.class, sentenceTokens);
            sents.add(sentence);
            sentenceTokens = new ArrayList<>();
          }
        } else {
          String word = splits[1];
          CoreLabel token = tf.makeToken(word, 0, 0);
          sentenceTokens.add(token);
        }
      }
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    } finally {
      IOUtils.closeIgnoringExceptions(reader);
    }
  }

}
