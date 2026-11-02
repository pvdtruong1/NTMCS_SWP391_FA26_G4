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
public class Desk implements Serializable{
    private int deskId;
    private String deskName;

    public Desk() {
    }

    public Desk(int deskId, String deskName) {
        this.deskId = deskId;
        this.deskName = deskName;
    }

    public Desk(String deskName) {
        this.deskName = deskName;
    }

    public int getDeskId() {
        return deskId;
    }

    public void setDeskId(int deskId) {
        this.deskId = deskId;
    }

    public String getDeskName() {
        return deskName;
    }

    public void setDeskName(String deskName) {
        this.deskName = deskName;
    }

    @Override
    public String toString() {
        return "Desk{" +
                "deskId=" + deskId +
                ", deskName='" + deskName + '\'' +
                '}';
    }
}
