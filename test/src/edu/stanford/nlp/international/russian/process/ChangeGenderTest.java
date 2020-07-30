package edu.stanford.nlp.international.russian.process;
import edu.stanford.nlp.util.StringUtils;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author George Cooper
 */
public class ChangeGenderTest extends TestCase {
    private final static String[][] VERBS = new String[][] {
            {"делал", "делала"}, {"лез", "лезла"}, {"нёс", "несла"}, {"вёз", "везла"}, {"вёл", "вела"},
            {"мёл", "мела"}, {"грёб", "гребла"}, {"рос", "росла"}, {"пёк", "пекла"}, {"шёл", "шла"}, {"ушёл", "ушла"},
            {"нашёл", "нашла"}, {"прошёл", "прошла"}, {"пришёл", "пришла"}, {"вышел", "вышла"},
            {"Одевался", "Одевалась"}
    };

    private final static String[][] SHORT_ADJECTIVES = new String[][] {
            {"готовый", "готов", "готова"}, {"короткий", "короток", "коротка"}, {"редкий", "редок", "редка"},
            {"важный", "важен", "важна"}, {"полный", "полон", "полна"}, {"смешной", "смешон", "смешна"},
//            {"больной", "болен", "больна"}
    };

    private final String[][] LONG_ADJECTIVES = new String[][] {
            {"Nom", "новый", "новый", "новая"}, {"Acc", "новый", "нового", "новую"},
            {"Gen", "новый", "нового", "новой"}, {"Dat", "новый", "новому", "новой"},
            {"Ins", "новый", "новым", "новой"}, {"Loc", "новый", "новом", "новой"},
            {"Nom", "синий", "синий", "синяя"}, {"Acc", "синий", "синего", "синюю"},
            {"Gen", "синий", "синего", "синей"}, {"Dat", "синий", "синему", "синей"},
            {"Ins", "синий", "синим", "синей"}, {"Loc", "синий", "синем", "синей"},
            {"Nom", "маленький", "маленький", "маленькая"}, {"Acc", "маленький", "маленького", "маленькую"},
            {"Gen", "маленький", "маленького", "маленькой"}, {"Dat", "маленький", "маленькому", "маленькой"},
            {"Ins", "маленький", "маленьким", "маленькой"}, {"Loc", "маленький", "маленьком", "маленькой"},
            {"Nom", "хороший", "хороший", "хорошая"}, {"Acc", "хороший", "хорошего", "хорошую"},
            {"Gen", "хороший", "хорошего", "хорошей"}, {"Dat", "хороший", "хорошему", "хорошей"},
            {"Ins", "хороший", "хорошим", "хорошей"}, {"Loc", "хороший", "хорошем", "хорошей"},
            {"Nom", "молодой", "молодой", "молодая"}, {"Acc", "молодой", "молодого", "молодую"},
            {"Gen", "молодой", "молодого", "молодой"}, {"Dat", "молодой", "молодому", "молодой"},
            {"Ins", "молодой", "молодым", "молодой"}, {"Loc", "молодой", "молодом", "молодой"},
    };

    private final String[][] PARTICIPLES = new String[][] {
            {"Nom", "делать", "делающий", "делающая"}, {"Acc", "делать", "делающего", "делающую"},
            {"Gen", "делать", "делающего", "делающей"}, {"Dat", "делать", "делающему", "делающей"},
            {"Ins", "делать", "делающим", "делающей"}, {"Loc", "делать", "делающем", "делающей"},
            {"Nom", "делать", "делающийся", "делающаяся"}, {"Acc", "делать", "делающегося", "делающуюся"},
            {"Gen", "делать", "делающегося", "делающейся"}, {"Dat", "делать", "делающемуся", "делающейся"},
            {"Ins", "делать", "делающимся", "делающейся"}, {"Loc", "делать", "делающемся", "делающейся"}
    };

    private final String[][] ARCHAIC_ADJECTIVES = new String[][] {
            {"Ins", "новый", "новым", "новою"}, {"Ins", "синий", "синим", "синею"}
    };

