package com.zanol.rssreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

        downloadUrl("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        String feedUrl;

        switch (id) {
            case R.id.mnuFree:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml";
                break;
            case R.id.mnuPaid:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=10/xml";
                break;
            case R.id.mnuSongs:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=10/xml";
                break;
            default:
                //Caso haja submenu
                return super.onOptionsItemSelected(item);
        }

        downloadUrl(feedUrl);
        return true;
    }

    private void downloadUrl(String feedUrl) {
        Log.d(TAG, "downloadUrl: iniciando a AsyncTask");
        DownloadData downloadData = new DownloadData();
        downloadData.execute(feedUrl);
        Log.d(TAG, "downloadUrl: terminou.");
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

//            Forma default de listView
/*            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<>(
                    MainActivity.this, R.layout.list_item, parser.getApplications()
            );

            rssListView.setAdapter(arrayAdapter);*/

//            Forma modificada de Layout com textos
            FeedAdapter feedAdapter = new FeedAdapter(
                    MainActivity.this, R.layout.list_records, parser.getApplications()
            );

//            Forma modificada de layout com imagem
            /*FeedImageAdapter feedAdapter = new FeedImageAdapter(
                    MainActivity.this, R.layout.list_records_with_imagem, parser.getApplications()
            );*/

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