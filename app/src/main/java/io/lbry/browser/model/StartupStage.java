package io.lbry.browser.model;

public class StartupStage {
    public Integer stage;
    public Boolean stageDone;

    public StartupStage(Integer stage, Boolean stageDone) {
        this.stage = stage;
        this.stageDone = stageDone;
    }
}
