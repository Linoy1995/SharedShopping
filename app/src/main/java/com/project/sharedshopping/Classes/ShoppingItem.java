package com.project.sharedshopping.Classes;

import android.os.Parcel;
import android.os.Parcelable;

//Parcelable- Interface for classes whose instances can be written to and restored from a Parcel
// It allows us to send lists of this class to activities
public class ShoppingItem implements Parcelable {
    private String product; //Includes amount
    private boolean selected;
    private String Amount;

    public ShoppingItem(){
        this.product="";
        this.selected=false;
        this.Amount = "0";
    }

    public ShoppingItem(String product, boolean selected, String amount){
        this.product=product;
        this.selected=selected;
        this.Amount = amount;
    }

    protected ShoppingItem(Parcel in) {
        product = in.readString();
        selected = in.readByte() != 0;
        Amount = in.readString();

    }

    public static final Creator<ShoppingItem> CREATOR = new Creator<ShoppingItem>() {
        @Override
        public ShoppingItem createFromParcel(Parcel in) {
            return new ShoppingItem(in);
        }

        @Override
        public ShoppingItem[] newArray(int size) {
            return new ShoppingItem[size];
        }
    };

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount) {
        Amount = amount;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(product);
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeString(Amount);

    }
}
