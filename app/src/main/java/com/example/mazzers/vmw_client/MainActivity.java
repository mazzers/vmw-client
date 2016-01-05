package com.example.mazzers.vmw_client;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.ion.Ion;
import com.larswerkman.lobsterpicker.LobsterPicker;
import com.larswerkman.lobsterpicker.OnColorListener;
import com.larswerkman.lobsterpicker.sliders.LobsterShadeSlider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    ListAdapter listAdapter;
    ListView listView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSearchDialogue();
            }
        });
        listView = (ListView) findViewById(R.id.listView);
    }


    private void initConnection(String tag, int r, int g, int b, int limit) throws Exception {
        RestTask task = new RestTask(this);
        String request = "?tag=" + tag + "&r=" + r + "&g=" + g + "&b=" + b + "&limit=" + limit;
        String address = "http://147.32.77.185:3456";
        task.execute(request,address);
    }

    private void showSearchDialogue() {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.searchdialogue);
        dialog.setTitle("Input tag and select color");
        final LobsterPicker lobsterPicker = (LobsterPicker) dialog.findViewById(R.id.lobsterpicker);
        LobsterShadeSlider shadeSlider = (LobsterShadeSlider) dialog.findViewById(R.id.shadeslider);

        lobsterPicker.addDecorator(shadeSlider);

        final EditText tagText = (EditText) dialog.findViewById(R.id.tagText);
        final EditText outputLimitText = (EditText) dialog.findViewById(R.id.output_limit);
        Button searchButton = (Button) dialog.findViewById(R.id.searchButton);
        // if button is clicked, close the custom dialog
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tagText.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Input tag", Toast.LENGTH_SHORT).show();
                } else {
                    int r = (lobsterPicker.getColor() >> 16) & 0xFF;
                    int g = (lobsterPicker.getColor() >> 8) & 0xFF;
                    int b = (lobsterPicker.getColor()) & 0xFF;
                    String result = "Tag is: " + tagText.getText().toString() + " | RGB:(" + r + "," + g + "," + b + ")";
                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                    int limit = 10;
                    if(!outputLimitText.getText().toString().isEmpty()) {
                        limit = Integer.parseInt(outputLimitText.getText().toString());
                    }
                    try {
                        initConnection(tagText.getText().toString(), r, g, b, limit);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }
            }
        });

        lobsterPicker.addOnColorListener(new OnColorListener() {
            @Override
            public void onColorChanged(int color) {
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = (color) & 0xFF;
                String hexColor = String.format("#%06X", (0xFFFFFF & color));

                Log.d("COLORPICKER", "Selected: RGB(" + r + "," + g + "," + b + ")");
                Log.d("COLORPICKER#INT", String.valueOf(color));
                Log.d("COLORPICKER#HEX", hexColor);
            }

            @Override
            public void onColorSelected(int color) {

            }
        });
        dialog.show();
    }


    public class RestTask extends AsyncTask<String, Void, String> {
        Context taskContext;

        public RestTask(Context context) {
            taskContext = context;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(taskContext);
            progressDialog.setMessage("Loading...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            StringBuffer response = new StringBuffer();

            // TODO: 31. 12. 2015 define server ip. This is virtualbox ip for emulator
//            String url = "http://192.168.202.1:3456/customapi/images/" + urls[0];
            String url = urls[1]+"/customapi/images/" + urls[0];
            URL obj = null;
            try {
                obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                

                int responseCode = con.getResponseCode();
//                System.out.println("\nSending 'GET' request to URL : " + url);
//                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();


                //print result
//                System.out.println(response.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String string) {
            ArrayList<String> urls = new ArrayList<>();
            ArrayList<String> values = new ArrayList<>();
            ArrayList<String> titles = new ArrayList<>();
            ArrayList<String> owners = new ArrayList<>();
            ArrayList<String> postedDates = new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONArray(string);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String addr = object.getString("photoUrl");
                    String value = object.getString("colorShare");
                    String title = object.getString("title");
                    String owner = object.getString("owner");
                    if(owner.isEmpty()) owner = "FlickrUser";
                    //String posted = object.getString("posted");
                    urls.add(addr);
                    values.add(value);
                    titles.add(title);
                    owners.add(owner);
                    //postedDates.add(posted);
//                    System.out.println("Parsed: addr - " + addr + " value: " + value);
                }
                listAdapter = new ListAdapter(getApplicationContext(), urls, values, titles,
                        owners);
                listView.setAdapter(listAdapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            progressDialog.dismiss();
        }
    }

    public class ListAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final ArrayList<String> imgUrls;
        private final ArrayList<String> imgValues;
        private final ArrayList<String> imgTitles;
        private final ArrayList<String> imgOwners;
        //private final ArrayList<String> imgPostedDates;

        public ListAdapter(Context context, ArrayList<String> imgUrls, ArrayList<String> imgValues,
                           ArrayList<String> imgTitles, ArrayList<String> imgOwners) {
            super(context, R.layout.activity_main, imgValues);
            this.imgValues = imgValues;
            this.imgUrls = imgUrls;
            this.imgTitles = imgTitles;
            this.imgOwners = imgOwners;
            //this.imgPostedDates = imgPostedDates;
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            View row = (View) inflater.inflate(R.layout.listitem, parent, false);

            TextView title = (TextView) row.findViewById(R.id.tv_title);
            TextView color_share = (TextView) row.findViewById(R.id.tv_color_share);
            TextView owner = (TextView) row.findViewById(R.id.tv_owner);
            //TextView posted = (TextView) row.findViewById(R.id.tv_posted_date);
            ImageView imageView = (ImageView) row.findViewById(R.id.imageView);
            Ion.with(context)
                    .load(imgUrls.get(position))
                    .withBitmap()
                    .smartSize(true)
                    .intoImageView(imageView);
            title.setText("Title: " + imgTitles.get(position));
            color_share.setText("Color share: " + imgValues.get(position));
            owner.setText("Owner: " + imgOwners.get(position));
            //posted.setText("Posted on: " + imgPostedDates.get(position));

            //if(position % 2 == 0) row.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_bright));

            return row;
        }

    }
}