    private final String[][] NOUNS = new String[][] {
            {"учитель", "учительница"}, {"писатель", "писательница"},
            // {"строитель", "строитель"},
            {"ученик", "ученица"}, {"художник", "художница"}, {"дворник", "дворник"},
            // {"лётчик", "лётчица"},
            {"переводчик", "переводчица"}, {"математик", "математик"}, {"историк", "историк"},
            {"австриец", "австрийка"}, {"американец", "американка"}, {"болгарин", "болгарка"},
            {"гражданин", "гражданка"}, {"египтянин", "египтянка"}, {"гитарист", "гитаристка"}, {"студент", "студентка"}
    };

    private final String[][] NOUN_DECLENSIONS = new String[][] {
            // First declension, stem ends in -я
            {"няня", "Nom", "Fem", "няня"}, {"няня", "Gen", "Fem", "няни"}, {"няня", "Dat", "Fem", "няне"},
            {"няня", "Acc", "Fem", "няню"}, {"няня", "Ins", "Fem", "няней"}, {"няня", "Loc", "Fem", "няне"},
            // First declension, stem ends in -а
            {"комната", "Nom", "Fem", "комната"}, {"комната", "Gen", "Fem", "комнаты"},
            {"комната", "Dat", "Fem", "комнате"}, {"комната", "Acc", "Fem", "комнату"},
            {"комната", "Ins", "Fem", "комнатой"}, {"комната", "Loc", "Fem", "комнате"},
            // First declension, stem ends in -ия
            {"линия", "Nom", "Fem", "линия"}, {"линия", "Gen", "Fem", "линии"}, {"линия", "Dat", "Fem", "линии"},
            {"линия", "Acc", "Fem", "линию"}, {"линия", "Ins", "Fem", "линией"}, {"линия", "Loc", "Fem", "линии"},
            // First declension, stem-stressed sibilant-stem
            {"птица", "Nom", "Fem", "птица"}, {"птица", "Gen", "Fem", "птицы"}, {"птица", "Dat", "Fem", "птице"},
            {"птица", "Acc", "Fem", "птицу"}, {"птица", "Ins", "Fem", "птицей"}, {"птица", "Loc", "Fem", "птице"},
            // First declension, stem ends in sibilant or velar
            {"книга", "Nom", "Fem", "книга"}, {"книга", "Gen", "Fem", "книги"}, {"книга", "Dat", "Fem", "книге"},
            {"книга", "Acc", "Fem", "книгу"}, {"книга", "Ins", "Fem", "книгой"}, {"книга", "Loc", "Fem", "книге"},
            // First declension, suffix-stressed sibilant-stem
            // {"свеча", "Nom", "Masc", "свеча"}, {"свеча", "Gen", "Masc", "свечи"}, {"свеча", "Dat", "Masc", "свече"},
            // {"свеча", "Acc", "Masc", "свечу"}, {"свеча", "Ins", "Masc", "свечой"}, {"свеча", "Loc", "Masc", "свече"},
            // Second declension, hard-stem
            {"брат", "Nom", "Masc", "брат"}, {"брат", "Gen", "Masc", "брата"}, {"брат", "Dat", "Masc", "брату"},
            {"брат", "Dat", "Masc", "брату"}, {"брат", "Ins", "Masc", "братом"}, {"брат", "Loc", "Masc", "брате"},
            // Second declension, soft-stem (ends in -ь)
            {"учитель", "Nom", "Masc", "учитель"}, {"учитель", "Gen", "Masc", "учителя"},
            {"учитель", "Dat", "Masc", "учителю"}, {"учитель", "Acc", "Masc", "учителя"},
            {"учитель", "Ins", "Masc", "учителем"}, {"учитель", "Loc", "Masc", "учителе"},
            // Second declension, soft-stem (ends in -й)
            {"герой", "Nom", "Masc", "герой"}, {"герой", "Gen", "Masc", "героя"}, {"герой", "Dat", "Masc", "герою"},
            {"герой", "Acc", "Masc", "героя"}, {"герой", "Ins", "Masc", "героем"}, {"герой", "Loc", "Masc", "герое"},
            // Second declension, masculine with stem ending in -ий
            {"гербарий", "Nom", "Masc", "гербарий"}, {"гербарий", "Gen", "Masc", "гербария"},
            {"гербарий", "Dat", "Masc", "гербарию"}, {"гербарий", "Acc", "Masc", "гербарий"},
            {"гербарий", "Ins", "Masc", "гербарием"}, {"гербарий", "Loc", "Masc", "гербарии"},
            // Second declension, stem-stressed sibilant-stem
            {"товарищ", "Nom", "Masc", "товарищ"}, {"товарищ", "Gen", "Masc", "товарища"},
            {"товарищ", "Dat", "Masc", "товарищу"}, {"товарищ", "Acc", "Masc", "товарища"},
            {"товарищ", "Ins", "Masc", "товарищем"}, {"товарищ", "Loc", "Masc", "товарище"},
            // Second declension, suffix-stressed sibilant-stem
            // {"врач", "Nom", "Masc", "врач"}, {"врач", "Gen", "Masc", "врача"}, {"врач", "Dat", "Masc", "врачу"},
            // {"врач", "Acc", "Masc", "врача"}, {"врач", "Ins", "Masc", "врачом"}, {"врач", "Loc", "Masc", "враче"},
            // Third declension
            {"боль", "Nom", "Fem", "боль"}, {"боль", "Gen", "Fem", "боли"}, {"боль", "Dat", "Fem", "боли"},
            {"боль", "Acc", "Fem", "боль"}, {"боль", "Ins", "Fem", "болью"}, {"боль", "Loc", "Fem", "боли"}
    };

