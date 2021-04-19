package com.project.sharedshopping.Classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ShoppingList implements Parcelable {

    private String title;
    private String id;
    private String creatorID;
    private ArrayList<ShoppingItem> shoppingItems;
    private String sharedUsers;

    public ShoppingList(){

    }

    public ShoppingList(String title, String id, String creatorID, ArrayList<ShoppingItem> shoppingItems, String shared) {
        this.title = title;
        this.id= id;
        this.shoppingItems = shoppingItems;
        this.creatorID=creatorID;
        if(shared==null||shared=="")
            sharedUsers="";
        else
        this.sharedUsers=shared;
    }

    protected ShoppingList(Parcel in) {
        title = in.readString();
        id = in.readString();
        creatorID=in.readString();
        shoppingItems = in.createTypedArrayList(ShoppingItem.CREATOR);
        sharedUsers=in.readString();
    }

    public static final Creator<ShoppingList> CREATOR = new Creator<ShoppingList>() {
        @Override
        public ShoppingList createFromParcel(Parcel in) {
            return new ShoppingList(in);
        }

        @Override
        public ShoppingList[] newArray(int size) {
            return new ShoppingList[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<ShoppingItem> getShoppingItems() {
        return shoppingItems;
    }

    public void setShoppingItems(ArrayList<ShoppingItem> shoppingItems) {
        this.shoppingItems = shoppingItems;
    }

    public String getSharedUsers() {
        return sharedUsers;
    }

    public void setSharedUsers(String sharedUsers) {
        this.sharedUsers = sharedUsers;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(id);
        dest.writeString(creatorID);
        dest.writeTypedList(shoppingItems);
        dest.writeString(sharedUsers);
    }

    public String getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }

    public boolean addProduct(ShoppingItem newItem){
        if(shoppingItems!=null) {
            for (ShoppingItem item : shoppingItems) {
                if (item.getProduct().equals(newItem.getProduct()))
                    return false;
            }
        }
        else
            shoppingItems=new ArrayList<>();
        shoppingItems.add(new ShoppingItem(newItem.getProduct(), newItem.isSelected(), newItem.getAmount()));
        return true;
    }
}
