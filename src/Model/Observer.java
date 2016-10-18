package Model;

import Model.Observable;
import Ui.UpdateNames;

/**
 * Created by karui on 2016/10/10.
 */
public abstract class Observer {
    protected Observable observable;

    public abstract void update(String updatedValue, UpdateNames updateName);

    public void setObservable(Observable o) {
        observable = o;
    }
}
