package com.project.sharedshopping.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPicker;
import com.project.sharedshopping.Classes.ShoppingItem;
import com.project.sharedshopping.CommonFunc;
import com.project.sharedshopping.R;

import java.util.ArrayList;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;


public class AddEditListAdapter extends RecyclerView.Adapter<AddEditListAdapter.viewHolder> implements CommonFunc {

    private Context context;
    private ArrayList<ShoppingItem> items;
    private View view;
    int count=0;
    public AddEditListAdapter(Context c, ArrayList<ShoppingItem> list_items)
    {
        context=c;
        items=list_items;
    }

    @NonNull
    @Override
    public AddEditListAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        view= LayoutInflater.from(context).inflate(R.layout.add_list_item, viewGroup, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AddEditListAdapter.viewHolder viewHolder, final int i) {
        choosedLangugage();

        viewHolder.product.setText(items.get(i).getProduct());
        viewHolder.amount.setText(items.get(i).getAmount());

        viewHolder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.edit_dialog, null);
                final EditText edit_product = view.findViewById(R.id.edit_product);
                edit_product.setText( viewHolder.product.getText()); //Set the current product to edit
                final ScrollableNumberPicker edit_numberPicker= view.findViewById(R.id.edit_product_amount);
                edit_numberPicker.setValue(Integer.parseInt(viewHolder.amount.getText().toString()));//Set the current amount to edit
                builder.setView(view).setTitle("Edit item");
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!edit_product.getText().toString().isEmpty()){
                            items.get(i).setProduct(edit_product.getText().toString());
                            items.get(i).setAmount(edit_numberPicker.getValueView().getText().toString());
                            notifyItemChanged(i);
                        }
                        else Toast.makeText(context, "product cannot be empty", Toast.LENGTH_LONG).show();

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create().show();
            }
        });

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeRemoved(0,items.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public static class viewHolder extends RecyclerView.ViewHolder
    {
        TextView product, amount;
        ImageView edit, delete;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            product= itemView.findViewById(R.id.productTv);
            amount = itemView.findViewById(R.id.productAmountTv);
            edit= itemView.findViewById(R.id.edit_item);
            delete= itemView.findViewById(R.id.delete_item);
        }
    }

    @Override
    public void choosedLangugage() {
        SharedPreferences sharedPref;
        sharedPref = this.context.getSharedPreferences("Language", MODE_PRIVATE);
        String language = sharedPref.getString("lang", null);
        if(language==null)
            language=Locale.getDefault().getDisplayLanguage();
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        view.setLayoutDirection(View.LAYOUT_DIRECTION_LOCALE);
    }
}
