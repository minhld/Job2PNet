package com.minhld.job2pnet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.minhld.job2p.jobs.JobDataParser;

import java.io.ByteArrayOutputStream;

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
        return "";
    }

    @Override
    public Object parseBytesToObject(byte[] byteData) throws Exception {
        return BitmapFactory.decodeByteArray(byteData, 0, byteData.length);
    }

    @Override
    public byte[] parseObjectToBytes(Object objData) throws Exception {
        Bitmap bmpData = (Bitmap) objData;

        // assign the binary data
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmpData.compress(Bitmap.CompressFormat.JPEG, 0, bos);
        byte[] byteData = bos.toByteArray();
        bos.close();

        return byteData;
    }

    @Override
    public Object getSinglePart(Object data, int numOfParts, int index) {
        Bitmap bmpData = (Bitmap) data;
        int pieceWidth = bmpData.getWidth() / numOfParts;
        return Bitmap.createBitmap(bmpData, (pieceWidth * index), 0, pieceWidth, bmpData.getHeight());
    }

    @Override
    public String getJsonMetadata(Object objData) {
        Bitmap bmp = (Bitmap) objData;
        return "{ 'width': " + bmp.getWidth() + ", 'height': " + bmp.getHeight() + " }";
    }

    @Override
    public Object createPlaceholder(String jsonMetadata) {
        return "";
    }

    @Override
    public Object copyPartToPlaceholder(Object placeholderObj, byte[] partObj, int index) {
        // get bitmap from original data
        Bitmap partBmp = BitmapFactory.decodeByteArray(partObj, 0, partObj.length);

        int pieceWidth = partBmp.getWidth();
        Canvas canvas = new Canvas((Bitmap) placeholderObj);
        canvas.drawBitmap(partBmp, index * pieceWidth, 0, null);
        return null;
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
