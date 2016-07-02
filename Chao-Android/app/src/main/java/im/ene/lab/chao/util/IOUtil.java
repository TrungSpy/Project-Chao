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

package im.ene.lab.chao.util;

import android.os.AsyncTask;
import android.os.Environment;
import im.ene.lab.chao.data.model.TatoebaLink;
import im.ene.lab.chao.data.model.TatoebaSentence;
import io.realm.Realm;
import io.realm.RealmList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by eneim on 7/2/16.
 */
public class IOUtil {

  public static final String SENTENCES_DETAIL = "sentences_detailed.csv";
  public static final String LINKS = "links.csv";

  private static File fetchFile(final String name) {
    File[] fetch = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        .listFiles(new FilenameFilter() {
          @Override public boolean accept(File file, String s) {
            return name.equals(s);
          }
        });

    if (fetch == null || fetch.length == 0) {
      return null;
    }

    return fetch[0];
  }

  public static File openSentenceDetailFile() {
    return fetchFile(SENTENCES_DETAIL);
  }

  public static File openLinksFile() {
    return fetchFile(LINKS);
  }

  public static Observable<String> readFile(final File file) {
    return Observable.create(new Observable.OnSubscribe<String>() {
      @Override public void call(Subscriber<? super String> subscriber) {
        BufferedReader reader = null;
        try {
          reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
          String line;
          while ((line = reader.readLine()) != null) {
            subscriber.onNext(line);
          }
          subscriber.onCompleted();
        } catch (IOException e) {
          e.printStackTrace();
          subscriber.onError(e);
        } finally {
          if (reader != null) {
            try {
              reader.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      }
    });
  }

  public static void readSentenceDetail(final IOUtilCallback callback) {
    new AsyncTask<File, String, Boolean>() {

      @Override protected Boolean doInBackground(File... files) {
        boolean success = false;
        BufferedReader reader = null;
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        int engCount = 0;
        int jpnCount = 0;
        int vieCount = 0;
        int cmnCount = 0;
        boolean willSave = false;
        try {
          reader = new BufferedReader(
              new InputStreamReader(new FileInputStream(openSentenceDetailFile())));
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
              publishProgress(vieCount + " - " + jpnCount + " - " + engCount + " - " + cmnCount);
            }
          }
          success = true;
        } catch (IOException e) {
          e.printStackTrace();
          success = false;
        } finally {
          if (reader != null) {
            try {
              reader.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
        realm.commitTransaction();
        return success;
      }

      @Override protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        callback.onString(values[0]);
      }

      @Override protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        callback.onDone(aBoolean);
      }
    }.execute();
  }

  public static void readLinks(final IOUtilCallback callback) {
    new AsyncTask<File, String, Boolean>() {

      @Override protected Boolean doInBackground(File... files) {
        int counter = 0;
        boolean success = false;
        BufferedReader reader = null;
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        try {
          reader = new BufferedReader(new InputStreamReader(new FileInputStream(openLinksFile())));
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
                  publishProgress(++counter + " - " + latestOrigin);
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
          success = true;
        } catch (IOException e) {
          e.printStackTrace();
          success = false;
        } finally {
          if (reader != null) {
            try {
              reader.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
        realm.commitTransaction();
        return success;
      }

      @Override protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        callback.onString(values[0]);
      }

      @Override protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        callback.onDone(aBoolean);
      }
    }.execute();
  }

  static boolean willSaveItem(TatoebaSentence item) {
    return item.createdAt.startsWith("2") && (item.updatedAt.length() >= 4
        && Integer.valueOf(item.updatedAt.substring(0, 4)) >= 2013);
  }

  public interface IOUtilCallback {

    void onString(String s);

    void onDone(Boolean success);
  }
}
