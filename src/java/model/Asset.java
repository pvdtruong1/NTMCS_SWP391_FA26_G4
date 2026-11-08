package model;

import java.io.Serializable;

/**
 * Entity representing an Asset in the newsroom.
 * Mapped to table [Asset].
 */
public class Asset implements Serializable {
    private static final long serialVersionUID = 1L;

    private int assetId;
    private String assetCode;
    private String assetName;
    private String assetCategory;
    private String assetStatus = "AVAILABLE";
    private String serialNumber;
    private String description;
    private String imagePath; // View helper field

    public Asset() {
        this.assetStatus = "AVAILABLE";
    }

    public Asset(int assetId, String assetName, String assetStatus, String assetCode, 
                 String assetCategory, String serialNumber, String description) {
        this.assetId = assetId;
        this.assetName = assetName;
        this.assetStatus = assetStatus;
        this.assetCode = assetCode;
        this.assetCategory = assetCategory;
        this.serialNumber = serialNumber;
        this.description = description;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetStatus() {
        return assetStatus;
    }

    public void setAssetStatus(String assetStatus) {
        this.assetStatus = assetStatus;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public String getAssetCategory() {
        return assetCategory;
    }

    public void setAssetCategory(String assetCategory) {
        this.assetCategory = assetCategory;
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
