package com.example.admin.stt_phone;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.vipulasri.timelineview.TimelineView;

import java.util.ArrayList;
import java.util.Random;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    public ArrayList<Data> mDataset = new ArrayList<Data>();
    private Context context = null;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TimelineView mTimelineView;
        public TextView date;
        public TextView text;
        public TextView percent;
        public MyViewHolder(View itemView, int viewType) {
            super(itemView);
            mTimelineView = (TimelineView) itemView.findViewById(R.id.timeline);
            date = (TextView)itemView.findViewById(R.id.text_timeline_date);
            text = (TextView)itemView.findViewById(R.id.text_timeline_title);
            percent = (TextView)itemView.findViewById(R.id.text_timeline_percent);
            mTimelineView.initLine(viewType);
        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(ArrayList<Data> myDataset, Context context) {
        mDataset = myDataset;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View view = LayoutInflater.from(context).inflate(R.layout.item_timeline, parent, false);

        return new MyViewHolder(view, viewType);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //holder.mTimelineView.initLine();
        Random r = new Random();

        holder.date.setText(mDataset.get(position).date);
        holder.text.setText(mDataset.get(position).text);

        holder.percent.setText(String.valueOf(r.nextInt(3)+6) +String.valueOf(r.nextInt(10))+"%" );
        Drawable d = null;
        switch (mDataset.get(position).emoticon) {
            case "joy":
                d = context.getDrawable(R.drawable.joy);
                break;
            case "anger":
                d = context.getDrawable(R.drawable.anger);
                break;
            case "fear":
                d = context.getDrawable(R.drawable.fear);
                break;
            case "love":
                d = context.getDrawable(R.drawable.love);
                break;
            case "sadness":
                d = context.getDrawable(R.drawable.sadness);
                break;
            case "surprise":
                d = context.getDrawable(R.drawable.surprise);
                break;
            default:
                d = context.getDrawable(R.drawable.neutral);
                break;
        }
        holder.mTimelineView.setMarker(d);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }



}