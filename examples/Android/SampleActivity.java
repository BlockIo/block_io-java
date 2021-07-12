package com.block.io;

import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import lib.blockIo.BlockIo;

public class MainActivity extends AppCompatActivity {
    private String TAG = "BlockIO";
    private String API_KEY = "<YOUR_API_KEY>";
    private String PIN = "<YOUR_PIN>";
    BlockIo blockLib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try
        {
            blockLib = new BlockIo(API_KEY, PIN);

            new NetworkRequestAsync().execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class NetworkRequestAsync extends AsyncTask<String, Void, Void> {

        private Exception exception;

        protected Void doInBackground(String... urls) {
            try {

                Log.d(TAG , blockLib.GetBalance(null).toJSONString());
                Log.d(TAG , blockLib.GetMyAddresses(null).toJSONString());

            } catch (Exception e) {
                Log.d(TAG, "Error: " + e.getLocalizedMessage());
            }

            return null;
        }

        protected void onPostExecute(Void feed) {
            Log.d(TAG , "Completed execution");
        }
    }
}