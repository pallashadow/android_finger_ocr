package com.health.service.face;

import android.graphics.Bitmap;

/**
 * projectName: 图灵机器人android人脸模组2020
 * fileName: FaceAlign.java
 * date: 2020年4月29日
 * copyright(c) 2017-2020 北京光年无限科技有限公司
 */

public class FaceInfo {
    public int id;
    public String name;
    public Bitmap faceImage;
    public float[] feature128;
    public float similarity=0;

    FaceInfo(int id, String name, Bitmap faceImage, float[] feature128){
        this.id = id;
        this.name = name;
        this.faceImage = faceImage.copy(Bitmap.Config.ARGB_8888, false);
        this.feature128 = feature128.clone();
    }
}
