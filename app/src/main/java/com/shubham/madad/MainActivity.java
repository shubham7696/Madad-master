package com.shubham.madad;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    DatabaseReference database;
    DatabaseReference childReference;
    FirebaseAuth mAuth;
    EditText user;
    EditText pass;
    Button sub;
    String userid = "";


    String addressholder = "";
    Double Lat;
    Double Lng;
    GsmCellLocation loc;
    TelephonyManager tm;
    String count = "";

    private OpenCellID openCellID;

    TextView signup;

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = FirebaseDatabase.getInstance().getReference();

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.WHITE);
        gd.setCornerRadius(10);
        gd.setStroke(2, Color.GRAY);


        GradientDrawable gd1 = new GradientDrawable();
        gd1.setColor(Color.WHITE);
        gd1.setCornerRadius(10);
        gd1.setStroke(2, Color.GRAY);


        GradientDrawable gd3 = new GradientDrawable();
        gd3.setColor(Color.WHITE);
        gd3.setCornerRadius(10);
        gd3.setStroke(2, Color.GRAY);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 124);

        }


        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        loc = (GsmCellLocation) tm.getCellLocation();
        String networkOperator = tm.getNetworkOperator();
        String mcc = networkOperator.substring(0, 3);
        String mnc = networkOperator.substring(3);


        int cellid = loc.getCid();
        int lac = loc.getLac();


        openCellID = new OpenCellID();
        openCellID.setMcc(mcc);
        openCellID.setMnc(mnc);
        openCellID.setCallID(cellid);
        openCellID.setCallLac(lac);
        new GetLocation().execute();


        Toolbar tb = (Toolbar) findViewById(R.id.tb);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        user = (EditText) findViewById(R.id.user);
        user.setBackground(gd);

        pass = (EditText) findViewById(R.id.pass);
        pass.setBackground(gd1);

        sub = (Button) findViewById(R.id.sub);

        signup = (TextView) findViewById(R.id.signup);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getApplicationContext(), Signup.class);
                startActivity(i);

            }
        });

        sub.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View view) {
                                       mAuth = FirebaseAuth.getInstance();
                                       childReference = database.child("admin");

                                       mAuth.signInWithEmailAndPassword(user.getText().toString(), pass.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                                                                                                                        @Override
                                                                                                                                                        public void onComplete(@NonNull Task<AuthResult> task)
                                                                                                                                                        {
                                                                                                                                                            if (task.isSuccessful())
                                                                                                                                                            {
                                                                                                                                                                final String userId = mAuth.getCurrentUser().getEmail();
                                                                                                                                                                childReference.addValueEventListener(new ValueEventListener()
                                                                                                                                                                {
                                                                                                                                                                    @Override
                                                                                                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                                                                                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                                                                                                                                            if (ds.getValue(String.class).equals(userId)) {

                                                                                                                                                                                setFlag(true);
                                                                                                                                                                                Toast.makeText(MainActivity.this, "in if condition", Toast.LENGTH_SHORT).show();
                                                                                                                                                                                Intent intent = new Intent(MainActivity.this, Admin_Login.class);
                                                                                                                                                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                                                                                                                startActivity(intent);
                                                                                                                                                                            }
                                                                                                                                                                        }
                                                                                                                                                                    }

                                                                                                                                                                    @Override
                                                                                                                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                                                                                                                    }
                                                                                                                                                                });

                                                                                                                                                                if (!isFlag()) {
                                                                                                                                                                    childReference = database.child("user");
                                                                                                                                                                    childReference.addValueEventListener(new ValueEventListener() {
                                                                                                                                                                        @Override
                                                                                                                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                                                                                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                                                                                                                                                if (ds.getValue(String.class).equals(userId)) {
                                                                                                                                                                                    Toast.makeText(MainActivity.this, "in if condition", Toast.LENGTH_SHORT).show();
                                                                                                                                                                                    Intent intent = new Intent(MainActivity.this, Student_Login.class);


                                                                                                                                                                                    Bundle extras = new Bundle();
                                                                                                                                                                                    extras.putString("Latitude",""+Lat);
                                                                                                                                                                                    extras.putString("Longtitude",""+Lng);

                                                                                                                                                                                    extras.putString("Address",addressholder );
                                                                                                                                                                                    extras.putString("Country",count);
                                                                                                                                                                                    intent.putExtras(extras);



                                                                                                                                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                                                                                                                    startActivity(intent);
                                                                                                                                                                                }
                                                                                                                                                                            }
                                                                                                                                                                        }

                                                                                                                                                                        @Override
                                                                                                                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                                                                                                                        }
                                                                                                                                                                    });
                                                                                                                                                                }
                                                                                                                                                            }
                                                                                                                                                        }
                                                                                                                                                    }

                                       );
                                   }
                               }

        );


    }

    class GetLocation extends AsyncTask<String, Void, Void> {
        private Exception exception;

        protected Void doInBackground(String... urls) {
            try {
                try {
                    openCellID.GetOpenCellID();
                    if (!openCellID.isError()) {

                        openCellID.getLocation();

                        //         textGeo.setText(openCellID.getLocation());
               /*       *//**//*  textRemark.setText( "\n\n"
                                + "URL sent: \n" + openCellID.getstrURLSent() + "\n\n"
                                + "response: \n" + openCellID.GetOpenCellID_fullresult);*//**//**/

                        Toast.makeText(getApplicationContext(), openCellID.getLocation(), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
//                textGeo.setText("Error");

                }
                return null;
            } catch (Exception e) {
                this.exception = e;
                //          textGeo.setText("Exception: " + e.toString());

                return null;
            }
        }

        protected void onPostExecute(Void feed) {
            // TODO: check this.exception
            // TODO: do something with the feed
        }
    }

    public class OpenCellID {
        String mcc; //Mobile Country Code
        String mnc; //mobile network code
        String cellid; //Cell ID
        String lac; //Location Area Code
        Boolean error;
        String strURLSent;
        String GetOpenCellID_fullresult;
        String latitude;
        String longitude;


        public Boolean isError() {
            return error;
        }

        public void setMcc(String value) {
            mcc = value;
        }

        public void setMnc(String value) {
            mnc = value;
        }

        public void setCallID(int value) {
            cellid = String.valueOf(value);
        }

        public void setCallLac(int value) {
            lac = String.valueOf(value);
        }

        public String getLocation() {
            return (latitude + " : " + longitude);
        }

        public void groupURLSent() {
            strURLSent =
                    "http://www.opencellid.org/cell/get?key=2170764e-427b-4513-98ca-b57889b6cf37&mcc=" + mcc
                            + "&mnc=" + mnc
                            + "&cellid=" + cellid
                            + "&lac=" + lac
                            + "&fmt=txt";
        }

        public String getstrURLSent() {
            return strURLSent;
        }

        public String getGetOpenCellID_fullresult() {
            return GetOpenCellID_fullresult;
        }

        public void GetOpenCellID() throws Exception {
            groupURLSent();
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(strURLSent);
            HttpResponse response = client.execute(request);
            GetOpenCellID_fullresult = EntityUtils.toString(response.getEntity());


            StringBuilder str = new StringBuilder(GetOpenCellID_fullresult);
            //  System.out.println("string = " + str);

            // prints substring from index 3
            //     System.out.println("substring is = " + str.substring(3));

            System.out.println("substring is = " + str.substring(66, 76));

            System.out.println("substring is = " + str.substring(83, 93));


            Lat = Double.parseDouble(str.substring(66, 76));

            Lng = Double.parseDouble(str.substring(83, 93));

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

            try {
                List<Address> listAddresses = geocoder.getFromLocation(Lat, Lng, 1);
                if (listAddresses != null && listAddresses.size() > 0) {
                    Log.i("PlaceInfo", listAddresses.get(0).toString());


                    for (int i = 0; i < listAddresses.get(0).getMaxAddressLineIndex(); i++) {
                        addressholder += listAddresses.get(0).getAddressLine(i) + "\n";
                    }

                    if (count != null) {
                        count += listAddresses.get(0).getCountryName();
                    }

                    //         textRemark.setText("Address :\n"+addressholder);

                    Log.d("Address", addressholder);


                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            spliteResult();
        }

        private void spliteResult() {
            if (GetOpenCellID_fullresult.equalsIgnoreCase("err")) {
                error = true;
            } else {
                error = false;
                String[] tResult = GetOpenCellID_fullresult.split(",");
                latitude = "";
                longitude = "";
            }
        }


    }
}
