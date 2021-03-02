package lq2007.mod.strangegenerator.register;

public interface IAutoApply {
    
    default int getPriority() {
        return 0;
    }
}
