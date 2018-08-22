package com.example.kiran.be_you;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kiran.be_you.model.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class AfterBlogActivity extends AppCompatActivity {
    private RecyclerView mpostlist;
    private DatabaseReference mDatabase;
    private LinearLayoutManager mlinearlayout;
    private FirebaseRecyclerAdapter<Post,BlogViewHolder> firebaseRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_blog);
        //firebase
        FirebaseUser currentuser= FirebaseAuth.getInstance().getCurrentUser();
       String currentuid=currentuser.getUid();

        //Recyclerview.
        mpostlist=(RecyclerView) findViewById(R.id.post_list);

        mlinearlayout=new LinearLayoutManager(this);
        mpostlist.setHasFixedSize(true);
        mpostlist.setLayoutManager(mlinearlayout);
       mlinearlayout.setReverseLayout(true);

        mDatabase=FirebaseDatabase.getInstance().getReference().child("blogs").child(currentuid);
        mDatabase.keepSynced(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Post> optionpost=
                new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(mDatabase,Post.class)
                .setLifecycleOwner(this)
                .build();
         firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Post, BlogViewHolder>(optionpost) {
             @NonNull
             @Override
             public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                 return new BlogViewHolder(LayoutInflater.from(parent.getContext())
                         .inflate(R.layout.post_row, parent, false));
             }

             @Override
             protected void onBindViewHolder(@NonNull BlogViewHolder holder, int position, @NonNull Post model) {
                 holder.setTitle(model.getTitle());
                 holder.setDesc(model.getDesc());
                 holder.setblog_image(getApplicationContext(),model.getBlog_image());
             }

            /* @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Post model, int position) {
                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setblog_image(getApplicationContext(),model.getBlog_image());
            }*/
        };
        mpostlist.setAdapter(firebaseRecyclerAdapter);
    }
    public static class BlogViewHolder extends RecyclerView.ViewHolder{

        View mview;
        public BlogViewHolder(View itemView) {
            super(itemView);
            mview=itemView;
        }
        public void setTitle(String title){
            TextView post_title=(TextView) mview.findViewById(R.id.postlist_title);
            post_title.setText(title);
        }
        public void setDesc(String desc){
            TextView post_desc=(TextView) mview.findViewById(R.id.postlist_desc);
            post_desc.setText(desc);
        }
        public void setblog_image(Context ctx, String blog_image){
            ImageView post_image=(ImageView) mview.findViewById(R.id.postlist_image);
            Picasso.with(ctx).load(blog_image).networkPolicy(NetworkPolicy.OFFLINE).into(post_image);
        }
    }


}
