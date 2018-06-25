package com.juphoon.rcs.jrdemo;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class GeoBaiduSearch implements OnGetGeoCoderResultListener {

    public interface IGeoBaiduSearch {
        void onAddress(boolean succ, String cookie, String address);
    }

    private GeoCoder mSearch = null;

    private static GeoBaiduSearch mGeoBaiduSearch = null;

    private class SearchItem {
        public WeakReference<IGeoBaiduSearch> mIGeoBaiduSearch;
        public LatLng mLocation;
        public String mCookie;
    }

    private ArrayList<SearchItem> mSeachList;
    private SearchItem mNowSearchItem;

    private GeoBaiduSearch() {
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
        mSeachList = new ArrayList<SearchItem>();
    }

    public void destroy() {
        mSearch.destroy();
        mGeoBaiduSearch = null;
    }

    public static GeoBaiduSearch getInstance() {
        if (mGeoBaiduSearch == null) {
            mGeoBaiduSearch = new GeoBaiduSearch();
        }
        return mGeoBaiduSearch;
    }

    public void addReverseGeoCodeSearch(String cookie,
                                        IGeoBaiduSearch geoBaiduSearch, LatLng location) {

        SearchItem item = new SearchItem();
        item.mCookie = cookie;
        item.mIGeoBaiduSearch = new WeakReference<IGeoBaiduSearch>(
                geoBaiduSearch);
        item.mLocation = location;
        mSeachList.add(item);

        doReverseGeoCode();
    }

    private boolean genReverseGeoCodeSearch() {

        if (mSeachList.size() > 0) {
            mNowSearchItem = mSeachList.get(0);
            mSeachList.remove(0);
        }

        return mNowSearchItem != null;
    }

    private void doReverseGeoCode() {

        if (mNowSearchItem != null)
            return;

        while (true) {
            if (genReverseGeoCodeSearch()) {
                if (mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                        .location(mNowSearchItem.mLocation)))
                    break;
            } else {
                break;
            }
        }
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (mNowSearchItem != null) {
            if (mNowSearchItem.mIGeoBaiduSearch.get() != null) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {

                    mNowSearchItem.mIGeoBaiduSearch.get().onAddress(false,
                            mNowSearchItem.mCookie, "");
                } else {
                    if (mNowSearchItem.mIGeoBaiduSearch.get() != null)
                        mNowSearchItem.mIGeoBaiduSearch.get().onAddress(true,
                                mNowSearchItem.mCookie, result.getAddress());
                }
            }
            mNowSearchItem = null;
        }
        doReverseGeoCode();
    }
}
