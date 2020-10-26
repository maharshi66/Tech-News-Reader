package maharshi.myfirstapp.technewsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> articleTitles = new ArrayList<>();
    ArrayList<String> articleContents = new ArrayList<>();

    ArrayAdapter arrayAdapter;
    SQLiteDatabase articleDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize the DB
        articleDB = this.openOrCreateDatabase("Articles", MODE_PRIVATE, null);
        articleDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleId INTEGER, title VARCHAR, content VARCHAR)");

        //Use the DownloadTask created
        DownloadTask task = new DownloadTask();
        try {
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Initialized ListView along with arrayAdapter
        ListView articlesListView = (ListView) findViewById(R.id.articlesListView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, articleTitles);
        articlesListView.setAdapter(arrayAdapter);

        articlesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), webViewActivity.class);
                intent.putExtra("content", articleContents.get(position));
                startActivity(intent);
            }
        });

        updateListView();
    }

    public void updateListView() {
        Cursor c = articleDB.rawQuery("SELECT * FROM articles", null);
        int titleIndex = c.getColumnIndex("title");
        int contentIndex = c.getColumnIndex("content");

        if (c.moveToFirst()) {
            articleTitles.clear();
            articleContents.clear();
            do {
                articleTitles.add(c.getString(titleIndex));
                articleContents.add(c.getString(contentIndex));
            } while (c.moveToNext());
            arrayAdapter.notifyDataSetChanged();
        }
    }
    public class DownloadTask extends AsyncTask<String, Void, String> {
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

                while (data != -1) {
                    char currData = (char) data;
                    result += currData;
                    data = reader.read();
                }
                //result now contains all the ids for top stories. Create JSON array to identify each id

                JSONArray jsonArray = new JSONArray(result);
                int numberOfItems = 1;
                if (jsonArray.length() < 1) {
                    numberOfItems = jsonArray.length();
                }
                articleDB.execSQL("DELETE FROM articles");

                for (int i = 0; i < numberOfItems; i++) {
                    String articleId = jsonArray.getString(i);
                    String articleInfo = "";
                    url = new URL("https://hacker-news.firebaseio.com/v0/item/" + articleId + ".json?print=pretty");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    inputStream = urlConnection.getInputStream();
                    reader = new InputStreamReader(inputStream);

                    data = reader.read();
                    while (data != -1) {
                        char currData = (char) data;
                        articleInfo += currData;
                        data = reader.read();
                    }
//                    Log.i("Article Info:",articleInfo);

                    JSONObject jsonObject = new JSONObject(articleInfo);
                    if (!jsonObject.isNull("title") && !jsonObject.isNull("url")) {
                        String articleTitle = jsonObject.getString("title");
                        String articleUrl = jsonObject.getString("url");
                        url = new URL(articleUrl);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        inputStream = urlConnection.getInputStream();
                        reader = new InputStreamReader(inputStream);
                        data = reader.read();

                        String articleContent = "";

                        while (data != -1) {
                            char currData = (char) data;
                            articleContent += data;
                            data = reader.read();
                        }

//                        Log.i("HTML", articleContent);
                        String sql = "INSERT INTO articles (articleId, title, content) VALUES (?, ?, ?)";
                        SQLiteStatement statement = articleDB.compileStatement(sql);
                        statement.bindString(1, articleId);
                        statement.bindString(2, articleTitle);
                        statement.bindString(3, articleContent);

                        statement.execute();
                    }
                }
//                Log.i("URL result", result);
                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            updateListView();
        }
    }
}