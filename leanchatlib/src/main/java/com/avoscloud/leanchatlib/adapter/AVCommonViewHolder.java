package com.avoscloud.leanchatlib.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;


/**
 * Created by wli on 15/8/25.
 * RecyclerView.ViewHolder 的公共类，做了一些封装。目的：
 * 1、ViewHolder 与 Adapter 解耦，和 ViewHolder 相关的逻辑都放到 ViewHolder 里边，避免 Adapter 有相关逻辑
 * 2、使用 ButterKnife，精简代码，提高代码阅读性
 */

public abstract class AVCommonViewHolder<T> extends RecyclerView.ViewHolder {

    public AVCommonViewHolder(Context context, ViewGroup root, int layoutRes) {
        super(LayoutInflater.from(context).inflate(layoutRes, root, false));
    }

    public Context getContext() {
        return itemView.getContext();
    }

    /**
     * 用给定的 mContactList 对 holder 的 view 进行赋值
     */
    public abstract void bindData(T t);

    public void setData(T t) {
        bindData(t);
    }

    /**
     * 因为 CommonListAdapter 里边无法对于未知类型的 Class 进行实例化
     * 所以需要如果想用 CommonListAdapter，必须要在对应的 CommonViewHolder 实例化一个 HOLDER_CREATOR
     * 注意：public static ViewHolderCreator HOLDER_CREATOR，名字与修饰符都不能更改，否则有可能引发失败
     * 具体样例可参见 DiscoverItemHolder
     * 如果不使用 CommonListAdapter，则不需要实例化 ViewHolderCreator
     *
     * @param <VH>
     */
    public interface ViewHolderCreator<VH extends AVCommonViewHolder> {
        public VH createByViewGroupAndType(ViewGroup parent, int viewType);
    }
}