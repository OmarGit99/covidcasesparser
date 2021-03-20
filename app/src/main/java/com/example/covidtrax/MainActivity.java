package com.example.covidtrax;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    public class Downloader extends AsyncTask<String, Void, Map<String , String[]>>{

        @Override
        protected Map<String, String[]> doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                String result = "";
                int data;

                data = inputStreamReader.read();
                while(data != -1){
                    char dat = (char) data;
                    result += dat;
                    data = inputStreamReader.read();
                }

                Document html = Jsoup.parse(result);
                Elements table = html.getElementsByClass("t_view");
                Elements td = html.getElementsByTag("td");
                List<String> strings1 = td.eachText();

                ArrayList<String> cropped_tdata = new ArrayList<>();

                for(int i = 0;i<strings1.size(); i++){
                    if(strings1.get(i).matches("TOTAL")){
                        cropped_tdata.add(strings1.get(i));
                        cropped_tdata.add(strings1.get(i+1));
                        cropped_tdata.add(strings1.get(i+2));
                        break;
                    }
                    else{
                        if(strings1.get(i).matches("\\s*")){

                        }
                        else{
                            cropped_tdata.add(strings1.get(i));
                        }
                    }

                }

                Map<String, String[]> casesanddistricts = new HashMap<>();
                int next = 0;

                for(int i = 0; i<cropped_tdata.size(); i++){
                    if(cropped_tdata.get(i).length() == 1){
                        next = 1;
                    }
                    else if(cropped_tdata.get(i).matches("Other States") && next == 1){
                        String[] li = new String[10];
                        li[0] = cropped_tdata.get(i+1);
                        li[1] = cropped_tdata.get(i+2);
                        li[2] = cropped_tdata.get(i+3);

                        casesanddistricts.put(cropped_tdata.get(i), li);
                        next =0;
                    }
                    else if(next == 1){
                        String[] li = new String[10];
                        li[0] = cropped_tdata.get(i+1);
                        li[1] = cropped_tdata.get(i+2);

                        casesanddistricts.put(cropped_tdata.get(i), li);
                        next = 0;
                    }
                }


                return casesanddistricts;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Downloader downloader = new Downloader();
        try {
            Map<String, String[]> texts = downloader.execute("https://arogya.maharashtra.gov.in/1175/Novel--Corona-Virus").get();

            for(String k : texts.get("Kolapur")){
                Log.i("codeya", k);
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}