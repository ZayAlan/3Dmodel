package com.example.chenx.aimodel.file;

import android.content.Context;
import android.os.Environment;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint3f;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileService {
    private Context context;

    public FileService(Context context){
        this.context = context;
    }

//    public void SaveToSDcard(String contentName,String filename, String content) throws IOException {
//        File file = new File(Environment.getExternalStorageDirectory(),contentName);
//        if (!file.exists()) file.mkdir();
//        filename = filename+".txt";
//        File realFile = new File(file,filename);
//        FileOutputStream outputStream = new FileOutputStream(realFile.getAbsolutePath());
//        outputStream.write(content.getBytes());
//        outputStream.flush();
//        outputStream.close();
//    }


    public void SaveToSDcard(String contentName, String filename, MatOfPoint3f structure){
        File file = new File(Environment.getExternalStorageDirectory(),contentName);
        if (!file.exists()) file.mkdir();
        filename = filename+".txt";
        File realFile = new File(file,filename);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(realFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String content = structure.toList().toString();
        try {
            outputStream.write(content.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void SaveToSDcard(String contentName, String filename, List<Mat> changeMat){
        File file = new File(Environment.getExternalStorageDirectory(),contentName);
        if (!file.exists()) file.mkdir();
        filename = filename+".txt";
        File realFile = new File(file,filename);
        List<byte[]> byteBufferList = new ArrayList<>();
        for (Mat mat: changeMat
             ) {
            byte[] byteBuffer = new byte[mat.height()  *  mat.width()];
            mat.get(0, 0, byteBuffer);
            byteBufferList.add(byteBuffer);
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(realFile.getAbsolutePath());
            for (byte[] byteBuffer : byteBufferList){
                outputStream.write(byteBuffer);
            }
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
