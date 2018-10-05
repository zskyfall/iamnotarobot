package com.powerranger.sow2.iamnotarobot;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> {
    ArrayList<User> arrayList = new ArrayList<>();
    Context context;

    public SearchAdapter(Context context, ArrayList<User> list) {
        this.arrayList = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.search_result_item, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.textName.setText(arrayList.get(i).getName());
        myViewHolder.textEmail.setText(arrayList.get(i).getEmail());
        Glide.with(context).load(arrayList.get(i).getAvatar()).into(myViewHolder.imageAvatar);
    }

    @Override
    public int getItemCount() {
        return arrayList != null? arrayList.size() : 0;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageAvatar;
        TextView textName;
        TextView textEmail;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageAvatar = itemView.findViewById(R.id.list_item_image_avatar);
            textName = itemView.findViewById(R.id.list_item_text_name);
            textEmail = itemView.findViewById(R.id.list_item_text_email);
        }
    }

    public void setFilter(ArrayList<User> newList) {
        arrayList = new ArrayList<>();
        arrayList.addAll(newList);

        notifyDataSetChanged();
    }
}