    public void testVerbs() {
        for (String[] wordPair : VERBS) {
            ChangeGender.AnnotatedToken annotatedToken = new ChangeGender.AnnotatedToken();
            annotatedToken.token = wordPair[0];
            annotatedToken.pos = "VERB";
            annotatedToken.features = new HashMap<>();
            annotatedToken.features.put("Gender", "Masc");
            annotatedToken.features.put("Tense", "Past");
            testAdjustGenderForToken(annotatedToken, wordPair[1]);

            annotatedToken.token = wordPair[1];
            annotatedToken.features.put("Gender", "Fem");
            testAdjustGenderForToken(annotatedToken, wordPair[0]);

            annotatedToken.features.put("Tense", "Pres");
            assertNull(ChangeGender.adjustGenderForToken(annotatedToken, true));
        }
    }

    public void testSelf() {
        ChangeGender.AnnotatedToken annotatedToken = new ChangeGender.AnnotatedToken();
        annotatedToken.lemma = "сам";
        annotatedToken.pos = "ADJ";
        annotatedToken.features = new HashMap<>();
        annotatedToken.features.put("Gender", "Fem");
        ChangeGender.SELF_DECLENSIONS_MASC.forEach((key, value) -> {
            annotatedToken.features.put("Case", key);
            annotatedToken.token = ChangeGender.SELF_DECLENSIONS_FEM.get(key);
            testAdjustGenderForToken(annotatedToken, value);
        });

        annotatedToken.features.put("Gender", "Masc");
        ChangeGender.SELF_DECLENSIONS_FEM.forEach((key, value) -> {
            annotatedToken.features.put("Case", key);
            annotatedToken.token = ChangeGender.SELF_DECLENSIONS_MASC.get(key);
            testAdjustGenderForToken(annotatedToken, value);
        });
    }

    public void testShortAdjectives() {
        for (String[] wordTriple : SHORT_ADJECTIVES) {
            ChangeGender.AnnotatedToken annotatedToken = new ChangeGender.AnnotatedToken();
            annotatedToken.lemma = wordTriple[0];
            annotatedToken.token = wordTriple[1];
            annotatedToken.pos = "ADJ";
            annotatedToken.features = new HashMap<>();
            annotatedToken.features.put("Variant", "Short");
            annotatedToken.features.put("Gender", "Masc");
            testAdjustGenderForToken(annotatedToken, wordTriple[2]);

            annotatedToken.token = wordTriple[2];
            annotatedToken.features.put("Gender", "Fem");
            testAdjustGenderForToken(annotatedToken, wordTriple[1]);
        }
    }

    public void testLongAdjectives() {
        for (String[] wordTuple : LONG_ADJECTIVES) {
            ChangeGender.AnnotatedToken annotatedToken = new ChangeGender.AnnotatedToken();
            annotatedToken.lemma = wordTuple[1];
            annotatedToken.token = wordTuple[2];
            annotatedToken.pos = "ADJ";
            annotatedToken.features = new HashMap<>();
            annotatedToken.features.put("Gender", "Masc");
            annotatedToken.features.put("Case", wordTuple[0]);
            testAdjustGenderForToken(annotatedToken, wordTuple[3]);

            annotatedToken.token = wordTuple[3];
            annotatedToken.features.put("Gender", "Fem");
            testAdjustGenderForToken(annotatedToken, wordTuple[2]);
        }
    }

