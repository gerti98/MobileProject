package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.activity.ChatActivity;
import com.example.chatapp.cache.FavoritesHandler;
import com.example.chatapp.dto.User;

import java.util.ArrayList;

public class ContactsAdapter extends ArrayAdapter<User> {

    public ContactsAdapter(Context context, ArrayList<User> contacts){
        super(context, 0, contacts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        User user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_item, parent, false);
        }

        //setting contact UI
        TextView email = (TextView) convertView.findViewById(R.id.contact_email);
        email.setText(user.getEmail());
        ImageButton imageButton = convertView.findViewById(R.id.favorites_button);
        if (FavoritesHandler.isFavorite(user.getUid())){
            imageButton.setImageResource(R.drawable.ic_active_favorite);
        }else{
            imageButton.setImageResource(R.drawable.ic_inactive_favorite);
        }
        Context context = getContext();

        //define the events of the elements

        //open chat event
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            //sending an intent about the user of the opened chat
            intent.putExtra("chat_user_name", user.getName());
            intent.putExtra("chat_user_uid", user.getUid());
            context.startActivity(intent);
        });

        //add to favorites event
        imageButton.setOnClickListener(v -> {
            if (FavoritesHandler.isFavorite(user.getUid())){
                imageButton.setImageResource(R.drawable.ic_inactive_favorite);
                FavoritesHandler.removeFromFavorites(user);
                Toast.makeText(context, user.getName()+" removed from favorites", Toast.LENGTH_SHORT).show();
            }
            else {
                if (FavoritesHandler.addToFavorites(user) == true) {
                    imageButton.setImageResource(R.drawable.ic_active_favorite);
                    Toast.makeText(context, user.getName() + " added to favorites", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "maximum favorites value has been reached!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //
        // Return the completed view to render on screen
        return convertView;
    }

}
