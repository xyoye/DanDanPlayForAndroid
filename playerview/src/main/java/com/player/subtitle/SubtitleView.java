package com.player.subtitle;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.player.ijkplayer.R;
import com.player.subtitle.util.Caption;
import com.player.subtitle.util.ISubtitleControl;
import com.player.subtitle.util.TimedTextObject;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 显示字幕的图层
 */
public class SubtitleView extends LinearLayout implements ISubtitleControl, SubtitleClickListener {
    /**
     * 只显示中文
     */
    public final static int LANGUAGE_TYPE_CHINA = 0;

    /**
     * 只显示英文
     */
    public final static int LANGUAGE_TYPE_ENGLISH = LANGUAGE_TYPE_CHINA + 1;

    /**
     * 双语显示
     */
    public final static int LANGUAGE_TYPE_BOTH = LANGUAGE_TYPE_ENGLISH + 1;

    /**
     * 不显示字幕
     */
    public final static int LANGUAGE_TYPE_NONE = LANGUAGE_TYPE_BOTH + 1;

    /**
     * 更新UI
     */
    private static int UPDATE_SUBTITLE = LANGUAGE_TYPE_NONE + 1;

    /**
     * 中文字幕
     */
    private SubtitleTextView subChina;

    /**
     * 英文字幕
     */
    private SubtitleTextView subEnglish;

    /**
     * 中文字幕
     */
    private SubtitleTextView subChinaTwo;

    /**
     * 英文字幕
     */
    private SubtitleTextView subEnglishTwo;

    /**
     * 当前显示节点
     */
    private View subTitleView;

    /**
     * 字幕数据
     */
    private TimedTextObject model = null;

    /**
     * 单条字幕数据
     */
    private Caption caption = null;

    /**
     * 是否是正在播放中
     */
    private boolean isPalying = false;

    /**
     * 是否显示字幕
     */
    private boolean isShow = false;

    /**
     * 后台播放
     */
    private boolean palyOnBackground = false;

    public SubtitleView(Context context) {
        super(context);
        init(context);
    }

