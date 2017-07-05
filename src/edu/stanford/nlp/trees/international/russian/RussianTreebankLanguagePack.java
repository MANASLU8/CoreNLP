package edu.stanford.nlp.trees.international.russian;

// import edu.stanford.nlp.international.russian.RussianMorphoFeatureSpecification;
import edu.stanford.nlp.international.morph.MorphoFeatureSpecification;
import edu.stanford.nlp.trees.AbstractTreebankLanguagePack;
import edu.stanford.nlp.trees.HeadFinder;

public class RussianTreebankLanguagePack extends AbstractTreebankLanguagePack {

  private static final long serialVersionUID = -7081852058934197646L;

  public static final String[] russianPunctTags = {"''", "``", ".", ":", ",", "PUNCT", "Пункт"}; // LRB
                                                                                                 // RRB

  private static final String[] russianSFPunctTags = {".", "PUNCT", "Пункт"};

  private static final String[] russianPunctWords = {"=", "*", "/", "\\", "]", "[", "\"", "''", "'",
      "``", "`", ".", "?", "!", ",", ":", "-", "--", "...", ";", "&quot;"};

  private static final String[] russianSFPunctWords = {".", "!", "?", "?!", "...", "!?"};

  private static final String[] russianStartSymbols = {"ROOT"};

  private static final char[] annotationIntroducingChars = {'-', '|', '#', '_'}; // убрала "=",
                                                                                 // добавила "-"

  // public static final String RU_ENCODING = "UTF-8";

  @Override
  public String[] sentenceFinalPunctuationWords() {
    return russianSFPunctWords;
  }

  @Override
  public String treebankFileExtension() {
    return "tree";
  }

  @Override
  public HeadFinder headFinder() {
    return new RussianHeadFinder(this);
  }

  @Override
  public HeadFinder typedDependencyHeadFinder() {
    return new RussianHeadFinder(this);
  }

  @Override
  public String[] punctuationTags() {
    return russianPunctTags;
  }

  @Override
  public String[] punctuationWords() {
    return russianPunctWords;
  }

  @Override
  public String[] sentenceFinalPunctuationTags() {
    return russianSFPunctTags;
  }

  @Override
  public String[] startSymbols() {
    return russianStartSymbols;
  }

  @Override
  public char[] labelAnnotationIntroducingCharacters() {
    return annotationIntroducingChars;
  }

  @Override
  public MorphoFeatureSpecification morphFeatureSpec() {
    return new RussianMorphoFeatureSpecification();
  }

}
