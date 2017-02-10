package com.ldm.mediarecorder.model;

import java.io.File;
import java.io.Serializable;

/**
 * description： 作者：ldm 时间：20172017/2/9 09:40 邮箱：1786911211@qq.com
 */
public class FileBean implements Serializable {
    //文件
    private File file;
    //文件时长
    private int fileLength;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getFileLength() {
        return fileLength;
    }

    public void setFileLength(int fileLength) {
        this.fileLength = fileLength;
    }
}
