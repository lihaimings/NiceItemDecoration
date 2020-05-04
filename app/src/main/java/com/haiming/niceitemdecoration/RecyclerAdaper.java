package com.haiming.niceitemdecoration;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerAdaper extends RecyclerView.Adapter<RecyclerAdaper.ViewHolder> {

    private LayoutInflater mLayoutInflater;
    private List<String> mData;

    public RecyclerAdaper(Context context, List<String> data) {
        this.mData = data;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.item_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.mTextView.setText(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData== null?0:mData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView=itemView.findViewById(R.id.txt);
        }
    }
}
