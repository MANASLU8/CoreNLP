# Stanford CoreNLP

[![Build Status](https://travis-ci.org/stanfordnlp/CoreNLP.svg?branch=master)](https://travis-ci.org/stanfordnlp/CoreNLP)
[![Maven Central](https://img.shields.io/maven-central/v/edu.stanford.nlp/stanford-corenlp.svg)](https://mvnrepository.com/artifact/edu.stanford.nlp/stanford-corenlp)
[![Twitter](https://img.shields.io/twitter/follow/stanfordnlp.svg?style=social&label=Follow)](https://twitter.com/stanfordnlp/)

Stanford CoreNLP provides a set of natural language analysis tools written in Java. It can take raw human language text input and give the base forms of words, their parts of speech, whether they are names of companies, people, etc., normalize and interpret dates, times, and numeric quantities, mark up the structure of sentences in terms of phrases or word dependencies, and indicate which noun phrases refer to the same entities. It was originally developed for English, but now also provides varying levels of support for (Modern Standard) Arabic, (mainland) Chinese, French, German, and Spanish. Stanford CoreNLP is an integrated framework, which make it very easy to apply a bunch of language analysis tools to a piece of text. Starting from plain text, you can run all the tools with just two lines of code. Its analyses provide the foundational building blocks for higher-level and domain-specific text understanding applications. Stanford CoreNLP is a set of stable and well-tested natural language processing tools, widely used by various groups in academia, industry, and government. The tools variously use rule-based, probabilistic machine learning, and deep learning components.

The Stanford CoreNLP code is written in Java and licensed under the GNU General Public License (v3 or later). Note that this is the full GPL, which allows many free uses, but not its use in proprietary software that you distribute to others.

### Build Instructions

Several times a year we distribute a new version of the software, which corresponds to a stable commit.

During the time between releases, one can always use the latest, under development version of our code.

Here are some helpful instructions to use the latest code:

#### Provided build

Sometimes we will provide updated jars here which have the latest version of the code.

At present [the current released version of the code](https://stanfordnlp.github.io/CoreNLP/#download) is our most recent released jar, though you can always build the very latest from GitHub HEAD yourself.

<!---
[stanford-corenlp.jar (last built: 2017-04-14)](http://nlp.stanford.edu/software/stanford-corenlp-2017-04-14-build.jar)
-->

#### Build with Ant

1. Make sure you have Ant installed, details here: [http://ant.apache.org/](http://ant.apache.org/)
2. Compile the code with this command: `cd CoreNLP ; ant`
3. Then run this command to build a jar with the latest version of the code: `cd CoreNLP/classes ; jar -cf ../stanford-corenlp.jar edu`
4. This will create a new jar called stanford-corenlp.jar in the CoreNLP folder which contains the latest code
5. The dependencies that work with the latest code are in CoreNLP/lib and CoreNLP/liblocal, so make sure to include those in your CLASSPATH.
6. When using the latest version of the code make sure to download the latest versions of the [corenlp-models](http://nlp.stanford.edu/software/stanford-corenlp-models-current.jar), [english-models](http://nlp.stanford.edu/software/stanford-english-corenlp-models-current.jar), and [english-models-kbp](http://nlp.stanford.edu/software/stanford-english-kbp-corenlp-models-current.jar) and include them in your CLASSPATH.  If you are processing languages other than English, make sure to download the latest version of the models jar for the language you are interested in.

#### Build with Maven

1. Make sure you have Maven installed, details here: [https://maven.apache.org/](https://maven.apache.org/)
2. If you run this command in the CoreNLP directory: `mvn package` , it should run the tests and build this jar file: `CoreNLP/target/stanford-corenlp-3.9.2.jar`
3. When using the latest version of the code make sure to download the latest versions of the [corenlp-models](http://nlp.stanford.edu/software/stanford-corenlp-models-current.jar), [english-models](http://nlp.stanford.edu/software/stanford-english-corenlp-models-current.jar), and [english-models-kbp](http://nlp.stanford.edu/software/stanford-english-kbp-corenlp-models-current.jar) and include them in your CLASSPATH.  If you are processing languages other than English, make sure to download the latest version of the models jar for the language you are interested in.  
4. If you want to use Stanford CoreNLP as part of a Maven project you need to install the models jars into your Maven repository.  Below is a sample command for installing the Spanish models jar.  For other languages just change the language name in the command.  To install `stanford-corenlp-models-current.jar` you will need to set `-Dclassifier=models`.  Here is the sample command for Spanish: `mvn install:install-file -Dfile=/location/of/stanford-spanish-corenlp-models-current.jar -DgroupId=edu.stanford.nlp -DartifactId=stanford-corenlp -Dversion=3.9.2 -Dclassifier=models-spanish -Dpackaging=jar` 

### Useful resources

You can find releases of Stanford CoreNLP on [Maven Central](https://search.maven.org/artifact/edu.stanford.nlp/stanford-corenlp/3.9.2/jar).

You can find more explanation and documentation on [the Stanford CoreNLP homepage](http://stanfordnlp.github.io/CoreNLP/).

The most recent models associated with the code in the HEAD of this repository can be found [here](http://nlp.stanford.edu/software/stanford-corenlp-models-current.jar).

Some of the larger (English) models -- like the shift-reduce parser and WikiDict -- are not distributed with our default models jar.
The most recent version of these models can be found [here](http://nlp.stanford.edu/software/stanford-english-corenlp-models-current.jar).

We distribute resources for other languages as well, including [Arabic models](http://nlp.stanford.edu/software/stanford-arabic-corenlp-models-current.jar),
[Chinese models](http://nlp.stanford.edu/software/stanford-chinese-corenlp-models-current.jar),
[French models](http://nlp.stanford.edu/software/stanford-french-corenlp-models-current.jar),
[German models](http://nlp.stanford.edu/software/stanford-german-corenlp-models-current.jar),
and [Spanish models](http://nlp.stanford.edu/software/stanford-spanish-corenlp-models-current.jar).

For information about making contributions to Stanford CoreNLP, see the file [CONTRIBUTING.md](CONTRIBUTING.md).

Questions about CoreNLP can either be posted on StackOverflow with the tag [stanford-nlp](http://stackoverflow.com/questions/tagged/stanford-nlp),
  or on the [mailing lists](https://nlp.stanford.edu/software/#Mail).


# Russian CoreNLP

1. General Information about the Russian Language Pipeline for Stanford CoreNLP
Russian pipeline for Stanford CoreNLP provides morphological analysis (POS-tags, morphological features, lemmatization) and dependency parsing (neural dependency parsing) models, trained with Stanford CoreNLP algorithms. Tokenizer and sentence splitter are default ones, provided by CoreNLP tool.
The pipeline also includes several special classes for integration with Stanford CoreNLP and performing lemmatization.
The Stanford CoreNLP pipeline for Russian language code is written in Java and licensed under the GNU General Public License (v3 or later). Third-party licenses are listed in [RESOUCES-LICENSE.md](https://github.com/MANASLU8/CoreNLP/blob/master/RESOURCE-LICENSES "https://github.com") file.

2. Build Instructions
Follow instructions for building Stanford CoreNLP.

3. Models and Resources

    Resources include 2 models for morphological analysis, 2 models for dependency parsing and a dictionary for lemmatization with vanilla ambiguity resolution. Models, supplied for morphological analysis, include:
* russian-ud.tagger - tagging model, which labels input text with part-of-speech-tags only according to Universal Dependencies v2 tagset, model training parameters are listed in russian-ud.tagger.props. Accuracy of POS-tagging is 97.92.
* russian-ud-mf.tagger - tagging model, which labels input text with morphological features, both according to Universal Dependencies v2 tagset, model training parameters are listed in russian-ud-mf.tagger.props. Accuracy of morphological features labeling is 86.30.
* dict.tsv - file with resources for lemmatization. 


  Models, supplied for dependency parsing, include:
* nndep.rus.model.wiki.txt.gz - dependency parsing model, which outputs a dependency tree, labeled with syntactic relations according to Universal Dependencies 2.0. The model was trained with embeddings, trained on Wikipedia corpus. UAS = 81.7314, LAS = 77.3084.  
* nndep.rus.model.ar.txt.gz - dependency parsing model, which outputs a dependency tree, labeled with syntactic relations according to Universal Dependencies 2.0. The model was trained with  embeddings, trained on Aranea corpus. UAS = 81.6326, LAS = 77.1235.
Both models show comparable quality, although nndep.rus.model.wiki.txt.gz is preferrable to start with.
 
4. Running Instructions
Annotators and default models’ and resources’ paths are listed in ‘StanfordCoreNLP-russian.properties’ file, which is given below:

```
# annotators
annotators = tokenize, ssplit, pos, custom.lemma, custom.morpho, depparse

# tokenize
# tokenize.language = en

# pos.model
pos.model = edu/stanford/nlp/models/pos-tagger/russian-ud-pos.tagger

# lemma
customAnnotatorClass.custom.lemma = edu.stanford.nlp.international.russian.process.RussianLemmatizationAnnotator
custom.lemma.dictionaryPath = edu/stanford/nlp/international/russian/process/dict.tsv

# morpho
customAnnotatorClass.custom.morpho = edu.stanford.nlp.international.russian.process.RussianMorphoAnnotator
custom.morpho.model = edu/stanford/nlp/models/pos-tagger/russian-ud-mf.tagger

# depparse
depparse.model    = edu/stanford/nlp/models/parser/nndep/nndep.rus.model.wiki.txt.gz
depparse.language = russian
```
  
The whole pipeline, including full morphological analysis (POS-tagging, morpho features, lemmatization) and parsing can be run with a command: 

* Using properties file:
```
java -Xmx5g edu.stanford.nlp.pipeline.StanfordCoreNLP -props StanfordCoreNLP-russian.properties -file ru_example.txt -outputFormat conllu
```

* Using Launcher: 
```
java -Xmx5g edu.stanford.nlp.international.russian.process.Launcher -mf
```


5. [Models](https://github.com/MANASLU8/CoreNLPRusModels "https://github.com"): 
* [Parser models](https://drive.google.com/drive/folders/0B4TmAgcGLMriS3hhTkV5VEFPVEU?usp=sharing "drive.google")
* [Tagger models and lemmatization resources](https://drive.google.com/drive/folders/0B4TmAgcGLMriMG96cFZSSWhWcEU?usp=sharing "drive.google")

If you find the pipeline useful in your research, please consider citing our paper:

```
@inproceedings{DBLP:conf/kesw/KovriguinaSSP17,
  author    = {Liubov Kovriguina and
               Ivan Shilin and
               Alexander Shipilo and
               Alina Putintseva},
  title     = {Russian Tagging and Dependency Parsing Models for Stanford CoreNLP
               Natural Language Toolkit},
  booktitle = {Knowledge Engineering and Semantic Web - 8th International Conference,
               {KESW} 2017, Szczecin, Poland, November 8-10, 2017, Proceedings},
  pages     = {101--111},
  year      = {2017},
  doi       = {10.1007/978-3-319-69548-8\_8}
}
```
