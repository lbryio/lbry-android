package io.lbry.browser.model;

public class StartupStage {
    public final Integer stage;
    public final Boolean stageDone;

    public StartupStage(Integer stage, Boolean stageDone) {
        this.stage = stage;
        this.stageDone = stageDone;
    }
}
