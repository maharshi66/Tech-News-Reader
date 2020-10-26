package maharshi.myfirstapp.technewsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> articleTitles = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialized ListView along with arrayAdapter
        ListView articlesListView = (ListView) findViewById(R.id.articlesListView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, articleTitles);
        articlesListView.setAdapter(arrayAdapter);
    }
}