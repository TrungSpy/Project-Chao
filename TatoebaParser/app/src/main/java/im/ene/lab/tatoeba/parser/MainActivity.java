package im.ene.lab.tatoeba.parser;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    findViewById(R.id.button_Start).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        //IOUtil.readSentenceDetail(new IOUtil.IOUtilCallback() {
        //  @Override public void onString(String s) {
        //    Log.i(TAG, "Sentence: " + s);
        //  }
        //
        //  @Override public void onDone(Boolean success) {
        //    Log.d(TAG, "onDone() called with: " + "success = [" + success + "]");
        //  }
        //});

        IOUtil.readLinks(new IOUtil.IOUtilCallback() {
          @Override public void onString(String s) {
            Log.d(TAG, "Link: " + s);
          }

          @Override public void onDone(Boolean success) {
            Log.d(TAG, "onDone() called with: " + "success = [" + success + "]");
          }
        });
      }
    });
  }

  @Override protected void onResume() {
    super.onResume();
  }
}
