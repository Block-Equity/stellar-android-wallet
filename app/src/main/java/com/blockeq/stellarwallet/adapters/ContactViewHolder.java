package com.blockeq.stellarwallet.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blockeq.stellarwallet.R;
import com.blockeq.stellarwallet.models.Contact;
import com.squareup.picasso.Picasso;


/**
 * Contains a Contact List Item
 */
public class ContactViewHolder extends RecyclerView.ViewHolder {
    private ImageView mImage;
    private TextView mLabel;
    private Contact mBoundContact; // Can be null

    public ContactViewHolder(final View itemView) {
        super(itemView);
        mImage = (ImageView) itemView.findViewById(R.id.rounded_iv_profile);
        mLabel = (TextView) itemView.findViewById(R.id.tv_label);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBoundContact != null) {
                    Toast.makeText(
                            itemView.getContext(),
                            "Hi, I'm " + mBoundContact.name,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void bind(Contact contact) {
        mBoundContact = contact;
        mLabel.setText(contact.name);
//        Picasso.with(itemView.getContext())
//                .load(contact.profilePic)
//                .placeholder(R.drawable.ic_launcher)
//                .error(R.drawable.ic_launcher)
//                .into(mImage);
    }
}
