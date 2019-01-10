package com.example.nilss.mqttlabb3;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.Holder>{
    private LayoutInflater inflater;
    private Context context;
    private List<Lampobject> content = new ArrayList<>();

    public ListAdapter(Context context){
        this(context, new ArrayList<>());
    }

    public ListAdapter(Context context, List<Lampobject> content){
        this.inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        this.content = content;
        this.context = context;
    }

    public void setContent(List<Lampobject> content){
        this.content = content;
        super.notifyDataSetChanged();
    }

    public List<Lampobject> getContent(){
        return this.content;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.listitem_layout, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.lampId.setText(content.get(position).getId());
    }

    @Override
    public int getItemCount() {
        return this.content.size();
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView lampId;
        private SeekBar seekBarHue;
        private SeekBar seekBarBri;
        private SeekBar seekBarSat;
        private Switch onoff;

        public Holder(@NonNull View itemView) {
            super(itemView);
            lampId = itemView.findViewById(R.id.textLamp);
            seekBarBri = itemView.findViewById(R.id.seekBarBri);
            seekBarHue = itemView.findViewById(R.id.seekBarHue);
            seekBarSat = itemView.findViewById(R.id.seekBarSat);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
