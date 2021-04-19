package com.project.sharedshopping.Classes;

import android.os.Parcel;
import android.os.Parcelable;

//Parcelable- Interface for classes whose instances can be written to and restored from a Parcel
// It allows us to send lists of this class to activities
public class Add_List_Item implements Parcelable {

    private String product;
    private String Amount;

    public Add_List_Item(){

    }

    public Add_List_Item(String product, String amount) {
        this.product = product;
        Amount = amount;
    }

    protected Add_List_Item(Parcel in) {
        product = in.readString();
        Amount = in.readString();
    }

    public static final Creator<Add_List_Item> CREATOR = new Creator<Add_List_Item>() {
        @Override
        public Add_List_Item createFromParcel(Parcel in) {
            return new Add_List_Item(in);
        }

        @Override
        public Add_List_Item[] newArray(int size) {
            return new Add_List_Item[size];
        }
    };

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount) {
        Amount = amount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(product);
        dest.writeString(Amount);
    }
}
