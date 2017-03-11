package com.example.gauravbharti.garagebluetooth;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by gauravbharti on 11/03/17.
 */

public class BluetoothAddressAdapter extends BaseAdapter{
    ArrayList<Details> detailsArrayList=new ArrayList<Details>();
    Context context;
    LayoutInflater layoutInflater;
    MainActivity mainActivity;
    edit_passwordFragment edit_passwordFragments;
    String connected_address;
    public BluetoothAddressAdapter(Context context,ArrayList<Details> detailsArrayList,String connected_address)
    {   super();
        this.context=context;
        layoutInflater=LayoutInflater.from(context);
        this.detailsArrayList=detailsArrayList;
        this.connected_address=connected_address;
    }
    @Override
    public int getCount() {
        return this.detailsArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return detailsArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {   if(convertView == null)
        {   convertView=layoutInflater.inflate(R.layout.bluetooth_card,parent,false);
        }
        final Details details=this.detailsArrayList.get(position);
        mainActivity=(MainActivity) GarageBluetoothApplication.getInstance().getCurrentActivity();
        TextView name=(TextView)convertView.findViewById(R.id.name);
        TextView type=(TextView)convertView.findViewById(R.id.type);
        TextView password=(TextView)convertView.findViewById(R.id.password);
        TextView address=(TextView)convertView.findViewById(R.id.address);
        name.setText(details.getName());
        type.setText(details.getType());
        password.setText(details.getPassword());
        address.setText(details.getAddress());
        ((ImageButton)convertView.findViewById(R.id.password_edit_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connected_address.equals("0"))
                {
                    Toast.makeText(mainActivity,"Not Connected to this device",Toast.LENGTH_SHORT).show();
                }
                else
                {   edit_passwordFragments=edit_passwordFragment.newInstance(details.getPassword(),details.getAddress(),details.getName());
                    mainActivity.switchFragment(edit_passwordFragments);
                }

            }
        });
        return convertView;
    }
}
