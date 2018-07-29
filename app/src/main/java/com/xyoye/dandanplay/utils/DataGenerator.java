package com.xyoye.dandanplay.utils;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.ui.homeMod.HomeFragment;
import com.xyoye.dandanplay.ui.personalMod.PersonalFragment;
import com.xyoye.dandanplay.ui.playMod.PlayFragment;

/**
 * Created by YE on 2018/6/28 0028.
 */


public class DataGenerator {

    public static final int []mTabRes = new int[]{R.drawable.ic_home_light, R.drawable.ic_folder_light,R.drawable.ic_account_box_light};
    public static final int []mTabResPressed = new int[]{R.drawable.ic_home_dark, R.drawable.ic_folder_dark,R.drawable.ic_account_box_dark};
    public static final String []mTabTitle = new String[]{"首页","媒体库","我的"};

    public static Fragment[] getFragments(){
        Fragment fragments[] = new Fragment[3];
        fragments[0] = HomeFragment.newInstance();
        fragments[1] = PlayFragment.newInstance();
        fragments[2] = PersonalFragment.newInstance();
        return fragments;
    }

    /**
     * 获取Tab 显示的内容
     */
    public static View getTabView(Context context, int position){
        View view = LayoutInflater.from(context).inflate(R.layout.layout_tab_content,null);
        ImageView tabIcon = (ImageView) view.findViewById(R.id.tab_content_image);
        tabIcon.setImageResource(DataGenerator.mTabRes[position]);
        TextView tabText = (TextView) view.findViewById(R.id.tab_content_text);
        tabText.setText(mTabTitle[position]);
        return view;
    }

}
