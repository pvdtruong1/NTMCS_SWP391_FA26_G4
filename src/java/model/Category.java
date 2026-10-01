/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.io.Serializable;

/**
 *
 * @author locpx
 */
public class Category implements Serializable{
    private int categoryId;
    private String categoryName;
    private Integer parentCategoryId;
    private int deskId;

    public Category() {
    }

    public Category(int categoryId, String categoryName,
                    Integer parentCategoryId, int deskId) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.parentCategoryId = parentCategoryId;
        this.deskId = deskId;
    }

    public Category(String categoryName,
                    Integer parentCategoryId,
                    int deskId) {
        this.categoryName = categoryName;
        this.parentCategoryId = parentCategoryId;
        this.deskId = deskId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(Integer parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public int getDeskId() {
        return deskId;
    }

    public void setDeskId(int deskId) {
        this.deskId = deskId;
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", parentCategoryId=" + parentCategoryId +
                ", deskId=" + deskId +
                '}';
    }
}
