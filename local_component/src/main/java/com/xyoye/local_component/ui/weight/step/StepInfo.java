package com.xyoye.local_component.ui.weight.step;

/**
 * Created by xyoye on 2021/2/23.
 */

public class StepInfo {
    private String content;
    private String describe;
    private StepState state;

    private int uniqueId;

    public StepInfo(int uniqueId, String content, StepState state) {
        this.uniqueId = uniqueId;
        this.content = content;
        this.state = state;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public StepState getState() {
        return state;
    }

    public void setState(StepState state) {
        this.state = state;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public void update(String content, StepState state){
        this.content = content;
        this.state = state;
    }
}
