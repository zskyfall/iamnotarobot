package com.powerranger.sow2.iamnotarobot;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.powerranger.sow2.iamnotarobot.interfaces.SearchItemClickListener;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> {
    ArrayList<User> arrayList = new ArrayList<>();
    Context context;
    SearchItemClickListener listener;

    public SearchAdapter(Context context, ArrayList<User> list) {
        this.arrayList = list;
        this.context = context;
    }

    public void addClickListenter(SearchItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.search_result_item, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {
        final String avatar = arrayList.get(i).getAvatar();
        final String name = arrayList.get(i).getName();
        final String email = arrayList.get(i).getEmail();

        myViewHolder.textName.setText(arrayList.get(i).getName());
        myViewHolder.textEmail.setText(arrayList.get(i).getEmail());
        Glide.with(context).load(arrayList.get(i).getAvatar()).into(myViewHolder.imageAvatar);
        myViewHolder.relativeSearchRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClicked(avatar, name, email);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList != null? arrayList.size() : 0;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout relativeSearchRow;
        ImageView imageAvatar;
        TextView textName;
        TextView textEmail;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeSearchRow = itemView.findViewById(R.id.relative_search_row);
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
