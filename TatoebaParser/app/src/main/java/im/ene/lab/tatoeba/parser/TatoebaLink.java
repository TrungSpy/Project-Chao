package im.ene.lab.tatoeba.parser;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by eneim on 7/2/16.
 */
public class TatoebaLink extends RealmObject {

  @PrimaryKey public String sentenceId;

  public RealmList<TatoebaSentence> translations;

  public TatoebaLink() {
  }
}
