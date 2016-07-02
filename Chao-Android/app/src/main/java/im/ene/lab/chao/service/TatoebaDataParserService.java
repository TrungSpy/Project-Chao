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

package im.ene.lab.chao.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import im.ene.lab.chao.Chao;
import im.ene.lab.chao.data.model.TatoebaLink;
import im.ene.lab.chao.data.model.TatoebaSentence;
import im.ene.lab.chao.util.IOUtil;
import io.realm.Realm;
import io.realm.RealmList;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by eneim on 7/2/16.
 */
public class TatoebaDataParserService extends IntentService {

  private static final String TAG = "TatoebaDataParser";

  public static final String PREF_LOAD_SENTENCE = "pref_chao_sentences_loaded";
  public static final String PREF_LOAD_LINKS = "pref_chao_links_loaded";

  public TatoebaDataParserService() {
    super(TAG);
  }

  @Override protected void onHandleIntent(Intent intent) {
    if (Chao.getPref().getBoolean(PREF_LOAD_SENTENCE, false)) {
      return;
    }

    try {
      readSentenceDetailToRealm();
      Chao.getPref().edit().putBoolean(PREF_LOAD_SENTENCE, true).apply();
      Chao.getPref().edit().putBoolean(PREF_LOAD_LINKS, false).apply();
    } catch (IOException e) {
      e.printStackTrace();
      Chao.getPref().edit().putBoolean(PREF_LOAD_SENTENCE, false).apply();
    }

    if (Chao.getPref().getBoolean(PREF_LOAD_LINKS, false)) {
      return;
    }

    try {
      readLinksToRealm();
      Chao.getPref().edit().putBoolean(PREF_LOAD_LINKS, true).apply();
    } catch (IOException e) {
      e.printStackTrace();
      Chao.getPref().edit().putBoolean(PREF_LOAD_LINKS, false).apply();
    }
  }

  private void readSentenceDetailToRealm() throws IOException {
    BufferedReader reader = null;
    Realm realm = Realm.getDefaultInstance();
    realm.beginTransaction();
    int engCount = 0;
    int jpnCount = 0;
    int vieCount = 0;
    int cmnCount = 0;
    boolean willSave = false;
    reader = new BufferedReader(
        new InputStreamReader(new FileInputStream(IOUtil.openSentenceDetailFile())));
    String line;
    while ((line = reader.readLine()) != null) {
      TatoebaSentence item = new TatoebaSentence(line);
      if (willSaveItem(item)) {
        if ("eng".equals(item.lang)) {
          engCount++;
          willSave = engCount <= 25;
        } else if ("cmn".equals(item.lang)) {
          cmnCount++;
          willSave = cmnCount <= 25;
        } else if ("jpn".equals(item.lang)) {
          jpnCount++;
          willSave = jpnCount <= 25;
        } else if ("vie".equals(item.lang)) {
          vieCount++;
          willSave = vieCount <= 25;
        }
      }

      if (willSave) {
        realm.copyToRealmOrUpdate(item);
        willSave = false;
        Log.i(TAG,
            "Sentence: " + vieCount + " - " + jpnCount + " - " + engCount + " - " + cmnCount);
      }
    }
    realm.commitTransaction();
    reader.close();
  }

  private void readLinksToRealm() throws IOException {
    int counter = 0;
    BufferedReader reader = null;
    Realm realm = Realm.getDefaultInstance();
    realm.beginTransaction();
    reader = new BufferedReader(new InputStreamReader(new FileInputStream(IOUtil.openLinksFile())));
    String line;
    String latestOrigin = null;
    String[] parts;
    TatoebaLink linkItem = null;
    while ((line = reader.readLine()) != null) {
      parts = line.split("\t");
      if (parts.length == 2) {
        if (!parts[0].equals(latestOrigin)) {
          // Reach new one, then update current one if there is any.
          if (linkItem != null && linkItem.translations.size() > 0) {
            realm.copyToRealmOrUpdate(linkItem);
            Log.i(TAG, "Links: " + ++counter + " - " + latestOrigin);
          }

          // 2. Renew item;
          latestOrigin = parts[0];
          linkItem = new TatoebaLink();
          linkItem.sentenceId = latestOrigin;
          if (linkItem.translations == null) {
            linkItem.translations = new RealmList<>();
          }
          TatoebaSentence trans =
              realm.where(TatoebaSentence.class).equalTo("id", parts[1]).findFirst();
          if (trans != null) {
            linkItem.translations.add(trans);
          }
        }
      }
    }
    realm.commitTransaction();
    reader.close();
  }

  private boolean willSaveItem(TatoebaSentence item) {
    return item.createdAt.startsWith("2") && (item.updatedAt.length() >= 4
        && Integer.valueOf(item.updatedAt.substring(0, 4)) >= 2013);
  }
}
