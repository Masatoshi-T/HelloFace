package jp.techacademy.masatoshi.tashiro.helloface;

import java.io.IOException;
import java.util.Random;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Parameters;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import static jp.techacademy.masatoshi.tashiro.helloface.Val.mirror;
import static jp.techacademy.masatoshi.tashiro.helloface.Val.turn;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SurfaceView mSvFacePreview;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera = null;
    private FaceMarkerView mFaceMarkerView;
    private MediaPlayer mediaPlayer;
    private MediaPlayer SecondMediaPlayer;
    private boolean isCheck;
    static final int REQUEST_CODE = 1;
    private AdView mAdView;
    private Toolbar mToolbar;
    int MODE = 0;
    private boolean GIRL_MODE = false;
    static final int RANDOM_MODE = 0;
    static final int FAMI_MODE = 1;
    static final int MAC_MODE = 2;
    private ImageButton imageButton;
    private boolean Onecheck = true;


    int[] mp3Sounds = new int[] {R.raw.konbini1, R.raw.conbini2,  R.raw.famima, R.raw.mac_poteto};
    int[] girlmp3 = new int[] {R.raw.josei_hay, R.raw.josei_irassyaimase, R.raw.josei_arigatou, R.raw.josei_sinnyu,
            R.raw.girl1_ookini1, R.raw.girl1_maidoarigatougozaimasu1, R.raw.girl1_rasshai1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        imageButton = (ImageButton) findViewById(R.id.turn_camera);

        checkPermission();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            isCheck = true;
            mSvFacePreview = (SurfaceView) findViewById(R.id.surface_view);

            mSurfaceHolder = mSvFacePreview.getHolder();
            mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    if (mCamera != null) {
                        mCamera.stopFaceDetection();
                        mCamera.stopPreview();
                        mCamera.release();
                        mCamera = null;
                    }
                }

                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    if (mirror) {
                        turn = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    } else {
                        turn = Camera.CameraInfo.CAMERA_FACING_BACK;
                    }
                    Log.d("Log_mirror",String.valueOf(mirror) + " turn:" + String.valueOf(turn));
                    mCamera = Camera.open(turn);
                    if (mCamera != null) {
                        try {
                            mCamera.setDisplayOrientation(90);
                            mCamera.setPreviewDisplay(mSurfaceHolder);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    if (mCamera != null) {
                        Log.d("Log1_W:", String.valueOf(width) + " H:" + String.valueOf(height));
                        Parameters params = mCamera.getParameters();
                        mCamera.startPreview();

                        int maxFaces = params.getMaxNumDetectedFaces();

                        if (maxFaces > 0) {
                            mCamera.setFaceDetectionListener(new FaceDetectionListener() {
                                @Override
                                public void onFaceDetection(Face[] faces, Camera camera) {
                                    mFaceMarkerView.faces = faces;
                                    mFaceMarkerView.invalidate();
                                    if (faces.length > 0 && isCheck) {
                                        audioSetup();
                                        audioPlay();
                                    }
                                }
                            });
                        }

                        try {
                            mCamera.startFaceDetection();
                        } catch (IllegalArgumentException e) {

                        } catch (RuntimeException e) {
                        }
                    }
                }
            });

            mFaceMarkerView = new FaceMarkerView(this);
            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.content_main);
            relativeLayout.addView(mFaceMarkerView, new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT));

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Onecheck) {
                        Onecheck = false;
                        mirror = !mirror;
                        mCamera.stopFaceDetection();
                        mCamera.stopPreview();
                        mCamera.release();
                        mCamera = null;
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.girl_mode) {
            GIRL_MODE = !GIRL_MODE;
            item.setIcon(GIRL_MODE ? R.drawable.girl_on : R.drawable.girl_off);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_random) {
            mToolbar.setTitle("Random");
            MODE = RANDOM_MODE;
        } else if (id == R.id.nav_fami) {
            mToolbar.setTitle("某コンビニ");
            MODE = FAMI_MODE;
        } else if (id == R.id.nav_mac) {
            mToolbar.setTitle("ポテトが揚がったよ");
            MODE = MAC_MODE;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class FaceMarkerView extends View {
        Face[] faces;

        public FaceMarkerView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawColor(Color.TRANSPARENT);
            Paint paint = new Paint();
            paint.setColor(Color.CYAN);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            if (faces != null) {
                for (int i = 0; i < faces.length; i++) {
                    int saveState = canvas.save();
                    Matrix matrix = new Matrix();
                    matrix.setScale(mirror ? -1 : 1, 1);
                    matrix.postRotate(90);
                    matrix.postScale(getWidth() / 2000f, getHeight() / 2000f);
                    matrix.postTranslate(getWidth() / 2f, getHeight() / 2f);
                    canvas.concat(matrix);
                    canvas.drawRect(faces[i].rect, paint);
                    canvas.restoreToCount(saveState);
                    Log.d("Log_W:", String.valueOf(getWidth()) + " H:" + String.valueOf(getHeight()));
                }
            }
        }
    }
    private void audioSetup() {
        mediaPlayer = new MediaPlayer();
        Random random = new Random();
        int n;
        switch (MODE) {
            case RANDOM_MODE:
                n = random.nextInt(4);
                mediaPlayer = MediaPlayer.create(this, mp3Sounds[n]);
                Log.d("Log_p", String.valueOf(n));
                break;
            case FAMI_MODE:
                mediaPlayer = MediaPlayer.create(this, mp3Sounds[2]);
                break;
            case MAC_MODE:
                mediaPlayer = MediaPlayer.create(this, mp3Sounds[3]);
                break;
        }
        if (GIRL_MODE) {
            int m = random.nextInt(7);
            SecondMediaPlayer = MediaPlayer.create(this, girlmp3[m]);
            this.SecondMediaPlayer.setVolume(1.0f, 1.0f);
        }
        this.mediaPlayer.setVolume(1.0f, 1.0f);
    }

    private void audioPlay() {
        isCheck = false;
        if (!GIRL_MODE) {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    audioStop();
                    isCheck = true;
                }
            });
        } else {
            mediaPlayer.start();
            SecondMediaPlayer.start();
            if (mediaPlayer.getDuration() > SecondMediaPlayer.getDuration()) {
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        audioStop();
                        SecondAudioStop();
                        isCheck = true;
                    }
                });
            }
        }
    }

    private void audioStop() {
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private void SecondAudioStop() {
        SecondMediaPlayer.stop();
        SecondMediaPlayer.reset();
        SecondMediaPlayer.release();
        SecondMediaPlayer = null;
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }
    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                new AlertDialog.Builder(this)
                        .setTitle("許可が必要です")
                        .setMessage("人の顔を検知するために、CAMERAを許可してください")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestCAMERA();
                            }
                        })
                        .setNegativeButton("Cancel",  new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showToast("許可されなかったので、アプリは動作しません");
                            }
                        })
                        .show();
            } else {
                requestCAMERA();
            }
        }
    }
    private void requestCAMERA() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    showToast("許可されなかったので、アプリは動作しません");
                }
            }
        }
    }
    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.release();
        }
        super.onDestroy();
    }
}
