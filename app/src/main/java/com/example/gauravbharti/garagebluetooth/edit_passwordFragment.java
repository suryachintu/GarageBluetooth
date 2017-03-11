package com.example.gauravbharti.garagebluetooth;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link edit_passwordFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link edit_passwordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class edit_passwordFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3="param3";
    // TODO: Rename and change types of parameters
    public String mParam1;
    public String mParam2;
    public String mParam3;
    SharedPreferences pref;
    String type;
    Button register;
    JSONArray jsonArray;
    JSONArray jsonArray1;
    JSONObject jsonObject;
    MainActivity mainActivity;
    EditText enter_password;
    private OnFragmentInteractionListener mListener;

    public edit_passwordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.//Password
     * @param param2 Parameter 2.//Address
     * @param param3 Parameter 3.//Name
     * @return A new instance of fragment edit_passwordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static edit_passwordFragment newInstance(String param1, String param2,String param3) {
        edit_passwordFragment fragment = new edit_passwordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3,param3);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mParam3=getArguments().getString(ARG_PARAM3);
        }
        jsonArray=new JSONArray();
        jsonArray1=new JSONArray();
        jsonObject=new JSONObject();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_edit_password, container, false);
        mainActivity=(MainActivity)GarageBluetoothApplication.getInstance().getCurrentActivity();
        pref = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
        String all_details=pref.getString("garage","[]");
        try
        {   jsonArray=new JSONArray(all_details);
            for (int i=0;i<jsonArray.length();i++)
            {   jsonObject=jsonArray.getJSONObject(i);
                if(jsonObject.getString("name").equals(mParam3))
                {   type=jsonObject.getString("type");

                }
                else
                {
                    jsonArray1.put(jsonObject);
                }

            }
        }
        catch (Exception e)
        {

        }
        Log.d("PARAM",mParam1);
        Log.d("PARAM",mParam2);
        Log.d("PARAM",mParam3);
        enter_password=(EditText)rootView.findViewById(R.id.enter_password);
        register=(Button)rootView.findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(enter_password.getText().toString().length()<6)
                {   Toast.makeText(mainActivity,"Password less than six digits",Toast.LENGTH_SHORT).show();

                }
                else
                {   mainActivity.update_password(mParam1,mParam3,enter_password.getText().toString());
                    try
                    {
                        jsonObject=new JSONObject();
                        jsonObject.put("name",mParam3);
                        jsonObject.put("address",mParam2);
                        jsonObject.put("type",type);
                        jsonObject.put("password",enter_password.getText().toString());
                        jsonArray1.put(jsonObject);
                        JSONArray currentj=new JSONArray();
                        currentj.put(jsonObject);
                        pref.edit().putString("current",currentj.toString()).commit();
                    }
                    catch (Exception e)
                    {

                    }
                    pref.edit().putString("garage",jsonArray1.toString()).commit();
                    getFragmentManager().popBackStack();
                }
            }
        });
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
