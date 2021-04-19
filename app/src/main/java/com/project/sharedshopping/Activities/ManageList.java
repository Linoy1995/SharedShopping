package com.project.sharedshopping.Activities;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.widget.ListAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.sharedshopping.Classes.ShoppingItem;
import com.project.sharedshopping.Classes.ShoppingList;
import com.project.sharedshopping.Adapters.AddEditListAdapter;
import com.project.sharedshopping.R;
import com.project.sharedshopping.Service.Receiver;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class ManageList extends AppCompatActivity {

//    private FirebaseAuth firebaseAuth;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private String title;
    private AddEditListAdapter addEditListAdapter;
    //private SharedPreferences sharedPref = getSharedPreferences("favoritesSP"+user.getEmail(), MODE_PRIVATE);
    private ListAdapter listAdapter;
    private SharedPreferences sharedPref;



    protected void saveList(final Map<String, Object> newList, final String title, final String docId) {

    }



    public ShoppingList getListDetails(QuerySnapshot queryDocumentSnapshots, String list_title, final String creatorID) {
        String[] split;
        String ID="";
        int i = 0;
        userConnection();
        //ShoppingList shoppingList;
        ArrayList<ShoppingItem>items= new ArrayList<>();
        String shareField="";
       // ChildRecyclerAdapter adapter;
        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
            String creatorField = doc.getString("creatorID");
            shareField = doc.getString("share");

            //finds all the products of a list
                if (creatorField.equals(user.getUid()) || (shareField != null && shareField.contains(user.getEmail()))) {
                    Map<String, Object> map = doc.getData();
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        if (entry.getKey().contains("description")) {
                            ID=doc.getString("ID");
                            String desc = doc.getString("description " + i);
                            split = desc.split(",");
                            if (desc.contains("checked")) {//desc can't be null because every document has a description
                                //desc = desc.replaceAll("checked", "");
                                items.add(new ShoppingItem(split[1], true, split[0]));
                            } else items.add(new ShoppingItem(split[1], false, split[0]));
                            i++;
                        }
                    }
                }
                i = 0;
        }
     //   shoppingList=new ShoppingList(list_title, creatorID, items);
        return new ShoppingList(list_title, ID, creatorID, items, shareField);
        //return new ChildRecyclerAdapter(context, shoppingList);
    }

    protected void userConnection(){
        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();
    }

    protected void choosedLangugage(int xmlValue) {
        sharedPref = getSharedPreferences("Language", MODE_PRIVATE);
        String language = sharedPref.getString("lang", null);
        if(language!=null) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
            config.setLayoutDirection(locale);
            this.setContentView(xmlValue);
        }
    }
}