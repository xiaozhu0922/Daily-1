package com.meiji.daily.mvp.postslist;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.meiji.daily.BaseActivity;
import com.meiji.daily.R;
import com.meiji.daily.adapter.PostsListAdapter;
import com.meiji.daily.bean.PostsListBean;
import com.meiji.daily.interfaces.IOnItemClickListener;

import java.util.List;

import static com.meiji.daily.bean.ZhuanlanBean.ZHUANLANBEAN_NAME;
import static com.meiji.daily.bean.ZhuanlanBean.ZHUANLANBEAN_POSTSCOUNT;
import static com.meiji.daily.bean.ZhuanlanBean.ZHUANLANBEAN_SLUG;

/**
 * Created by Meiji on 2016/11/18.
 */

public class PostsListView extends BaseActivity implements IPostsList.View, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "PostsListView";
    private Toolbar toolbar;
    private SwipeRefreshLayout refresh_layout;
    private RecyclerView recycler_view;

    private PostsListAdapter adapter;
    private int postCount;
    private boolean flag = false;
    private IPostsList.Presenter presenter;
    private String slug;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postslist);
        presenter = new PostsListPresenter(this);
        initView();
        initData();
        onRequestData();
    }

    private void initData() {
        Intent intent = getIntent();
        slug = intent.getStringExtra(ZHUANLANBEAN_SLUG);
        postCount = intent.getIntExtra(ZHUANLANBEAN_POSTSCOUNT, 0);
        String title = intent.getStringExtra(ZHUANLANBEAN_NAME);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestData() {
        presenter.doRequestData(slug, 0);
    }

    @Override
    public void onSetAdapter(final List<PostsListBean> list) {
        if (adapter == null) {
            adapter = new PostsListAdapter(list, this);
            recycler_view.setAdapter(adapter);
            adapter.setOnItemClickListener(new IOnItemClickListener() {
                @Override
                public void onClick(View view, int position) {
                    presenter.doOnClickItem(position);
                }
            });
        } else {
            adapter.notifyItemInserted(list.size());
        }
        flag = true;

        recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!recyclerView.canScrollVertically(1)) {
                        // 列表文章 < 总文章 继续加载 这里要判断recyclerview是否滚动到底再执行 不然后台一直加载
                        if ((list.size() < postCount) && flag) {
                            presenter.doRequestData(slug, list.size());
                            flag = false;
                        } else if ((list.size() == postCount)) {
                            Snackbar.make(refresh_layout, R.string.no_more, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    @Override
    public void onShowRefreshing() {
        refresh_layout.setRefreshing(true);
    }

    @Override
    public void onHideRefreshing() {
        refresh_layout.setRefreshing(false);
    }

    @Override
    public void onFail() {
        Snackbar.make(refresh_layout, R.string.network_error, Snackbar.LENGTH_SHORT).show();
        refresh_layout.setEnabled(true);
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_title);
        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
        refresh_layout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recycler_view.smoothScrollToPosition(0);
            }
        });
        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        recycler_view.setHasFixedSize(true);
        // 设置下拉刷新的按钮的颜色
        refresh_layout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        // 设置手指在屏幕上下拉多少距离开始刷新
        refresh_layout.setDistanceToTriggerSync(300);
        // 设置下拉刷新按钮的背景颜色
        refresh_layout.setProgressBackgroundColorSchemeColor(Color.WHITE);
        // 设置下拉刷新按钮的大小
        refresh_layout.setSize(SwipeRefreshLayout.DEFAULT);
        refresh_layout.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        recycler_view.setVisibility(View.GONE);
        presenter.doRefresh();
        recycler_view.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }
}