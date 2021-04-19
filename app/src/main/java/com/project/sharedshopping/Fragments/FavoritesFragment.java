package com.project.sharedshopping.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.sharedshopping.Adapters.ListsAdapter;
import com.project.sharedshopping.Classes.ShoppingItem;
import com.project.sharedshopping.Classes.ShoppingList;
import com.project.sharedshopping.ManageFragments;
import com.project.sharedshopping.R;

import java.util.ArrayList;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;


public class FavoritesFragment extends Fragment implements ManageFragments{

    private TextView favourite_textview;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private SharedPreferences sharedPref;
    private boolean backNarrowSearch;

    private ArrayList<ShoppingList> list;
    ListsAdapter adapter;
    FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_favorites, container, false);

        db= FirebaseFirestore.getInstance();

        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();

        favourite_textview= view.findViewById(R.id.favoriteTV);
        progressBar= view.findViewById(R.id.favoritesPB);

        sharedPref = getActivity().getSharedPreferences("favoritesSP"+user.getEmail(), MODE_PRIVATE);

        list= new ArrayList<>();

        recyclerView= view.findViewById(R.id.favoriteRv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter= new ListsAdapter(getActivity(),list);

        backNarrowSearch=false;

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!backNarrowSearch) {
            firebaseAuth = FirebaseAuth.getInstance();
            user = firebaseAuth.getCurrentUser();
            favourite_textview.setVisibility(View.GONE);
            showLists();
        }
        else
            backNarrowSearch=false;
    }



    @Override
    public void showLists() {
        list.clear();
        db.collection("Shopping list").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                String sharedUsers="";
                String ID="";
                int i=0; //Get the current position of the item
                boolean isSelected=false; //Check if the item is selected by the user
                for(QueryDocumentSnapshot doc: queryDocumentSnapshots){
                    sharedUsers=doc.getString("share");
                    ID=doc.getString("ID");
                    String title= doc.getString("title");
                    String value=sharedPref.getString(ID,null);
                    String [] split={"0", "0"};
                    if(((sharedPref!=null&&sharedPref.contains(ID)))&& (value!=null && value.contains(title))){ // Check if the current list is found in shared preferences
                        ArrayList<ShoppingItem> items = new ArrayList<>(); // Array list to store all the products and their status
                        Map<String, Object> map = doc.getData();
                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            if(entry.getKey().contains("description")){
                                String desc= doc.getString("description "+i);
                                split=desc.split(",");
                                if(desc.contains("checked")){
                                    //  desc= desc.replaceAll("checked","");
                                    isSelected=true;
                                }
                                items.add(new ShoppingItem(split[1],isSelected, split[0])); //desc can't be null because every document has a description
                                i++;
                            }
                            isSelected=false;
                        }
                        i=0; //initializes for the next document
                        ShoppingList shoppingList= new ShoppingList(doc.getString("title"),doc.getString("ID"),doc.getString("creatorID"),items, sharedUsers);
                        //list.add(shoppingList);
                        adapter.addList(shoppingList);
                    }
                }
               // adapter= new ListsAdapter(getActivity(),list);
                if(adapter.getItemCount()==0) favourite_textview.setVisibility(View.VISIBLE);
                recyclerView.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Show lists","Error displaying lists: "+e.getMessage());
            }
        });

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); //enable options menu
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu,menu);
        MenuItem searchViewItem = menu.findItem(R.id.search_menu);

        SearchView searchView = (SearchView) searchViewItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { //when the user types a character, the result will appear
                ArrayList<ShoppingList> searchlist= new ArrayList<>();
                for(ShoppingList document: list)
                {
                    if (document.getTitle().toLowerCase().startsWith(newText.toLowerCase()) || (newText.equals(""))) // newText= what the user types
                    {
                        searchlist.add(document);
                    }
                }
                adapter = new ListsAdapter(getActivity(), searchlist);
                recyclerView.setAdapter(adapter);
                return false;
            }
        });


        searchViewItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                firebaseAuth = FirebaseAuth.getInstance();
                user = firebaseAuth.getCurrentUser();
                favourite_textview.setVisibility(View.GONE);
                showLists();
                backNarrowSearch=true;
                return true;
            }

        });

        super.onCreateOptionsMenu(menu, inflater);
    }

}