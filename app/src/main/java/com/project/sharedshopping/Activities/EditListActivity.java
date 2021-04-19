package com.project.sharedshopping.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPicker;
import com.project.sharedshopping.Adapters.AddEditListAdapter;
import com.project.sharedshopping.Classes.DividerItemDecorator;
import com.project.sharedshopping.Classes.ShoppingItem;
import com.project.sharedshopping.Classes.ShoppingList;
import com.project.sharedshopping.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class EditListActivity extends ManageList{

    private Toolbar toolbar;
    private EditText title;
    private ScrollableNumberPicker numberPicker;
    private EditText product;
    private ProgressBar progressBar;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    boolean isExist = false;
    private RecyclerView mRecyclerView;
    private ShoppingList newList;
    private AddEditListAdapter addEditListAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private ShoppingList originalShoppingList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_list);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        // views initialization
        title = findViewById(R.id.titleEt);
        product = findViewById(R.id.productET);
        numberPicker = findViewById(R.id.productAmount);
        progressBar = findViewById(R.id.saveListPB);

        originalShoppingList=new ShoppingList();
        originalShoppingList= getIntent().getExtras().getParcelable("ShoppingList");
        title.setText(originalShoppingList.getTitle());

        toolbar = findViewById(R.id.addListToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.Add_List);  //toolbar title
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send the toolbar title to MyListActivity
                //this sp save the the updated name of the list
                onBackPressed();
            }
        });

        //back arrow implementation
        toolbar.setNavigationIcon(getDrawable(R.drawable.ic_back));     //set icon of back option
        sharedPref = getSharedPreferences("favoritesSP"+user.getEmail(), MODE_PRIVATE);

        mRecyclerView = findViewById(R.id.currentListRV);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        getList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        // show all menu icons to edit existing list
        inflater.inflate(R.menu.edit_list_menu, menu);
        String value = sharedPref.getString(newList.getId(), null);
        if (value == null) {
            // the list does not exist on favorites
            menu.findItem(R.id.favorite_list).setIcon(R.drawable.ic_not_favorite);
        } else {
            //if the user put on favorites
            if (value.contains(title.getText().toString().trim()))
                menu.findItem(R.id.favorite_list).setIcon(R.drawable.ic_favorite);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {

        final CollectionReference itemsRef = db.collection("Shopping list");
        final String list_title = title.getText().toString();
        final Query query = itemsRef.whereEqualTo("title", list_title);
        switch (item.getItemId()) {
            case R.id.share_list:
                shareList(query, itemsRef);
                break;
            case R.id.favorite_list:
                addToFavorites(item);
                break;

            case R.id.save_list:
                saveEditList();
                break;
            }

        return super.onOptionsItemSelected(item);
    }

    private void getList() {
        //if we got parameters from previous activity then we want to edit list
        newList = new ShoppingList();
        newList.setId(originalShoppingList.getId());
        newList.setCreatorID(originalShoppingList.getCreatorID());
        newList.setTitle(originalShoppingList.getTitle());
        newList.setShoppingItems(new ArrayList<>(originalShoppingList.getShoppingItems()));

        addEditListAdapter = new AddEditListAdapter(this, newList.getShoppingItems());
        mRecyclerView.setAdapter(addEditListAdapter);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(this, R.drawable.divider));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    public void add_product(View view) {
        //update list
        if(!product.getText().toString().trim().isEmpty()) {
            ShoppingItem newItem=new ShoppingItem(product.getText().toString().trim(), false, numberPicker.getValueView().getText().toString());
            if(!newList.addProduct(newItem))
                Toast.makeText(this, newItem.getProduct() + " is already on the list", Toast.LENGTH_SHORT).show();
            else {
                numberPicker.setValue(1);
                product.getText().clear();
                addEditListAdapter.notifyDataSetChanged();
            }
        }
        else
            Toast.makeText(this, "You must write a product name", Toast.LENGTH_SHORT).show();
        // add divider to the list
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(this, R.drawable.divider));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void addToFavorites(MenuItem item) {
        String value = sharedPref.getString(newList.getId(), null);  //check if the list is on sp

        if(value==null){ //if the user would like to add the list to favorites
            item.setIcon(R.drawable.ic_favorite);
            editor = sharedPref.edit();
            editor.putString(newList.getId(), title.getText().toString().trim());
            editor.apply();
            Toast.makeText(EditListActivity.this, "The list was added to your favourites", Toast.LENGTH_SHORT).show();
        }
        else if(!value.contains(","+title.getText().toString().trim())&&!value.contains(title.getText().toString().trim()+",")&&!value.equals(title.getText().toString().trim())){
            item.setIcon(R.drawable.ic_favorite);
            editor = sharedPref.edit();
            editor.putString(newList.getId(), value+","+title.getText().toString().trim());
            editor.apply();
            Toast.makeText(EditListActivity.this, "The list was added to your favourites", Toast.LENGTH_SHORT).show();
        }
        else if(value.equals(title.getText().toString().trim())){ //if there is only list title- remove from favorites
            item.setIcon(R.drawable.ic_not_favorite);
            editor = sharedPref.edit();
            editor.remove(newList.getId()).commit();
            editor.apply();
            Toast.makeText(EditListActivity.this, "The list was removed from your favourites", Toast.LENGTH_SHORT).show();
        }
        else if(value.contains(","+title.getText().toString().trim())||value.contains(title.getText().toString().trim()+",")){ //if there is more than one title- need to check if contains
            item.setIcon(R.drawable.ic_not_favorite);
            editor = sharedPref.edit();
            if(value.contains(","+title.getText().toString().trim()))
                editor.putString(newList.getId(), value.replaceAll(","+title.getText().toString().trim(),""));
            if(value.contains(title.getText().toString().trim()+","))
                editor.putString(newList.getId(), value.replaceAll(title.getText().toString().trim()+",",""));
            editor.apply();
            Toast.makeText(EditListActivity.this, "The list was removed from your favourites", Toast.LENGTH_SHORT).show();
        }

    }

    private void saveEditList() {
        progressBar.setVisibility(View.VISIBLE);
        if(validation())
            editIfExist(newList.getId());
    }

    private void editIfExist(String listID) {
        if (!(title.getText().toString().trim().equals(newList.getTitle()))) {    //if the title was changed

            db.collection("Shopping list").whereEqualTo("title", newList.getTitle()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (QueryDocumentSnapshot documentSnapshots : queryDocumentSnapshots) {
                        String creator_id = documentSnapshots.getString("creatorID");
                        String shareField = documentSnapshots.getString("share");
                        if ((creator_id.equals(user.getUid()) && (user.getUid().equals(newList.getId()))) || (shareField != null &&
                                (shareField.equals(user.getEmail()) || shareField.contains("," + user.getEmail()) || shareField.contains(user.getEmail() + ","))
                                && creator_id.equals(newList.getId()))) {
                            title.setFocusable(true);
                            Toast.makeText(EditListActivity.this, "This title is already exists in your lists", Toast.LENGTH_LONG).show();
                            isExist = true;
                            progressBar.setVisibility(View.GONE);
                            return;
                        }
                    }
                    if (!isExist)
                        updateByQuery(newList, true);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditListActivity.this, "The list could not be saved. Please try again.", Toast.LENGTH_LONG).show();
                }
            });
        }
        else updateByQuery(newList, false);

    }

    private void updateByQuery(final ShoppingList newList, boolean titleChanged){
        final HashMap<String, Object> list;
        list = new HashMap<>(createListToSave());
        if(titleChanged==true){
            String value = sharedPref.getString(newList.getId(),null);   //check if the ID is on SP file.
            if (value != null) {
                if (!value.equals(title.getText().toString().trim())) {
                    editor = sharedPref.edit();
                    editor.putString(newList.getId(), title.getText().toString().trim());
                    editor.apply();
                }
            }
        }
        db.collection("Shopping list").whereEqualTo("ID", newList.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    newList.setTitle(title.getText().toString().trim());
                    //String creator_id=document.getString("creatorID");
                    String shareField=document.getString("share");

                        if(shareField!=null)
                            list.put("share", shareField);
                        db.collection("Shopping list").document(document.getId()).set(list); //update list
                        String value = sharedPref.getString(newList.getId(),null);   //check if the ID is on SP file.
                        String oldTitle=getIntent().getStringExtra("list title");
                        editor = sharedPref.edit();
                        //if the list is on sp and the name changed- update on sp
                        if(value!=null && (value.equals(oldTitle)||value.contains(","+oldTitle)||value.contains(oldTitle+","))){
                            editor.putString(newList.getId(), value.replaceAll(oldTitle,newList.getTitle()));
                            editor.apply();
                        }
                        Toast.makeText(EditListActivity.this, "The list was updated successfully", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        Intent intent= new Intent(getApplicationContext(), MyListActivity.class);
                        intent.putExtra("list",newList);      //pass the list of the items
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);// clear the top stack
                        startActivity(intent);
                    }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e( "Error getting documents", e.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
        progressBar.setVisibility(View.GONE);
    }


    private boolean validation(){
        if(title.getText().toString().trim().isEmpty()) {
            title.setError("Please enter a title");
            title.setFocusable(true);
            progressBar.setVisibility(View.GONE);
            return false;
        }
        if (newList.getShoppingItems().size() == 0) {
            Toast.makeText(EditListActivity.this, "The list must contain at least one item", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            return false;
        }

      /*  int same=0;
        if(originalShoppingList!=null) {
            for (int i = 0; i < newList.getShoppingItems().size(); i++) {
                    for (int j = 0; j < originalShoppingList.getShoppingItems().size(); j++) {
                        if (newList.getShoppingItems().get(i).getProduct().equals(originalShoppingList.getShoppingItems().get(j).getProduct()) &&
                                newList.getShoppingItems().get(i).getAmount().equals(originalShoppingList.getShoppingItems().get(j).getAmount())) {
                            same++;
                        }
                    }
            }
    }
            if (same == originalShoppingList.getShoppingItems().size() && same == newList.getShoppingItems().size()&&newList.getTitle().equals(title.getText().toString().trim())) {
                Toast.makeText(EditListActivity.this, "You didn't change anything", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                return false;
            }*/
        return true;
    }

    private HashMap<String, Object> createListToSave(){
        HashMap<String, Object> list = new HashMap<>();
        list.put("title", title.getText().toString().trim());
        list.put("creatorID", newList.getCreatorID());
        if(newList.getShoppingItems()!=null) {
            for (int i = 0; i < newList.getShoppingItems().size(); i++) {
                if (newList.getShoppingItems().get(i).isSelected())
                    list.put("description " + i, newList.getShoppingItems().get(i).getAmount() + "," + newList.getShoppingItems().get(i).getProduct() + ",checked");
                else
                    list.put("description " + i, newList.getShoppingItems().get(i).getAmount() + "," + newList.getShoppingItems().get(i).getProduct());
            }
        }
        list.put("ID", newList.getId());
        return list;
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
        itemsRef.whereEqualTo("title",title.getText().toString().trim()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
                    Toast.makeText(EditListActivity.this, "Please check the email", Toast.LENGTH_LONG).show();
                    return;
                }


                //Check if the email is registered to the app
                firebaseAuth.fetchSignInMethodsForEmail(email.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                boolean isEmpty = task.getResult().getSignInMethods().isEmpty();
                                if (isEmpty)
                                    Toast.makeText(EditListActivity.this, "This email is not registered to the app", Toast.LENGTH_LONG).show();
                                else {
                                    query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            Map<String, Object> list = new HashMap<>();
                                            WriteBatch batch = db.batch(); // Write a new data to your existing document

                                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                if (documentSnapshot.getString("cretorID").equals(user.getUid()) ||
                                                        (documentSnapshot.getString("share") != null && documentSnapshot.getString("share").contains(user.getEmail()))) {
                                                    if (documentSnapshot.getString("share") == null) {
                                                        //There is no share field -> create and add the email
                                                        list.put("share", email.getText().toString());
                                                        batch.update(documentSnapshot.getReference(), list);
                                                        Toast.makeText(EditListActivity.this, "The list was shared with " + email.getText().toString(), Toast.LENGTH_LONG).show();
                                                    }

                                                    // Check if the given email is already shared with this list
                                                    else if (documentSnapshot.getString("share").equals(email.getText().toString())) // Check if the given email is already shared
                                                        Toast.makeText(EditListActivity.this, "The user is already shared in the list", Toast.LENGTH_LONG).show();

                                                        // Check if the given email belongs to the user
                                                    else if (documentSnapshot.getString("cretorID").equals(user.getUid()))
                                                        Toast.makeText(EditListActivity.this, "You cannot share the list with yourself", Toast.LENGTH_LONG).show();

                                                        //Add another email to the list
                                                    else {
                                                        list.put("share", documentSnapshot.getString("share") + "," + email.getText().toString());
                                                        db.collection("Shopping list").document(documentSnapshot.getId()).set(list, SetOptions.merge());
                                                        Toast.makeText(EditListActivity.this, "The list was shared with " + email.getText().toString(), Toast.LENGTH_LONG).show();
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

                            }
                        });
            }
        }).setNegativeButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Check if the email is correct or is not empty
                if(!pat.matcher(email.getText().toString()).matches() || email.getText().toString().isEmpty())
                {
                    Toast.makeText(EditListActivity.this, "Please check the email", Toast.LENGTH_LONG).show();
                    return;
                }

                //Check if the email is shared with this list
                if(!sharedEmails.getText().toString().contains(email.getText().toString()))
                {
                    Toast.makeText(EditListActivity.this, "This email is not shared with this list", Toast.LENGTH_LONG).show();
                    return;
                }


                query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Map<String, Object> list = new HashMap<>();
                        WriteBatch batch = db.batch(); // Write a new data to your existing document

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if(documentSnapshot.getString("creatorID").equals(user.getUid()) ||
                                    (documentSnapshot.getString("share")!=null && documentSnapshot.getString("share").contains(user.getEmail())))
                            {
                                //Check if the list was shared with only one user
                                if (documentSnapshot.getString("share") != null && !documentSnapshot.getString("share").contains(",")) {
                                    list.put("share", FieldValue.delete());
                                    batch.update(documentSnapshot.getReference(), list);
                                    Toast.makeText(EditListActivity.this, "The list is no longer shared with " + email.getText().toString(), Toast.LENGTH_SHORT).show();
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
                                            Toast.makeText(EditListActivity.this, "The list is no longer shared with " + email.getText().toString(), Toast.LENGTH_SHORT).show();
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
