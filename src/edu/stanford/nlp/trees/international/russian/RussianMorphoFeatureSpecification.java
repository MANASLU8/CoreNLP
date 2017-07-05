package edu.stanford.nlp.trees.international.russian;

import edu.stanford.nlp.international.arabic.ArabicMorphoFeatureSpecification.ArabicMorphoFeatures;
import edu.stanford.nlp.international.morph.MorphoFeatureSpecification;
import edu.stanford.nlp.international.morph.MorphoFeatures;

import java.util.Arrays;
import java.util.List;

public class RussianMorphoFeatureSpecification extends MorphoFeatureSpecification {

  private static final long serialVersionUID = -2084844364133605410L;

  private static final String[] animacyVals = {"ANIM", "INAN"};
  private static final String[] aspectVals = {"IMPF", "PERF"};
  private static final String[] caseVals = {"NOM", "GEN", "DAT", "ACC", "INS", "LOC", "VOC", "PAR"};
  private static final String[] degreeVals = {"POS", "CMP", "SUP"};
  private static final String[] genderVals = {"MASC", "FEM", "NEUT"};
  private static final String[] moodVals = {"IND", "IMP"};
  private static final String[] numberVals = {"SING", "PLUR"};
  private static final String[] personVals = {"1", "2", "3"};
  private static final String[] tenseVals = {"PAST", "PRES", "FUT"};
  private static final String[] transitivityVals = {"TRNS", "INTRANS"};
  private static final String[] variantVals = {"SHORT"};
  private static final String[] verbFormVals = {"FIN", "INF", "PART", "TRANS"};
  private static final String[] voiceVals = {"ACT", "PASS", "MID"};


  @Override
  public List<String> getValues(MorphoFeatureType feat) {
    if (feat == MorphoFeatureType.ANIMACY)
      return Arrays.asList(animacyVals);
    else if (feat == MorphoFeatureType.ASP)
      return Arrays.asList(aspectVals);
    else if (feat == MorphoFeatureType.CASE)
      return Arrays.asList(caseVals);
    else if (feat == MorphoFeatureType.DEGREE)
      return Arrays.asList(degreeVals);
    else if (feat == MorphoFeatureType.GENDER)
      return Arrays.asList(genderVals);
    else if (feat == MorphoFeatureType.MOOD)
      return Arrays.asList(moodVals);
    else if (feat == MorphoFeatureType.NUM)
      return Arrays.asList(numberVals);
    else if (feat == MorphoFeatureType.PER)
      return Arrays.asList(personVals);
    else if (feat == MorphoFeatureType.TENSE)
      return Arrays.asList(tenseVals);
    else if (feat == MorphoFeatureType.TRANSITIVITY)
      return Arrays.asList(transitivityVals);
    else if (feat == MorphoFeatureType.VARIANT)
      return Arrays.asList(variantVals);
    else if (feat == MorphoFeatureType.VERBFORM)
      return Arrays.asList(verbFormVals);
    else if (feat == MorphoFeatureType.VOICE)
      return Arrays.asList(voiceVals);
    else
      throw new IllegalArgumentException(
          "French does not support feature type: " + feat.toString());
  }

