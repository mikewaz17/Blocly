package io.bloc.android.blocly.ui.activity;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
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
import android.widget.Toast;

import java.util.ArrayList;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;
import io.bloc.android.blocly.ui.adapter.ItemAdapter;
import io.bloc.android.blocly.ui.adapter.NavigationDrawerAdapter;

public class BloclyActivity extends AppCompatActivity
        implements
        NavigationDrawerAdapter.NavigationDrawerAdapterDelegate,
        ItemAdapter.DataSource,
        ItemAdapter.Delegate {
    //#47 BloclyActivity implements Delegate and DataSource for ItemAdapter
    private ItemAdapter itemAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationDrawerAdapter navigationDrawerAdapter;
    private Menu menu;
    private View overflowButton;
    //#46 added a menu object and another for the overflow button.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocly);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_activity_blocly);
        setSupportActionBar(toolbar);
        //Assigning the ToolBar as our ActionBar
        itemAdapter = new ItemAdapter();
        itemAdapter.setDataSource(this);
        itemAdapter.setDelegate(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_activity_blocly);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(itemAdapter);
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
            public void onDrawerSlide(View drawerView, float slideOffset) {
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
        RecyclerView navigationRecyclerView = (RecyclerView) findViewById(R.id.rv_nav_activity_blocly);
        navigationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        navigationRecyclerView.setItemAnimator(new DefaultItemAnimator());
        navigationRecyclerView.setAdapter(navigationDrawerAdapter);
    }
    //#45 BloclyActivity implements the NavigationDrawerAdapterDelegate and sets it as such.

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_blocly, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }
    //#46 removed the drawer checker and brought in a the menu object.

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

    /*
     * NavigationDrawerAdapterDelegate
     */

    @Override
    public void didSelectNavigationOption(NavigationDrawerAdapter adapter, NavigationDrawerAdapter.NavigationOption navigationOption) {
        drawerLayout.closeDrawers();
        Toast.makeText(this, "Show the " + navigationOption.name(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didSelectFeed(NavigationDrawerAdapter adapter, RssFeed rssFeed) {
        drawerLayout.closeDrawers();
        Toast.makeText(this, "Show RSS items from " + rssFeed.getTitle(), Toast.LENGTH_SHORT).show();
    }
     /*
      * ItemAdapter.DataSource
      */

    @Override
    public RssItem getRssItem(ItemAdapter itemAdapter, int position) {
        return BloclyApplication.getSharedDataSource().getItems().get(position);
    }

    @Override
    public RssFeed getRssFeed(ItemAdapter itemAdapter, int position) {
        return BloclyApplication.getSharedDataSource().getFeeds().get(0);
    }

    @Override
    public int getItemCount(ItemAdapter itemAdapter) {
        return BloclyApplication.getSharedDataSource().getItems().size();
    }

     /*
      * ItemAdapter.Delegate
      */
    //#47 setting BloclyActivity as ItemAdapter's delegate and data source It shows all items found in DataSource class.
    @Override
    public void onItemClicked(ItemAdapter itemAdapter, RssItem rssItem) {
        int positionToExpand = -1;
        int positionToContract = -1;

        if (itemAdapter.getExpandedItem() != null) {
            positionToContract = BloclyApplication.getSharedDataSource().getItems().indexOf(itemAdapter.getExpandedItem());
        }

        if (itemAdapter.getExpandedItem() != rssItem) {
            positionToExpand = BloclyApplication.getSharedDataSource().getItems().indexOf(rssItem);
            itemAdapter.setExpandedItem(rssItem);
        } else {
            itemAdapter.setExpandedItem(null);
        }
        if (positionToContract > -1) {
            itemAdapter.notifyItemChanged(positionToContract);
        }
        if (positionToExpand > -1) {
            itemAdapter.notifyItemChanged(positionToExpand);
        }

    }
    //#45 when the user clicks on the RssItem or the NavigationOption the corresponding text will appear.
}
//#47 This helps determine which items the ItemAdapter displays from the users selection.
