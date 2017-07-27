package edu.stanford.nlp.international.russian.process;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import edu.stanford.nlp.util.Timing;
import edu.stanford.nlp.util.concurrent.MulticoreWrapper;
import edu.stanford.nlp.util.concurrent.ThreadsafeProcessor;
import edu.stanford.nlp.util.logging.Redwood;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RussianMorphoAnnotator implements Annotator {

  public static final String DEFAULT_POS_MODEL =
      "src//edu//stanford//nlp//models//pos-tagger//russian//russian-ud-mf.tagger";


  private static Redwood.RedwoodChannels log = Redwood.channels(RussianMorphoAnnotator.class);

  private final MaxentTagger pos;

  private final int maxSentenceLength;

  private final int nThreads;

  private final boolean reuseTags;

  private final Pattern pattern = Pattern.compile("[0-9]+");

  private static Map<String, List<String>> featsMap = new HashMap<String, List<String>>();

  private static Map<String, List<String>> featsValMap = new HashMap<String, List<String>>();

  static {
    init();
  }

  private static void init() {
    featsMap.put("DET", new ArrayList<String>() {
      {
        add("Animacy");
        add("Case");
        add("Degree");
        add("Gender");
        add("Number");
      }
    });
    featsMap.put("ADV", new ArrayList<String>() {
      {
        add("Degree");
        add("Polarity");
      }
    });
    featsMap.put("AUX", new ArrayList<String>() {
      {
        add("Aspect");
        add("Case");
        add("Gender");
        add("Mood");
        add("Number");
        add("Person");
        add("Tense");
        add("VerbForm");
        add("Voice");
      }
    });
    featsMap.put("PRON", new ArrayList<String>() {
      {
        add("Animacy");
        add("Case");
        add("Gender");
        add("Number");
        add("Person");
      }
    });
    featsMap.put("PROPN", new ArrayList<String>() {
      {
        add("Animacy");
        add("Case");
        add("Foreign");
        add("Gender");
        add("Number");
      }
    });
    featsMap.put("PART", new ArrayList<String>() {
      {
        add("Mood");
        add("Polarity");
      }
    });
    featsMap.put("ADJ", new ArrayList<String>() {
      {
        add("Animacy");
        add("Case");
        add("Degree");
        add("Foreign");
        add("Gender");
        add("Number");
        add("Variant");
      }
    });
    featsMap.put("VERB", new ArrayList<String>() {
      {
        add("Animacy");
        add("Aspect");
        add("Case");
        add("Gender");
        add("Mood");
        add("Number");
        add("Person");
        add("Tense");
        add("Variant");
        add("VerbForm");
        add("Voice");
      }
    });
    featsMap.put("NUM", new ArrayList<String>() {
      {
        add("Animacy");
        add("Case");
        add("Gender");
      }
    });
    featsMap.put("X", new ArrayList<String>() {
      {
        add("Foreign");
      }
    });
    featsMap.put("SCONJ", new ArrayList<String>() {
      {
        add("Mood");
      }
    });
    featsMap.put("NOUN", new ArrayList<String>() {
      {
        add("Animacy");
        add("Case");
        add("Foreign");
        add("Gender");
        add("Number");
      }
    });

    featsValMap.put("Tense", new ArrayList<String>() {
      {
        add("Fut");
        add("Past");
        add("Pres");
      }
    });
    featsValMap.put("Foreign", new ArrayList<String>() {
      {
        add("Yes");
      }
    });
    featsValMap.put("Animacy", new ArrayList<String>() {
      {
        add("Anim");
        add("Inan");
      }
    });
    featsValMap.put("Degree", new ArrayList<String>() {
      {
        add("Cmp");
        add("Pos");
        add("Sup");
      }
    });
    featsValMap.put("VerbForm", new ArrayList<String>() {
      {
        add("Conv");
        add("Fin");
        add("Inf");
        add("Part");
      }
    });
    featsValMap.put("Gender", new ArrayList<String>() {
      {
        add("Fem");
        add("Masc");
        add("Neut");
      }
    });
    featsValMap.put("Aspect", new ArrayList<String>() {
      {
        add("Imp");
        add("Perf");
      }
    });
    featsValMap.put("Case", new ArrayList<String>() {
      {
        add("Acc");
        add("Dat");
        add("Gen");
        add("Ins");
        add("Loc");
        add("Nom");
        add("Par");
        add("Voc");
      }
    });
    featsValMap.put("Variant", new ArrayList<String>() {
      {
        add("Short");
      }
    });
    featsValMap.put("Mood", new ArrayList<String>() {
      {
        add("Cnd");
        add("Imp");
        add("Ind");
      }
    });
    featsValMap.put("Number", new ArrayList<String>() {
      {
        add("Plur");
        add("Sing");
      }
    });
    featsValMap.put("Polarity", new ArrayList<String>() {
      {
        add("Neg");
      }
    });
    featsValMap.put("Voice", new ArrayList<String>() {
      {
        add("Act");
        add("Mid");
        add("Pass");
      }
    });
    featsValMap.put("Person", new ArrayList<String>() {
      {
        add("1");
        add("2");
        add("3");
      }
    });
  }

  /**
   * Create a tagger annotator using the default Russian tagger from the models (and non-verbose
   * initialization).
   */
  public RussianMorphoAnnotator() {
    this(false);
  }

  public RussianMorphoAnnotator(boolean verbose) {
    this(System.getProperty("pos.model", DEFAULT_POS_MODEL), verbose);
  }

  public RussianMorphoAnnotator(String posLoc, boolean verbose) {
    this(posLoc, verbose, Integer.MAX_VALUE, 1);
  }

  /**
   * Create a POS tagger annotator.
   *
   * @param posLoc Location of POS tagger model (may be file path, classpath resource, or URL
   * @param verbose Whether to show verbose information on model loading
   * @param maxSentenceLength Sentences longer than this length will be skipped in processing
   * @param numThreads The number of threads for the POS tagger annotator to use
   */
  public RussianMorphoAnnotator(String posLoc, boolean verbose, int maxSentenceLength,
      int numThreads) {
    this(loadModel(posLoc, verbose), maxSentenceLength, numThreads);
  }

  public RussianMorphoAnnotator(MaxentTagger model) {
    this(model, Integer.MAX_VALUE, 1);
  }

  public RussianMorphoAnnotator(MaxentTagger model, int maxSentenceLength, int numThreads) {
    this.pos = model;
    this.maxSentenceLength = maxSentenceLength;
    this.nThreads = numThreads;
    this.reuseTags = false;
  }

  public RussianMorphoAnnotator(String annotatorName, Properties props) {
    String posLoc = props.getProperty(annotatorName + ".model");
    if (posLoc == null) {
      posLoc = DEFAULT_POS_MODEL;
    }
    boolean verbose = PropertiesUtils.getBool(props, annotatorName + ".verbose", false);
    this.pos = loadModel(posLoc, verbose);
    this.maxSentenceLength =
        PropertiesUtils.getInt(props, annotatorName + ".maxlen", Integer.MAX_VALUE);
    this.nThreads = PropertiesUtils.getInt(props, annotatorName + ".nthreads",
        PropertiesUtils.getInt(props, "nthreads", 1));
    this.reuseTags = PropertiesUtils.getBool(props, annotatorName + ".reuseTags", false);
  }

  private static MaxentTagger loadModel(String loc, boolean verbose) {
    Timing timer = null;
    if (verbose) {
      timer = new Timing();
      timer.doing("Loading POS Model [" + loc + ']');
    }
    MaxentTagger tagger = new MaxentTagger(loc);
    if (verbose) {
      timer.done();
    }
    return tagger;
  }

  @Override
  public void annotate(Annotation annotation) {
    // turn the annotation into a sentence
    if (annotation.containsKey(CoreAnnotations.SentencesAnnotation.class)) {
      if (nThreads == 1) {
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
          doOneSentence(sentence);
        }
      } else {
        MulticoreWrapper<CoreMap, CoreMap> wrapper =
            new MulticoreWrapper<>(nThreads, new POSTaggerProcessor());
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

  private class POSTaggerProcessor implements ThreadsafeProcessor<CoreMap, CoreMap> {
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
    List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
    List<TaggedWord> tagged = null;
    if (tokens.size() <= maxSentenceLength) {
      try {
        for (CoreLabel token : tokens) {
          // tagged = pos.tagSentence(tokens, this.reuseTags);
          // pos.tagString(toTag);
          // pos.tagString(token.originalText());
          tagged = pos.apply(new ArrayList<CoreLabel>() {
            {
              add(token);
            }
          });

          if (tagged != null) {
            setAnnotations(token, tagged.get(0).tag());
          } else {
            setAnnotations(token, "X");
          }
          // setAnnotations(token, );
        }
      } catch (OutOfMemoryError e) {
        log.error(e); // Beware that we can now get an OOM in logging, too.
        log.warn("Tagging of sentence ran out of memory. " + "Will ignore and continue: "
            + SentenceUtils.listToString(tokens));
      }
    }


    /*
     * if (tagged != null) { for (int i = 0, sz = tokens.size(); i < sz; i++) {
     * setAnnotations(tokens.get(i), tagged.get(i).tag()); } } else { for (CoreLabel token : tokens)
     * { setAnnotations(token, "X"); } }
     */
    return sentence;
  }

  private void setAnnotations(CoreLabel token, String pos) {
    String resPos = pos;
    Matcher matcher = pattern.matcher(pos);
    if (matcher.find()) {
      resPos = pos.substring(0, matcher.start());
      String feats = pos.substring(matcher.start());
      HashMap<String, String> featsMap = mappingFeats(resPos, feats);
      token.set(CoreAnnotations.CoNLLUFeats.class, featsMap);
    }
    token.set(CoreAnnotations.PartOfSpeechAnnotation.class, resPos);
    token.set(CoreAnnotations.CoarseTagAnnotation.class, resPos);
  }

  HashMap<String, String> mappingFeats(String resPos, String feats) {
    HashMap<String, String> featsHM = new HashMap<String, String>();

    List<String> listFeats = featsMap.get(resPos);
    if (listFeats != null && listFeats.size() == feats.length()) {
      for (int i = 0; i < listFeats.size(); i++) {
        List<String> values = featsValMap.get(listFeats.get(i));
        int index = Integer.valueOf(feats.substring(i, i + 1)) - 1;
        if (index != -1 && values.size() > index) {
          featsHM.put(listFeats.get(i), values.get(index));
        }
      }
    }
    return featsHM;
  }

  @Override
  public Set<Class<? extends CoreAnnotation>> requires() {
    return Collections.unmodifiableSet(new ArraySet<>(
        Arrays.asList(CoreAnnotations.TextAnnotation.class, CoreAnnotations.TokensAnnotation.class,
            CoreAnnotations.CharacterOffsetBeginAnnotation.class,
            CoreAnnotations.CharacterOffsetEndAnnotation.class,
            CoreAnnotations.SentencesAnnotation.class)));
  }

  @Override
  public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
    return Collections.singleton(CoreAnnotations.PartOfSpeechAnnotation.class);
  }

  public static void main(String[] args) {
    Properties props = new Properties();
    props.setProperty("annotators", "tokenize, ssplit");
    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
    pipeline.addAnnotator(new RussianMorphoAnnotator());
    // new MaxentTagger("C://Users//Ivan//Desktop//russian-ud-mf.tagger")));

    Annotation annotation = pipeline.process("бежал домами");

    CoreLabel cl1 = annotation.get(TokensAnnotation.class).get(0);
    CoreLabel cl2 = annotation.get(TokensAnnotation.class).get(1);

    System.out.println(cl1.get(CoreAnnotations.PartOfSpeechAnnotation.class) + " "
        + cl1.get(CoreAnnotations.CoarseTagAnnotation.class) + " "
        + cl1.get(CoreAnnotations.CoNLLUFeats.class));
    System.out.println(cl2.get(CoreAnnotations.PartOfSpeechAnnotation.class) + " "
        + cl2.get(CoreAnnotations.CoarseTagAnnotation.class) + " "
        + cl2.get(CoreAnnotations.CoNLLUFeats.class));
  }

}
