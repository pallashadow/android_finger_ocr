package com.health.service.face;
/**
 * projectName: 图灵机器人android人脸模组2020
 * fileName: FaceAlign.java
 * date: 2020年4月29日
 * copyright(c) 2017-2020 北京光年无限科技有限公司
 */

import android.graphics.Bitmap;

import java.util.HashMap;

public class Retrieval {

    private HashMap<Integer, FaceInfo> faceInfoMap = new HashMap<Integer, FaceInfo>();

    private int nextFaceId = 1;

    private String savePath = "./storage/sdcard0/faceData.txt";

    public Retrieval(String savePath){
        this.savePath = savePath;
    }

    public FaceInfo add(String name, Bitmap faceImage, float[] feature128){
        /** 增
        * name: 人脸名称（姓名）
        * imageData: 图片数据
        * feature128: 128位特征数据
        */
        int id = this.nextFaceId;
        this.nextFaceId += 1;
        if (name.length()==0) {
            name = Integer.toString(id);
        }
        FaceInfo faceInfo1 = update(id, name, faceImage, feature128);
        return faceInfo1;
    }

    public void delete(int id){
        /**
         * 删
         */
        faceInfoMap.remove(id);
        saveToDisk(this.savePath);
    }

    public void deleteAll(){
        /**
         * 全删
         */
        faceInfoMap.clear();
        nextFaceId = 1;
        saveToDisk(this.savePath);
    }


    public FaceInfo update(int id, String name, Bitmap faceImage, float[] feature128){
        /**
         * 改
         */
        FaceInfo faceInfo1 = new FaceInfo(id, name, faceImage, feature128);
        faceInfoMap.put(id, faceInfo1);
        saveToDisk(this.savePath);
        return faceInfo1;
    }

    public FaceInfo retrievalByName(String name){
        /**
         * 查：之根据姓名查询
         */
        for (int id : faceInfoMap.keySet()) {
            FaceInfo faceInfo1 = faceInfoMap.get(id);
            if (faceInfo1.name == name){
                return faceInfo1;
            }
        }
        return createEmptyFaceInfo();
    }

    public FaceInfo retrievalByFeature128(float[] feature128){
        /**
         * 查：根据人脸特征查最相似
         */
        //HashMap<Integer, Float> simMap = new HashMap<Integer, Float>();
        if (faceInfoMap.size()==0) return createEmptyFaceInfo();
        int idMax = -1;
        float simMax = -1;
        for (int id : faceInfoMap.keySet()) {
            float sim = calcSimilar(feature128, faceInfoMap.get(id).feature128);
            System.out.println(sim);
            if (sim > simMax){
                idMax = id;
                simMax = sim;
            }
            //simMap.put(id, sim);
        }
        FaceInfo faceInfo1 = faceInfoMap.get(idMax);
        faceInfo1.similarity = simMax;
        return faceInfo1;
    }


    private void saveToDisk(String filePath){
        /* TODO 同步人脸库到SD
        try {
            FileOutputStream f = new FileOutputStream(new File(filePath));
            ObjectOutputStream o = new ObjectOutputStream(f);
            // Write objects to file
            o.writeObject(this);
            o.close();
            f.close();
        } catch (FileNotFoundException e) {
            System.out.println("FaceData File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        }
         */
    }

    private static float calcSimilar(float[] feature1, float[] feature2) {
        assert(feature1.length == feature2.length);
        float ret = 0;
        for (int i=0;i<128;i++){
            ret += feature1[i] * feature2[i];
        }
        return ret;
    }

    private static FaceInfo createEmptyFaceInfo(){
        Bitmap emptyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        float[] emptyFeature = new float[128];
        return new FaceInfo(-1, "empty", emptyBitmap, emptyFeature);
    }


}
