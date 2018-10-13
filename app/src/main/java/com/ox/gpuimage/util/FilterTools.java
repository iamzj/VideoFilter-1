package com.ox.gpuimage.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ox.gpuimage.GPUImageFilter;
import com.ox.gpuimage.GPUImageFilterGroup;
import com.ox.gpuimage.Rotation;
import com.ox.imageutil.NativeImageUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

public class FilterTools {

	private static final int GET_NEW_FILTER_REQUEST_CODE = 10011;

	public static final String FILTER_NAME_DEFAULT = "Original";

	/**
	 * 解密并得到Bitmap
	 * @param context
	 * @param id
	 * @return
	 */
	public static Bitmap getDecryptBitmap(final Context context, int id){
		InputStream is = null;
		Bitmap bitmap = null;
		ByteArrayInputStream bais = null;
		try {
			is = context.getResources().openRawResource(id);
			byte[] buffer = new byte[is.available()];
			is.read(buffer, 0, is.available());
			byte[] result = CryptTool.decrypt(buffer, NativeImageUtil.getOxString());
			bais = new ByteArrayInputStream(result);
			bitmap = BitmapFactory.decodeStream(bais);
			return bitmap;
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
				bais.close();
			} catch (Throwable e) {

			}
		}
		return null;
	}

	/**
	 * 解密字符串
	 * @param str
	 * @return
	 */
	public static String getDecryptString(String str){
		return CryptTool.decrypt(str, NativeImageUtil.getOxString());
	}

	/**
	 * 旋转贴图效果
	 *
	 * @param filter
	 * @param rotation
	 * @param isBadTwoInputFilter
	 */
	public static void rotateFilter(GPUImageFilter filter, Rotation rotation, boolean isBadTwoInputFilter) {
		if (isBadTwoInputFilter) {
			rotateTwoInputFilter(filter, rotation);
		} else {
			filter.setRotation(rotation, false, false);
		}
	}

	public static void rotateTwoInputFilter(GPUImageFilter filter, Rotation rotation) {
		try {
			int twoInputRotation = 0;
			if (rotation == Rotation.ROTATION_90) {
				twoInputRotation = 270;
			} else if (rotation == Rotation.ROTATION_270) {
				twoInputRotation = 90;
			} else if (rotation == Rotation.ROTATION_180) {
				twoInputRotation = 180;
			}
			Class filterCl = filter.getClass();
			if (filterCl.getSuperclass().getSimpleName().equals("GPUImageTwoInputFilter")) {
				Class rotationCl = filterCl.getClassLoader().loadClass(filterCl.getPackage().getName() + ".Rotation");
				Method rotationfromInt = rotationCl.getDeclaredMethod("fromInt", int.class);
				Method setRotationMethod = filterCl.getSuperclass().getDeclaredMethod("setRotation",
						rotationCl, boolean.class, boolean.class);
				setRotationMethod.invoke(filter, rotationfromInt.invoke(null, twoInputRotation), false, false);
			} else if (filter instanceof GPUImageFilterGroup) {
				GPUImageFilterGroup group = (GPUImageFilterGroup) filter;
				for(GPUImageFilter ft : group.getMergedFilters()) {
					filterCl = ft.getClass();
					if (filterCl.getSuperclass().getSimpleName().equals("GPUImageTwoInputFilter")) {
						Class rotationCl = filterCl.getClassLoader().loadClass(filterCl.getPackage().getName() + ".Rotation");
						Method rotationfromInt = rotationCl.getDeclaredMethod("fromInt", int.class);
						Method setRotationMethod = filterCl.getSuperclass().getDeclaredMethod("setRotation",
								rotationCl, boolean.class, boolean.class);
						setRotationMethod.invoke(ft, rotationfromInt.invoke(null, twoInputRotation), false, false);
					} else {
						rotateHolloweenFilter(ft, twoInputRotation);
					}
				}
			} else if (filterCl.getSuperclass().getSimpleName().equals("GPUImageFilterGroup")) {
				Method getMergedFiltersMethod = filterCl.getSuperclass().getDeclaredMethod("getMergedFilters");
				List<GPUImageFilter> filters = (List<GPUImageFilter>) getMergedFiltersMethod.invoke(filter);
				for(GPUImageFilter ft : filters) {
					filterCl = ft.getClass();
					if (filterCl.getSuperclass().getSimpleName().equals("GPUImageTwoInputFilter")) {
						Class rotationCl = filterCl.getClassLoader().loadClass(filterCl.getPackage().getName() + ".Rotation");
						Method rotationfromInt = rotationCl.getDeclaredMethod("fromInt", int.class);
						Method setRotationMethod = filterCl.getSuperclass().getDeclaredMethod("setRotation",
								rotationCl, boolean.class, boolean.class);
						setRotationMethod.invoke(ft, rotationfromInt.invoke(null, twoInputRotation), false, false);
					} else {
						rotateHolloweenFilter(ft, twoInputRotation);
					}
				}
			}
		} catch (Throwable tr) {
		}
	}

	public static void rotateHolloweenFilter(GPUImageFilter filter, int rotation) {
		try {
			Class filterCl = filter.getClass();
			if (filterCl.getSuperclass().getSimpleName().equals("GPUImageFilterGroup")) {
				Method getMergedFiltersMethod = filterCl.getSuperclass().getDeclaredMethod("getMergedFilters");
				List<GPUImageFilter> filters = (List<GPUImageFilter>) getMergedFiltersMethod.invoke(filter);
				for (GPUImageFilter ft : filters) {
					filterCl = ft.getClass();
					if (filterCl.getSuperclass().getSimpleName().equals("GPUImageTwoInputFilter")) {
						Class rotationCl = filterCl.getClassLoader().loadClass(filterCl.getPackage().getName() + ".Rotation");
						Method rotationfromInt = rotationCl.getDeclaredMethod("fromInt", int.class);
						Method setRotationMethod = filterCl.getSuperclass().getDeclaredMethod("setRotation",
								rotationCl, boolean.class, boolean.class);
						setRotationMethod.invoke(ft, rotationfromInt.invoke(null, rotation), false, false);
					}
				}
			}
		} catch (Throwable tr) {
		}
	}
}
