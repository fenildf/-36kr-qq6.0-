package com.wuyinlei.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.wuyinlei.DefineView;
import com.wuyinlei.activity.NewsDetailActivity;
import com.wuyinlei.activity.R;
import com.wuyinlei.adapter.HomeRecycleAdapter;
import com.wuyinlei.bean.ArticleTv;
import com.wuyinlei.bean.CategoriesBean;
import com.wuyinlei.bean.HomeNewsBean;
import com.wuyinlei.biz.HomeNewsDataManager;
import com.wuyinlei.http.OkHttpManager;
import com.wuyinlei.url.Config;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by 若兰 on 2016/1/22.
 * 一个懂得了编程乐趣的小白，希望自己
 * 能够在这个道路上走的很远，也希望自己学习到的
 * 知识可以帮助更多的人,分享就是学习的一种乐趣
 * QQ:1069584784
 * csdn:http://blog.csdn.net/wuyinlei
 */

public class PageFragment extends BaseFragment implements DefineView {
    private View mView;

    private RecyclerView mRecyclerView;
    //
    private FrameLayout home_framlayuot;
    //三个加载提示布局
    private LinearLayout empty, error, loading;

    private static final String KEY = "EXTRA";

    /**
     * 分类数据
     */
    private CategoriesBean mCategoriesBean;

    /**
     * RecycleView的adapter
     */
    private HomeRecycleAdapter mAdapter;

    /**
     * 下拉刷新控件
     */
    private SwipeRefreshLayout mRefreshLayout;

   // private
    private ArticleTv mArticleTv;


    private LinearLayoutManager linearLayoutManager;

    //判断是否是最后一个item
    private int lastItem;

    /**
     * 解决上拉加载更多重复加载的
     */
    private boolean isLordMore = true;


    /**
     * 每个分类的新闻数据
     */
    private List<HomeNewsBean> mHomeNewsBeans;

    public static PageFragment newInstance(CategoriesBean extra) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY, extra);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCategoriesBean = (CategoriesBean) bundle.getSerializable(KEY);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.page_fragment, container, false);
        }
        initView();
        initValidata();
        initListener();
        return mView;
    }


    @Override
    public void initView() {

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recycleview);
        home_framlayuot = (FrameLayout) mView.findViewById(R.id.home_framlayout);
        empty = (LinearLayout) mView.findViewById(R.id.empty);
        error = (LinearLayout) mView.findViewById(R.id.error);
        loading = (LinearLayout) mView.findViewById(R.id.loading);
        mRefreshLayout = (SwipeRefreshLayout) mView.findViewById(R.id.swiperefreshlayout);
        //mRecyclerView.setHasFixedSize(true);
        // mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    @Override
    public void initValidata() {

        //设置下拉的时候的圈圈的颜色
        mRefreshLayout.setColorSchemeResources(
                android.R.color.background_dark,
                android.R.color.holo_red_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark);
        //进度条颜色
        // mRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.color_white);

        //拖动多长的时候开始刷新
        mRefreshLayout.setDistanceToTriggerSync(100);
        //mRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.color_white));

        //设置大小
        mRefreshLayout.setSize(SwipeRefreshLayout.LARGE);

        //设置进度条的偏移量
        mRefreshLayout.setProgressViewOffset(false, 0, 50);

        mRecyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext(), OrientationHelper.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        //获取数据
        OkHttpManager.getAsync(mCategoriesBean.getHref(), new OkHttpManager.DataCallBack() {
            @Override
            public void requestFailure(Request request, IOException e) {
                Log.d("PageFragment", "数据加载失败");
            }

            @Override
            public void requestSuccess(String result) throws Exception {
                //Log.d("PageFragment", "请求成功");

                /**
                 * jsoup解析数据
                 */
                Document document = Jsoup.parse(result, Config.CRAWLER_URL);
                mHomeNewsBeans = new HomeNewsDataManager().getHomeBeans(document);
                //Log.d("PageFragment", "mHomeNewsBeans.size():" + mHomeNewsBeans.size());

                if (mHomeNewsBeans != null && mHomeNewsBeans != null) {
                    bindData();
                }
            }
        });
    }

    @Override
    public void initListener() {

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mRefreshLayout.isRefreshing()) {
                            {
                                mRefreshLayout.setRefreshing(false);
                            }
                            Toast.makeText(getContext(), "刷新完成", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, 5000);
            }
        });

        /**
         *
         */
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastItem + 1 == linearLayoutManager.getItemCount()) {
                    if (isLordMore) {
                        isLordMore = false;
                        //构造下一页数据的地址

                        String lordMoreUrl = mCategoriesBean.getHref()
                                + "?b_url_code="
                                + mHomeNewsBeans.get(mHomeNewsBeans.size() - 1).gettId()
                                + "&d=next";

                        OkHttpManager.getAsync(lordMoreUrl, new OkHttpManager.DataCallBack() {
                            @Override
                            public void requestFailure(Request request, IOException e) {

                            }

                            @Override
                            public void requestSuccess(String result) throws Exception {
                                Document document = Jsoup.parse(result, Config.CRAWLER_URL);

                                List<HomeNewsBean> tempHomeNewsBeans = new HomeNewsDataManager().getHomeBeans(document);
                                bindDataMore(tempHomeNewsBeans);
                                isLordMore = true;
                            }
                        });
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastItem = linearLayoutManager.findLastVisibleItemPosition();
            }
        });
    }

    /**
     * 加载更多数据的方法
     * @param tempHomeNewsBeans
     */
    private void bindDataMore(List<HomeNewsBean> tempHomeNewsBeans) {
        mHomeNewsBeans.addAll(tempHomeNewsBeans);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void bindData() {
        mAdapter = new HomeRecycleAdapter(getContext(), 1);

        /**
         * 不要忘记添加数据  要不然啥都没有
         */
        mAdapter.setHomeNewsBeans(mHomeNewsBeans);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new HomeRecycleAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, Object o) {
               // Toast.makeText(getContext(), o.toString(), Toast.LENGTH_SHORT).show();
                HomeNewsBean bean = (HomeNewsBean) o;
                Intent mIntent = new Intent(getActivity(),NewsDetailActivity.class);
                mIntent.putExtra("titleUrl",bean.getHref());
                mIntent.putExtra("titleId", bean.gettId());
                mIntent.putExtra("news_item", (Serializable) o);
                getActivity().startActivity(mIntent);
            }
        });
    }
}
