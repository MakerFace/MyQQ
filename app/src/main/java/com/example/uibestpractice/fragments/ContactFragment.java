package com.example.uibestpractice.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uibestpractice.R;

import java.util.ArrayList;
import java.util.List;


public class ContactFragment extends Fragment {

    private List<Contact> contacts = new ArrayList<>();
    private static final String TAG = "ContactFragment";
    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_fragment, container, false);
        Log.i(TAG, "onCreateView: initContact");
        recyclerView = (RecyclerView) view.findViewById(R.id.cot_recycler_view);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 1);
        } else {
            readContact();
        }
        contactAdapter = new ContactAdapter(contacts);
        recyclerView.setAdapter(contactAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    readContact();
                } else {
                    Toast.makeText(getContext(), "you denied the permission", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void readContact() {
        Cursor cursor = null;
        try {
            cursor = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Contact contact = new Contact();
                    contact.setName(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                    contact.setNumber(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    contacts.add(contact);
                    Log.i(TAG, "readContact: " + contact.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    class ContactAdapter extends RecyclerView.Adapter<ViewHolder> {

        List<Contact> contacts;

        ContactAdapter(List<Contact> list) {
            contacts = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Contact contact = contacts.get(position);
            holder.nameText.setText(contact.getName());
            holder.numText.setText(contact.getNumber());
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView numText;
        TextView nameText;

        ViewHolder(View itemView) {
            super(itemView);
            numText = (TextView) itemView.findViewById(R.id.number);
            nameText = (TextView) itemView.findViewById(R.id.name);
        }
    }

    class Contact {
        private String number;
        private String name;

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
