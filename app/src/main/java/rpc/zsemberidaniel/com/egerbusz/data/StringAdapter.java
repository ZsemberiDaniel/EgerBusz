package rpc.zsemberidaniel.com.egerbusz.data;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import rpc.zsemberidaniel.com.egerbusz.R;

/**
 * Created by zsemberi.daniel on 2017. 05. 08..
 */

public class StringAdapter extends ArrayAdapter<String> {

    private Context context;

    public StringAdapter(@NonNull Context context, @NonNull List<String> objects) {
        super(context, R.layout.line_list_item, objects);
        this.context = context;
    }

    private static class ViewHolder {
        private TextView idTextView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder = null;

        // We cannot convert the old view to the new one, so create one
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.line_list_item, null);

            // Store everything in ViewHolder
            holder = new ViewHolder();
            holder.idTextView = (TextView) convertView.findViewById(R.id.lineIdText);

            // Store the ViewHolder for future use
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Assign stuff to the UI
        String currLine = getItem(position);

        holder.idTextView.setText(currLine);

        return convertView;
    }
}
