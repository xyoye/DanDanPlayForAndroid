package com.xyoye.dandanplay.utils.jlibtorrent;

/**
 * Created by xyoye on 2019/6/13.
 */

public class BtFilePrices {
    private boolean[] prices;
    private int startIndex;
    private int endIndex;

    public BtFilePrices(int startIndex, int endIndex){
        if (startIndex > endIndex)
            prices = new boolean[0];
        prices = new boolean[endIndex - startIndex];
    }

    public void setPriceDownloaded(int index){
        if (index < 0 || index > prices.length)
            return;
        prices[index] = true;
    }

    public boolean isPriceDownload(int index){
        if (index < 0 || index > prices.length)
            return false;
        return prices[index];
    }

    public void setDownloadOver(){
        for (int i=0; i<prices.length; i++){
            prices[i] = true;
        }
    }
}
