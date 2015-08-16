//------------------------------------------------------------------------
// <copyright file="MyActivity.java" company="Advisor ICT Solutions">
// Advisor ICT Solutions, Mijdrecht, The Netherlands. All rights reserved.
// </copyright>
//------------------------------------------------------------------------

package com.example.fabian.matchit;

import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fabian.matchit.Adapters.lvAdapterShoppingCart;
import com.example.fabian.matchit.JSONURL.JSONURLObjectDELETE;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class FragmentDrawerShoppingCart extends Fragment {

    View v;

    // Order
    public lvAdapterShoppingCart                   aListShoppingCart;
    OrderlineRefresh                                       FOrderline;

    private ListView                                lvShoppingCartItems;
    private TextView                                tvAmount;

    String                                          stId;
    int                                             iPickedPosition;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.drawer_shopping_cart, container, false);
        tvAmount = (TextView) v.findViewById(R.id.tvTotalShoppingCardAmount);
        lvShoppingCartItems = (ListView) v.findViewById(R.id.lvShoppingCartItems);
        Button btnBetalen = (Button) v.findViewById(R.id.btnBetalen);
        btnBetalen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ActivityBetalen.class);
                startActivity(i);
            }
        });

        lvShoppingCartItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long arg3) {
                //iPickedPosition = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

                return true;
            }
        });

        // Haal winkelwagentje op
        // Maak orderline vast klaar

        tvAmount.setText("\u20ac " +  GlobalVariables.SHOPPING_TOTAL);
        Log.i("bash", String.valueOf(GlobalVariables.alOr2ders));
        aListShoppingCart = new lvAdapterShoppingCart(getActivity(), GlobalVariables.alOr2ders);
        lvShoppingCartItems.setAdapter(aListShoppingCart);

        return v;
    }


    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    stId = GlobalVariables.alOr2ders.get(iPickedPosition).get("Id");

                    new deleteOrderline().execute();

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    private class deleteOrderline extends AsyncTask<Void, Void, Void> {

        String stBooleanDeleted;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            stBooleanDeleted = JSONURLObjectDELETE.getJSONfromURL(GlobalVariables.URL_ORDERLINE + "/" + stId);
            Log.i("stBoolea", stBooleanDeleted);

            return null;
        }

        @Override
        protected void onPostExecute(Void args)
        {
            if(stBooleanDeleted.equals("t")){
                Toast.makeText(getActivity(), getResources().getString(R.string.deleteOrderTrue), Toast.LENGTH_SHORT).show();
                GlobalVariables.alOr2ders.remove(0);
                refresh();
            }
            else{
                Toast.makeText(getActivity(), getResources().getString(R.string.deleteOrderFalse), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void refresh(){
        Log.i("refreshhhh", String.valueOf(GlobalVariables.alOr2ders));
        tvAmount.setText("\u20ac " + GlobalVariables.SHOPPING_TOTAL);
        aListShoppingCart = new lvAdapterShoppingCart(getActivity(), GlobalVariables.alOr2ders);
        lvShoppingCartItems.setAdapter(aListShoppingCart);
    }



}