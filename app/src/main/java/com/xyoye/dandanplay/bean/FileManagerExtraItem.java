package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.ui.weight.dialog.FileManagerDialog.OnExtraClickListener;

public class FileManagerExtraItem {
        private String extraText;
        private OnExtraClickListener listener;

        public FileManagerExtraItem(String extraText, OnExtraClickListener listener) {
            this.extraText = extraText;
            this.listener = listener;
        }

        public String getExtraText() {
            return extraText;
        }

        public void setExtraText(String extraText) {
            this.extraText = extraText;
        }

        public OnExtraClickListener getListener() {
            return listener;
        }

        public void setListener(OnExtraClickListener listener) {
            this.listener = listener;
        }
    }