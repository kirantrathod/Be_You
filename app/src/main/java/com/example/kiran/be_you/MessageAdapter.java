package com.example.kiran.be_you;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kiran.be_you.Messages;
import com.example.kiran.be_you.R;
import com.google.firebase.auth.FirebaseAuth;
import com.vanniktech.emoji.EmojiTextView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth fAuth;

    public MessageAdapter(List<Messages> mMessageList){

        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);


        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public EmojiTextView messageTextIn, messageTextOut;
        public CardView messageCard;
        public LinearLayout messageIn, messageOut, mainMessageLayout;

        public MessageViewHolder(View view) {
            super(view);

            messageTextIn = (EmojiTextView) view.findViewById(R.id.message_text_in);
            messageTextOut = (EmojiTextView) view.findViewById(R.id.message_text_out);
           //messageCard = (CardView) view.findViewById(R.id.message_card);
            messageIn = (LinearLayout) view.findViewById(R.id.message_layout_in);
            messageOut = (LinearLayout) view.findViewById(R.id.message_layout_out);
            mainMessageLayout = (LinearLayout) view.findViewById(R.id.main_message_layout);

        }

    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

        fAuth = FirebaseAuth.getInstance();

        String current_user_id = fAuth.getCurrentUser().getUid();

        Messages c = mMessageList.get(position);
        String from_user = c.getFrom();
        String message_type=c.getType();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);


        if (from_user.equals(current_user_id)){

            holder.messageIn.setVisibility(View.GONE);
            holder.messageOut.setVisibility(View.VISIBLE);

            params.gravity = Gravity.RIGHT;
            holder.mainMessageLayout.setLayoutParams(params);

        } else {

            holder.messageIn.setVisibility(View.VISIBLE);
            holder.messageOut.setVisibility(View.GONE);


            params.gravity = Gravity.LEFT;
            holder.mainMessageLayout.setLayoutParams(params);

        }

        holder.messageTextIn.setText(c.getMessage());
        holder.messageTextOut.setText(c.getMessage());
        holder.itemView.setTag(mMessageList.get(position));
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}
