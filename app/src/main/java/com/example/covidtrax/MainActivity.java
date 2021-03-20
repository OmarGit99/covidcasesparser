package com.example.covidtrax;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    ArrayList<String> districts;
    private FirebaseAuth mAuth;

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
                        String[] li = new String[5];
                        li[0] = cropped_tdata.get(i+1);
                        li[1] = cropped_tdata.get(i+2);
                        li[2] = cropped_tdata.get(i+3);

                        casesanddistricts.put(cropped_tdata.get(i), li);
                        districts.add(cropped_tdata.get(i));

                        next =0;
                    }
                    else if(next == 1){
                        String[] li = new String[10];
                        li[0] = cropped_tdata.get(i+1);
                        li[1] = cropped_tdata.get(i+2);

                        casesanddistricts.put(cropped_tdata.get(i), li);
                        districts.add(cropped_tdata.get(i));

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
        districts = new ArrayList<>();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword("owner@project.com", "owneraccess")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("codeya", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("codeya", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });

        Downloader downloader = new Downloader();
        try {
            Map<String, String[]> texts = downloader.execute("https://arogya.maharashtra.gov.in/1175/Novel--Corona-Virus").get();

            for(String district: districts){
                String final_str = district +": \n";
                for (int i =0; i< texts.get(district).length;i++){
                    if(i == 0){
                        final_str = final_str+"\t Name: "+ district+ "\n";
                        final_str = final_str+"\t AC: "+ texts.get(district)[i]+"\n";
                    }
                    else if(i ==1){
                        final_str = final_str+"\t Death cases: "+ texts.get(district)[i]+"\n";
                    }
                    else{
                        final_str = final_str+"\t Other: "+ texts.get(district)[i]+"\n";
                    }

                }
                Log.i("codeya", final_str);

            }



        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}