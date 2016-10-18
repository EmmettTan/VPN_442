package Model;

import Ui.UpdateNames;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karui on 2016/10/10.
 */
public abstract class Observable {
    List<Observer> observers = new ArrayList<>();

    public String getMessage() {
        throw new UnsupportedOperationException();
    };

    public void addObserver(Observer observer) {
        observers.add(observer);
        observer.setObservable(this);
    }

    public void notifyAllObservers(String updatedValue, UpdateNames updateName){
        for (Observer observer : observers) {
            observer.update(updatedValue, updateName);
        }
    }
}
