package edu.stanford.nlp.international.russian.process;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.concurrent.MulticoreWrapper;
import edu.stanford.nlp.util.concurrent.ThreadsafeProcessor;
import edu.stanford.nlp.util.logging.Redwood;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class RussianLemmatizationAnnotator implements edu.stanford.nlp.pipeline.Annotator {

  private static Redwood.RedwoodChannels log =
      Redwood.channels(RussianLemmatizationAnnotator.class);

  // fix
  private static Map<String, List<Pair<String, String>>> dict =
      new HashMap<String, List<Pair<String, String>>>();
  private final int nThreads;

  /*
   * static { init(); }
   * 
   * private static void init() { // fix || singletone? loadDictionary(); }
   */

  private static void loadDictionary() {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(
          new FileInputStream("C://Users//Ivan//workspace//manaslu8//stanford//CoreNLP//"
              + "src//edu//stanford//nlp//international//russian//process//dict.tsv"),
              //"src//edu//stanford//nlp//international//russian//process//dict.tsv"),
          "UTF-8"));
      for (String line; (line = br.readLine()) != null;) {
        String[] ln = line.split("\t");
        Pair<String, String> lemmaTag = new Pair<String, String>(ln[1], ln[2]);
        if (dict.containsKey(ln[0])) {
          List<Pair<String, String>> dlist = dict.get(ln[0]);
          if (!dlist.contains(lemmaTag)) {
            dlist.add(lemmaTag);
          }
        } else {
          List<Pair<String, String>> lst = new ArrayList<Pair<String, String>>();
          lst.add(lemmaTag);
          dict.put(ln[0], lst);
        }
      }
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }


  public RussianLemmatizationAnnotator() {
    this(1);
  }

  public RussianLemmatizationAnnotator(int numThreads) {
    if (dict.isEmpty()) {
      loadDictionary();
    }
    this.nThreads = numThreads;
  }

  public void annotate(Annotation annotation) {

    if (annotation.containsKey(CoreAnnotations.SentencesAnnotation.class)) {
      if (nThreads == 1) {
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
          doOneSentence(sentence);
        }
      } else {
        MulticoreWrapper<CoreMap, CoreMap> wrapper =
            new MulticoreWrapper<>(nThreads, new LemmatizationProcessor());
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
          wrapper.put(sentence);
          while (wrapper.peek()) {
            wrapper.poll();
          }
        }
        wrapper.join();
        while (wrapper.peek()) {
          wrapper.poll();
        }
      }
    } else {
      throw new RuntimeException("unable to find words/tokens in: " + annotation);
    }

  }

  private class LemmatizationProcessor implements ThreadsafeProcessor<CoreMap, CoreMap> {
    @Override
    public CoreMap process(CoreMap sentence) {
      return doOneSentence(sentence);
    }

    @Override
    public ThreadsafeProcessor<CoreMap, CoreMap> newInstance() {
      return this;
    }
  }

  private CoreMap doOneSentence(CoreMap sentence) {
    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
      if (token.get(LemmaAnnotation.class) == null) {
        token.setLemma(getLemma(token.originalText(), token.tag()));// getLemma(token.originalText()));
      }
    }

    return sentence;
  }

  private String getLemma(String token, String tag) {
    String lemma = token; // FIXME
    if (dict.containsKey(token)) {
      List<Pair<String, String>> tokenList = dict.get(token);
      if (tokenList.size() == 1) {
        lemma = tokenList.get(0).first;
      } else {
        for (Pair<String, String> value : tokenList) {
          if (value.second.equals(tag)) {
            lemma = value.first;
            break;
          }
        }
      }
    }
    return lemma;
  }

  public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
    return Collections.emptySet(); // singleton(CoreAnnotations.LemmaAnnotation.class);
  }

  public Set<Class<? extends CoreAnnotation>> requires() {
    return new ArraySet<Class<? extends CoreAnnotation>>(CoreAnnotations.TextAnnotation.class,
        CoreAnnotations.TokensAnnotation.class, CoreAnnotations.SentencesAnnotation.class,
        CoreAnnotations.PartOfSpeechAnnotation.class);
  }

  public static void main(String[] args) {
    Properties props = new Properties();
    props.setProperty("annotators", "tokenize, ssplit");
    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
    pipeline.addAnnotator(new RussianLemmatizationAnnotator());

    Annotation annotation1 = pipeline.process("домами");
    Annotation annotation2 = pipeline.process("книги");

    System.out.println(annotation1.get(TokensAnnotation.class).get(0).get(LemmaAnnotation.class));
    System.out.println(annotation2.get(TokensAnnotation.class).get(0).get(LemmaAnnotation.class));
  }

}
