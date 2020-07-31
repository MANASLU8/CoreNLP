package edu.stanford.nlp.international.russian.process;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.DependencyParseAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.StringUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class for changing the grammatical gender of words in Russian text to match the gender of the intended speaker and
 * addressee.
 *
 * @author George Cooper
 */

public class ChangeGender {

    private final static String DEFAULT_PATH_PARSER_MODEL =
            "edu//stanford//nlp//models//parser//nndep//nndep.rus.model.wiki.txt.gz";
    private final static String DEFAULT_PATH_TAGGER =
            "edu//stanford//nlp//models//pos-tagger//russian-ud-pos.tagger";
    private final static String DEFAULT_PATH_MF_TAGGER =
            "edu//stanford//nlp//models//pos-tagger//russian-ud-mf.tagger";

    private static final String LRB_PATTERN = "(?i)-LRB-";
    private static final String RRB_PATTERN = "(?i)-RRB-";

    @SafeVarargs
    private static <T> Set<T> toSet(T... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    private final static Set<String> RELATIONS = toSet("cop", "conj", "amod");

    private final static Set<Character> CONSONANTS =
            toSet('б', 'в', 'г', 'д', 'ж', 'з', 'й', 'к', 'л', 'м', 'н', 'п', 'р', 'с', 'т', 'ф', 'х', 'ц', 'ч', 'ш',
                    'щ');

    private final static Set<Character> VOWELS = toSet('а', 'э', 'ы', 'у', 'о', 'я', 'е', 'ё', 'ю', 'и');

    private final static Set<Character> SOFT_INDICATING_LETTERS = toSet('й', 'ь');

    private final static Set<Character> VELARS = toSet('к', 'г', 'х');

    private final static Set<Character> SIBILANTS = toSet('ж', 'ч', 'ш', 'щ');

    private final static Set<Character> SIBILANTS_AND_VELARS = new HashSet<>();
    static {
        SIBILANTS_AND_VELARS.addAll(SIBILANTS);
        SIBILANTS_AND_VELARS.addAll(VELARS);
    }

    private final static Set<Character> SIBILANTS_FOR_NOUNS = toSet('ж', 'ч', 'ш', 'щ', 'ц');

    private final static Set<String> RELEVANT_PRONOUNS = toSet("я", "ты");

    private final static Set<String> RELEVANT_PERSONS = toSet("1", "2");

    private final static Set<String> REFLEXIVE_SUFFIXES = toSet("ся", "сь");

    private final static String[][] IRREGULAR_VERBS = new String[][] {
            {"лез", "лезла"}, {"нёс", "несла"}, {"вёз", "везла"}, {"вёл", "вела"}, {"мёл", "мела"}, {"грёб", "гребла"},
            {"рос", "росла"}, {"пёк", "пекла"}, {"шёл", "шла"}, {"ушёл", "ушла"}, {"нашёл", "нашла"},
            {"прошёл", "прошла"}, {"пришёл", "пришла"}, {"вышел", "вышла"}
    };
    private final static Map<String, String> IRREGULAR_VERBS_MAP =
            Stream.of(IRREGULAR_VERBS).collect(Collectors.toMap(data -> data[0], data -> data[1]));
    private final static Map<String, String> IRREGULAR_VERBS_MAP_REVERSE =
            Stream.of(IRREGULAR_VERBS).collect(Collectors.toMap(data -> data[1], data -> data[0]));

    public final static Map<String, String> SELF_DECLENSIONS_MASC = Stream.of(new String[][]{
            {"Nom", "сам"},
            {"Acc", "самого"},
            {"Gen", "самого"},
            {"Dat", "самому"},
            {"Ins", "самим"},
            {"Loc", "самом"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    public final static Map<String, String> SELF_DECLENSIONS_FEM = Stream.of(new String[][]{
            {"Nom", "сама"},
            {"Acc", "саму"},
            {"Gen", "самой"},
            {"Dat", "самой"},
            {"Ins", "самой"},
            {"Loc", "самой"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    /* Mapping of adjective suffixes which can be unambiguously mapped to their corresponding opposite gender version.
    The keys are pairs of (grammatical case, gender) and the values are pairs of suffixes and their opposite gender
    version.

    See http://www.ruscorpora.ru/new/corpora-morph.html for definitions of the cases
    */
    private static final Map<Pair<String, String>, String[][]> ADJECTIVE_SUBSTITUTIONS = new HashMap<>();
    static {
        ADJECTIVE_SUBSTITUTIONS.put(new Pair<>("Nom", "Masc"), new String[][]{{"ый", "ая"}, {"ой", "ая"}});
        ADJECTIVE_SUBSTITUTIONS.put(new Pair<>("Nom", "Fem"), new String[][]{{"яя", "ий"}});
        ADJECTIVE_SUBSTITUTIONS.put(new Pair<>("Acc", "Masc"), new String[][]{{"ого", "ую"}});
        ADJECTIVE_SUBSTITUTIONS.put(new Pair<>("Acc", "Fem"), new String[][]{{"юю", "его"}});
        ADJECTIVE_SUBSTITUTIONS.put(new Pair<>("Gen", "Masc"), new String[][]{{"ого", "ой"}, {"его", "ей"}});
        ADJECTIVE_SUBSTITUTIONS.put(new Pair<>("Gen", "Fem"), new String[][]{{"ой", "ого"}, {"ей", "его"},
                                                                             {"ою", "ого"}, {"ею", "его"}});
        ADJECTIVE_SUBSTITUTIONS.put(new Pair<>("Dat", "Masc"), new String[][]{{"ому", "ой"}, {"ему", "ей"}});
        ADJECTIVE_SUBSTITUTIONS.put(new Pair<>("Dat", "Fem"), new String[][]{{"ой", "ому"}, {"ей", "ему"},
                                                                             {"ою", "ому"}, {"ею", "ему"}});
        ADJECTIVE_SUBSTITUTIONS.put(new Pair<>("Ins", "Masc"), new String[][]{{"ым", "ой"}});
        ADJECTIVE_SUBSTITUTIONS.put(new Pair<>("Ins", "Fem"), new String[][]{{"ой", "ым"}, {"ей", "им"}, {"ою", "ым"},
                                                                             {"ею", "им"}});
        ADJECTIVE_SUBSTITUTIONS.put(new Pair<>("Loc", "Masc"), new String[][]{{"ом", "ой"}, {"ем", "ей"}});
        ADJECTIVE_SUBSTITUTIONS.put(new Pair<>("Loc", "Fem"), new String[][]{{"ой", "ом"}, {"ей", "ем"}, {"ою", "ом"},
                                                                             {"ею", "ем"}});
    }

    static class Substitution {
        public String oldSuffix;
        public String newSuffix1;
        public String newSuffix2;
        public Set<Character> conditioningChars;

        public Substitution(String oldSuffix, String newSuffix1, String newSuffix2, Set<Character> conditioningChars) {
            this.oldSuffix = oldSuffix;
            this.newSuffix1 = newSuffix1;
            this.newSuffix2 = newSuffix2;
            this.conditioningChars = conditioningChars;
        }
    }

    private static final Map<Pair<String, String>, Substitution> CONDITIONAL_ADJECTIVE_SUBSTITUTIONS = new HashMap<>();
    static {
        CONDITIONAL_ADJECTIVE_SUBSTITUTIONS.put(new Pair<>("Nom", "Masc"),
                                                new Substitution("ий", "ая", "яя", SIBILANTS_AND_VELARS));
        CONDITIONAL_ADJECTIVE_SUBSTITUTIONS.put(new Pair<>("Nom", "Fem"),
                                                new Substitution("ая", "ий", "ый", SIBILANTS_AND_VELARS));
        CONDITIONAL_ADJECTIVE_SUBSTITUTIONS.put(new Pair<>("Acc", "Masc"),
                                                new Substitution("его", "ую", "юю", SIBILANTS));
        CONDITIONAL_ADJECTIVE_SUBSTITUTIONS.put(new Pair<>("Acc", "Fem"),
                                                new Substitution("ую", "его", "ого", SIBILANTS));
        CONDITIONAL_ADJECTIVE_SUBSTITUTIONS.put(new Pair<>("Ins", "Masc"),
                                                new Substitution("им", "ой", "ей", VELARS));
        CONDITIONAL_ADJECTIVE_SUBSTITUTIONS.put(new Pair<>("Ins", "Fem"),
                                                new Substitution("ой", "им", "ым", VELARS));
    }

    public final static Map<String, String> FIRST_DECLENSION_NOUN_SUFFIXES_1 = Stream.of(new String[][]{
            {"Acc", "у"},
            {"Gen", "ы"},
            {"Dat", "е"},
            {"Ins", "ой"},
            {"Loc", "е"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    public final static Map<String, String> FIRST_DECLENSION_NOUN_SUFFIXES_2 = Stream.of(new String[][]{
            {"Acc", "ю"},
            {"Gen", "и"},
            {"Dat", "е"},
            {"Ins", "ей"},
            {"Loc", "е"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    public final static Map<String, String> SECOND_DECLENSION_NOUN_SUFFIXES_HARD = Stream.of(new String[][]{
            {"Acc", "а"},
            {"Gen", "а"},
            {"Dat", "у"},
            {"Ins", "ом"},
            {"Loc", "е"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    public final static Map<String, String> SECOND_DECLENSION_NOUN_SUFFIXES_SOFT = Stream.of(new String[][]{
            {"Acc", "я"},
            {"Gen", "я"},
            {"Dat", "ю"},
            {"Ins", "ем"},
            {"Loc", "е"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    public final static Map<String, String> THIRD_DECLENSION_NOUN_SUFFIXES = Stream.of(new String[][]{
            {"Acc", "ь"},
            {"Gen", "и"},
            {"Dat", "и"},
            {"Ins", "ью"},
            {"Loc", "и"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
/*    // See http://masterrussian.com/nounsandcases/suffixes.htm
    private static final String[][] NOUN_SUFFIXES = new String[][] {
            {"тель", "тельница"}, {"ник", "ница"}, {"чик", "чица"}, {"анин", "анка"}, {"янин", "янка"},
            {"ист", "истка"}, {"ец", "ка"}, {"ин", "ка"}, {"", "ка"}
    };*/

    public enum TargetGender {FEM, MASC, UNCHANGED};

    public static void main(String[] args) throws IOException {
        Properties pr = StringUtils.argsToProperties(args);
        StanfordCoreNLP pipeline = getStanfordCoreNLP(pr);
        boolean speakerIsMale = false;
        boolean addresseeIsMale = false;
        if (pr.containsKey("speakerIsMale")) {
            speakerIsMale = true;
        }
        if (pr.containsKey("addresseeIsMale")) {
            addresseeIsMale = true;
        }
        BufferedReader stdInReader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        StringBuilder outputStringBuilder = new StringBuilder();

        while ((line = stdInReader.readLine()) != null) {
            outputStringBuilder.append(line);
            outputStringBuilder.append("\n");
        }
        System.out.println(
                adjustGenderForText(
                        outputStringBuilder.toString(), speakerIsMale ? TargetGender.MASC : TargetGender.FEM,
                        addresseeIsMale ? TargetGender.MASC : TargetGender.FEM,
                        pipeline
                ));
    }

    private static boolean isFirstOrSecondPersonSingularVerb(AnnotatedToken annotatedToken, String person) {
        return Objects.equals(annotatedToken.pos, "VERB") && RELEVANT_PERSONS.contains(person) &&
                Objects.equals(annotatedToken.features.get("Number"), "Sing");
    }

    private static boolean isFirstPersonSingularVerb(AnnotatedToken annotatedToken, String person) {
        return Objects.equals(annotatedToken.pos, "VERB") &&
                Objects.equals(person, "1") &&
                Objects.equals(annotatedToken.features.get("Number"), "Sing");
    }

    private static boolean isSecondPersonSingularVerb(AnnotatedToken annotatedToken, String person) {
        return Objects.equals(annotatedToken.pos, "VERB") &&
                Objects.equals(person, "2") &&
                Objects.equals(annotatedToken.features.get("Number"), "Sing");
    }

    private static boolean isFirstOrSecondPersonSubjectPronoun(AnnotatedToken annotatedToken) {
        return RELEVANT_PRONOUNS.contains(annotatedToken.lemma) && Objects.equals(annotatedToken.relnName, "nsubj");
    }

    private static boolean isFirstPersonSubjectPronoun(AnnotatedToken annotatedToken) {
        return "я".equals(annotatedToken.lemma) && Objects.equals(annotatedToken.relnName, "nsubj");
    }

    private static boolean isSecondPersonSubjectPronoun(AnnotatedToken annotatedToken) {
        return "ты".equals(annotatedToken.lemma) && Objects.equals(annotatedToken.relnName, "nsubj");
    }

    public static String adjustSpeakerGenderForText(String text, TargetGender targetSpeakerGender, StanfordCoreNLP pipeline) {
        return adjustGenderForText(text, targetSpeakerGender, TargetGender.UNCHANGED, pipeline);
    }

    public static String adjustAddresseeGenderForText(String text, TargetGender targetAddresseeGender, StanfordCoreNLP pipeline) {
        return adjustGenderForText(text, TargetGender.UNCHANGED, targetAddresseeGender, pipeline);
    }

    public static String adjustGenderForText(String text, TargetGender targetSpeakerGender, TargetGender targetAddresseeGender, StanfordCoreNLP pipeline) {
        int lastCharacterIndexEnd;
        StringBuilder outputStringBuilder = new StringBuilder();
        if (!text.isEmpty()) {
            lastCharacterIndexEnd = 0;

            Annotation annotation = pipeline.process(text);

            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            for (CoreMap sentence : sentences) {
                SortedMap<Integer, Boolean> tokensToAdjust = new TreeMap<>();

                ParsedSentence parsedSentence = getParsedSentence(sentence);
                for (AnnotatedToken annotatedToken : parsedSentence.annotatedTokens) {
                    String person = annotatedToken.features.get("Person");
                    if (targetSpeakerGender != TargetGender.UNCHANGED) {
                        addSpeakerToken(annotatedToken, targetSpeakerGender.equals(TargetGender.MASC), person, tokensToAdjust);
                    }
                    if (targetAddresseeGender != TargetGender.UNCHANGED) {
                        addAddresseeToken(annotatedToken, targetAddresseeGender.equals(TargetGender.MASC), person, tokensToAdjust);
                    }

                    // Identify additional tokens requiring gender change due to their relation to the subject or verb tokens
                    Map<Integer, Boolean> newTokensToAdjust = processRelatedTokens(tokensToAdjust, parsedSentence);

                    tokensToAdjust.putAll(newTokensToAdjust);
                }

                // Adjust the gender of selected tokens
                for (Integer tokenIndex : tokensToAdjust.keySet()) {
                    AnnotatedToken tokenToAdjust = parsedSentence.annotatedTokens.get(tokenIndex);
                    String newToken = adjustGenderForToken(tokenToAdjust, tokensToAdjust.get(tokenIndex));
                    if (newToken != null) {
                        outputStringBuilder.append(text, lastCharacterIndexEnd, tokenToAdjust.characterStartIndex);
                        outputStringBuilder.append(newToken);
                        lastCharacterIndexEnd = tokenToAdjust.characterEndIndex;
                    }
                }
            }
            outputStringBuilder.append(text.substring(lastCharacterIndexEnd));
        }
        return outputStringBuilder.toString();
    }

    public static void addSpeakerToken(AnnotatedToken annotatedToken, boolean toMasculine, String person, SortedMap<Integer, Boolean> tokensToAdjust) {
        if (isFirstPersonSingularVerb(annotatedToken, person)) {
            tokensToAdjust.put(annotatedToken.tokenIndex, toMasculine);
            // See https://universaldependencies.org/u/dep/index.html for descriptions of the dependency
            // relations
        } else if (isFirstPersonSubjectPronoun(annotatedToken)) {
            tokensToAdjust.put(annotatedToken.govIdx, toMasculine);
        }
    }

    public static void addAddresseeToken(AnnotatedToken annotatedToken, boolean toMasculine, String person, SortedMap<Integer, Boolean> tokensToAdjust) {
        if (isSecondPersonSingularVerb(annotatedToken, person)) {
            tokensToAdjust.put(annotatedToken.tokenIndex, toMasculine);
            // See https://universaldependencies.org/u/dep/index.html for descriptions of the dependency
            // relations
        } else if (isSecondPersonSubjectPronoun(annotatedToken)) {
            tokensToAdjust.put(annotatedToken.govIdx, toMasculine);
        }
    }

    public static Map<Integer, Boolean> processRelatedTokens(SortedMap<Integer, Boolean> originalTokens, ParsedSentence parsedSentence) {
        Map<Integer, Boolean> newTokensToAdjust = new HashMap<>();
        for (Map.Entry<Integer, Boolean> entry : originalTokens.entrySet()) {
            List<Arc> outgoingArcs = parsedSentence.outgoingArcs.get(entry.getKey());
            if (outgoingArcs != null) {
                for (Arc outgoingArc : outgoingArcs) {
                    if (RELATIONS.contains(outgoingArc.relnName) ||
                            (Objects.equals(outgoingArc.relnName, "obl") &&
                                    Objects.equals(parsedSentence.annotatedTokens.get(outgoingArc.tokenIndex).lemma,
                                            "сам"))) {
                        newTokensToAdjust.put(outgoingArc.tokenIndex, entry.getValue());
                    }
                }
            }
        }
        return newTokensToAdjust;
    }

    public static StanfordCoreNLP getStanfordCoreNLP(Properties pr) {
        String tagger = DEFAULT_PATH_TAGGER;
        String taggerMF = DEFAULT_PATH_MF_TAGGER;
        String parser = DEFAULT_PATH_PARSER_MODEL;
        String pLemmaDict = null;
        if (pr.containsKey("tagger")) {
            tagger = pr.getProperty("tagger");
        }
        if (pr.containsKey("taggerMF")) {
            taggerMF = pr.getProperty("taggerMF");
        }
        if (pr.containsKey("parser")) {
            parser = pr.getProperty("parser");
        }
        if (pr.containsKey("pLemmaDict")) {
            pLemmaDict = pr.getProperty("pLemmaDict");
        }
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        pipeline.addAnnotator(new RussianMorphoAnnotator(new MaxentTagger(taggerMF)));
        pipeline.addAnnotator(new POSTaggerAnnotator(new MaxentTagger(tagger)));

        Properties propsParser = new Properties();
        propsParser.setProperty("model", parser);
        propsParser.setProperty("tagger.model", tagger);
        pipeline.addAnnotator(new DependencyParseAnnotator(propsParser));

        if (pLemmaDict == null) {
            pipeline.addAnnotator(new RussianLemmatizationAnnotator());
        } else {
            pipeline.addAnnotator(new RussianLemmatizationAnnotator(pLemmaDict));
        }
        return pipeline;
    }

    public static String adjustGenderForToken(AnnotatedToken annotatedToken, boolean shouldBeMale) {
        // If the word has no gender or already has the desired gender, no need to change it
        if (!annotatedToken.features.containsKey("Gender") ||
                (Objects.equals(annotatedToken.features.get("Gender"), "Masc") && shouldBeMale) ||
                (Objects.equals(annotatedToken.features.get("Gender"), "Fem") && !shouldBeMale)) {
            return null;
        }

        String lowerToken = annotatedToken.token.toLowerCase();

        // Remove the reflexive suffix if it exists before adjusting the main part of the verb, then add it back again 
        // after doing so.
        boolean isReflexive = false;
        if (lowerToken.length() >= 2) {
            String lastTwoChars = lowerToken.substring(lowerToken.length() - 2);
            isReflexive = Objects.equals(annotatedToken.pos, "VERB") && REFLEXIVE_SUFFIXES.contains(lastTwoChars);
        }
        String cleanToken = isReflexive ? StringUtils.removeLastNChars(lowerToken, 2) : lowerToken;

        String result = null;
        boolean isParticiple = Objects.equals(annotatedToken.features.get("VerbForm"), "Part");
        // Participles are declined like adjectives
        if (Objects.equals(annotatedToken.pos, "ADJ") ||
                (Objects.equals(annotatedToken.pos, "VERB") && isParticiple)) {
            result = adjustGenderForAdjective(cleanToken, annotatedToken, shouldBeMale);
        } else if ((Objects.equals(annotatedToken.pos, "VERB") || Objects.equals(annotatedToken.pos, "AUX")) &&
                Objects.equals(annotatedToken.features.get("Tense"), "Past")) {
            result = adjustGenderForVerb(cleanToken, shouldBeMale);
        } else if (Objects.equals(annotatedToken.pos, "NOUN")) {
            result = adjustGenderForNoun(cleanToken, annotatedToken, shouldBeMale);
        }

        // See http://www.russianlessons.net/grammar/verbs_reflexive.php
        if (isReflexive && result != null) {
            result += isParticiple || CONSONANTS.contains(result.charAt(result.length() - 1)) ? "ся" : "сь";
        }

        // Imitate the capitalization scheme of the original token in the replacement
        if (result != null) {
            if (StringUtils.isAllUpper(annotatedToken.token)) {
                return result.toUpperCase();
            } else if (StringUtils.isCapitalized(annotatedToken.token)) {
                return StringUtils.capitalize(result);
            }
        }

        return result;
    }

    // http://www.study-languages-online.com/russian-adjectives-short.html
    private static String adjustGenderForShortAdjective(String cleanToken, AnnotatedToken annotatedToken,
                                                        boolean shouldBeMale) {
        if (shouldBeMale) {
            if (cleanToken.endsWith("а")) {
                String stem = StringUtils.removeLastNChars(cleanToken, 1);
                char lastChar = stem.charAt(stem.length() - 1);
                if (stem.length() >= 2 && CONSONANTS.contains(lastChar) &&
                        CONSONANTS.contains(stem.charAt(stem.length() - 2))) {
                    String interfix = null;
                    if (lastChar == 'к' || Objects.equals(stem, "полн") || Objects.equals(stem, "смешн")) {
                        interfix = "о";
                    } else if (lastChar == 'н') {
                        interfix = "е";
                    }

                    if (interfix != null) {
                        stem = StringUtils.removeLastNChars(stem, 1) + interfix + lastChar;
                    }
                }
                return stem;
            }
        } else {
            String stem = cleanToken;
            char penultimateChar = stem.charAt(stem.length() - 2);
            if ((penultimateChar == 'о' || penultimateChar == 'е') &&
                    annotatedToken.lemma.charAt(annotatedToken.lemma.length() - 4) != penultimateChar) {
                stem = StringUtils.removeLastNChars(stem, 2) + stem.charAt(stem.length() - 1);
            }
            return stem + "а";
        }
        return null;
    }

    private static String adjustGenderForRegularAdjective(String cleanToken, AnnotatedToken annotatedToken,
                                                          boolean shouldBeMale, String tokenCase) {

        // The lemma is actually the nominative masculine singular form of the adjective, so if that's what we want,
        // just use it. For participles, the lemma is the infinitive of the verb, so we cannot use this shortcut in
        // that case.
        if (Objects.equals(annotatedToken.pos, "ADJ") && Objects.equals(tokenCase, "Nom") && shouldBeMale) {
            return annotatedToken.lemma.toLowerCase();
        }

        // https://www.russlandjournal.de/en/learn-russian/adjectives/

        String oldSuffix = null;
        String newSuffix = null;

        Pair<String, String> caseGender = new Pair<>(tokenCase, annotatedToken.features.get("Gender"));
        Substitution substitution = CONDITIONAL_ADJECTIVE_SUBSTITUTIONS.get(caseGender);
        if (substitution != null && cleanToken.endsWith(substitution.oldSuffix)) {
            oldSuffix = substitution.oldSuffix;
            if (substitution.conditioningChars.contains(cleanToken.charAt(cleanToken.length() - (oldSuffix.length() + 1)))) {
                newSuffix = substitution.newSuffix1;
            } else {
                newSuffix = substitution.newSuffix2;
            }
        }

        if (oldSuffix == null && ADJECTIVE_SUBSTITUTIONS.containsKey(caseGender)) {
            for (String[] suffixes : ADJECTIVE_SUBSTITUTIONS.get(caseGender)) {
                if (cleanToken.endsWith(suffixes[0])) {
                    oldSuffix = suffixes[0];
                    newSuffix = suffixes[1];
                    break;
                }
            }
        }

        if (oldSuffix == null) {
            return null;
        } else {
            return StringUtils.removeLastNChars(cleanToken, oldSuffix.length()) + newSuffix;
        }
    }

    private static String adjustGenderForAdjective(String cleanToken, AnnotatedToken annotatedToken,
                                                   boolean shouldBeMale) {
        String tokenCase = annotatedToken.features.get("Case");

        // http://masterrussian.com/vocabulary/sam_myself.htm
        if (Objects.equals(annotatedToken.lemma, "сам")) {
            if (shouldBeMale) {
                return SELF_DECLENSIONS_MASC.get(tokenCase);
            }
            return SELF_DECLENSIONS_FEM.get(tokenCase);
        }

        if (Objects.equals(annotatedToken.features.get("Variant"), "Short")) {
            return adjustGenderForShortAdjective(cleanToken, annotatedToken, shouldBeMale);
        }

        return adjustGenderForRegularAdjective(cleanToken, annotatedToken, shouldBeMale, tokenCase);
    }

    /**
     * Declines a noun. Only designed to work for animate singular masculine and feminine nouns. See:
     * <ul>
     *     <li>http://russianlearn.com/grammar/category/declension_of_nouns</li>
     *     <li>http://masterrussian.com/aa052000a.shtml</li>
     *     <li>https://en.wiktionary.org/wiki/Appendix:Russian_nouns#Declension_paradigms</li>
     * </ul>
     *
     * @param lemma   The lemma of the noun to decline
     * @param annotatedToken   The annotatedToken for the token that an opposite-gender noun that was changed
     * @param shouldBeMale   If true, the lemma is masculine
     * @return   The declined noun
     */
    public static String declineNoun(String lemma, AnnotatedToken annotatedToken, boolean shouldBeMale) {
        String tokenCase = annotatedToken.features.get("Case");
        if (Objects.equals(tokenCase, "Nom")) {
            return lemma;
        }
        String oldSuffix = null;
        String newSuffix = null;
        char lastChar = lemma.charAt(lemma.length() - 1);
        if (lastChar == 'а' || lastChar == 'я' || (!shouldBeMale && lastChar != 'ь')) {
            char stemLastChar;
            if (VOWELS.contains(lastChar)) {
                oldSuffix = String.valueOf(lastChar);
                stemLastChar = lemma.charAt(lemma.length() - 2);
            } else {
                stemLastChar = lastChar;
            }

            if (SIBILANTS_FOR_NOUNS.contains(stemLastChar) && Objects.equals(tokenCase, "Ins")) {
                // Not correct if accent is on the last syllable
                newSuffix = "ей";
            } else if ((lemma.endsWith("ия") && (Objects.equals(tokenCase, "Dat") || Objects.equals(tokenCase, "Loc"))) ||
                        (SIBILANTS_AND_VELARS.contains(stemLastChar) && Objects.equals(tokenCase, "Gen"))) {
                newSuffix = "и";
            } else if (lastChar == 'я') {
                newSuffix = FIRST_DECLENSION_NOUN_SUFFIXES_2.get(tokenCase);
            } else {
                newSuffix = FIRST_DECLENSION_NOUN_SUFFIXES_1.get(tokenCase);
            }
        } else if (shouldBeMale) {
            if (lemma.endsWith("ий")) {
                oldSuffix = "й";
                if (Objects.equals(tokenCase, "Loc")) {
                    newSuffix = "и";
                } else if (Objects.equals(tokenCase, "Acc")) {
                    return lemma;
                }
            } else if (SIBILANTS_FOR_NOUNS.contains(lastChar) && Objects.equals(tokenCase, "Ins")) {
                // Not correct if accent is on the last syllable
                newSuffix = "ем";
            }
            if (newSuffix == null) {
                if (SOFT_INDICATING_LETTERS.contains(lastChar)) {
                    oldSuffix = String.valueOf(lastChar);
                    newSuffix = SECOND_DECLENSION_NOUN_SUFFIXES_SOFT.get(tokenCase);
                } else {
                    newSuffix = SECOND_DECLENSION_NOUN_SUFFIXES_HARD.get(tokenCase);
                }
            }
        } else {
            oldSuffix = "ь";
            newSuffix = THIRD_DECLENSION_NOUN_SUFFIXES.get(tokenCase);
        }

        if (newSuffix == null) {
            return lemma;
        }
        return (oldSuffix == null ? lemma : StringUtils.removeLastNChars(lemma, oldSuffix.length())) + newSuffix;
    }

    private static String adjustGenderForNoun(String cleanToken, AnnotatedToken annotatedToken, boolean shouldBeMale) {
/*        String oldSuffix = null;
        String newSuffix = null;
        for (String[] wordPair : NOUN_SUFFIXES) {
            if (shouldBeMale && cleanToken.endsWith(wordPair[1])) {
                oldSuffix = wordPair[1];
                newSuffix = wordPair[0];
                break;
            } else if (!shouldBeMale && cleanToken.endsWith(wordPair[0])) {
                oldSuffix = wordPair[0];
                newSuffix = wordPair[1];
                break;
            }
        }

        if (newSuffix != null) {
            return cleanToken.substring(0, cleanToken.length() - oldSuffix.length()) + newSuffix;
        }*/

        // TODO: handle other declensions
        if (shouldBeMale && Nouns.FEM_TO_MASC_MAP.containsKey(cleanToken)) {
            return declineNoun(Nouns.FEM_TO_MASC_MAP.get(cleanToken), annotatedToken, shouldBeMale);
        } else if (!shouldBeMale && Nouns.MASC_TO_FEM_MAP.containsKey(cleanToken)) {
            return declineNoun(Nouns.MASC_TO_FEM_MAP.get(cleanToken), annotatedToken, shouldBeMale);
        }

        return null;
    }

    private static String adjustGenderForVerb(String cleanToken, boolean shouldBeMale) {
        // https://en.wikipedia.org/wiki/Russian_grammar#Past_tense
        String newToken = cleanToken;

        if (shouldBeMale) {
            if (IRREGULAR_VERBS_MAP_REVERSE.containsKey(cleanToken)) {
                return IRREGULAR_VERBS_MAP_REVERSE.get(cleanToken);
            }
        } else if (IRREGULAR_VERBS_MAP.containsKey(cleanToken)) {
            return IRREGULAR_VERBS_MAP.get(cleanToken);
        }

        if (shouldBeMale) {
            if (cleanToken.endsWith("а")) {
                return StringUtils.removeLastNChars(cleanToken, 1);
            }
        } else {
            if (!cleanToken.endsWith("л")) {
                newToken += "л";
            }
            return newToken + "а";
        }

        return cleanToken;
    }

    // Adapted from src.edu.stanford.nlp.trees.ud.CoNLLUDocumentWriter.printSemanticGraph()
    private static ParsedSentence getParsedSentence(CoreMap sentence) {
        SemanticGraph sg = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
        List<AnnotatedToken> annotatedTokens = new ArrayList<>();
        Map<Integer, List<Arc>> outgoingArcs = new HashMap<>();

        for (IndexedWord token : sg.vertexListSorted()) {
            AnnotatedToken annotatedToken = new AnnotatedToken();
            annotatedToken.tokenIndex = token.index() - 1;
            annotatedToken.characterStartIndex = token.beginPosition();
            annotatedToken.characterEndIndex = token.endPosition();

            /* Try to find main governor and additional dependencies. */
            annotatedToken.govIdx = null;
            GrammaticalRelation reln = null;
            for (IndexedWord parent : sg.getParents(token)) {
                SemanticGraphEdge edge = sg.getEdge(parent, token);
                if (annotatedToken.govIdx == null && !edge.isExtra()) {
                    annotatedToken.govIdx = parent.index() - 1;
                    reln = edge.getRelation();
                }
            }

            annotatedToken.token = token.word();
            annotatedToken.features = token.get(CoreAnnotations.CoNLLUFeats.class);
            if (annotatedToken.features == null) {
                annotatedToken.features = new HashMap<>();
            }
            annotatedToken.pos = token.getString(CoreAnnotations.PartOfSpeechAnnotation.class);
            // The string comparisons that are done against the lemma in this class are not case sensitive
            annotatedToken.lemma = token.getString(CoreAnnotations.LemmaAnnotation.class).toLowerCase();
            annotatedToken.relnName = reln == null ? null : reln.toString();

            /* Root. */
            if (annotatedToken.govIdx == null && sg.getRoots().contains(token)) {
                annotatedToken.govIdx = -1;
                annotatedToken.relnName = GrammaticalRelation.ROOT.toString();
            } else if (annotatedToken.govIdx == null) {
                annotatedToken.relnName = null;
            }

            annotatedToken.token = annotatedToken.token.replaceAll(LRB_PATTERN, "(");
            annotatedToken.token = annotatedToken.token.replaceAll(RRB_PATTERN, ")");
            annotatedToken.lemma = annotatedToken.lemma.replaceAll(LRB_PATTERN, "(");
            annotatedToken.lemma = annotatedToken.lemma.replaceAll(RRB_PATTERN, ")");

            annotatedTokens.add(annotatedToken);
            List<Arc> outgoingArcsForNode = outgoingArcs.computeIfAbsent(annotatedToken.govIdx, k -> new ArrayList<>());
            outgoingArcsForNode.add(new Arc(annotatedToken.relnName, annotatedToken.tokenIndex));
        }

        return new ParsedSentence(annotatedTokens, outgoingArcs);
    }

    static class AnnotatedToken {
        public int tokenIndex;
        public String token;
        public String lemma;
        public String pos;
        public Map<String, String> features;
        public Integer govIdx;
        public String relnName;
        public int characterStartIndex;
        public int characterEndIndex;
    }

    static class Arc {
        public String relnName;
        public int tokenIndex;

        public Arc(String relnName, int tokenIndex) {
            this.relnName = relnName;
            this.tokenIndex = tokenIndex;
        }
    }

    static class ParsedSentence {
        public List<AnnotatedToken> annotatedTokens;
        public Map<Integer, List<Arc>> outgoingArcs;

        public ParsedSentence(List<AnnotatedToken> annotatedTokens, Map<Integer, List<Arc>> outgoingArcs) {
            this.annotatedTokens = annotatedTokens;
            this.outgoingArcs = outgoingArcs;
        }
    }
}