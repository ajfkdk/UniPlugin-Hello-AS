package cn.hzw.doodle.imagepicker;

/**
 * Created by Administrator on 2019-06-11.
 */

public class BeanImage {

    String filePath;
    boolean isChhoice;

    public BeanImage(String filePath, boolean isChhoice) {
        this.filePath = filePath;
        this.isChhoice = isChhoice;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isChhoice() {
        return isChhoice;
    }

    public void setChhoice(boolean chhoice) {
        isChhoice = chhoice;
    }
}
