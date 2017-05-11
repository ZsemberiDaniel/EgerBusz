package rpc.zsemberidaniel.com.egerbusz.data;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import rpc.zsemberidaniel.com.egerbusz.R;

/**
 * Created by zsemberi.daniel on 2017. 05. 08..
 */

public class StopAdapter extends ArrayAdapter<StopAdapter.StopForAdapter> {

    private Context context;

    // The drawables cached that we use
    private Drawable startDrawable;
    private Drawable betweenDrawable;
    private Drawable endDrawable;

    /**
     * An adapter for the stations list view.
     * @param context The current context
     * @param objects The station names
     */
    public StopAdapter(@NonNull Context context, @NonNull List<StopForAdapter> objects) {
        super(context, R.layout.station_list_item, objects);
        this.context = context;

        startDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_linestart, null);
        betweenDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_linebetween, null);
        endDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_lineend, null);
    }

    private static class ViewHolder {
        private TextView idTextView;
        private TextView nextBusTextView;
        private ImageView lineImageView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder = null;

        // We cannot convert the old view to the new one, so create one
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            // inflate the correct layout based on the withPics
            convertView = inflater.inflate(R.layout.stations_list_item_with_given_line, null);

            // Store everything in ViewHolder
            holder = new ViewHolder();
            holder.idTextView = (TextView) convertView.findViewById(R.id.lineIdText);
            holder.nextBusTextView = (TextView) convertView.findViewById(R.id.lineNextTime);
            holder.lineImageView = (ImageView) convertView.findViewById(R.id.lineImageView);

            // Store the ViewHolder for future use
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Assign stuff to the UI
        StopForAdapter currLine = getItem(position);

        holder.idTextView.setText(currLine.stopName);

        if (currLine.isTimeGiven())
            holder.nextBusTextView.setText((currLine.nextHour < 10 ? "0" : " ") + currLine.nextHour +
                    ":" + (currLine.nextMinute < 10 ? "0" : "") + currLine.nextMinute);
        else
            holder.nextBusTextView.setText("");

        // assign correct picture
        if (position == 0) holder.lineImageView.setImageDrawable(startDrawable);
        else if (position == getCount() - 1) holder.lineImageView.setImageDrawable(endDrawable);
        else holder.lineImageView.setImageDrawable(betweenDrawable);

        return convertView;
    }

    /**
     * A data class for the StopAdapter
     */
    public static class StopForAdapter {
        /**
         * The name of the stop
         */
        private String stopName;
        /**
         * When the next bus comes to this stop (hour)
         */
        private int nextHour;
        /**
         * When the next bus comes to this stop (minute)
         */
        private int nextMinute;

        /**
         * Initializes a class for the usage of StopAdapter
         * @param stopName The name of the stop
         * @param nextHour When the next bus comes to this stop (hour)
         * @param nextMinute When the next bus comes to this stop (minute)
         */
        public StopForAdapter(String stopName, int nextHour, int nextMinute) {
            this.stopName = stopName;
            this.nextHour = nextHour;
            this.nextMinute = nextMinute;
        }

        /**
         * Initializes a class for the usage of StopAdapter. The hour and the minute will be set to -1 -1
         * So please don't forget to set them as well
         * @param stopName The name of the stop
         */
        public StopForAdapter(String stopName) {
            this(stopName, -1, -1);
        }

        /**
         * @return Whether a time is given in this class.
         */
        public boolean isTimeGiven() {
            return nextHour != -1 && nextMinute != -1;
        }
    }
}
