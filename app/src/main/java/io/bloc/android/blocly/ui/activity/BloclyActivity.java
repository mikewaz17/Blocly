package io.bloc.android.blocly.ui.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.DataSource;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;
import io.bloc.android.blocly.ui.adapter.NavigationDrawerAdapter;
import io.bloc.android.blocly.ui.fragment.RssItemListFragment;

public class BloclyActivity extends AppCompatActivity implements
        NavigationDrawerAdapter.NavigationDrawerAdapterDelegate,
        NavigationDrawerAdapter.NavigationDrawerAdapterDataSource,
        RssItemListFragment.Delegate{

    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationDrawerAdapter navigationDrawerAdapter;
    private Menu menu;
    private View overflowButton;
    private List<RssFeed> allFeeds = new ArrayList<RssFeed>();
    private RssItem expandedItem = null;

    //#46 added a menu object and another for the overflow button.
    //#55 Created the BroadcastReceiver which resets the data found in itemAdapter and navigationDrawerAdapter.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocly);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_activity_blocly);
        setSupportActionBar(toolbar);

        //Assigning the ToolBar as our ActionBar

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout = (DrawerLayout) findViewById(R.id.dl_activity_blocly);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, 0, 0) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (overflowButton != null) {
                    overflowButton.setAlpha(1f);
                    overflowButton.setEnabled(true);
                }
                if (menu == null) {
                    return;
                }
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem item = menu.getItem(i);
                    item.setEnabled(true);
                    Drawable icon = item.getIcon();
                    if (icon != null) {
                        icon.setAlpha(255);
                    }
                }
            }

            /*#46 when the drawer is open the items are dimmed and they go back to normal
             *once the drawer is closed. The menu can either be enabled or disabled and are clickable
             *when the drawer is open.
             */

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (overflowButton != null) {
                    overflowButton.setEnabled(false);
                }
                if (menu == null) {
                    return;
                }
                for (int i = 0; i < menu.size(); i++) {
                    menu.getItem(i).setEnabled(false);
                }
            }
                @Override
                public void onDrawerSlide (View drawerView,float slideOffset){
                    super.onDrawerSlide(drawerView, slideOffset);
                    if (overflowButton == null) {
                        ArrayList<View> foundViews = new ArrayList<View>();
                        getWindow().getDecorView().findViewsWithText(foundViews,
                                getString(R.string.abc_action_menu_overflow_description),
                                View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                        if (foundViews.size() > 0) {
                            overflowButton = foundViews.get(0);
                        }
                    }
                    if (overflowButton != null) {
                        overflowButton.setAlpha(1f - slideOffset);
                    }
                    if (menu == null) {
                        return;
                    }
                    for (int i = 0; i < menu.size(); i++) {
                        MenuItem item = menu.getItem(i);
                        if (item.getItemId() == R.id.action_share
                                && expandedItem == null) {
                            continue;
                        }
                        Drawable icon = item.getIcon();
                        if (icon != null) {
                            icon.setAlpha((int) ((1f - slideOffset) * 255));
                        }
                    }
                }
            };


        /*#46 If the drawer is opened then we don't use our XML menu, otherwise it's used as it
         *normally would. This class overrides the ActionBarDrawerToggle's default settings
         * The menu items are overflow button are enabled when the drawer is opened
         * The drawer layout ranges from 0f near the edge of the screen to 1f at the max width.
         * Searches the View and its children Views for matching text.
         */
        drawerLayout.setDrawerListener(drawerToggle);
        navigationDrawerAdapter = new NavigationDrawerAdapter();
        navigationDrawerAdapter.setDelegate(this);
        navigationDrawerAdapter.setDataSource(this);
        RecyclerView navigationRecyclerView = (RecyclerView)findViewById(R.id.rv_activity_blocly);
        navigationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        navigationRecyclerView.setItemAnimator(new DefaultItemAnimator());
        navigationRecyclerView.setAdapter(navigationDrawerAdapter);

        BloclyActivity.getSharedDataSource().
                fetchAllFeeds(new DataSource.Callback<List<RssFeed>>(){
                   @Override
                   public void onSuccess(List<RssFeed> rssFeeds){
                       allFeeds.addAll(rssFeeds);
                       navigationDrawerAdapter.notifyDataSetChanged();
                       getFragmentManager()
                               .beginTransaction()
                               .add(R.id.fl_activity_blocly, RssItemListFragment.fragmentForRssFeed(rssFeeds.get(0)))
                               .commit();
                   }

                @Override
                public void onError (String errorMessage){
                }

                });
        }


        @Override
        protected void onPostCreate (Bundle savedInstanceState){
            super.onPostCreate(savedInstanceState);
            drawerToggle.syncState();
        }

        @Override
        public void onConfigurationChanged (Configuration newConfig){
            super.onConfigurationChanged(newConfig);
            drawerToggle.onConfigurationChanged(newConfig);
        }

        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.blocly, menu);
            this.menu = menu;
            animateShareItem(expandedItem != null);
            return super.onCreateOptionsMenu(menu);
        }

        // #46 removed the drawer checker and brought in a the menu object.
        // #49 animate the Item when its created

        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();
            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            }
            if (drawerToggle.onOptionsItemSelected(item)) {
                return true;
            }
            if (item.getItemId() == R.id.action_share) {
                RssItem itemToShare = expandedItem;
                if (itemToShare == null) {
                    return false;
                }
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        String.format("%s (%s)", itemToShare.getTitle(), itemToShare.getUrl()));
                shareIntent.setType("text/plain");
                Intent chooser = Intent.createChooser(shareIntent, getString(R.string.share_chooser_title));
                startActivity(chooser);
            } else {
                Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        /* #49 Intent was created with an action String, passed info to the receiving Activity with putExtra(String,string)
         * Set the Intents type with text/plain. a new Activity called chooser was created and start the Activity with the
         * startActivity method.
         */

            return super.onOptionsItemSelected(item);
        }

    /*
     * NavigationDrawerAdapterDelegate
     */

        @Override
        public void didSelectNavigationOption (NavigationDrawerAdapter
        adapter, NavigationDrawerAdapter.NavigationOption navigationOption){
            drawerLayout.closeDrawers();
            Toast.makeText(this, "Show the " + navigationOption.name(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void didSelectFeed (NavigationDrawerAdapter adapter, RssFeed rssFeed){
            drawerLayout.closeDrawers();
            Toast.makeText(this, "Show RSS items from " + rssFeed.getTitle(), Toast.LENGTH_SHORT).show();
        }
        /*
         * NavigationDrawerAdapterDataSource
         */

        @Override
        public List<RssFeed> getFeeds (NavigationDrawerAdapter adapter){
            return allFeeds;
        }
       /*
        *  RssListFragment.Delegate
        */

        @Override
        public void onItemExpanded (RssItemListFragment rssItemListFragment, RssItem rssItem){
            expandedItem = rssItem;
            animateShareItem(expandedItem != null);
        }
        @Override
        public void onItemContracted (RssItemListFragment rssItemListFragment, RssItem rssItem){
            if (expandedItem == rssItem) {
                expandedItem = null;
            }
            animateShareItem(expandedItem != null);
        }
        @Override
        public void onItemVisitClicked (RssItemListFragment rssItemListFragment, RssItem rssItem){
            Intent visitIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(rssItem.getUrl()));
            startActivity(visitIntent);
        }

     /*
      * Private methods
      */
    // #49 set the Intent's Uniform Resource Identifier(specify the location of a resource) to the RssItem's URL

    private void animateShareItem(final boolean enabled) {
        MenuItem shareItem = menu.findItem(R.id.action_share);

        if (shareItem.isEnabled() == enabled) {
            return;
        }
        shareItem.setEnabled(enabled);
        final Drawable shareIcon = shareItem.getIcon();
        ValueAnimator valueAnimator = ValueAnimator.ofInt(enabled ? new int[]{0, 255} : new int[]{255, 0});
        valueAnimator.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                shareIcon.setAlpha((Integer) animation.getAnimatedValue());
            }
        });
        valueAnimator.start();
    }
    //#45 when the user clicks on the RssItem or the NavigationOption the corresponding text will appear.
}

    // #47 This helps determine which items the ItemAdapter displays from the users selection.
    /* #48 Avoid scrolling the RecyclerView when there's no View to expand from. We find the position
     * of the LayoutManager by invoking findViewByPosition(int). If finds the View and returns it if it's
     * on the screen, if not it's null. Setting the recyclerView to scroll smoothly, returning the distance
     * from the top of the View to top of it's parent
     */
    /* #48 figuring out how much less scrolling is necessary in the RecyclerView if the contracting View is above the new
     * expanded item View
     */
    // #49 adding this method in order to animate the menu item
