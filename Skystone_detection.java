package org.firstinspires.ftc.teamcode;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;

import com.vuforia.Image;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Bitmap.createScaledBitmap;
import android.graphics.Matrix;

public class Skystone_detection{

    VuforiaLocalizer vuforia;

    public Skystone_detection(VuforiaLocalizer vuforia) {
        this.vuforia = vuforia;
    }

    public enum skystonePos {
        LEFT, CENTER, RIGHT;
    }

    public String vuforiascan(boolean saveBitmaps, boolean red) {
        Image rgbImage = null;

        double yellowCountL = 1;
        double yellowCountC = 1;
        double yellowCountR = 1;

        double blackCountL = 1;
        double blackCountC = 1;
        double blackCountR = 1;
        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true);
        VuforiaLocalizer.CloseableFrame closeableFrame = null;
        this.vuforia.setFrameQueueCapacity(1);
        while (rgbImage == null) {
            try {
                closeableFrame = this.vuforia.getFrameQueue().take();
                long numImages = closeableFrame.getNumImages();

                for (int i = 0; i < numImages; i++) {
                    if (closeableFrame.getImage(i).getFormat() == PIXEL_FORMAT.RGB565) {
                        rgbImage = closeableFrame.getImage(i);
                        if (rgbImage != null) {
                            break;
                        }
                    }
                }
            } catch (InterruptedException exc) {

            } finally {
                if (closeableFrame != null) closeableFrame.close();
            }
        }

        if (rgbImage != null) {

            // copy the bitmap from the Vuforia frame
            Bitmap bitmap = Bitmap.createBitmap(rgbImage.getWidth(), rgbImage.getHeight(), Bitmap.Config.RGB_565);
            bitmap.copyPixelsFromBuffer(rgbImage.getPixels());

            String path = Environment.getExternalStorageDirectory().toString();
            FileOutputStream out = null;

            String bitmapName;
            String croppedBitmapName;

            if (red) {
                bitmapName = "BitmapRED.png";
                croppedBitmapName = "BitmapCroppedRED.png";
            } else {
                bitmapName = "BitmapBLUE.png";
                croppedBitmapName = "BitmapCroppedBLUE.png";
            }

            //Save bitmap to file
            if (saveBitmaps) {
                try {
                    File file = new File(path, bitmapName);
                    out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.flush();
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            //rotate the bitmap
            Matrix matrix = new Matrix();
            matrix.postRotate(90);

            bitmap = createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            int cropStartX;
            int cropStartY;
            int cropWidth;
            int cropHeight;

            if (red) {
                cropStartX = (int) ((20.0 / 480.0) * bitmap.getWidth());
                cropStartY = (int) ((315.0 / 720.0) * bitmap.getHeight());
                cropWidth = (int) ((440.0 / 480.0) * bitmap.getWidth());
                cropHeight = (int) ((35.0 / 720.0) * bitmap.getHeight());
            } else {
                cropStartX = (int) ((20.0 / 480.0) * bitmap.getWidth());
                cropStartY = (int) ((315.0 / 720.0) * bitmap.getHeight());
                cropWidth = (int) ((440.0 / 480.0) * bitmap.getWidth());
                cropHeight = (int) ((35.0 / 720.0) * bitmap.getHeight());
            }

            bitmap = createBitmap(bitmap, cropStartX, cropStartY, cropWidth, cropHeight); //Cropped Bitmap to show only stones

            // Save cropped bitmap to file
            if (saveBitmaps) {
                try {
                    File file = new File(path, croppedBitmapName);
                    out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.flush();
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            bitmap = createScaledBitmap(bitmap, 110, 20, true); //Compress bitmap to reduce scan time


            int height;
            int width;
            int pixel;
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            int colWidth = (int) ((double) bitmapWidth / 6.0);
            int colorLStartCol = (int) ((double) bitmapWidth * (0.75 / 6.0) - ((double) colWidth / 2.0));
            int colorCStartCol = (int) ((double) bitmapWidth * (2.5 / 6.0) - ((double) colWidth / 2.0));
            int colorRStartCol = (int) ((double) bitmapWidth * (5.0 / 6.0) - ((double) colWidth / 2.0));

            for (height = 0; height < bitmapHeight; ++height) {
                for (width = colorLStartCol; width < colorLStartCol + colWidth; ++width) {
                    pixel = bitmap.getPixel(width, height);
                    if (Color.red(pixel) < 200 || Color.green(pixel) < 200 || Color.blue(pixel) < 200) {
                        yellowCountL += Color.red(pixel);
                        blackCountL += Color.blue(pixel);
                    }
                }
                for (width = colorCStartCol; width < colorCStartCol + colWidth; ++width) {
                    pixel = bitmap.getPixel(width, height);

                    if (Color.red(pixel) < 200 || Color.green(pixel) < 200 || Color.blue(pixel) < 200) {
                        yellowCountC += Color.red(pixel);
                        blackCountC += Color.blue(pixel);
                    }
                }

                for (width = colorRStartCol; width < colorRStartCol + colWidth; ++width) {
                    pixel = bitmap.getPixel(width, height);

                    if (Color.red(pixel) < 200 || Color.green(pixel) < 200 || Color.blue(pixel) < 200) {
                        yellowCountR += Color.red(pixel);
                        blackCountR += Color.blue(pixel);
                    }
                }
            }
        }

        double blackYellowRatioL = blackCountL / yellowCountL;
        double blackYellowRatioC = blackCountC / yellowCountC;
        double blackYellowRatioR = blackCountR / yellowCountR;


        String pos;
        if (blackYellowRatioL > blackYellowRatioC && blackYellowRatioL > blackYellowRatioR) {
            pos = red ? "CENTER" : "RIGHT";
        } else if (blackYellowRatioC > blackYellowRatioL && blackYellowRatioC > blackYellowRatioR) {
            pos = red ? "LEFT" : "CENTER";
        } else {
            pos = red ? "RIGHT" : "LEFT";
        }


        return pos;
    }


}