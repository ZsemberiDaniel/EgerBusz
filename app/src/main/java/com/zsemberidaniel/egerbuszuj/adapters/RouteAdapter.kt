package com.zsemberidaniel.egerbuszuj.adapters

import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.zsemberidaniel.egerbuszuj.R
import com.zsemberidaniel.egerbuszuj.realm.objects.Stop
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder

/**
 * Created by zsemberi.daniel on 2017. 05. 27..
 */
class RouteAdapter(items: MutableList<RouteAdapterItem>) : FlexibleAdapter<RouteAdapter.RouteAdapterItem>(items) {

    class RouteAdapterItem(val stop: Stop) : AbstractFlexibleItem<RouteAdapterItem.RouteAdapterItemViewHolder>() {

        override fun equals(other: Any?): Boolean = other is RouteAdapterItem && other.stop.id == stop.id
        override fun hashCode(): Int = stop.id?.hashCode() ?: "".hashCode()
        override fun getLayoutRes(): Int = R.layout.stop_list_item_on_route

        override fun createViewHolder(adapter: FlexibleAdapter<out IFlexible<*>>,
                                      inflater: LayoutInflater, parent: ViewGroup): RouteAdapterItemViewHolder =
                RouteAdapterItemViewHolder(inflater.inflate(layoutRes, parent, false), adapter)

        override fun bindViewHolder(adapter: FlexibleAdapter<out IFlexible<*>>, holder: RouteAdapterItemViewHolder,
                                    position: Int, payloads: MutableList<Any?>) {

            holder.stopTextView.text = stop.name
            // Change whether the image is starred or not
            holder.starredImageView.setImageDrawable(ResourcesCompat.getDrawable(
                    holder.starredImageView.resources,
                    if (stop.isStarred) R.drawable.ic_star else R.drawable.ic_star_border,
                    null)
            )

            // change stopType image based on the position
            val stopTypeDrawable = ResourcesCompat.getDrawable(
                    holder.stopTypeImageView.resources,
                    when (position) {
                        0 -> R.drawable.ic_linestart
                        adapter.itemCount - 1 -> R.drawable.ic_lineend
                        else -> R.drawable.ic_linebetween
                    },
                    null
            )
            holder.stopTypeImageView.setImageDrawable(stopTypeDrawable)
        }

        class RouteAdapterItemViewHolder(view: View, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(view, adapter) {
            val stopTextView = view.findViewById(R.id.stopNameText) as TextView
            val starredImageView = view.findViewById(R.id.starredImgView) as ImageView
            val stopTypeImageView = view.findViewById(R.id.stopTypeImageView) as ImageView
        }
    }

    companion object {

    }
}