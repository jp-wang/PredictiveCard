package com.jp.predictivecard;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.telenav.api.common.v1.LatLon;
import com.telenav.predictivecards.PredictiveCard;
import com.telenav.predictivecards.PredictiveCardConfig;
import com.telenav.predictivecards.PredictiveCardListener;
import com.telenav.predictivecards.PredictiveCardManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements PredictiveCardListener {

    private final static String WIKI_NAME = "predcard.pdf";

    private final static int REQUEST_WRITE_EXTERNAL_STORAGE = 100;

    @BindView(R.id.predictiveCardTv)
    TextView predictiveCardTv;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PredictiveCardManager.getInstance().start();
                Snackbar.make(view, "Predictive card was started successfully!", Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PredictiveCardManager.getInstance().stop();
                            }
                        }).show();
            }
        });

        initPredCard();
    }

    private void initPredCard() {
        PredictiveCardConfig config = new PredictiveCardConfig();
        config.setProperty(PredictiveCardConfig.KEY_PREDICTIVE_CARD_USER_ID, "6YH4J2VDKC0ZHI1O9Q4IO28Q2");
        PredictiveCardManager.getInstance().init(config, new DataServiceProxy("http://arpstage.telenav.com/data/"));
        PredictiveCardManager.getInstance().addListener(this);
        LatLon latLon = new LatLon();
        latLon.setLat(37.44172);
        latLon.setLon(-122.154985);
        PredictiveCardManager.getInstance().updateCondition(-1, latLon);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        PredictiveCardManager.getInstance().stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openPredCardPdf();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openPredCardPdf();
                }
                break;
        }
    }

    private void openPredCardPdf() {
        try {
            File externalPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + WIKI_NAME);
            if (!externalPath.exists()) {
                copyAssetIntoExternal(externalPath);
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(externalPath), "application/pdf");
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(fab, "Cannot find any application that reads predcard.pdf", Snackbar.LENGTH_LONG).setAction("Redo", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPredCardPdf();
                }
            }).show();
        }
    }

    private void copyAssetIntoExternal(File external) {
        try {
            InputStream is = getAssets().open(WIKI_NAME);
            OutputStream os = new FileOutputStream(external);
            copyIO(is, os);
            is.close();
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyIO(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = is.read(buffer)) != -1) {
            os.write(buffer, 0, read);
        }
    }

    @Override
    public void notifyPredictiveCards(final List<PredictiveCard> cards) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                predictiveCardTv.append(cards.toString() + "\n");
            }
        });
    }
}
