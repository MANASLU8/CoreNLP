package edu.stanford.nlp.parser.lexparser;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.trees.DiskTreebank;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.MemoryTreebank;
import edu.stanford.nlp.trees.ModCollinsHeadFinder;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeReaderFactory;
import edu.stanford.nlp.trees.TreeTransformer;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.international.russian.RussianHeadFinder;
import edu.stanford.nlp.trees.international.russian.RussianTreebankLanguagePack;

import java.util.List;

public class RussianTreebankParserParams extends AbstractTreebankParserParams {

  private static final long serialVersionUID = 3638801831677887266L;

  private HeadFinder headFinder;

  protected RussianTreebankParserParams(TreebankLanguagePack tlp) {
    super(tlp);
  }

  public RussianTreebankParserParams() {
    super(new RussianTreebankLanguagePack());

    setInputEncoding("UTF-8");

    headFinder = new RussianHeadFinder(tlp);
  }

  @Override
  public HeadFinder headFinder() {
    return headFinder;
  }

  @Override
  public HeadFinder typedDependencyHeadFinder() {
    return headFinder();
  }

  @Override
  public TreeReaderFactory treeReaderFactory() {
    return in -> new PennTreeReader(in);
  }

  @Override
  public List<? extends HasWord> defaultTestSentence() {
    String[] sent = {"Я", "иду", "искать", "."};
    return SentenceUtils.toWordList(sent);
  }

  @Override
  public MemoryTreebank memoryTreebank() {
    return new MemoryTreebank(treeReaderFactory(), inputEncoding);
  }

  @Override
  public DiskTreebank diskTreebank() {
    return new DiskTreebank(treeReaderFactory(), inputEncoding);
  }

  @Override
  public TreeTransformer collinizer() {
    return new TreeCollinizer(treebankLanguagePack());
  }

  @Override
  public TreeTransformer collinizerEvalb() {
    return new TreeCollinizer(treebankLanguagePack(), false, false);
  }

  @Override
  public String[] sisterSplitters() {
    return new String[0];
  }

  @Override
  public Tree transformTree(Tree t, Tree root) {
    return null;
  }

  @Override
  public void display() {
    System.out.println("CUSTOM display RussianTreeParserParams");
  }

}
