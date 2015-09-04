package io.bloc.android.blocly.ui.adapter;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;
import io.bloc.android.blocly.ui.UIUtils;

/**
 * Created by Mike on 8/3/2015.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemAdapterViewHolder> {
    public static interface DataSource {
        public RssItem getRssItem(ItemAdapter itemAdapter, int position);
        public RssFeed getRssFeed(ItemAdapter itemAdapter, int position);
        public int getItemCount(ItemAdapter itemAdapter);
    }
    public static interface Delegate {
        public void onItemClicked(ItemAdapter itemAdapter, RssItem rssItem);
        public void onVisitClicked(ItemAdapter itemAdapter, RssItem rssItem);
    }
    // #49 delegated the visiting click to a different class
    private static String TAG = ItemAdapter.class.getSimpleName();
    private Map<Long, Integer> rssFeedToColor = new HashMap<Long, Integer>();
    private RssItem expandedItem = null;
    private WeakReference<Delegate> delegate;
    private WeakReference<DataSource> dataSource;
    private int collapsedItemHeight;
    private int expandedItemHeight;
    /*#47 DataSource gives the ItemAdapter info. The ItemAdapter seperates from DataSource and allows
     * any controller to use ItemAdapter
     */
    //#48 added two fields to track height within ItemAdapter
    //#59 Added a data structure to store a color for each Rss feed shown by the adapter.

    @Override
    public ItemAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int index) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rss_item, viewGroup, false);
        return new ItemAdapterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ItemAdapterViewHolder itemAdapterViewHolder, int index) {
        if (getDataSource() == null) {
            return;
        }
        RssItem rssItem = getDataSource().getRssItem(this, index);
        RssFeed rssFeed = getDataSource().getRssFeed(this, index);
        itemAdapterViewHolder.update(rssFeed, rssItem);
    }
    //#47 the RSS items now behave as a view

    @Override
    public int getItemCount() {
        if (getDataSource() == null) {
            return 0;
        }
        return getDataSource().getItemCount(this);
    }

    public DataSource getDataSource() {
        if (dataSource == null) {
            return null;
        }
        return dataSource.get();
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = new WeakReference<DataSource>(dataSource);
    }
    /* #47 no longer using the ItemAdapter's dataSource. The Rss items to display isn't with ItemAdapter
     * any longer and is now a view.
     */

    public Delegate getDelegate() {
        if (delegate == null) {
            return null;
        }
        return delegate.get();
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = new WeakReference<Delegate>(delegate);
    }

    public RssItem getExpandedItem() {
        return expandedItem;
    }

    public void setExpandedItem(RssItem expandedItem) {
        this.expandedItem = expandedItem;
    }
    //#47 the expandedItem represents the RssItem
    public int getCollapsedItemHeight() {
        return collapsedItemHeight;
    }

    private void setCollapsedItemHeight(int collapsedItemHeight) {
        this.collapsedItemHeight = collapsedItemHeight;
    }

    public int getExpandedItemHeight() {
        return expandedItemHeight;
    }

    private void setExpandedItemHeight(int expandedItemHeight) {
        this.expandedItemHeight = expandedItemHeight;
    }
    //#48 added private setters, public getters for CollapsedItemHeight and ExpandedItemHeight

    class ItemAdapterViewHolder extends RecyclerView.ViewHolder implements ImageLoadingListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        boolean onTablet;
        boolean contentExpanded;

        //ItemAdapterViewHolder is now implementing OnClickListener
        //A boolean added to keep track of expansion

        TextView title;
        TextView content;
        // Phone only
        TextView feed;
        View headerWrapper;
        ImageView headerImage;
        CheckBox archiveCheckbox;
        CheckBox favoriteCheckbox;
        View expandedContentWrapper;
        TextView expandedContent;
        TextView visitSite;
        // Tablet Only
        TextView callout;
        RssItem rssItem;
        //RssItem added in order to be referenced later.
        //new hidden views
        //#59 Added onTablet boolean to keep track of items being displayed in the tablet.

        public ItemAdapterViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tv_rss_item_title);
            content = (TextView) itemView.findViewById(R.id.tv_rss_item_content);

            // Attempt to recover phone Views
            if (itemView.findViewById(R.id.tv_rss_item_feed_title) != null) {
                feed = (TextView) itemView.findViewById(R.id.tv_rss_item_feed_title);
                headerWrapper = itemView.findViewById(R.id.fl_rss_item_image_header);
                headerImage = (ImageView) headerWrapper.findViewById(R.id.iv_rss_item_image);
                archiveCheckbox = (CheckBox) itemView.findViewById(R.id.cb_rss_item_check_mark);
                favoriteCheckbox = (CheckBox) itemView.findViewById(R.id.cb_rss_item_favorite_star);
                expandedContentWrapper = itemView.findViewById(R.id.ll_rss_item_expanded_content_wrapper);
                expandedContent = (TextView) expandedContentWrapper.findViewById(R.id.tv_rss_item_content_full);
                visitSite = (TextView) expandedContentWrapper.findViewById(R.id.tv_rss_item_visit_site);
                visitSite.setOnClickListener(this);
                archiveCheckbox.setOnCheckedChangeListener(this);
                favoriteCheckbox.setOnCheckedChangeListener(this);
            } else {
                // Recover Tablet Views
                onTablet = true;
                callout = (TextView) itemView.findViewById(R.id.tv_rss_item_callout);
                // #3
                if (Build.VERSION.SDK_INT >= 21) {
                    callout.setOutlineProvider(new ViewOutlineProvider() {
                        @Override
                        public void getOutline(View view, Outline outline) {
                            outline.setOval(0, 0, view.getWidth(), view.getHeight());
                        }
                    });
                    callout.setClipToOutline(true);
                }
             /*#59 Checking for tv_rss_item_feed_title within itemView, retrieving phone related View and ViewGroup.
              *Making the callout TextView circular with setOval. */
            }

            itemView.setOnClickListener(this);

            //Assigning the ItemAdapterViewHolder to the ItemView onClickListener
            //Setting the ItemAdapterViewHolder to the OnCheckedChangeListener for both check boxes
            //Setting visitSite's OnClickListener while finding the hidden views.
        }

        void update(RssFeed rssFeed, RssItem rssItem) {
            this.rssItem = rssItem;
            title.setText(rssItem.getTitle());
            content.setText(rssItem.getDescription());
            if(onTablet){
                callout.setText("" + Character.toUpperCase(rssFeed.getTitle().charAt(0)));
                Integer color = rssFeedToColor.get(rssFeed.getRowId());
                if (color == null) {
                    color = UIUtils.generateRandomColor(itemView.getResources().getColor(android.R.color.white));
                    rssFeedToColor.put(rssFeed.getRowId(), color);
                }
                callout.setBackgroundColor(color);
                return;
            }
            feed.setText(rssFeed.getTitle());
         //#59 Tablet support added, set the callout's text, ItemAdapter can present both RSS item layouts.
            expandedContent.setText(rssItem.getDescription());

            //content and expandedContent have the same text which is the rssItem's description
            if (rssItem.getImageUrl() != null) {
                headerWrapper.setVisibility(View.VISIBLE);
                headerImage.setVisibility(View.INVISIBLE);
                ImageLoader.getInstance().loadImage(rssItem.getImageUrl(), this);
            } else {
                headerWrapper.setVisibility(View.GONE);
            }
            animateContent(getExpandedItem() == rssItem);
        }
        //#47 if the RssItem for AdapterViewHolder matches ItemAdapter the view expands, if not it contracts.

        /*
         * ImageLoadingListener
         */

        @Override
        public void onLoadingStarted(String imageUri, View view) {
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            Log.e(TAG, "onLoadingFailed: " + failReason.toString() + " for URL: " + imageUri);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (imageUri.equals(rssItem.getImageUrl())) {
                //if the image is equal to the rssItem then it gets the ImageUrl
                headerImage.setImageBitmap(loadedImage);
                headerImage.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            ImageLoader.getInstance().loadImage(imageUri, this);
        }

        /*
         * OnClickListener
         */
        @Override
        public void onClick(View view) {
            if (view == itemView) {
                if (getDelegate() != null) {
                    getDelegate().onItemClicked(ItemAdapter.this, rssItem);
                }
                //#47 delegating reaction to the clicks
            } else {
                if (getDelegate() != null) {
                    getDelegate().onVisitClicked(ItemAdapter.this, rssItem);
                }
                // #49 delegated the response of the clicks to the delegate object
            }
            //onClick(View) is responsible for clicks on itemView and visitSite.
        }

        /*
         * OnCheckedChangedListener
         */

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            Log.v(TAG, "Checked changed to: " + isChecked);
        }
         /*
          * Private Methods
          */

        private void animateContent(final boolean expand) {
            if ((expand && contentExpanded) || (!expand && !contentExpanded)) {
                return;
            }
            int startingHeight = expandedContentWrapper.getMeasuredHeight();
            int finalHeight = content.getMeasuredHeight();
            if (expand) {
                setCollapsedItemHeight(itemView.getHeight());
                //#48 getting the item height if it starts to expand
                startingHeight = finalHeight;
                expandedContentWrapper.setAlpha(0f);
                expandedContentWrapper.setVisibility(View.VISIBLE);
                expandedContentWrapper.measure(
                        View.MeasureSpec.makeMeasureSpec(content.getWidth(), View.MeasureSpec.EXACTLY),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                finalHeight = expandedContentWrapper.getMeasuredHeight();
            } else {
                content.setVisibility(View.VISIBLE);
            }
            startAnimator(startingHeight, finalHeight, new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float animatedFraction = valueAnimator.getAnimatedFraction();
                    float wrapperAlpha = expand ? animatedFraction : 1f - animatedFraction;
                    float contentAlpha = 1f - wrapperAlpha;

                    expandedContentWrapper.setAlpha(wrapperAlpha);
                    content.setAlpha(contentAlpha);
                    expandedContentWrapper.getLayoutParams().height = animatedFraction == 1f ?
                            ViewGroup.LayoutParams.WRAP_CONTENT :
                            (Integer) valueAnimator.getAnimatedValue();
                    expandedContentWrapper.requestLayout();
                    if (animatedFraction == 1f) {
                        if (expand) {
                            content.setVisibility(View.GONE);
                            setExpandedItemHeight(itemView.getHeight());
                            //#48 setting the expanded height when the animation is finished
                        } else {
                            expandedContentWrapper.setVisibility(View.GONE);
                        }
                    }
                }
            });
            contentExpanded = expand;
        }

        private void startAnimator(int start, int end, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(start, end);
            valueAnimator.addUpdateListener(animatorUpdateListener);
            valueAnimator.setDuration(itemView.getResources().getInteger(android.R.integer.config_mediumAnimTime));
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.start();
        }
    }
}
//allowing multiple check boxes to be selected at the same time
//adding this comment to commit
/* #42 Added the animateContent boolean to either contract or expand the hidden content. created initial and final height
 * variables to animate. Made the full length content visible and intend to animate from full transparency to full opacity.
 * In order to figure out the target height of expansion, the View's method was invoked 'measure(int, int). This method
 * asks a View to measure itself give the provided constraints. The width of 'content' is constrained but the height is
 * unlimited. getMeasureHeight() provides the height that expandedContent Wrapper wants to be. AnimatorUpdateListener gets the
 * updates during an animation and its progress is set as a float value. The height of expandedContentWrapper is set from startingHeight
 * to finalHeight while the integer value is recovered by using getAnimatedValue(). Set the height of expandedContentWrapper in the
 * LayoutParams. calling on the requestLayout() method to ask the View to redraw itself on the screen.
 * Set the duration of the animation with setDuration and config_medAnimTime. The AccelerateDecelerateInterpolator accelerates to a
 * constant speed then decelerates to stop at the end.
 */

