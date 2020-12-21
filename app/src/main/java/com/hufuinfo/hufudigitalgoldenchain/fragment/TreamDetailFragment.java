package com.hufuinfo.hufudigitalgoldenchain.fragment;


import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.QueryUserInfo;
import com.hufuinfo.hufudigitalgoldenchain.bean.TeamInfo;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.DateUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TreamDetailFragment extends Fragment {
    private Activity mActivity;
    private String antiFake;
    private TextView mTotalInvesTv;
    private TextView mTotalUserTeamTv;
    private TextView mTotalIncomeTeamTv;
    private SwipeRefreshLayout mTeamSwipRefresh;
    private RecyclerView mTeamRecyclerView;

    public TreamDetailFragment() {
    }

    public static TreamDetailFragment newInstance() {
        TreamDetailFragment fragment = new TreamDetailFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        antiFake = mActivity.getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE)
                .getString(ConstantUtils.ANTI_FAKE, null);
        queryTreamDetailInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tream_detail, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mTotalInvesTv = view.findViewById(R.id.total_invest_tv);
        mTotalUserTeamTv = view.findViewById(R.id.total_team_user_tv);
        mTotalIncomeTeamTv = view.findViewById(R.id.total_team_income_tv);
        mTeamSwipRefresh = view.findViewById(R.id.team_srl);
        mTeamRecyclerView = view.findViewById(R.id.team_recycler_view);
        mTeamSwipRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryTreamDetailInfo();
            }
        });
        view.findViewById(R.id.team_return_tv).setOnClickListener(listener -> {
            getActivity().onBackPressed();
        });
    }


    private void queryTreamDetailInfo() {

        QueryUserInfo goldenOrder = RetrofitUtils.create(QueryUserInfo.class);
        goldenOrder.queryTeamDetails(antiFake)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(teamInfoResult -> {
                    mTeamSwipRefresh.setRefreshing(false);
                    if (teamInfoResult.success) {
                        mTotalInvesTv.setText(""
                                + DateUtils.getStringDouble(teamInfoResult.data.myInput));
                        mTotalUserTeamTv.setText("团队总人数:" + teamInfoResult.data.memberAll);
                        mTotalIncomeTeamTv.setText("团队总效益（股）:" + DateUtils.getStringDouble(teamInfoResult.data.proceeds));
                        showRecyclerView(teamInfoResult.data.subor);

                    } else {
                    }
                }, error -> {
                    mTeamSwipRefresh.setRefreshing(false);
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });

    }

    private TeamInfoItemDecoration mTeamInfoItemDecoration;

    private void showRecyclerView(List<TeamInfo.Subor> datas) {
        if (mTeamInfoItemDecoration == null) mTeamInfoItemDecoration = new TeamInfoItemDecoration();
        mTeamRecyclerView.removeItemDecoration(mTeamInfoItemDecoration);
        mTeamRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTeamRecyclerView.addItemDecoration(mTeamInfoItemDecoration);
        mTeamRecyclerView.setAdapter(new TeamInfoAdapter(datas));
        mTeamRecyclerView.setHasFixedSize(true);
    }

    private class TeamInfoAdapter extends RecyclerView.Adapter<TeamInfoAdapter.TeamInfoViewHolder> {
        private List<TeamInfo.Subor> suborList;

        public TeamInfoAdapter(List<TeamInfo.Subor> suborList) {
            this.suborList = suborList;
        }


        @Override
        public TeamInfoAdapter.TeamInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new TeamInfoViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.item_team_info, parent, false));
        }

        @Override
        public void onBindViewHolder(TeamInfoAdapter.TeamInfoViewHolder holder, int position) {
            holder.teamAccountTv.setText(suborList.get(position).userId);
            holder.teamOwnUserTv.setText(suborList.get(position).fId + "");
            holder.teamTotalImcomeTv.setText(DateUtils.getStringDouble(suborList.get(position).stockRight));
        }

        @Override
        public int getItemCount() {
            return suborList.size();
        }

        class TeamInfoViewHolder extends RecyclerView.ViewHolder {

            private final TextView teamAccountTv;
            private final TextView teamOwnUserTv;
            private final TextView teamTotalImcomeTv;

            public TeamInfoViewHolder(View itemView) {
                super(itemView);
                teamAccountTv = itemView.findViewById(R.id.team_account_tv);
                teamOwnUserTv = itemView.findViewById(R.id.team_owns_user_tv);
                teamTotalImcomeTv = itemView.findViewById(R.id.team_proceeds_tv);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private class TeamInfoItemDecoration extends RecyclerView.ItemDecoration {
        Paint dividerPaint;
        int dividerHeight;

        public TeamInfoItemDecoration() {
            //paint
            dividerPaint = new Paint();
            dividerPaint.setColor(getResources().getColor(R.color.teamdividerColor));
            dividerHeight = getResources().getDimensionPixelSize(R.dimen.divider_height);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.bottom = dividerHeight;
        }


        @Override
        public void onDraw(Canvas c, RecyclerView parent) {
            int childCount = parent.getChildCount();
            int right = parent.getWidth() - parent.getPaddingRight();
            int left = parent.getPaddingLeft();

            for (int i = 0; i < childCount; i++) {
                View childAt = parent.getChildAt(i);
                int top = childAt.getBottom();
                int bottom = top + dividerHeight;
                c.drawRect(left, top, right, bottom, dividerPaint);
            }
        }
    }
}
