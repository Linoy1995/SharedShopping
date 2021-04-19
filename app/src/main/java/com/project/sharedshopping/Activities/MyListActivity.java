package com.project.sharedshopping.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.project.sharedshopping.Classes.ShoppingItem;
import com.project.sharedshopping.Classes.ShoppingList;
import com.project.sharedshopping.Adapters.ChildRecyclerAdapter;
import com.project.sharedshopping.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class MyListActivity extends ManageList {

    private Toolbar toolbar;
   // private ArrayList<ShoppingItem> items;
    private ChildRecyclerAdapter adapter;
    private RecyclerView recyclerView;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private ShoppingList shoppingList;
    private String list_title;
    private String creator_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);
        super.choosedLangugage(R.layout.activity_my_list);

        //My list toolbar
        toolbar = findViewById(R.id.mylist_toolbar);
        setSupportActionBar(toolbar);
        updateDetails();

        //back arrow implementation
        toolbar.setNavigationIcon(getDrawable(R.drawable.ic_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              onBackPressed();
            }
        });

        // Firebase initialization
        db= FirebaseFirestore.getInstance();
        firebaseAuth= FirebaseAuth.getInstance();
        user= firebaseAuth.getCurrentUser();


        // Show Recycler View
        recyclerView= findViewById(R.id.MyListRV);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //shoppingList=new ShoppingList(list_title, creator_id, items);
        adapter = new ChildRecyclerAdapter(this, shoppingList);
        recyclerView.setAdapter(adapter);

    }

    protected void updateDetails(){
    //Get the items from List adapter
    shoppingList= getIntent().getParcelableExtra("list");
    list_title= shoppingList.getTitle();
    creator_id= shoppingList.getCreatorID();
    Objects.requireNonNull(getSupportActionBar()).setTitle(list_title); //Update the toolbar if needed
}

    // Show the updated list
    @Override
    protected void onRestart() {
        super.onRestart();

        final CollectionReference itemsRef = db.collection("Shopping list");
        Query query;
        updateDetails();

        query = itemsRef.whereEqualTo("title", shoppingList.getTitle());
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int i=0;
                ShoppingList shoppingList=getListDetails(queryDocumentSnapshots, list_title, creator_id);
                adapter = new ChildRecyclerAdapter(MyListActivity.this, shoppingList);
                recyclerView.setAdapter(adapter);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Show lists","Error displaying lists: "+e.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.edit_list:
                Intent intent = new Intent(getApplicationContext(), EditListActivity.class);
                intent.putExtra("ShoppingList", shoppingList);
                startActivity(intent);
                break;

            case R.id.selectAll:
                //UnCheck all the checkboxes
                if(shoppingList!=null) {
                    for (int i = 0; i < shoppingList.getShoppingItems().size(); i++) {
                        RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
                        if (viewHolder != null) {
                            CheckBox checkBox = viewHolder.itemView.findViewById(R.id.checked); //The current checkbox of the item
                            checkBox.setChecked(false);
                        }
                    }
                }
                break;

            case R.id.share_list:
                final CollectionReference itemsRef = db.collection("Shopping list");
                final Query query = itemsRef.whereEqualTo("ID", shoppingList.getId());
                shareList(query, itemsRef);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareList(final Query query, CollectionReference itemsRef){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.share_dialog, null);
        final EditText email = view.findViewById(R.id.share_email);
        final TextView sharedEmails= view.findViewById(R.id.shared_emails);

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$"; //email validation

        final Pattern pat = Pattern.compile(emailRegex);

        // Show shared emails with this list
        itemsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    if (documentSnapshot.getString("share") != null)
                        sharedEmails.setText("Shared emails: "+documentSnapshot.getString("share"));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Error getting documents", e.getMessage());
            }
        });


        builder.setView(view).setTitle("Share the list");
        builder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Check if the email is correct or is not empty
                if(!pat.matcher(email.getText().toString()).matches() || email.getText().toString().isEmpty())
                {
                    Toast.makeText(MyListActivity.this, "Please check the email", Toast.LENGTH_LONG).show();
                    return;
                }


                //Check if the email is registered to the app
                firebaseAuth.fetchSignInMethodsForEmail(email.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                boolean isEmpty = task.getResult().getSignInMethods().isEmpty();
                                if (isEmpty)
                                    Toast.makeText(MyListActivity.this, "This email is not registered to the app", Toast.LENGTH_LONG).show();
                                else {
                                    query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            Map<String, Object> list = new HashMap<>();
                                            WriteBatch batch = db.batch(); // Write a new data to your existing document

                                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                //if ((documentSnapshot.getString("share") != null && !documentSnapshot.getString("share").contains(user.getEmail()))) {

                                                if (documentSnapshot.getString("share") == null) {
                                                    //There is no share field -> create and add the email
                                                    list.put("share", email.getText().toString());
                                                    batch.update(documentSnapshot.getReference(), list);
                                                    Toast.makeText(MyListActivity.this, "The list was shared with " + email.getText().toString(), Toast.LENGTH_LONG).show();
                                                }
                                                    // Check if the given email is already shared with this list
                                                    else if (documentSnapshot.getString("share").contains(","+email.getText().toString())||documentSnapshot.getString("share").contains(","+email.getText().toString()+",")||
                                                            documentSnapshot.getString("share").contains(email.getText().toString()+","))
                                                            Toast.makeText(MyListActivity.this, "The user is already shared in the list", Toast.LENGTH_LONG).show();

                                                        // Check if the given email belongs to the user
                                                    else if (documentSnapshot.getString("share").contains(user.getEmail()))
                                                        Toast.makeText(MyListActivity.this, "You cannot share the list with yourself", Toast.LENGTH_LONG).show();

                                                        //Add another email to the list
                                                    else {
                                                        list.put("share", documentSnapshot.getString("share") + "," + email.getText().toString());
                                                        db.collection("Shopping list").document(documentSnapshot.getId()).set(list, SetOptions.merge());
                                                        Toast.makeText(MyListActivity.this, "The list was shared with " + email.getText().toString(), Toast.LENGTH_LONG).show();
                                                    }


                                            }
                                            batch.commit(); //execute
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("Error getting documents", e.getMessage());
                                        }
                                    });

                                }

                            }
                        });
            }
        }).setNegativeButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Check if the email is correct or is not empty
                if(!pat.matcher(email.getText().toString()).matches() || email.getText().toString().isEmpty())
                {
                    Toast.makeText(MyListActivity.this, "Please check the email", Toast.LENGTH_LONG).show();
                    return;
                }

                //Check if the email is shared with this list
                if(!sharedEmails.getText().toString().contains(email.getText().toString()))
                {
                    Toast.makeText(MyListActivity.this, "This email is not shared with this list", Toast.LENGTH_LONG).show();
                    return;
                }


                query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Map<String, Object> list = new HashMap<>();
                        WriteBatch batch = db.batch(); // Write a new data to your existing document

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if(documentSnapshot.getString("ID").equals(shoppingList.getId()) ||
                                    (documentSnapshot.getString("share")!=null && documentSnapshot.getString("share").contains(user.getEmail())))
                            {
                                //Check if the list was shared with only one user
                                if (documentSnapshot.getString("share") != null && !documentSnapshot.getString("share").contains(",")) {
                                    list.put("share", FieldValue.delete());
                                    batch.update(documentSnapshot.getReference(), list);
                                    Toast.makeText(MyListActivity.this, "The list is no longer shared with " + email.getText().toString(), Toast.LENGTH_SHORT).show();
                                }
                                //The list was shared with some users
                                if(documentSnapshot.getString("share") != null && documentSnapshot.getString("share").contains(",")){
                                    String[] arr= sharedEmails.getText().toString().split(",");
                                    arr[0]=arr[0].replaceAll("Shared emails: ","");
                                    int position=0;
                                    for(int i=0; i<arr.length; i++){
                                        if(arr[i].contains(email.getText().toString())){
                                            position=i;
                                            break;
                                        }
                                    }
                                    Map<String, Object> map = documentSnapshot.getData();
                                    for (Map.Entry<String, Object> entry : map.entrySet()){
                                        if(entry.getKey().contains("share")){
                                            String current=entry.getValue().toString();
                                            if (position==0)
                                                current= current.replaceAll(email.getText().toString()+",","").trim();
                                            else
                                                current= current.replaceAll(","+email.getText().toString(),"").trim();
                                            list.put(entry.getKey(),current);
                                            db.collection("Shopping list").document(documentSnapshot.getId()).set(list, SetOptions.merge());
                                            Toast.makeText(MyListActivity.this, "The list is no longer shared with " + email.getText().toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }

                        }
                        batch.commit(); //execute
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error getting documents", e.getMessage());
                    }
                });

            }
        });
        builder.create().show();

    }

}