/*
 * Copyright 2016 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.ene.lab.chao.data.model;

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
