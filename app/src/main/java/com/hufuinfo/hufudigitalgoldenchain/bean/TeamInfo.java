package com.hufuinfo.hufudigitalgoldenchain.bean;

import java.util.List;

public class TeamInfo {
    /**
     * 响应码
     */
    public boolean success;
    /**
     * 有效返回数据
     */
    public TeamData data;
    /**
     * 异常说明原因
     */
    public String msg;


    /**
     *
     */
    public static class TeamData {
        /**
         * 团队总人数
         */
        public int memberAll;
        /**
         * 我的投资金额
         */
        public double myInput;
        /**
         * 我的团队总收益
         */
        public double proceeds;
        /**
         * 我的团队直接联系人
         */
        public List<Subor> subor;

    }

    public static class Subor {
        public int id;
        /**
         * 直系团队联系人
         */
        public String userId;
        /**
         * 每个团队总人数
         */
        public int fId;
        /**
         * 每个团队的收益总和
         */
        public double stockRight;
    }
}
