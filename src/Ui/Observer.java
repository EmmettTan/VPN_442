package Ui;

import Model.Observable;

/**
 * Created by karui on 2016/10/10.
 */
public abstract class Observer {
    protected Observable observable;

    public abstract void update();

    public void setObservable(Observable o) {
        observable = o;
    }
}
