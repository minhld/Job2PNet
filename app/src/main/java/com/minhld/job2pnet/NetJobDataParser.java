package com.minhld.job2pnet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.minhld.job2p.com.minhld.extra.WebPart;
import com.minhld.job2p.jobs.JobDataParser;
import com.minhld.job2p.supports.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * GPS data parser
 *
 * Created by minhld on 12/30/2015.
 */
public class NetJobDataParser implements JobDataParser {

    @Override
    public Class getDataClass() {
        return String.class;
    }

    @Override
    public Object readFile(String path) throws Exception {
        return "http://vnexpress.net/photo/thoi-su/nguoi-viet-nam-lan-dau-di-bau-cu-70-nam-truoc-3338003.html";
    }

    @Override
    public Object parseBytesToObject(byte[] byteData) throws Exception {
        WebPart webPart = (WebPart) Utils.deserialize(byteData);
        return webPart;
    }

    @Override
    public byte[] parseObjectToBytes(Object objData) throws Exception {
        return Utils.serialize(objData);
    }

    @Override
    public Object getSinglePart(Object data, int numOfParts, int index) {
        WebPart webPart = new WebPart();
        webPart.url = (String) data;
        webPart.numOfParts = numOfParts;
        webPart.index = index;
        return webPart;
    }

    @Override
    public String getJsonMetadata(Object objData) {
        // return the folder name for the placeholder, which will be
        // located inside the Download folder
        return "web";
    }

    @Override
    public Object createPlaceholder(String jsonMetadata) {
        // the place holder will be an empty folder
        // delete the placeholder if it is already there
        String placeholderPath = Utils.getDownloadPath() + jsonMetadata;
        File f = new File(placeholderPath);
        if (f.exists()) {
            try {
                Utils.delete(f);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        // create the placeholder
        f.mkdir();

        return placeholderPath;
    }

    @Override
    public Object copyPartToPlaceholder(Object placeholderObj, byte[] partObj, int index) {
        String placeholderPath = (String) placeholderObj;

        // save to a file
        String uuid = UUID.randomUUID().toString();
        String tempZipPath = Utils.getDownloadPath() + "/" + uuid + ".zip";

        try {
            FileOutputStream fos = new FileOutputStream(tempZipPath);
            fos.write(partObj, 0, partObj.length);
            fos.flush();
            fos.close();

            Utils.unzipFile(tempZipPath, placeholderPath, false);

            // delete the tempo ZIP file
            Utils.delete(new File(tempZipPath));
        } catch(Exception e) {

        }

        // return
        return placeholderPath;
    }

    @Override
    public void destroy(Object data) {
        data = null;
    }

    @Override
    public boolean isObjectDestroyed(Object data) {
        return true;
    }
}
