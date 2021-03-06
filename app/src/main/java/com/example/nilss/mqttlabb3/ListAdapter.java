package com.example.nilss.mqttlabb3;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Color.GRAY;
import static android.graphics.Color.valueOf;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.Holder>{
    private LayoutInflater inflater;
    private Context context;
    private List<Lampobject> content = new ArrayList<>();
    private MainActivity activity;
    private Boolean recieveOk = true;
    private double hueScaling = 655.35;
    private double satScaling = 2.54;
    private double briScaling = 2.54; //65535

    public ListAdapter(Context context, MainActivity activity){
        this(context, new ArrayList<>(),activity );
    }

    public ListAdapter(Context context, List<Lampobject> content, MainActivity activity){
        this.inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        this.content = content;
        this.context = context;
        this.activity = activity;
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
        //holder.itemView.setFocusable(false);
        //holder.itemView.setClickable(false);
        holder.lampId.setText(content.get(position).getId());
        if(content.get(position).getOnoff().equals("true")){
            holder.onoff.setChecked(true);
            holder.onoff.setText("On");
        }else{
            holder.onoff.setChecked(false);
            holder.onoff.setText("Off");
        }
        //holder.seekBarSat.setProgress(Integer.parseInt(content.get(position).getSat()));
        holder.sat.setText("Sat: " + content.get(position).getSat());
        //holder.seekBarHue.setProgress(Integer.parseInt(content.get(position).getHue()));
        holder.hue.setText("Hue: "+ content.get(position).getHue());
        //holder.seekBarBri.setProgress(Integer.parseInt(content.get(position).getBri()));
        holder.bri.setText("Bri: "+ content.get(position).getBri());
        //holder.interactOff();
        if(content.get(position).getInRange()){
            holder.interactOn();
        }else{
            holder.interactOff();
        }
    }

    @Override
    public int getItemCount() {
        return this.content.size();
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView lampId;
        private TextView bri;
        private TextView hue;
        private TextView sat;
        private Button btn;
        private SeekBar seekBarHue;
        private SeekBar seekBarBri;
        private SeekBar seekBarSat;
        private Switch onoff;

        public Holder(@NonNull View itemView) {
            super(itemView);
            btn = itemView.findViewById(R.id.btnSend);
            lampId = itemView.findViewById(R.id.textLamp);
            onoff = itemView.findViewById(R.id.idmode);
            bri = itemView.findViewById(R.id.textBri);
            hue = itemView.findViewById(R.id.textHue);
            sat = itemView.findViewById(R.id.textSat);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getAdapterPosition()==3){
                        setAllValues();
                    }else{
                        activity.publish();
                    }
                    //activity.publish(content.get(getAdapterPosition()));
                }
            });

            seekBarBri = itemView.findViewById(R.id.seekBarBri);
            seekBarBri.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    bri.setText("Bri "+ (Math.round(progress*briScaling)));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekBar.getProgress();
                    Log.i("AdapterPosition", String.valueOf(getAdapterPosition()));
                    if(getAdapterPosition() != -1)
                        content.get(getAdapterPosition()).setBri(String.valueOf(Math.round(progress*briScaling)));
                }
            });

            seekBarHue = itemView.findViewById(R.id.seekBarHue);
            seekBarHue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    hue.setText("Hue "+ Math.round(progress*hueScaling));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    Log.i("AdapterPosition", String.valueOf(getAdapterPosition()));
                    if(getAdapterPosition() != -1)
                        content.get(getAdapterPosition()).setHue(String.valueOf(Math.round(seekBar.getProgress()*hueScaling)));
                }
            });

            seekBarSat = itemView.findViewById(R.id.seekBarSat);
            seekBarSat.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    sat.setText("Sat "+ Math.round(progress*satScaling));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekBar.getProgress();
                    Log.i("AdapterPosition", String.valueOf(getAdapterPosition()));
                    if(getAdapterPosition() != -1)
                        content.get(getAdapterPosition()).setSat(String.valueOf(Math.round(progress*satScaling)));
                }
            });

            onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        if(getAdapterPosition()>=0) {
                            content.get(getAdapterPosition()).setOnoff("true");
                            onoff.setText("On");
                        }
                    }else{
                        if(getAdapterPosition()>=0){
                            content.get(getAdapterPosition()).setOnoff("false");
                            onoff.setText("Off");
                        }
                    }
                }
            });
        }

        public void interactOff(){
            seekBarBri.setClickable(false);
            seekBarBri.setEnabled(false);
            seekBarHue.setClickable(false);
            seekBarHue.setEnabled(false);
            seekBarSat.setClickable(false);
            seekBarSat.setEnabled(false);
            onoff.setClickable(false);
            onoff.setEnabled(false);
            btn.setClickable(false);
            btn.setEnabled(false);
        }

        public void interactOn(){
            seekBarBri.setClickable(true);
            seekBarBri.setEnabled(true);
            seekBarHue.setClickable(true);
            seekBarHue.setEnabled(true);
            seekBarSat.setClickable(true);
            seekBarSat.setEnabled(true);
            onoff.setClickable(true);
            onoff.setEnabled(true);
            btn.setClickable(true);
            btn.setEnabled(true);
        }

        public void setAllValues(){
            int sat = (int)Math.round(seekBarSat.getProgress()*satScaling);
            int hue = (int)Math.round(seekBarHue.getProgress()*hueScaling);
            int bri = (int)Math.round(seekBarBri.getProgress()*briScaling);
            boolean mode = onoff.isChecked();
            int lampsoff = 0;
            for(int i = 0; i < content.size(); i ++){
                if(content.get(i).getInRange()) {
                    content.get(i).setSat(String.valueOf(sat));
                    content.get(i).setHue(String.valueOf(hue));
                    content.get(i).setBri(String.valueOf(bri));
                    if (mode) {
                        content.get(i).setOnoff("true");
                    } else {
                        content.get(i).setOnoff("false");
                    }
                }else{
                    lampsoff++;
                }
            }
            if(lampsoff==3){
                Toast.makeText(activity, "No lamps in range, couldn't configure",
                        Toast.LENGTH_LONG).show();
            }else{
                notifyDataSetChanged();
                activity.publish();
            }
        }
        @Override
        public void onClick(View v) {

        }
    }
}
