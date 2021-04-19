package com.project.sharedshopping.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.sharedshopping.Activities.MyListActivity;
import com.project.sharedshopping.Classes.ShoppingList;
import com.project.sharedshopping.CommonFunc;
import com.project.sharedshopping.R;

import java.util.ArrayList;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;


public class ListsAdapter extends RecyclerView.Adapter<ListsAdapter.viewHolder> implements CommonFunc {
    private Context context;
    private ArrayList<ShoppingList> lists;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth= FirebaseAuth.getInstance();
    private FirebaseUser user= firebaseAuth.getCurrentUser();
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    Intent intent;
    checkFragment fragmentLlistener;
    View view;
    public ListsAdapter(Context c, ArrayList<ShoppingList> list_items)
    {
        context=c;
        lists=list_items;
    }

    @NonNull
    @Override
    public ListsAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        view= LayoutInflater.from(context).inflate(R.layout.section_rows, viewGroup, false);
        return new ListsAdapter.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ListsAdapter.viewHolder viewHolder, int i) {
        final ShoppingList shoppingList = lists.get(i);
        //ttrdte
        choosedLangugage();
        sharedPref = this.context.getSharedPreferences("favoritesSP"+user.getEmail(), MODE_PRIVATE);
        int index=viewHolder.getAdapterPosition();
        final String currentTitle=lists.get(index).getTitle();    //the title of the wanted list
        final String currenId=lists.get(index).getId();    //the ID of the wanted list

        fragmentLlistener=(checkFragment) context;


        String value = sharedPref.getString(currenId, null);
        if (value == null) {
            // the list does not exist on favorites
            viewHolder.favorites.setImageResource(R.drawable.ic_not_favorite);
        } else {
            //if the user put on favorites
            if(value.contains(currentTitle))
                viewHolder.favorites.setImageResource(R.drawable.ic_favorite);
        }

        viewHolder.title.setText(lists.get(i).getTitle());

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Alert dialog before delete the list
                final AlertDialog.Builder builder= new AlertDialog.Builder(context);
                builder.setTitle("Warning").setMessage("You are about to delete the list");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Delete the list
                        final String name=viewHolder.title.getText().toString().trim();
                        lists.remove(viewHolder.getAdapterPosition());// remove from the array list
                        notifyItemRemoved(viewHolder.getAdapterPosition());
                        final CollectionReference itemsRef = db.collection("Shopping list");
                        Query query = itemsRef.whereEqualTo("creatorID", user.getUid()).whereEqualTo("title", viewHolder.title.getText().toString());
                        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    itemsRef.document(document.getId()).delete(); //Delete from Database
                                    String value = sharedPref.getString(shoppingList.getId(), null);  //check if the list is on sp
                                    if(value!=null) {
                                        if (value.equals(name)) { //if there is only list title- remove from favorites
                                            editor = sharedPref.edit();
                                            editor.remove(shoppingList.getId()).commit();
                                            editor.apply();
                                        }
                                        if (value.contains("," + viewHolder.title.getText()) || value.contains(viewHolder.title.getText() + ",")) { //if there is more than one title- need to check if contains
                                            editor = sharedPref.edit();
                                            if (value.contains("," + viewHolder.title.getText()))
                                                editor.putString(shoppingList.getId(), value.replaceAll("," + viewHolder.title.getText(), ""));
                                            if (value.contains(viewHolder.title.getText() + ","))
                                                editor.putString(shoppingList.getId(), value.replaceAll(viewHolder.title.getText() + ",", ""));
                                            editor.apply();
                                        }
                                    }
                                    Fragment f=fragmentLlistener.getFragment();
                                    fragmentLlistener.reload(f);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e( "Error getting documents", e.getMessage());
                            }
                        });
                    }

                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Dismiss the dialog automatically
                    }
                });
                builder.create().show(); //create and show the dialog
            }
        });



        viewHolder.favorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = sharedPref.getString(currenId, null);  //check if the list is on sp

                if (value == null) { //if the user would like to add the list to favorites
                    viewHolder.favorites.setImageResource(R.drawable.ic_favorite);
                    editor = sharedPref.edit();
                    editor.putString(currenId, currentTitle);
                    editor.apply();
                    Toast.makeText(context, "The list was added to your favourites", Toast.LENGTH_SHORT).show();
                }
                else if(value.equals(currentTitle)){
                    viewHolder.favorites.setImageResource(R.drawable.ic_not_favorite);
                    editor = sharedPref.edit();
                    editor.remove(currenId).commit();
                    editor.apply();
                    Fragment f=fragmentLlistener.getFragment();
                    if(f.toString().contains("FavoritesFragment")){
                        lists.remove(viewHolder.getAdapterPosition());// remove from the array list
                        notifyItemRemoved(viewHolder.getAdapterPosition());
                        fragmentLlistener.reload(f);
                    }
                    Toast.makeText(context, "The list was removed from your favourites", Toast.LENGTH_SHORT).show();
                }
            }
        });




        viewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent= new Intent(context, MyListActivity.class);

                //ArrayList<ShoppingItem> descriptions = shoppingList.getShoppingItems();
                //ShoppingList wantedList=new ShoppingList(viewHolder.title.getText().toString(),shoppingList.getId(),shoppingList.getCreatorID(),descriptions, shoppingList.getSharedUsers());
                intent.putExtra("list",shoppingList);      //pass the shopping list

                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder
    {
        TextView title;
        ImageView delete;
        ImageView favorites;
        RelativeLayout relativeLayout;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title_list);
            delete = itemView.findViewById(R.id.delete_list);
            favorites = itemView.findViewById(R.id.favorite_list);
            relativeLayout = itemView.findViewById(R.id.lists_RL);
        }
    }

    public ArrayList<ShoppingList> addList(ShoppingList list){
        lists.add(list);
        return lists;
    }

    public interface checkFragment{
        Fragment getFragment();
        void reload(Fragment fragment);
    }
    @Override
    public void choosedLangugage()  {
        sharedPref = this.context.getSharedPreferences("Language", MODE_PRIVATE);
        String language = sharedPref.getString("lang", null);
        if(language==null)
            language=Locale.getDefault().getDisplayLanguage();
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        view.setLayoutDirection(View.LAYOUT_DIRECTION_LOCALE);
    }
}
