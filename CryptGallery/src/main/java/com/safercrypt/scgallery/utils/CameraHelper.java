package com.safercrypt.scgallery.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * рализация работы с камерой в основном для кнопки фаб
 */

public class CameraHelper extends AppCompatActivity {

    private int image_count_before = 0;
    private final int CAPTURE_IMAGES_FROM_CAMERA = 666;


   //Метод запуска камеры в полном режиме (много фоток реализовано через галерею)
    public void startCameraActivity() {
        Cursor cursor = loadCursor();
        //current images in mediaStore
        image_count_before = cursor.getCount();
        cursor.close();
        Intent cameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        List<ResolveInfo> activities = getPackageManager().queryIntentActivities(cameraIntent, 0);
        if (activities.size() > 0)
            startActivityForResult(cameraIntent, CAPTURE_IMAGES_FROM_CAMERA);
        else
            Toast.makeText(this, "Нету камеры", Toast.LENGTH_SHORT).show();
        }
    // задание курсора для родной галереи устройства
    public Cursor loadCursor() {
        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
        final String orderBy = MediaStore.Images.Media.DATE_ADDED;
        return getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
    }
    // наполнение именами файлов в родной галерее для дальнейшего анализа
    public String[] getImagePaths(Cursor cursor, int startPosition) {
        int size = cursor.getCount() - startPosition;
        if (size <= 0) return null;
        String[] paths = new String[size];
        int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        for (int i = startPosition; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            paths[i - startPosition] = cursor.getString(dataColumnIndex);
        }
        return paths;
    }
    // анализ того что было нафоткано и запрос переноса файлов в мою галерею
    private void exitingCamera() {
        Cursor cursor = loadCursor();
        //get the paths to newly added images
        String[] paths = getImagePaths(cursor, image_count_before);
        for (String path : paths) {
            try {
                String p = FileHelper.getDirectory().getPath()
                        + "/photo"
                        + FileHelper.generateNamePhoto();
                moveFile(new File(path),
                        new File(p));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
    }
    //перенос файлов и формирование имени новым фото
    private void moveFile(File file, File newFile) throws Exception {
        InputStream in;
        OutputStream out;
        in = new FileInputStream(file.getPath());
        out = new FileOutputStream(newFile.getPath());
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.flush();
        out.close();
        if (!file.delete()) {
            return;
        }
        updateGalleryCache(file);
    }
    // обновление кеша родной галереии
    private void updateGalleryCache(File file) {
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = new String[]{file.getAbsolutePath()};
        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = getContentResolver();
        Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
        if (c.moveToFirst()) {
            long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            contentResolver.delete(deleteUri, null, null);
        }
        c.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGES_FROM_CAMERA) {
            exitingCamera();
        }
    }

    //метод вызова внешней камеры для одиночного фото
    private void takeOnePictureIntent(int actionCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileHelper.generateFileUri());
        startActivityForResult(takePictureIntent, actionCode);
    }
}
