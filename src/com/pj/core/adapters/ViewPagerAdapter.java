package com.pj.core.adapters;

import java.util.List;

import com.pj.core.viewholders.ViewHolder;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * @author 陆振文[pengju]
 *
 */
public class ViewPagerAdapter extends PagerAdapter{
	
	private List<? extends ViewHolder> views;
	
	public ViewPagerAdapter(List<? extends ViewHolder> views){
		this.views=views;
	}

	/**  
     * Remove a page for the given position.  The adapter is responsible  
     * for removing the view from its container, although it only must ensure  
     * this is done by the time it returns from {@link #finishUpdate()}.  
     *  
     * @param container The containing View from which the page will be removed.  
     * @param position The page position to be removed.  
     * @param object The same object that was returned by  
     * {@link #instantiateItem(View, int)}.  
     */
	@Override
	public void destroyItem(View group, int pos, Object view) {
		// TODO Auto-generated method stub
		((ViewGroup) group).removeView(views.get(pos).getView());
	}

	/**  
     * Called when the a change in the shown pages has been completed.  At this  
     * point you must ensure that all of the pages have actually been added or  
     * removed from the container as appropriate.  
     * @param container The containing View which is displaying this adapter's  
     * page views.  
     */
	@Override
	public void finishUpdate(View arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return views.size();
	}

	/**  
     * Create the page for the given position.  The adapter is responsible  
     * for adding the view to the container given here, although it only  
     * must ensure this is done by the time it returns from  
     * {@link #finishUpdate()}.  
     *  
     * @param container The containing View in which the page will be shown.  
     * @param position The page position to be instantiated.  
     * @return Returns an Object representing the new page.  This does not  
     * need to be a View, but can be some other container of the page.  
     */
	@Override
	public Object instantiateItem(View arg0, int arg1) {
		// TODO Auto-generated method stub
		ViewPager viewPager=(ViewPager) arg0;
		View view=views.get(arg1).getView();
		viewPager.addView(view);
		return view;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0==arg1;
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Parcelable saveState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
		// TODO Auto-generated method stub
		
	}
}
