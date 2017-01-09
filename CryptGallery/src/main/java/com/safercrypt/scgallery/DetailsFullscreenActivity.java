package com.safercrypt.scgallery;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */



//TODO сделать слайд фото
//TODO реализовать зумм

public class DetailsFullscreenActivity extends AppCompatActivity {

    private String path, pathPreview;
    private Bitmap bitmap;
    private ImageView imageView;
    private ActionBar actionBar;
    private Button bottomShare;
    private Button bottomDelete;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            imageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private LinearLayout mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_details_fullscreen);

        mVisible = true;
        mControlsView = (LinearLayout) findViewById(R.id.fullscreen_content_controls);
        bottomShare = (Button) findViewById(R.id.bottomShare);
        bottomDelete = (Button) findViewById(R.id.bottomDelete);

        path = getIntent().getStringExtra("path");
        bitmap = getIntent().getParcelableExtra("bitmap");
        pathPreview = getIntent().getStringExtra("pathPreview");
        imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageBitmap(bitmap);

        TaskBitmapPhoto t = new TaskBitmapPhoto();
        t.execute();


        // нахожу тулбар и нпаолняю его
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(nameFotoOnMenu(path));
        actionBar.setDisplayHomeAsUpEnabled(true);
        //делаю прозрачным статус бар, пока так, думаю есть проще вариант
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorCleen));
        //назначаю флаг для отображения сразу на весь экран
        imageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        //потом прячу все бары в методе onPostCreate

        // Set up the user interaction to manually show or hide the system UI.
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        bottomShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent();
                Uri imageUri = Uri.parse("file://" + path);
                //ниже залочено на будущее(когда будет шифрование) код для реализации обмена фото через
                //общую папку android com.safercrypt.android.scgallery.share.files
                //раельизция через FileProvider
                //File file = new File(path);
                //Uri imageUri = FileProvider.getUriForFile(Context, "com.safercrypt.android.scgallery.share.files", file);
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("image/jpeg");
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
            }
        });
        bottomDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(deleteFileButton()) finish();
            }
        });

        //Кнопка перехода на камеру как и в основном активити
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("BAG","нажал на кнопку");
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, MainActivity.generateFileUri());
                startActivityForResult(takePictureIntent, 1);
            }
        });

        // На будущее для перелистывания фото в фулскрине
        /*imageView.setOnTouchListener(new View.OnTouchListener(){
            public boolean onTouch(View v, MotionEvent event){
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:// нажатие
                        Log.d("BAG","нажал на экран");
                        toggle();
                        break;
                    case MotionEvent.ACTION_MOVE: // движение
                        Log.d("BAG","провел по экрану");
                        break;
                    case MotionEvent.ACTION_UP: // отпускание
                        Log.d("BAG","отпускание");
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                return true;
           }
        });*/

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }
    //проверяю состояние меню и вызываю соответственные методы
    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        imageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //меню пока спрятал переношу вниз экрана
        //getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override // кнопки меню и их реализация
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuDelete:
                if(deleteFileButton()) finish();
                return true;
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            case R.id.menuShare:
                // создаю опрос по интенту для share (нужно проверить права на передаваемый файл или создавать временный файл)
                Intent shareIntent = new Intent();
                Uri imageUri = Uri.parse("file://" + path);
                Log.d("BAG","Передача в shere");
                //ниже залочено на будущее(когда будет шифрование) код для реализации обмена фото через
                //общую папку android com.safercrypt.android.scgallery.share.files
                //раельизция через FileProvider
                //File file = new File(path);
                //Uri imageUri = FileProvider.getUriForFile(Context, "com.safercrypt.android.scgallery.share.files", file);
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("image/jpeg");
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                //стартую актвивит шары
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // метод удаления файла
    private boolean deleteFileButton(){
        boolean a;
        if(new File(path).delete() & new File(pathPreview).delete()){
            a = true;
        }else {
            a = false;
        }
        return a;
    }

    // метод формирования только даты создания в имени файла
    private String nameFotoOnMenu(String path){
        String name = path;
        int i = name.lastIndexOf("photo_");
        if (i != -1) {
            name = path.substring(i+6, path.length()-7);
        }
        return  name;
    }

    // поток для привидения в bitmap оригинала фотки c сжатием
    public class TaskBitmapPhoto extends AsyncTask<Void, Void, Void> {
        int i;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //TODO реализовать смену i при зуме
            i = 600;
        }
        @Override
        protected Void doInBackground(Void... params) {
            //bitmap = GalleryActivity.detectExifAndRotate(path, (BitmapFactory.decodeFile(path)));
            bitmap = MainActivity.decodeSampledBitmapFromResource(path, i, i);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            imageView.setImageBitmap(bitmap);
        }
    }
}
