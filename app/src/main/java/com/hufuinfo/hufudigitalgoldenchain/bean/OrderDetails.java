package com.hufuinfo.hufudigitalgoldenchain.bean;

public class OrderDetails {
    public String sellerId;
    public String buyerId;
    public int transactionType;
    public String userId;
    public int status;
    public String startTimeOne;
    public String endTimeOne;
    public int page;
    public int rows;
    public String sorte;
    public int order= -1;
    public String initiatorId;
    public int  userType;

    public OrderDetails() {
    }

    public OrderDetails(String sellerId, String buyerId, int transactionType, String userId,
                        int status, String startTimeOne, String endTimeOne, int page,
                        int rows, String sorte, int order, String initiatorId, int  userType) {
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.transactionType = transactionType;
        this.userId = userId;
        this.status = status;
        this.startTimeOne = startTimeOne;
        this.endTimeOne = endTimeOne;
        this.page = page;
        this.rows = rows;
        this.sorte = sorte;
        this.order = order;
        this.initiatorId = initiatorId;
        this.userType = userType;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public void setTransactionType(int transactionType) {
        this.transactionType = transactionType;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setStartTimeOne(String startTimeOne) {
        this.startTimeOne = startTimeOne;
    }

    public void setEndTimeOne(String endTimeOne) {
        this.endTimeOne = endTimeOne;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setRow(int rows) {
        this.rows = rows;
    }

    public void setSorte(String sorte) {
        this.sorte = sorte;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setInitiatorId(String initiatorId) {
        this.initiatorId = initiatorId;
    }

    public void setUserType(int  userType) {
        this.userType = userType;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public int getTransactionType() {
        return transactionType;
    }

    public String getUserId() {
        return userId;
    }

    public int getStatus() {
        return status;
    }

    public String getStartTimeOne() {
        return startTimeOne;
    }

    public String getEndTimeOne() {
        return endTimeOne;
    }

    public int getPage() {
        return page;
    }

    public int getRow() {
        return rows;
    }

    public String getSorte() {
        return sorte;
    }

    public int getOrder() {
        return order;
    }

    public String getInitiatorId() {
        return initiatorId;
    }

    public int getUserType() {
        return userType;
    }
}
