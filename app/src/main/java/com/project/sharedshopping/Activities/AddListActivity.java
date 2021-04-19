package com.project.sharedshopping.Activities;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPicker;
import com.project.sharedshopping.Adapters.AddEditListAdapter;
import com.project.sharedshopping.Classes.DividerItemDecorator;
import com.project.sharedshopping.Classes.ShoppingItem;
import com.project.sharedshopping.Classes.ShoppingList;
import com.project.sharedshopping.R;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AddListActivity extends ManageList {

    private Toolbar toolbar;
    private EditText title;
    private ScrollableNumberPicker numberPicker;
    private EditText product;
    private ProgressBar progressBar;
    boolean isExist = false;
    private RecyclerView mRecyclerView;
    private ShoppingList newList;
    private AddEditListAdapter addEditListAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_list);
        super.choosedLangugage(R.layout.activity_add_list);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        // views initialization
        title = findViewById(R.id.titleAt);
        product = findViewById(R.id.productET);
        numberPicker = findViewById(R.id.productAmount);
        progressBar = findViewById(R.id.saveListPB);

        //Addlist toolbar
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

        newList=new ShoppingList();
        //back arrow implementation
        toolbar.setNavigationIcon(getDrawable(R.drawable.ic_back));     //set icon of back option

        mRecyclerView = findViewById(R.id.currentListRV);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        saveNewlist(); //Show only save icon and save a new list
        return super.onOptionsItemSelected(item);
    }

    public void add_product(View view) {
        //update list
        if (!product.getText().toString().trim().isEmpty()) {
            ShoppingItem newItem = new ShoppingItem(product.getText().toString().trim(), false, numberPicker.getValueView().getText().toString());
            if (!newList.addProduct(newItem))
                Toast.makeText(this, newItem.getProduct() + " is already on the list", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, "You must write a product name", Toast.LENGTH_SHORT).show();
        product.setText("");
        //    addEditListAdapter.notifyDataSetChanged();
        addEditListAdapter = new AddEditListAdapter(this, newList.getShoppingItems());
        mRecyclerView.setAdapter(addEditListAdapter);
        numberPicker.setValue(1);
        // add divider to the list
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(this, R.drawable.divider));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void saveNewlist() {
        progressBar.setVisibility(View.VISIBLE);

        if (validation()) {
            // Add the list to Firestore Database
            addNewList(title.getText().toString().trim());
        } else return;
    }

    private void addNewList(final String list_title) {
        final String docId = UUID.randomUUID().toString(); //List reference
        final Map<String, Object> list; //Hash map for Firestore collection
        newList.setId(docId);
        isExist=false;
        list = new HashMap<>(createListToSave());
        db.collection("Shopping list").whereEqualTo("creatorID", user.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshots : queryDocumentSnapshots) {
                    if (Objects.equals(documentSnapshots.getString("title"), list_title)) {
                        title.setFocusable(true);
                        Toast.makeText(AddListActivity.this, "This title is already exists in your lists", Toast.LENGTH_LONG).show();
                        isExist = true;
                        progressBar.setVisibility(View.GONE);
                        break;
                    }
                }
                if (!isExist) {
                    // Add the list to Firestore Database
                    db.collection("Shopping list").document(docId).set(list).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AddListActivity.this, "The list was saved successfully", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);// clear the top stack
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AddListActivity.this, "The list could not be saved. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    });
                } else
                    progressBar.setVisibility(View.GONE);
            }
        });
    }

    private boolean validation() {
        if (title.getText().toString().trim().isEmpty()) {
            title.setError("Please enter a title");
            title.setFocusable(true);
            progressBar.setVisibility(View.GONE);
            return false;
        }

        return true;
    }

    private HashMap<String, Object> createListToSave() {
        HashMap<String, Object> list = new HashMap<>();
        list.put("title", title.getText().toString().trim());
        list.put("ID", newList.getId());
        list.put("creatorID", user.getUid());
        if (newList.getShoppingItems() != null) {
            for (int i = 0; i < newList.getShoppingItems().size(); i++) {
                if (newList.getShoppingItems().get(i).isSelected())
                    list.put("description " + i, newList.getShoppingItems().get(i).getAmount() + "," + newList.getShoppingItems().get(i).getProduct() + " checked");
                else
                    list.put("description " + i, newList.getShoppingItems().get(i).getAmount() + "," + newList.getShoppingItems().get(i).getProduct());
            }
        }
        return list;
    }
}