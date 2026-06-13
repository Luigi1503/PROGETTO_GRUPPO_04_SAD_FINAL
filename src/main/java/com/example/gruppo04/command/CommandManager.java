package com.example.gruppo04.command;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Stack;


public class CommandManager {
    private Stack<Command> stack;
    private final BooleanProperty canUndoProperty = new SimpleBooleanProperty(false);

    public CommandManager() {
        this.stack = new Stack<>();
    }


    public void executeCommand(Command cmd){
        if (cmd != null) {
            cmd.execute();
            stack.push(cmd);
            updateUndoState(); // Aggiorna il valore del megafono
        }
    }

    public void undo(){
        if(!stack.isEmpty()){
            Command command = stack.pop();
            command.undo();
            updateUndoState(); // Aggiorna il valore del megafono
        }
    }

    private void updateUndoState() {
        // canUndoProperty.set() "spara" la notifica a tutti i listener in ascolto
        canUndoProperty.set(!stack.isEmpty());
    }

    public BooleanProperty canUndoProperty() {
        return canUndoProperty;
    }

}
