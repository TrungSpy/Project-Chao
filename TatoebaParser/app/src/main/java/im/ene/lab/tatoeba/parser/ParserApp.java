package im.ene.lab.tatoeba.parser;

import android.app.Application;
import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;
import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;

/**
 * Created by eneim on 7/2/16.
 */
public class ParserApp extends Application {

  @Override public void onCreate() {
    super.onCreate();
    RealmConfiguration configuration =
        new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded()
            .migration(new RealmMigration() {
              @Override public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

              }
            })
            .name("tatoeba.realm")
            .build();

    Realm.setDefaultConfiguration(configuration);

    Stetho.initialize(Stetho.newInitializerBuilder(this)
        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
        .build());
  }
}
