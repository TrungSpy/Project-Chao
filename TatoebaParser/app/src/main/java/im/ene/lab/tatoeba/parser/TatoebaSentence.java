package im.ene.lab.tatoeba.parser;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by eneim on 7/2/16.
 */
public class TatoebaSentence extends RealmObject {

  @PrimaryKey public String id;

  public String lang; // eg: eng

  public String text;

  public String ownerId;  // tatoeba user name

  public String createdAt;  // String

  public String updatedAt; // eg: 2013-08-05 22:16:51

  public TatoebaSentence() {
  }

  public TatoebaSentence(String source) {
    String[] parts = source.split("\t");
    if (parts.length == 6) {
      this.id = parts[0];
      this.lang = parts[1];
      this.text = parts[2];
      this.ownerId = parts[3];
      this.createdAt = parts[4];
      this.updatedAt = parts[5];
    }
  }
}
