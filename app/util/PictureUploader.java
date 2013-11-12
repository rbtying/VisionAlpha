package util;

import org.opencv.core.Mat;

public interface PictureUploader {
	public abstract String uploadImage(String fname);

	public abstract String uploadImage(Mat m);
}
