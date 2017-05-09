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

public class StringAdapter extends ArrayAdapter<String> {

    private Context context;
    private boolean withPics;

    // The drawables cached that we use
    private Drawable startDrawable;
    private Drawable betweenDrawable;
    private Drawable endDrawable;

    /**
     * An adapter for the stations list view.
     * @param context The current context
     * @param objects The station names
     * @param withPics Whether we want the circle pictures net to the names or not
     */
    public StringAdapter(@NonNull Context context, @NonNull List<String> objects, boolean withPics) {
        super(context, R.layout.station_list_item, objects);
        this.context = context;
        this.withPics = withPics;

        startDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_linestart, null);
        betweenDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_linebetween, null);
        endDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_lineend, null);
    }

    private static class ViewHolder {
        private TextView idTextView;
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
            convertView = inflater.inflate(withPics ? R.layout.stations_list_item_with_given_line : R.layout.station_list_item, null);

            // Store everything in ViewHolder
            holder = new ViewHolder();
            holder.idTextView = (TextView) convertView.findViewById(R.id.lineIdText);
            if (withPics)
                holder.lineImageView = (ImageView) convertView.findViewById(R.id.lineImageView);

            // Store the ViewHolder for future use
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Assign stuff to the UI
        String currLine = getItem(position);

        holder.idTextView.setText(currLine);
        if (withPics) {
            if (position == 0) holder.lineImageView.setImageDrawable(startDrawable);
            else if (position == getCount() - 1) holder.lineImageView.setImageDrawable(endDrawable);
            else holder.lineImageView.setImageDrawable(betweenDrawable);
        }

        return convertView;
    }
}
