package maharshi.myfirstapp.technewsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> articleTitles = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    public class DownloadTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);

                int data = reader.read();

                while(data != -1)
                {
                    char currData = (char) data;
                    result += currData;
                    data = reader.read();
                }
                Log.i("URL result:", result);
                return result;

            }catch (Exception e)
            {
                e.printStackTrace();
            }
            return result;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Use the DownloadTask created
        DownloadTask task = new DownloadTask();
        try {
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //Initialized ListView along with arrayAdapter
        ListView articlesListView = (ListView) findViewById(R.id.articlesListView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, articleTitles);
        articlesListView.setAdapter(arrayAdapter);
    }
}