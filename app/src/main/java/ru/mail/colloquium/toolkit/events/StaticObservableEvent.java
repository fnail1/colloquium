package ru.mail.colloquium.toolkit.events;

/**
 *
 */
public abstract class StaticObservableEvent<Handler,Sender, Argument> extends ObservableEventBase<Handler,Sender,Argument> {
    public void fire(Sender sender, Argument args) {
        super.fire(sender, args);
    }
}
