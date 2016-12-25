package com.safercrypt.scgallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;

import java.io.File;

//TODO сделать меню нормальное и убрать выдвижение меню слева
//TODO сделать слайд фото
//TODO сделать управление фотографиями
public class DetailsActivity extends MainActivity {
    private String path, pathPreview;
    private Bitmap bitmap;
    private ImageView imageView;
    private ShareActionProvider mShareActionProvider;
    private ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //добавляю к контентмейн галери детайлс
        RelativeLayout relativeLayoutMain = (RelativeLayout) findViewById(R.id.content_main);
        LayoutInflater inflater = getLayoutInflater();
        View galleryGrid = inflater.inflate(R.layout.gallery_details_activity, null);
        relativeLayoutMain.addView(galleryGrid);

        // TODO может интентом передавать сразу ArrayList <ImageItem>?
        path = getIntent().getStringExtra("path");
        bitmap = getIntent().getParcelableExtra("bitmap");
        pathPreview = getIntent().getStringExtra("pathPreview");
        imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageBitmap(bitmap);

        TaskBitmapPhoto t = new TaskBitmapPhoto();
        t.execute();

        // Hide the status bar.
        /*View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);*/
        // нахожу тулбар и наполняю кнопками
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(nameFotoOnMenu(path));
        actionBar.setDisplayHomeAsUpEnabled(true);
        //вконце прячу так как нужно что б отображался только при нажатии на экран
        actionBar.hide();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("BAG","нажал на кнопку");
            }
        });

        imageView.setOnTouchListener(new View.OnTouchListener(){
            public boolean onTouch(View v, MotionEvent event){
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:// нажатие
                        Log.d("BAG","нажал на экран");
                        if (actionBar.isShowing()) {
                            Log.d("BAG","Меню отображается скрываю");
                            actionBar.hide();
                        }else{
                            Log.d("BAG","Меню скрыто отображаю");
                            actionBar.show();
                        }
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
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
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
            case R.id.share:
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
                shareIntent.putExtra(Intent.EXTRA_TEXT,"Image from SecureCamera: " + nameFotoOnMenu(path));
                // TODO передаю ActionProvider пока не работает
                //mShareActionProvider = (ShareActionProvider) item.getActionProvider();
                Log.d("BAG","Двигаемся3");
                // формирую список програм приемников
                setShareIntent(shareIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
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