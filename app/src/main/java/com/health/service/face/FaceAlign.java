package com.health.service.face;
/**
 * projectName: 图灵机器人android人脸模组2020
 * fileName: FaceAlign.java
 * date: 2020年4月29日
 * copyright(c) 2017-2020 北京光年无限科技有限公司
 */

import android.graphics.Bitmap;
import java.nio.ByteBuffer;
import android.util.Log;
import android.graphics.Color;

import static java.lang.Math.abs;

public class FaceAlign {

	private static final String TAG = "FaceAlign";

	private byte[] imageData;
	private int height;
	private int width;
	private byte[] alignedData;
	public float[] orientations = {0,0,0};
	public float sharpness = 0;
	
	//public Bitmap getAlignedFace(Bitmap bitmap, float[] landmarks5, float[] det) {
	public byte[] getAlignedFaceByIndex(byte[] imageData, int height, int width,
											   float[] faceInfo, int faceIdx) {
		assert(faceInfo[0]>0);
		int numFaces = (int)faceInfo[0];
		float l = faceInfo[1+14*faceIdx];
		float t = faceInfo[2+14*faceIdx];
		float r = faceInfo[3+14*faceIdx];
		float b = faceInfo[4+14*faceIdx];
		float[] det = {l,t,r,b};
		float[] landmarks5 = new float[10];
		for (int i=0;i<10;i++){
			landmarks5[i] = faceInfo[14*faceIdx+5+i];
		}
		this.alignedData = jniFaceAlign(imageData, height, width, landmarks5, det);
		this.orientations = jniEstimateFaceOrient(landmarks5, height, width);
		this.sharpness = jniEstimateAlignedFaceSharpness(this.alignedData);
		return alignedData;
	}
	
	public int getLargestFaceIndex(float[] faceInfo){
		assert(faceInfo[0]>0);
		int numFaces = (int)faceInfo[0];
		if (numFaces==1) return 0;
		float maxArea = 0;
		int idx = 0;
		for (int i=0;i<numFaces;i++){
			float l = faceInfo[1+14*i];
			float t = faceInfo[2+14*i];
			float r = faceInfo[3+14*i];
			float b = faceInfo[4+14*i];
			float area = (r-l)*(b-t);
			if (area > maxArea){
				maxArea = area;
				idx = i;
			}
		}
		return idx;
	}

	public void setImage(byte[] imageData, int height, int width){
		this.imageData = imageData;
		this.height = height;
		this.width = width;
	}
	
	public void faceAlignLargest(float[] faceInfo){
		assert((int)faceInfo[0]>0);
		int faceIdx = getLargestFaceIndex(faceInfo);
		getAlignedFaceByIndex(this.imageData, this.height, this.width, faceInfo, faceIdx);
	}

	public byte[] getAlignedData(){
		return this.alignedData;
	}

	public boolean checkValid4Recognition(){
		if (this.sharpness<6) return false; // face blur
		if (abs(this.orientations[0])>40) return false; // head up down rotation degree
		if (abs(this.orientations[1])>30) return false; // head left right rotation degree
		return true;
	}

	static {
		try {
			System.loadLibrary("faceAlign");
		} catch (UnsatisfiedLinkError e) {
			android.util.Log.e("FaceAlign", "library not found!");
		}
	}
    private native byte[] jniFaceAlign(byte[] data1, int height, int width, float[] landmarks5, float[] det);

	private native float[] jniEstimateFaceOrient(float[] landmarks5, int height, int width);

	private native float jniEstimateAlignedFaceSharpness(byte[] data1);
}