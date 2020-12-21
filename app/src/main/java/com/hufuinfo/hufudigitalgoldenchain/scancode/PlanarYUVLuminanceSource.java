/*
 * Copyright 2009 ZXing authors
 * Download by http://www.codefans.net
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hufuinfo.hufudigitalgoldenchain.scancode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.zxing.LuminanceSource;

import java.io.FileNotFoundException;

/**
 * This object extends LuminanceSource around an array of YUV data returned from the camera driver,
 * with the option to crop to a rectangle within the full data. This can be used to exclude
 * superfluous pixels around the perimeter and speed up decoding.
 *
 * It works for any pixel format where the Y channel is planar and appears first, including
 * YCbCr_420_SP and YCbCr_422_SP.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class PlanarYUVLuminanceSource extends LuminanceSource {
  private final byte[] yuvData;
  private final int dataWidth;
  private final int dataHeight;
  private final int left;
  private final int top;

  public PlanarYUVLuminanceSource(byte[] yuvData, int dataWidth, int dataHeight, int left, int top,
      int width, int height) {
    super(width, height);

    if (left + width > dataWidth || top + height > dataHeight) {
      throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
    }

    this.yuvData = yuvData;
    this.dataWidth = dataWidth;
    this.dataHeight = dataHeight;
    this.left = left;
    this.top = top;
  }

    public PlanarYUVLuminanceSource(Bitmap bitmap) {
        super(bitmap.getWidth(), bitmap.getHeight());
        //set width and heigt
        this.dataWidth = bitmap.getWidth();
        this.dataHeight = bitmap.getHeight();
        this.left = 0;
        this.top = 0;
        int[] pixels = new int[dataWidth * dataHeight];
        bitmap.getPixels(pixels, 0, dataWidth, 0, 0, dataWidth, dataHeight);
        yuvData = new byte[dataWidth * dataHeight];
        for (int y = 0; y < dataHeight; y++) {
            int offset = y * dataWidth;
            for (int x = 0; x < dataWidth; x++) {
                int pixel = pixels[x + offset];
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;
                if (r == g && g == b) {
                    yuvData[offset + x] = (byte) r;
                } else {
                    yuvData[offset + x] = (byte) ((r + g + g + b) >> 2);
                }
            }
        }
    }

    public PlanarYUVLuminanceSource(String imagePath) throws FileNotFoundException {
        this(loadBitmap(imagePath));
    }

    private static Bitmap loadBitmap(String path) throws FileNotFoundException {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (bitmap == null) {
            throw new FileNotFoundException("Couldn't open" + path);
        }
        return bitmap;
    }

    @Override
    public byte[] getRow(int y, byte[] row) {
        if (y < 0 || y >= getHeight()) {
            throw new IllegalArgumentException("Requested row is outside the image: " + y);
        }
        int width = getWidth();
        if (row == null || row.length < width) {
            row = new byte[width];
        }
        int offset = (y + top) * dataWidth + left;
        System.arraycopy(yuvData, offset, row, 0, width);
        return row;
    }

  @Override
  public byte[] getMatrix() {
    int width = getWidth();
    int height = getHeight();

    // If the caller asks for the entire underlying image, save the copy and give them the
    // original data. The docs specifically warn that result.length must be ignored.
    if (width == dataWidth && height == dataHeight) {
      return yuvData;
    }

    int area = width * height;
    byte[] matrix = new byte[area];
    int inputOffset = top * dataWidth + left;

    // If the width matches the full width of the underlying data, perform a single copy.
    if (width == dataWidth) {
      System.arraycopy(yuvData, inputOffset, matrix, 0, area);
      return matrix;
    }

    // Otherwise copy one cropped row at a time.
    byte[] yuv = yuvData;
    for (int y = 0; y < height; y++) {
      int outputOffset = y * width;
      System.arraycopy(yuv, inputOffset, matrix, outputOffset, width);
      inputOffset += dataWidth;
    }
    return matrix;
  }

  @Override
  public boolean isCropSupported() {
    return true;
  }

  public int getDataWidth() {
    return dataWidth;
  }

  public int getDataHeight() {
    return dataHeight;
  }

  public Bitmap renderCroppedGreyscaleBitmap() {
    int width = getWidth();
    int height = getHeight();
    int[] pixels = new int[width * height];
    byte[] yuv = yuvData;
    int inputOffset = top * dataWidth + left;

    for (int y = 0; y < height; y++) {
      int outputOffset = y * width;
      for (int x = 0; x < width; x++) {
        int grey = yuv[inputOffset + x] & 0xff;
        pixels[outputOffset + x] = 0xFF000000 | (grey * 0x00010101);
      }
      inputOffset += dataWidth;
    }

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
    return bitmap;
  }
}
