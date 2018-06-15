package ui.call;

public abstract class Layer {
    private int id;

    int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    public abstract void show();

    public abstract void hide();

    public abstract boolean visible();
}
