package io.bloc.android.blocly.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssFeed;

/**
 * Created by Mike on 8/11/2015.
 */
public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder> {

    public enum NavigationOption {
        NAVIGATION_OPTION_INBOX,
        NAVIGATION_OPTION_FAVORITES,
        NAVIGATION_OPTION_ARCHIVED
    }

    public static interface NavigationDrawerAdapterDelegate {
        public void didSelectNavigationOption(NavigationDrawerAdapter adapter, NavigationOption navigationOption);
        public void didSelectFeed(NavigationDrawerAdapter adapter, RssFeed rssFeed);
    }
    WeakReference<NavigationDrawerAdapterDelegate> delegate;

    /*#45 when a user clicks the Inbox, Favorites or Archived the NavigationOption is invoked. The same
     *goes for when a user clicks the RssFeed, it invokes the RssFeed. The WeakReference is used to store the
     * delegate.
     */

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.navigation_item, viewGroup, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        RssFeed rssFeed = null;
        if (position >= NavigationOption.values().length) {
            int feedPosition = position - NavigationOption.values().length;
            rssFeed = BloclyApplication.getSharedDataSource().getFeeds().get(feedPosition);
        }
        viewHolder.update(position, rssFeed);
        /*#44 created an array with the three primary titles arranged in order. the RssFeed object
        is recovered if below the three primary navigation elements.*/
    }

    @Override
    public int getItemCount() {
        return NavigationOption.values().length
                + BloclyApplication.getSharedDataSource().getFeeds().size();
    }

    public NavigationDrawerAdapterDelegate getDelegate() {
        if (delegate == null) {
            return null;
        }
        return delegate.get();
    }
    public void setDelegate(NavigationDrawerAdapterDelegate delegate) {
        this.delegate = new WeakReference<NavigationDrawerAdapterDelegate>(delegate);
    }
    /*Added a setter and getter for the delegate. The WeakReference is used to recover the object inside
    * but the method will return null if the original reference is taken out
    */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View topPadding;
        TextView title;
        View bottomPadding;
        View divider;

        int position;
        RssFeed rssFeed;

        public ViewHolder(View itemView) {
            super(itemView);
            topPadding = itemView.findViewById(R.id.v_nav_item_top_padding);
            title = (TextView) itemView.findViewById(R.id.tv_nav_item_title);
            bottomPadding = itemView.findViewById(R.id.v_nav_item_bottom_padding);
            divider = itemView.findViewById(R.id.v_nav_item_divider);
            itemView.setOnClickListener(this);
        }
        //#45 In the ViewHolder, the position is tracked for the RssFeed.
        void update(int position, RssFeed rssFeed) {
            this.position = position;
            this.rssFeed = rssFeed;
            boolean shouldShowTopPadding = position == NavigationOption.NAVIGATION_OPTION_INBOX.ordinal()
                    || position == NavigationOption.values().length;
            topPadding.setVisibility(shouldShowTopPadding ? View.VISIBLE : View.GONE);

            boolean shouldShowBottomPadding = position == NavigationOption.NAVIGATION_OPTION_ARCHIVED.ordinal()
                    || position == getItemCount() - 1;
            bottomPadding.setVisibility(shouldShowBottomPadding ? View.VISIBLE : View.GONE);

            divider.setVisibility(position == NavigationOption.NAVIGATION_OPTION_ARCHIVED.ordinal()
                    ? View.VISIBLE : View.GONE);
            if (position < NavigationOption.values().length) {
                int[] titleTexts = new int[]{R.string.navigation_option_inbox,
                        R.string.navigation_option_favorites,
                        R.string.navigation_option_archived};
                title.setText(titleTexts[position]);
            } else {
                title.setText(rssFeed.getTitle());
            }
        }

         /*
          * OnClickListener
          */

        @Override
        public void onClick(View v) {
            if (getDelegate() == null) {
                return;
            }
            if (position < NavigationOption.values().length) {
                getDelegate().didSelectNavigationOption(NavigationDrawerAdapter.this,
                        NavigationOption.values()[position]);
            } else {
                getDelegate().didSelectFeed(NavigationDrawerAdapter.this, rssFeed);
            }
            /*Using the NavigationDrawerAdapter.this to reference the NavigationDrawerAdapter. This provides
             *access to the original object if the delegate class doesn't have a strong reference to delegating
             *NavigationDrawerAdapter.
             */
        }
    }
}
