package com.xyoye.dandanplay.ui.weight.preference;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

import com.xyoye.dandanplay.R;

public class LongClickPreference extends Preference {
    private OnPreferenceLongClickListener listener;

    public LongClickPreference(Context context) {
        this(context, null);
    }

    public LongClickPreference(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.preferenceStyle);
    }

    public LongClickPreference(Context context, AttributeSet attributeSet, int defStyleAttr) {
        this(context, attributeSet, defStyleAttr, 0);
    }

    public LongClickPreference(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
    }

    public void setOnPreferenceLongClickListener(OnPreferenceLongClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        if (holder != null) {
            holder.itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    return listener.OnPreferenceLongClick(this);
                }
                return false;
            });
        }
        super.onBindViewHolder(holder);
    }

    public interface OnPreferenceLongClickListener {
        boolean OnPreferenceLongClick(Preference preference);
    }
}
