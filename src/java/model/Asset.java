package model;

import java.io.Serializable;

public class Asset implements Serializable {

    private static final long serialVersionUID = 1L;

    private int assetId;
    private String assetCode;
    private String assetName;
    private String assetCategory;
    private String assetStatus;
    private String serialNumber;
    private String description;
    private String imagePath; // Kept for model compatibility; not persisted to DB

    public Asset() {
        this.assetStatus = "AVAILABLE";
    }

    public Asset(int assetId, String assetCode, String assetName, String assetCategory,
                 String assetStatus, String serialNumber, String description) {
        this.assetId = assetId;
        this.assetCode = assetCode;
        this.assetName = assetName;
        this.assetCategory = assetCategory;
        this.assetStatus = assetStatus;
        this.serialNumber = serialNumber;
        this.description = description;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetCategory() {
        return assetCategory;
    }

    public void setAssetCategory(String assetCategory) {
        this.assetCategory = assetCategory;
    }

    public String getAssetStatus() {
        return assetStatus;
    }

    public void setAssetStatus(String assetStatus) {
        this.assetStatus = assetStatus;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}