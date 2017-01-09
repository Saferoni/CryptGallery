package com.safercrypt.scgallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

//TODO сгруппировать и вынести методы в отдельные классы

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // обекты для контроля intent
    private final int PHOTO_OK = 1;
    private final int NO_PHOTO = 0;
    // обявил файлы для дериктории сохранения фото и переменная для хронения имени сделанного фото
    private static File directory;

    private String directoryString;
    private int startI = 0; // переменная нужна будет для подгрузки фото в галерею
    private GridViewAdapter gridAdapter;
    private ArrayList<ImageItem> data = new ArrayList<ImageItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout relativeLayoutMain = (RelativeLayout) findViewById(R.id.content_main);
        LayoutInflater inflater = getLayoutInflater();

        View galleryGrid = inflater.inflate(R.layout.gallery_grid, null);
        relativeLayoutMain.addView(galleryGrid);

        GridView gridView = (GridView) findViewById(R.id.gridView);
        gridAdapter = new GridViewAdapter(this, R.layout.gallery_grid_item, data);
        gridView.setAdapter(gridAdapter);
        // запускаю проверку и создание диерктории для файлов
        detectAndCreateDirectory();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePictureIntent(PHOTO_OK);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });

        //инициализация и активация бокового выдвижного меню
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        // Ожидание выбора нужного фото и переход на него
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ImageItem item = (ImageItem) parent.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, DetailsFullscreenActivity.class);
                intent.putExtra("path", item.getPath());
                intent.putExtra("bitmap",item.getImage());
                intent.putExtra("pathPreview", item.getPathPreview());
                startActivity(intent);
            }
        });

    }

    //метод вызова внешней камеры
    private void takePictureIntent(int actionCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri());
        startActivityForResult(takePictureIntent, actionCode);
    }

    // метод формирующий URI (путь и имя ) создаваемых файлов для передачи в интенте камере
    protected static Uri generateFileUri() {
        long t = System.currentTimeMillis();
        File uriNameFile = new File(directory.getPath()
                + "/photo/"
                + "photo_"
                + String.format("%tF_%<tH-%<tM-%<tS",t)
                + ".jpg");
        return Uri.fromFile(uriNameFile);
    }

    // проверка и формирование если необходимо деректорий
    private void detectAndCreateDirectory() {
        //Получаем состояние SD карты сравниваем его с состоянимем когда доступна и если true выполняем условие
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            createDirectoryFile(Environment.getExternalStorageDirectory().toString());
        }else{
            // если катры нету берем папку программы
            createDirectoryFile(getFilesDir().getPath());
        }
    }

    // метод создания дикректории для хранения файлов и привю
    private void createDirectoryFile(String dir){
        directory = new File(dir + "/CryptGallery/media");
        // проверяю есть ли директория, если нету создаю
        if (!directory.exists()){
            createFileNoMedia(directory.getPath() + "/photo/.nomedia");
            createFileNoMedia(directory.getPath() + "/preview/.nomedia");
        }
    }

    // создание файла noMedia
    private void createFileNoMedia(String dir) {
        File noMediaFileCreate = new File(dir);
        noMediaFileCreate.getParentFile().mkdirs();
        try {
            noMediaFileCreate.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Ниже все для формирования выдвижного меню
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        data.clear();
        gridAdapter.clear();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // поток для создания масивов ImageItem-ов с выводом на экран
        TaskForGridAdapter t = new TaskForGridAdapter(this);
        t.execute();
    }

    // ПОТОК для наплнения ArrayList<ImageItem> data
    public class TaskForGridAdapter extends AsyncTask<Void, Void, ArrayList<ImageItem>> {

        private MainActivity activity;

        TaskForGridAdapter(MainActivity activity) {
            this.activity = activity;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected ArrayList<ImageItem> doInBackground(Void... voids) {
            data = activity.getData();
            return data;
        }
        @Override
        protected void onPostExecute(ArrayList<ImageItem> data) {
            gridAdapter.addAll(data);

        }
    }

    //Вызов и наполнение арай листа превю обектами типа ImageItem пока все обекты за раз через поток выше
    private ArrayList<ImageItem> getData() {
        directoryString = directory.getPath();
        ArrayList<ImageItem> imageItems = new ArrayList<ImageItem>();
        String[] imgs = new File(directoryString + "/photo").list();
        /** указывю уровень сжимается фото  в 200 px для превю*/
        int px = 200;
        /** опреледю сколько файлов в папке и при проверке пережимаю, кладу в папку привю
         * и ложу в арай лист  */
        for (int i = 1; i < imgs.length; i++) {
            String path = directoryString + "/photo/" + imgs[i];
            String pathPreview = directoryString + "/preview/" + imgs[i];
            if (!new File(pathPreview).exists()) {
                createPreviewFile((decodeSampledBitmapFromResource(path, px, px)), pathPreview);
            }
            Bitmap bitmap = BitmapFactory.decodeFile(pathPreview);
            /** передаю обработанное фото в лист imageItem */
            imageItems.add(new ImageItem(bitmap, path, pathPreview));
        }
        return imageItems;
    }



    //Метод перекодирования фото для пиревю
    public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
        // Читаем с inJustDecodeBounds=true для определения размеров
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        // Вычисляем inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Читаем с использованием inSampleSize коэффициента
        options.inJustDecodeBounds = false;
        //Делаем проверку на ориентацию фото с оригиналом и возварщаем привю
        return detectExifAndRotate(path,(BitmapFactory.decodeFile(path, options)));
    }

    //анализ фотографии и разворот если нужно
    public static Bitmap detectExifAndRotate(String path, Bitmap bitm){
        int rotate = 0;
        Bitmap bitmap = bitm;
        try {
            ExifInterface exif = new ExifInterface(path);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (rotate != 0) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            Matrix mtx = new Matrix();
            mtx.preRotate(rotate);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
        }
        return bitmap;
    }

    //метод для вычисления размера пережимаемого фото под заданные параметры
    protected static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Реальные размеры изображения
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Вычисляем наибольший inSampleSize, который будет кратным двум и оставит полученные размеры больше, чем требуемые
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    //Метод записи файлов привю в папку preview
    private void createPreviewFile(Bitmap bitmap, String path){
        try {
            FileOutputStream filePreview = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, filePreview);
            filePreview.flush();
            filePreview.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
