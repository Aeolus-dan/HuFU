package com.hufuinfo.hufudigitalgoldenchain.bean;

public class ProductInfo {
    private Integer imageUrl;
    //产品名称
    private String productName;

    public ProductInfo() {

    }

    public Integer getImageUrl() {
        return imageUrl;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setImageUrl(Integer imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getProductName() {
        return productName;
    }
}
