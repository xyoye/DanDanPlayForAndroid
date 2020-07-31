package com.xyoye.dandanplay.ui.weight.preference;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

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
        super.onBindViewHolder(holder);
        if (holder != null) {
            holder.itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    return listener.onPreferenceLongClick(this);
                }
                return false;
            });
        }
    }

    public interface OnPreferenceLongClickListener {
        boolean onPreferenceLongClick(Preference preference);
    }
}
