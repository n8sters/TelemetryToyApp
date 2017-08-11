package com.example.natepowers.telemetrytoyapp;

/**
 * Created by:
 * ~~~~~~~~~_  __     __
 * ~~~~~~~~/ |/ ___ _/ /____
 * ~~~~~~~/    / _ `/ __/ -_)
 * ~~~~~~/_/|_/\_,_/\__/\__/
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * ~~~~~~~~~~~~~~~~~~~  at Copia PBC   ~~~~~~~
 */


class TelemetryPacket {


    /**
     * token : eyJhbGciOiJIUzI1NiJ9.eyJVU0lEIjoiOTFkNTI4NzhjMTgxYWRmNDY4OGU2ODA0ZThkODU0NTA2NzUzMmQ0MyIsInRzIjoxNTAwNTg0ODY4fQ.D5A9WaoA-D3B0XWUAlsFHBs0yRJdd5_5gS_1lcxS-WU
     * messageId : 557264d2-ee65-41a9-b3b5-83d205562431
     * payload : {"lat":1.1,"lng":1.1,"hAcc":1.1,"alt":1.1,"vAcc":1.1,"speed":1.1,"course":1.1,"batt":1.00001,"ts":98298723420209}
     * ts : 12345698723
     */

    private String token;
    private String messageId;
    private PayloadBean payload;
    private long ts;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public PayloadBean getPayload() {
        return payload;
    }

    public void setPayload(PayloadBean payload) {
        this.payload = payload;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public static class PayloadBean {
        /**
         * lat : 1.1
         * lng : 1.1
         * hAcc : 1.1
         * alt : 1.1
         * vAcc : 1.1
         * speed : 1.1
         * course : 1.1
         * batt : 1.00001
         * ts : 98298723420209
         */

        private double lat;
        private double lng;
        private double hAcc;
        private double alt;
        private double vAcc;
        private double speed;
        private double course;
        private double batt;
        private long ts;

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }

        public double getHAcc() {
            return hAcc;
        }

        public void setHAcc(double hAcc) {
            this.hAcc = hAcc;
        }

        public double getAlt() {
            return alt;
        }

        public void setAlt(double alt) {
            this.alt = alt;
        }

        public double getVAcc() {
            return vAcc;
        }

        public void setVAcc(double vAcc) {
            this.vAcc = vAcc;
        }

        public double getSpeed() {
            return speed;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }

        public double getCourse() {
            return course;
        }

        public void setCourse(double course) {
            this.course = course;
        }

        public double getBatt() {
            return batt;
        }

        public void setBatt(double batt) {
            this.batt = batt;
        }

        public long getTs() {
            return ts;
        }

        public void setTs(long ts) {
            this.ts = ts;
        }
    }
}

