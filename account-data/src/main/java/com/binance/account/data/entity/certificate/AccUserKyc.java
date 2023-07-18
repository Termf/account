package com.binance.account.data.entity.certificate;

import java.util.Date;

public class AccUserKyc {
    private Long id;

    private Long userId;

    private Byte status;

    private Date createTime;

    private Date updateTime;

    private String jumioId;

    private String scanReference;

    private String front;

    private String back;

    private String face;

    private String fillFirstName;

    private String fillMiddleName;

    private String fillLastName;

    private Date fillDob;

    private String fillAddress;

    private String fillPostalCode;

    private String fillCity;

    private String fillCountry;

    private String formerFirstName;

    private String formerMiddleName;

    private String formerLastName;

    private String nationality;

    private String checkFirstName;

    private String checkLastName;

    private String checkDob;

    private String checkAddress;

    private String checkPostalCode;

    private String checkCity;

    private String checkIssuingCountry;

    private String checkExpiryDate;

    private String checkNumber;

    private String checkType;

    private String checkStatus;

    private String checkSource;

    private String failReason;

    private String memo;

    private String faceStatus;

    private String faceRemark;

    private String transFaceLogId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getJumioId() {
        return jumioId;
    }

    public void setJumioId(String jumioId) {
        this.jumioId = jumioId == null ? null : jumioId.trim();
    }

    public String getScanReference() {
        return scanReference;
    }

    public void setScanReference(String scanReference) {
        this.scanReference = scanReference == null ? null : scanReference.trim();
    }

    public String getFront() {
        return front;
    }

    public void setFront(String front) {
        this.front = front == null ? null : front.trim();
    }

    public String getBack() {
        return back;
    }

    public void setBack(String back) {
        this.back = back == null ? null : back.trim();
    }

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face == null ? null : face.trim();
    }

    public String getFillFirstName() {
        return fillFirstName;
    }

    public void setFillFirstName(String fillFirstName) {
        this.fillFirstName = fillFirstName == null ? null : fillFirstName.trim();
    }

    public String getFillMiddleName() {
        return fillMiddleName;
    }

    public void setFillMiddleName(String fillMiddleName) {
        this.fillMiddleName = fillMiddleName == null ? null : fillMiddleName.trim();
    }

    public String getFillLastName() {
        return fillLastName;
    }

    public void setFillLastName(String fillLastName) {
        this.fillLastName = fillLastName == null ? null : fillLastName.trim();
    }

    public Date getFillDob() {
        return fillDob;
    }

    public void setFillDob(Date fillDob) {
        this.fillDob = fillDob;
    }

    public String getFillAddress() {
        return fillAddress;
    }

    public void setFillAddress(String fillAddress) {
        this.fillAddress = fillAddress == null ? null : fillAddress.trim();
    }

    public String getFillPostalCode() {
        return fillPostalCode;
    }

    public void setFillPostalCode(String fillPostalCode) {
        this.fillPostalCode = fillPostalCode == null ? null : fillPostalCode.trim();
    }

    public String getFillCity() {
        return fillCity;
    }

    public void setFillCity(String fillCity) {
        this.fillCity = fillCity == null ? null : fillCity.trim();
    }

    public String getFillCountry() {
        return fillCountry;
    }

    public void setFillCountry(String fillCountry) {
        this.fillCountry = fillCountry == null ? null : fillCountry.trim();
    }

    public String getFormerFirstName() {
        return formerFirstName;
    }

    public void setFormerFirstName(String formerFirstName) {
        this.formerFirstName = formerFirstName == null ? null : formerFirstName.trim();
    }

    public String getFormerMiddleName() {
        return formerMiddleName;
    }

    public void setFormerMiddleName(String formerMiddleName) {
        this.formerMiddleName = formerMiddleName == null ? null : formerMiddleName.trim();
    }

    public String getFormerLastName() {
        return formerLastName;
    }

    public void setFormerLastName(String formerLastName) {
        this.formerLastName = formerLastName == null ? null : formerLastName.trim();
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality == null ? null : nationality.trim();
    }

    public String getCheckFirstName() {
        return checkFirstName;
    }

    public void setCheckFirstName(String checkFirstName) {
        this.checkFirstName = checkFirstName == null ? null : checkFirstName.trim();
    }

    public String getCheckLastName() {
        return checkLastName;
    }

    public void setCheckLastName(String checkLastName) {
        this.checkLastName = checkLastName == null ? null : checkLastName.trim();
    }

    public String getCheckDob() {
        return checkDob;
    }

    public void setCheckDob(String checkDob) {
        this.checkDob = checkDob == null ? null : checkDob.trim();
    }

    public String getCheckAddress() {
        return checkAddress;
    }

    public void setCheckAddress(String checkAddress) {
        this.checkAddress = checkAddress == null ? null : checkAddress.trim();
    }

    public String getCheckPostalCode() {
        return checkPostalCode;
    }

    public void setCheckPostalCode(String checkPostalCode) {
        this.checkPostalCode = checkPostalCode == null ? null : checkPostalCode.trim();
    }

    public String getCheckCity() {
        return checkCity;
    }

    public void setCheckCity(String checkCity) {
        this.checkCity = checkCity == null ? null : checkCity.trim();
    }

    public String getCheckIssuingCountry() {
        return checkIssuingCountry;
    }

    public void setCheckIssuingCountry(String checkIssuingCountry) {
        this.checkIssuingCountry = checkIssuingCountry == null ? null : checkIssuingCountry.trim();
    }

    public String getCheckExpiryDate() {
        return checkExpiryDate;
    }

    public void setCheckExpiryDate(String checkExpiryDate) {
        this.checkExpiryDate = checkExpiryDate == null ? null : checkExpiryDate.trim();
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber == null ? null : checkNumber.trim();
    }

    public String getCheckType() {
        return checkType;
    }

    public void setCheckType(String checkType) {
        this.checkType = checkType == null ? null : checkType.trim();
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus == null ? null : checkStatus.trim();
    }

    public String getCheckSource() {
        return checkSource;
    }

    public void setCheckSource(String checkSource) {
        this.checkSource = checkSource == null ? null : checkSource.trim();
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason == null ? null : failReason.trim();
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }

    public String getFaceStatus() {
        return faceStatus;
    }

    public void setFaceStatus(String faceStatus) {
        this.faceStatus = faceStatus == null ? null : faceStatus.trim();
    }

    public String getFaceRemark() {
        return faceRemark;
    }

    public void setFaceRemark(String faceRemark) {
        this.faceRemark = faceRemark == null ? null : faceRemark.trim();
    }

    public String getTransFaceLogId() {
        return transFaceLogId;
    }

    public void setTransFaceLogId(String transFaceLogId) {
        this.transFaceLogId = transFaceLogId == null ? null : transFaceLogId.trim();
    }
}