    public void testParticiples() {
        for (String[] wordTuple : PARTICIPLES) {
            ChangeGender.AnnotatedToken annotatedToken = new ChangeGender.AnnotatedToken();
            annotatedToken.lemma = wordTuple[1];
            annotatedToken.token = wordTuple[2];
            annotatedToken.pos = "VERB";
            annotatedToken.features = new HashMap<>();
            annotatedToken.features.put("VerbForm", "Part");
            annotatedToken.features.put("Gender", "Masc");
            annotatedToken.features.put("Case", wordTuple[0]);
            testAdjustGenderForToken(annotatedToken, wordTuple[3]);

            annotatedToken.token = wordTuple[3];
            annotatedToken.features.put("Gender", "Fem");
            testAdjustGenderForToken(annotatedToken, wordTuple[2]);
        }
    }

    public void testArchaicAdjectives() {
        for (String[] wordTuple : ARCHAIC_ADJECTIVES) {
            ChangeGender.AnnotatedToken annotatedToken = new ChangeGender.AnnotatedToken();
            annotatedToken.lemma = wordTuple[1];
            annotatedToken.token = wordTuple[3];
            annotatedToken.pos = "ADJ";
            annotatedToken.features = new HashMap<>();
            annotatedToken.features.put("Gender", "Fem");
            annotatedToken.features.put("Case", wordTuple[0]);
            testAdjustGenderForToken(annotatedToken, wordTuple[2]);
        }
    }

    public void testNouns() {
        for (String[] wordPair : NOUNS) {
            ChangeGender.AnnotatedToken annotatedToken = new ChangeGender.AnnotatedToken();
            annotatedToken.token = wordPair[0];
            annotatedToken.pos = "NOUN";
            annotatedToken.features = new HashMap<>();
            annotatedToken.features.put("Case", "Nom");
            annotatedToken.features.put("Gender", "Masc");
            testAdjustGenderForToken(annotatedToken, wordPair[1]);

            annotatedToken.token = wordPair[1];
            annotatedToken.features.put("Gender", "Fem");
            testAdjustGenderForToken(annotatedToken, wordPair[0]);
        }
    }

    public void testNounDeclensions() {
        for (String[] tokenInfo : NOUN_DECLENSIONS) {
            ChangeGender.AnnotatedToken annotatedToken = new ChangeGender.AnnotatedToken();
            annotatedToken.pos = "NOUN";
            annotatedToken.features = new HashMap<>();
            annotatedToken.features.put("Case", tokenInfo[1]);
            String result = ChangeGender.declineNoun(tokenInfo[0], annotatedToken,
                                                     Objects.equals(tokenInfo[2], "Masc"));
            assertEquals(tokenInfo[3], result);
        }
    }

    private void testAdjustGenderForToken(ChangeGender.AnnotatedToken annotatedToken, String expected) {
        // First verify that the token is changed appropriately when the desired gender is the opposite of the current
        // gender
        boolean shouldBeMale = Objects.equals(annotatedToken.features.get("Gender"), "Fem");
        String result = ChangeGender.adjustGenderForToken(annotatedToken, shouldBeMale);
        if (Objects.equals(annotatedToken.token, expected)) {
            assertNull(result);
        } else {
            assertEquals(expected, result);
            // Verify that the capitalization scheme of the replacement follows the capitalization scheme of the
            // original
            testCapitalizationScheme(annotatedToken, expected, shouldBeMale);
        }
        // When the desired gender is the same as the current gender, the return value should be null, indicating no
        // change
        assertNull(ChangeGender.adjustGenderForToken(annotatedToken, !shouldBeMale));
    }

    private void testCapitalizationScheme(ChangeGender.AnnotatedToken annotatedToken, String expected,
                                          boolean shouldBeMale) {
        String originalToken = annotatedToken.token;
        annotatedToken.token = originalToken.toUpperCase();
        assertEquals(expected.toUpperCase(), ChangeGender.adjustGenderForToken(annotatedToken, shouldBeMale));
        annotatedToken.token = StringUtils.capitalize(originalToken);
        assertEquals(StringUtils.capitalize(expected),
                ChangeGender.adjustGenderForToken(annotatedToken, shouldBeMale));
        annotatedToken.token = originalToken;
    }
}
