package com.ashwin2k.airventory;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ListViewHolder extends RecyclerView.ViewHolder {
    TextView timestamp,scanid,category;
    ImageView photo;
    public ListViewHolder(@NonNull View itemView) {
        super(itemView);
        timestamp=itemView.findViewById(R.id.timestamp);
        scanid=itemView.findViewById(R.id.scanid);
        category=itemView.findViewById(R.id.category);
    }
}
