package com.safercrypt.scgallery.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Данный класс будет отвечать за работу с данными в памяти телефона
 * - запись файлов , удаление , создание директорий,
 * Так же за формирование имени файлов
 */

public class FileHelper {

    private static File directory;

    public static File getDirectory() {
        return directory;
    }

    // (старый вариант) метод формирующий URI (путь и имя ) создаваемых файлов для передачи в интенте камере
    public static Uri generateFileUri() {
        long t = System.currentTimeMillis();
        File uriNameFile = new File(directory.getPath()
                + "/photo/"
                + "photo_"
                + String.format("%tF_%<tH-%<tM-%<tS",t)
                + ".jpg");
        return Uri.fromFile(uriNameFile);
    }

    public static String generateNamePhoto(){
        long t = System.currentTimeMillis();
        String namePhoto = "/photo_" + String.format("%tF_%<tH-%<tM-%<tS",t);

        while (new File(directory.getPath() +"/photo"+ namePhoto + ".jpg").exists()){
            namePhoto = namePhoto + "+";
        }
        namePhoto = namePhoto + ".jpg";
        return namePhoto;
    }

    // проверка и формирование если необходимо деректорий
    public static void detectAndCreateDirectory(String programDirectory) {
        //Получаем состояние SD карты сравниваем его с состоянимем когда доступна и если true выполняем условие
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            createDirectoryFile(Environment.getExternalStorageDirectory().toString());
        }else{
            // если катры нету берем папку программы
            createDirectoryFile(programDirectory);
        }
    }

    //Метод записи файлов привю в папку preview
    public static void createPreviewFile(Bitmap bitmap, String path){
        try {
            FileOutputStream filePreview = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, filePreview);
            filePreview.flush();
            filePreview.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Создание файла noMedia
    private static void createFileNoMedia(String dir) {
        File noMediaFileCreate = new File(dir);
        noMediaFileCreate.getParentFile().mkdirs();
        try {
            noMediaFileCreate.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // метод создания дикректории для хранения файлов и привю
    private static void createDirectoryFile(String dir){
        directory = new File(dir + "/CryptGallery/media");
        // проверяю есть ли директория, если нету создаю
        if (!directory.exists()){
            createFileNoMedia(directory.getPath() + "/photo/.nomedia");
            createFileNoMedia(directory.getPath() + "/preview/.nomedia");
        }
    }

}
