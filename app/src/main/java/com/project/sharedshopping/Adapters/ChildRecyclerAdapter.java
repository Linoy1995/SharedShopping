package com.project.sharedshopping.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.project.sharedshopping.Classes.ShoppingList;
import com.project.sharedshopping.CommonFunc;
import com.project.sharedshopping.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;


public class ChildRecyclerAdapter extends RecyclerView.Adapter<ChildRecyclerAdapter.ViewHolder> implements CommonFunc {

    private Context context;
  //  private ArrayList<ShoppingItem> items;
    private String title;
    private String creatorId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth= FirebaseAuth.getInstance();
    private FirebaseUser user= firebaseAuth.getCurrentUser();
    ShoppingList shoppingList;
    private View view;
    public ChildRecyclerAdapter(Context context, ShoppingList list) {
        this.context=context;
        this.shoppingList=list;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        choosedLangugage();
        holder.amountTextView.setText(shoppingList.getShoppingItems().get(position).getAmount()+" ");
        holder.nameTextView.setText(shoppingList.getShoppingItems().get(position).getProduct().replaceAll("checked",""));
        holder.checkBox.setChecked(shoppingList.getShoppingItems().get(position).isSelected()); // Checks the product if needed

        if(holder.checkBox.isChecked()) //If the product is checked
        {
            holder.amountTextView.setPaintFlags(holder.amountTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // Add a line through the product
            holder.nameTextView.setPaintFlags(holder.nameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // Add a line through the product
        }

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final CollectionReference itemsRef = db.collection("Shopping list");
                title=shoppingList.getTitle();
                Query query = itemsRef.whereEqualTo("ID",shoppingList.getId());
                if(isChecked) //add "checked" to the item in DB
                    checkItem(holder,query);
                else //if the user uncheck, remove "checked" from the item
                   unCheckItem(holder,query);
            }
        });

    }

    @Override
    public int getItemCount() {
        return shoppingList.getShoppingItems().size();
    }

    private void checkItem(final ViewHolder holder, Query query){
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                String itemDescription;
                for(QueryDocumentSnapshot doc: queryDocumentSnapshots){
                    {
                        Map<String, Object> map = doc.getData();
                        Map<String, Object> list = new HashMap<>();
                        for (Map.Entry<String, Object> entry : map.entrySet()){
                            itemDescription=holder.amountTextView.getText().toString().trim()+","+holder.nameTextView.getText().toString().trim();
                            if(entry.getValue().toString().trim().equals(itemDescription)){
                                list.put(entry.getKey(),itemDescription+",checked");
                                db.collection("Shopping list").document(doc.getId()).set(list, SetOptions.merge());
                                holder.amountTextView.setPaintFlags(holder.amountTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // Add a line through the product
                                holder.nameTextView.setPaintFlags(holder.nameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // Add a line through the product
                                holder.checkBox.setChecked(true);
                                break;
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Show lists","Error displaying lists: "+e.getMessage());
            }
        });
    }

    private void unCheckItem(final ViewHolder holder, Query query){
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot doc: queryDocumentSnapshots){
                    if(doc.getId().equals(shoppingList.getId()))
                    {
                        Map<String, Object> map = doc.getData();
                        Map<String, Object> list = new HashMap<>();
                        String itemDescription;
                        for (Map.Entry<String, Object> entry : map.entrySet()){
                            if(entry.getValue().toString().contains(",checked")){
                                String current=entry.getValue().toString();
                                current= current.replaceAll(",checked","").trim();
                                itemDescription=holder.amountTextView.getText().toString().trim()+","+holder.nameTextView.getText().toString().replaceAll(",checked","").trim();
                                if(current.equals(itemDescription)){
                                    list.put(entry.getKey(),current);
                                    db.collection("Shopping list").document(doc.getId()).set(list, SetOptions.merge());
                                    holder.amountTextView.setPaintFlags(0); //Remove the line through the product
                                    holder.nameTextView.setPaintFlags(0); //Remove the line through the product
                                    holder.checkBox.setChecked((false));
                                    break;

                                }

                            }
                        }
                    }


                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Show lists","Error displaying lists: "+e.getMessage());
            }
        });
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout item;
        TextView nameTextView;
        TextView amountTextView;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item=itemView.findViewById(R.id.item);
            nameTextView = itemView.findViewById(R.id.productNameText);
            amountTextView = itemView.findViewById(R.id.productAmountText);
            checkBox= itemView.findViewById(R.id.checked);
        }
    }

    @Override
    public void choosedLangugage() {
        Locale lang;
        SharedPreferences sharedPref = this.context.getSharedPreferences("Language", MODE_PRIVATE);
        String language = sharedPref.getString("lang", null);
        if(language==null)
            language=Locale.getDefault().getDisplayLanguage();
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        view.setLayoutDirection(View.LAYOUT_DIRECTION_LOCALE);
    }

}
