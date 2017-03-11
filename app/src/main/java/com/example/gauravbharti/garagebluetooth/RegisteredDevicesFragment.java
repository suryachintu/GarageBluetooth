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
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RegisteredDevicesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RegisteredDevicesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisteredDevicesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    SharedPreferences pref;
    View rootView;
    public ArrayList<Details> Details=new ArrayList<Details>();
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public RegisteredDevicesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment RegisteredDevicesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisteredDevicesFragment newInstance(String param1) {
        RegisteredDevicesFragment fragment = new RegisteredDevicesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView=inflater.inflate(R.layout.fragment_registered_devices, container, false);
        pref = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
        String all_details=pref.getString("garage","[]");
        Log.d("pref",all_details);
        setAdapter(all_details);
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
    public void setAdapter(String all_details)
    {   try
        {   JSONArray jsonArray=new JSONArray(all_details);
            JSONObject jsonObject=new JSONObject();
            Details.clear();
            for (int i=0;i<jsonArray.length();i++)
            {   jsonObject=jsonArray.getJSONObject(i);
                String name=jsonObject.getString("name");
                String address=jsonObject.getString("address");
                String type=jsonObject.getString("type");
                String password=jsonObject.getString("password");
                com.example.gauravbharti.garagebluetooth.Details det=new Details(name,address,type,password);
                Details.add(det);
            }
            ListView details_list=(ListView)rootView.findViewById(R.id.details_list);
            details_list.setAdapter(new BluetoothAddressAdapter(getActivity().getApplicationContext(),Details,mParam1));
        }
        catch (Exception e)
        {

        }
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
