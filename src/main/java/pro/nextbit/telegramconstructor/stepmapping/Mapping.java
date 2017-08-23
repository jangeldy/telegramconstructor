package pro.nextbit.telegramconstructor.stepmapping;

public class Mapping {

    private String handleClassName;
    private String handleMethod;
    private String commandText;
    private String step;
    private boolean isRedirect = false;

    public String getHandleClassName() {
        return handleClassName;
    }

    void setHandleClassName(String handleClassName) {
        this.handleClassName = handleClassName;
    }

    public String getHandleMethod() {
        return handleMethod;
    }

    void setHandleMethod(String handleMethod) {
        this.handleMethod = handleMethod;
    }

    public String getCommandText() {
        return commandText;
    }

    void setCommandText(String commandText) {
        this.commandText = commandText;
    }

    public String getStep() {
        return step;
    }

    void setStep(String step) {
        this.step = step;
    }

    public boolean isRedirect() {
        return isRedirect;
    }

    public void setRedirect(boolean redirect) {
        isRedirect = redirect;
    }
}
