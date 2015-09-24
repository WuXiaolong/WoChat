package com.wuxiaolong.wochat.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

import com.wuxiaolong.wochat.R;

/**
 * Created by WuXiaolong on 2015/7/13.
 */
public class TipDialog extends Dialog {
    Context mContext;

    public TipDialog(Context context) {
        super(context);
        mContext = context;
    }

    public TipDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
    }

    protected TipDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tip_dialog);

        Window win = this.getWindow();
        WindowManager.LayoutParams params = win.getAttributes();

        int cxScreen = getScreenWidth((Activity) mContext);
        int cyScreen = getScreenHeight((Activity) mContext);

        int cy = (int) mContext.getResources().getDimension(R.dimen.cyloginingdlg);
        int lrMargin = (int) mContext.getResources().getDimension(R.dimen.loginingdlg_lr_margin);
        int tMargin = (int) mContext.getResources().getDimension(R.dimen.loginingdlg_t_margin);

        params.x = -(cxScreen - lrMargin * 2) / 2;
        params.y = (-(cyScreen - cy) / 2) + tMargin;
        params.width = cxScreen;
        params.height = cy;

        setCanceledOnTouchOutside(true);

    }

    private int getScreenWidth(Activity context) {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    private int getScreenHeight(Activity context) {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }
}