    public SubtitleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public SubtitleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SubtitleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        subTitleView = View.inflate(context, R.layout.layout_subtitle, null);
        subChina = subTitleView.findViewById(R.id.subTitleChina);
        subEnglish = subTitleView.findViewById(R.id.subTitleEnglish);
        subChinaTwo = subTitleView.findViewById(R.id.subTitleChinaTwo);
        subEnglishTwo = subTitleView.findViewById(R.id.subTitleEnglishTwo);
        subChina.setSubtitleOnTouchListener(this);
        subEnglish.setSubtitleOnTouchListener(this);
        subChinaTwo.setSubtitleOnTouchListener(this);
        subEnglishTwo.setSubtitleOnTouchListener(this);
        this.setOrientation(VERTICAL);
        this.addView(subTitleView);
    }

    @Override
    public void setItemSubtitle(TextView view, String item) {
        view.setText(Html.fromHtml(item));
    }

    @Override
    public void setData(TimedTextObject model) {
        if (this.model != null){
            this.model = null;
        }
        this.model = model;
    }

    @Override
    public void setLanguage(int type) {
        if (type == LANGUAGE_TYPE_CHINA) {
            subChina.setVisibility(View.VISIBLE);
            subEnglish.setVisibility(View.GONE);
            subChinaTwo.setVisibility(View.VISIBLE);
            subEnglishTwo.setVisibility(View.GONE);
        } else if (type == LANGUAGE_TYPE_ENGLISH) {
            subChina.setVisibility(View.GONE);
            subEnglish.setVisibility(View.VISIBLE);
            subChinaTwo.setVisibility(View.GONE);
            subEnglishTwo.setVisibility(View.VISIBLE);
        } else if (type == LANGUAGE_TYPE_BOTH) {
            subChina.setVisibility(View.VISIBLE);
            subEnglish.setVisibility(View.VISIBLE);
            subChinaTwo.setVisibility(View.VISIBLE);
            subEnglishTwo.setVisibility(View.VISIBLE);
        } else {
            subChina.setVisibility(View.GONE);
            subEnglish.setVisibility(View.GONE);
            subChinaTwo.setVisibility(View.GONE);
            subEnglishTwo.setVisibility(View.GONE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
    }

    @Override
    public void start() {
        isShow = true;
        isPalying = true;
    }

    @Override
    public void pause() {
        isPalying = false;
    }

    @Override
    public void seekTo(long position) {
        if(palyOnBackground || !isPalying || !isShow) {
            return;
        }
        if (model != null && !model.captions.isEmpty()) {
            List<Caption> captions = searchSub(model.captions, position);
            if(captions.size() > 1) {//之多同时显示两个字幕，如需同时显示两个以上的只需重新自定义view增加即可
                caption = captions.get(0);
                if (caption != null) {
                    setItemSubtitle(subChina, caption.content);
                    setItemSubtitle(subEnglish, caption.content);
                } else {
                    setItemSubtitle(subChina, "");
                    setItemSubtitle(subEnglish, "");
                }
                caption = captions.get(1);
                if (caption != null) {
                    setItemSubtitle(subChinaTwo, caption.content);
                    setItemSubtitle(subEnglishTwo, caption.content);
                } else {
                    setItemSubtitle(subChinaTwo, "");
                    setItemSubtitle(subEnglishTwo, "");
                }
            }else if (captions.size() == 1){
                caption = captions.get(0);
                if (caption != null) {
                    setItemSubtitle(subChina, caption.content);
                    setItemSubtitle(subEnglish, caption.content);
                }
            }else {
                setItemSubtitle(subChina, "");
                setItemSubtitle(subEnglish, "");
            }

        }
    }

    @Override
    public void stop() {
        isPalying = false;
    }

    @Override
    public void setPlayOnBackground(boolean pb) {
        this.palyOnBackground = pb;
    }

    @Override
    public void setTextSize(int languageType, float textSize) {
        if (languageType == LANGUAGE_TYPE_CHINA){
            subChina.setTextSize(textSize);
            subChinaTwo.setTextSize(textSize);
        }else {
            subEnglish.setTextSize(textSize);
            subEnglishTwo.setTextSize(textSize);
        }
    }

    @Override
    public void setTextSize(float chineseSize, float englishSize) {
        subChina.setTextSize(chineseSize);
        subChinaTwo.setTextSize(chineseSize);
        subEnglish.setTextSize(englishSize);
        subEnglishTwo.setTextSize(englishSize);
    }

    @Override
    public void hide() {
        isShow = false;
        setItemSubtitle(subChina, "");
        setItemSubtitle(subEnglish, "");
        setItemSubtitle(subChinaTwo, "");
        setItemSubtitle(subEnglishTwo, "");
    }

    @Override
    public void show() {
        isShow = true;
    }

    /**
     * @param list 全部字幕
     * @param key  播放的时间点
     */
    public List<Caption> searchSub(TreeMap<Integer, Caption> list, long key) {
        List<Caption> captions = new ArrayList<>();
        System.out.println("captionKey："+key);
        try {
            //最小时间
            int min = list.firstKey();
            //时间大于最小时间才开始解析
            if (Integer.parseInt(String.valueOf(key)) > min){
                //比当前时间小的前一个字幕位置
                int start = list.lowerKey(Integer.parseInt(String.valueOf(key)));
                //比当前时间小的前一个到结尾的所有字幕
                SortedMap<Integer, Caption> temp = list.subMap(start, list.lastKey());
                for(Integer key1 : temp.keySet()) {
                    Caption caption = temp.get(key1);
                    //开始时间小于当前时间，结束时间大于当前时间
                    if (key >= caption.start.getMseconds() && key <= caption.end.getMseconds()){
                        captions.add(caption);
                        System.out.println(caption.content);
                    }
                    //减少查找时间，从开始大于当前时间开始break
                    if (caption.start.getMseconds() > key){
                        break;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return captions;
    }

    @Override
    public void ClickDown() {
//        Toast.makeText(context, "ClickDown", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void ClickUp() {
//        Toast.makeText(context, "ClickUp", Toast.LENGTH_SHORT).show();
    }
}
