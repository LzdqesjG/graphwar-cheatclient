package Graphwar;

import java.util.ArrayList;
import java.util.List;

public class FunctionHistory {
    private List<String> history;
    private static final int MAX_HISTORY = 50;
    
    public FunctionHistory() {
        history = new ArrayList<>();
    }
    
    public void add(String function) {
        if (function != null && !function.isEmpty()) {
            history.remove(function);
            history.add(0, function);
            
            if (history.size() > MAX_HISTORY) {
                history.remove(history.size() - 1);
            }
        }
    }
    
    public void remove(int index) {
        if (index >= 0 && index < history.size()) {
            history.remove(index);
        }
    }
    
    public String get(int index) {
        if (index >= 0 && index < history.size()) {
            return history.get(index);
        }
        return null;
    }
    
    public int size() {
        return history.size();
    }
    
    public List<String> getAll() {
        return new ArrayList<>(history);
    }
    
    public void clear() {
        history.clear();
    }
}