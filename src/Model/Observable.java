package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karui on 2016/10/10.
 */
public abstract class Observable {
    List<Observer> observers = new ArrayList<>();

    public abstract String getMessage();

    public void addObserver(Observer observer) {
        observers.add(observer);
        observer.setObservable(this);
    }

    public void notifyAllObservers(){
        for (Observer observer : observers) {
            observer.update();
        }
    }
}