  @Override
  public MorphoFeatures strToFeatures(String spec) {
    MorphoFeatures features = new ArabicMorphoFeatures();

    // Check for the boundary symbol
    if (spec == null || spec.equals("")) {
      return features;
    }


    if (isActive(MorphoFeatureType.ANIMACY)) {
      if (spec.contains("ANIM")) {
        features.addFeature(MorphoFeatureType.ANIMACY, animacyVals[0]);
      } else if (spec.contains("INAN")) {
        features.addFeature(MorphoFeatureType.ANIMACY, animacyVals[1]);
      }
    }

    if (isActive(MorphoFeatureType.ASP)) {
      if (spec.contains("IMPF")) {
        features.addFeature(MorphoFeatureType.ASP, aspectVals[0]);
      } else if (spec.contains("PERF")) {
        features.addFeature(MorphoFeatureType.ASP, aspectVals[1]);
      }
    }

    if (isActive(MorphoFeatureType.CASE)) {
      if (spec.contains("NOM")) {
        features.addFeature(MorphoFeatureType.CASE, caseVals[0]);
      } else if (spec.contains("GEN")) {
        features.addFeature(MorphoFeatureType.CASE, caseVals[1]);
      } else if (spec.contains("DAT")) {
        features.addFeature(MorphoFeatureType.CASE, caseVals[2]);
      } else if (spec.contains("ACC")) {
        features.addFeature(MorphoFeatureType.CASE, caseVals[3]);
      } else if (spec.contains("INS")) {
        features.addFeature(MorphoFeatureType.CASE, caseVals[4]);
      } else if (spec.contains("LOC")) {
        features.addFeature(MorphoFeatureType.CASE, caseVals[5]);
      } else if (spec.contains("VOC")) {
        features.addFeature(MorphoFeatureType.CASE, caseVals[6]);
      } else if (spec.contains("PAR")) {
        features.addFeature(MorphoFeatureType.CASE, caseVals[7]);
      }
    }

    if (isActive(MorphoFeatureType.DEGREE)) {
      if (spec.contains("POS")) {
        features.addFeature(MorphoFeatureType.DEGREE, degreeVals[0]);
      } else if (spec.contains("CMP")) {
        features.addFeature(MorphoFeatureType.DEGREE, degreeVals[1]);
      } else if (spec.contains("SUP")) {
        features.addFeature(MorphoFeatureType.DEGREE, degreeVals[2]);
      }
    }

    if (isActive(MorphoFeatureType.GENDER)) {
      if (spec.contains("MASC")) {
        features.addFeature(MorphoFeatureType.GENDER, genderVals[0]);
      } else if (spec.contains("FEM")) {
        features.addFeature(MorphoFeatureType.GENDER, genderVals[1]);
      } else if (spec.contains("NEUT")) {
        features.addFeature(MorphoFeatureType.GENDER, genderVals[2]);
      }
    }

    if (isActive(MorphoFeatureType.MOOD)) {
      if (spec.contains("IND")) {
        features.addFeature(MorphoFeatureType.MOOD, moodVals[0]);
      } else if (spec.contains("IMP")) {
        features.addFeature(MorphoFeatureType.MOOD, moodVals[1]);
      }
    }

    if (isActive(MorphoFeatureType.NUM)) {
      if (spec.contains("SING")) {
        features.addFeature(MorphoFeatureType.NUM, numberVals[0]);
      } else if (spec.contains("PLUR")) {
        features.addFeature(MorphoFeatureType.NUM, numberVals[1]);
      }
    }

    if (isActive(MorphoFeatureType.PER)) {
      if (spec.contains("1")) {
        features.addFeature(MorphoFeatureType.PER, personVals[0]);
      } else if (spec.contains("2")) {
        features.addFeature(MorphoFeatureType.PER, personVals[1]);
      } else if (spec.contains("3")) {
        features.addFeature(MorphoFeatureType.PER, personVals[2]);
      }
    }

    if (isActive(MorphoFeatureType.TENSE)) {
      if (spec.contains("PAST")) {
        features.addFeature(MorphoFeatureType.TENSE, tenseVals[0]);
      } else if (spec.contains("PRES")) {
        features.addFeature(MorphoFeatureType.TENSE, tenseVals[1]);
      } else if (spec.contains("FUT")) {
        features.addFeature(MorphoFeatureType.TENSE, tenseVals[2]);
      }
    }

    if (isActive(MorphoFeatureType.TRANSITIVITY)) {
      if (spec.contains("TRNS")) {
        features.addFeature(MorphoFeatureType.TENSE, tenseVals[0]);
      } else if (spec.contains("INTRANS")) {
        features.addFeature(MorphoFeatureType.TENSE, tenseVals[1]);
      }
    }

    if (isActive(MorphoFeatureType.VARIANT)) {
      if (spec.contains("SHORT")) {
        features.addFeature(MorphoFeatureType.VARIANT, variantVals[0]);
      }
    }

    if (isActive(MorphoFeatureType.VERBFORM)) {
      if (spec.contains("FIN")) {
        features.addFeature(MorphoFeatureType.VERBFORM, verbFormVals[0]);
      } else if (spec.contains("INF")) {
        features.addFeature(MorphoFeatureType.VERBFORM, verbFormVals[1]);
      } else if (spec.contains("PART")) {
        features.addFeature(MorphoFeatureType.VERBFORM, verbFormVals[2]);
      } else if (spec.contains("TRANS")) {
        features.addFeature(MorphoFeatureType.VERBFORM, verbFormVals[3]);
      }
    }

    if (isActive(MorphoFeatureType.VOICE)) {
      if (spec.contains("ACT")) {
        features.addFeature(MorphoFeatureType.VOICE, voiceVals[0]);
      } else if (spec.contains("PASS")) {
        features.addFeature(MorphoFeatureType.VOICE, voiceVals[1]);
      } else if (spec.contains("MID")) {
        features.addFeature(MorphoFeatureType.VOICE, voiceVals[2]);
      }
    }

    return null;
  }

}
