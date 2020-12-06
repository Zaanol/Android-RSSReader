package com.zanol.rssreader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static ListView rssListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rssListView = findViewById(R.id.rssListView);

        Log.d(TAG, "onCreate: iniciando a AsyncTask");
        DownloadData downloadData = new DownloadData();
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml");
        Log.d(TAG, "onCreate: terminou.");
    }

    //Parametros: envio, progresso, resultado
    private class DownloadData extends AsyncTask<String, Void, String> {
        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            Log.d(TAG, "onPostExecute: o parâmetro recebido é " + s);
            ParseApplications parser = new ParseApplications();
            parser.parse(s);

/*            Forma default de listView
            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<>(
                    MainActivity.this, R.layout.list_item, parser.getApplications()
            );

            rssListView.setAdapter(arrayAdapter);*/

            FeedAdapter feedAdapter = new FeedAdapter(
                    MainActivity.this, R.layout.list_records, parser.getApplications()
            );

            rssListView.setAdapter(feedAdapter);
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: recebeu " + strings[0]);
            String contents = downloadContents(strings[0]);

            if(contents == null){
                Log.e(TAG, "doInBackground: Erro baixando dados.");
            }

            return contents;
        }

        private String downloadContents(String urlPath){
            StringBuilder result = new StringBuilder();

            try{
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "downloadContents: O cóidigo de resposta foi: " + responseCode);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                int charsRead;
                char[] inputBuffer = new char[500];

                while(true){
                    charsRead = reader.read(inputBuffer);
                    if(charsRead < 0){
                        break;
                    }

                    if(charsRead > 0){
                        result.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    }
                }

                reader.close();

                return result.toString();
            }catch (MalformedURLException e){
                Log.e(TAG, "downloadContents: URL inválida: " + e.getMessage());
            }catch (IOException e) {
                Log.e(TAG, "downloadContents: IOException ao ler os dados: " + e.getMessage());
            }catch (SecurityException e){
                Log.e(TAG, "downloadContents: Exceção de segurança. Falta permissão? " + e.getMessage());
            }

            return null;
        }
    }
}